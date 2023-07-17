package com.gomu.gomustock.ui.simulation;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.R;
import com.gomu.gomustock.network.MyWeb;
import com.gomu.gomustock.network.YFDownload;
import com.gomu.gomustock.stockengin.BBandTest;
import com.gomu.gomustock.stockengin.Balance;
import com.gomu.gomustock.stockengin.PriceBox;
import com.gomu.gomustock.stockengin.StockDic;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SimulationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SimulationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public SimulationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SimulationFragment.
     */
    // TODO: Rename and change types and number of parameters

    private View view;
    private RecyclerView recyclerView;

    private SimulAdapter simul_adapter;
    private List<Integer> chart_data1 = new ArrayList<Integer>();
    private List<Integer> chart_data2 = new ArrayList<Integer>();
    private List<Integer> chart1_data1 = new ArrayList<Integer>();
    private List<Integer> chart1_data2 = new ArrayList<Integer>();


    private String open_api_data="empty";
    private String yesterday_price="empty";

    TextView simselect_bt, simdl_bt, simtool_bt, simsim_bt;
    TextView noti_board;
    ImageView addnew_img;

    private boolean stop_flag = false;

    private List<String> sim_stock = new ArrayList<>();
    Dialog dialog_listmanagery; // 커스텀 다이얼로그
    private List<Integer> chartcolor = new ArrayList<>();
    MyExcel myexcel = new MyExcel();
    StockDic stockdic = new StockDic();
    List<BBandTest> bbandtestlist = new ArrayList<>();

    public static SimulationFragment newInstance(String param1, String param2) {
        SimulationFragment fragment = new SimulationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_simulation, container, false);

        dialog_listmanagery = new Dialog(getActivity());       // Dialog 초기화
        dialog_listmanagery.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog_listmanagery.setContentView(R.layout.dialog_buy);

        SimulationView(view);
        return view;
    }
    public void SimulationView(View view) {

        init_resource(view);
        simulation();

        simselect_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setDefaultTextColor();
                sim_stock = myexcel.readSimullist();
                notice_ok(0);
            }
        });

        simdl_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setDefaultTextColor();
                sim_stock = myexcel.readSimullist();
                // 1년치 히스토리다운로드. 1년 증시오픈일 약 248일
                dl_yahoofinance_price(sim_stock);
            }
        });

        simtool_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String code, name;
                setDefaultTextColor();
                // backtest용 data를 만들어 excel에 저장한다
                for(int i =0;i<sim_stock.size();i++) {
                    code = sim_stock.get(i);
                    //MyMagic01 mymagic01 = new MyMagic01(code, "069500");
                    //mymagic01.makeBackdata();
                    PriceBox pricebox = new PriceBox(code);
                    List<Float> closeprice = pricebox.getClose();
                    BBandTest bbtest = new BBandTest(code,closeprice, 60);
                    bbandtestlist.add(bbtest);
                }
                simul_adapter.putBBandTestList(bbandtestlist);
                notice_ok(2);
            }
        });

        simsim_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setDefaultTextColor();
                simulation();
                notice_ok(3);
            }
        });

        addnew_img.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listmanager_dialog();
                //app_restart();
            }
        });

    }

    public String code2name(List<String> codelist) {
        String name = " 시뮬레이션 종목 리스트 : \n";
        StockDic stockdic = new StockDic();
        int size = codelist.size();
        if(size <= 0) {
            name = " 종목을 추가해주세요 \n";
        }
        else {
            for (int i = 0; i < size; i++) {
                name += stockdic.getStockname(codelist.get(i));
                name += " ";
            }
        }
        return name;
    }

    public boolean checkTestFile(List<String> codelist) {
        // 파일이 없으면 false, 있으면 true를 반환한다
        boolean flag=true;
        int size = codelist.size();
        for(int i =0;i<size;i++) {
            if(!myexcel.file_check(sim_stock.get(i)+"_testset.xls")) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    public void listmanager_dialog(){

        dialog_listmanagery.show(); // 다이얼로그 띄우기
        dialog_listmanagery.findViewById(R.id.buyBtn).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // 원하는 기능 구현

                // dialog 화면에서 입력된 정보를 읽어온다
                EditText NAME = dialog_listmanagery.findViewById(R.id.stock_name);
                EditText CODE = dialog_listmanagery.findViewById(R.id.stock_code);
                String name = NAME.getText().toString();
                String code = CODE.getText().toString();

                String stock_code="",stock_name="";
                if(!name.equals("")) {
                    stock_code = stockdic.getStockcode(name);
                    stock_name = name;
                    if (stock_code.equals("")){
                        Toast.makeText(context, "종목코드 오류", Toast.LENGTH_SHORT).show();
                        return;
                    }

                } else if(!code.equals("")) {
                    stock_name = stockdic.getStockname(code);
                    stock_code = code;
                    if (stock_name.equals("")){
                        Toast.makeText(context, "종목명 오류", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                // adapter list에 종목코드가 없으면 update사켜주고
                List<String> adapterList = simul_adapter.getRecyclerList();
                if(!adapterList.contains(stock_code)) {
                    adapterList.add(stock_code);
                    // 파일에도 추가해주고..info는 추가로 불러와야 함
                    addSimulFile(stock_code);
                    //simul_adapter.putRecyclerList(adapterList);
                }

                // list를 update하며 안된다
                // 정보가 없으니까...정보불러오기 끝나고 simul 버튼 누른 후 update 시키는 것으로
                // simul_adapter.putStocklist(sim_stock);

                simul_adapter.refresh();
                dialog_listmanagery.dismiss(); // 다이얼로그 닫기
            }
        });
        //  버튼
        dialog_listmanagery.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText stock_name = dialog_listmanagery.findViewById(R.id.stock_name);
                String name = stock_name.getText().toString();

                String stock_no = stockdic.getStockcode(name);
                if(stock_no.equals("")) {
                    Toast.makeText(context, "종목명 오류",Toast.LENGTH_SHORT).show();
                    return;
                }

                delSimulFile(stock_no);

                // 지우는 것은 price 파일을 불러올 필요없으니
                // 리스트에서 바로 삭제한다
                int index = sim_stock.indexOf(stock_no);
                sim_stock.remove(index);
                simul_adapter.putRecyclerList(sim_stock);
                simul_adapter.refresh();
                dialog_listmanagery.dismiss(); // 다이얼로그 닫기
            }
        });
    }


    void dl_yahoofinance_price(List<String> stocklist) {
        MyWeb myweb = new MyWeb();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                for(int i=0;i<stocklist.size();i++) {
                    new YFDownload(stocklist.get(i));
                }
                notice_ok(1);
            }
        }).start();
    }

    public void notice_ok(int i) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1L); // 잠시라도 정지해야 함
                    //Toast.makeText(context, "home fragment", Toast.LENGTH_SHORT).show();
                    if(i==0) simselect_bt.setTextColor(Color.YELLOW);
                    if(i==1) simdl_bt.setTextColor(Color.YELLOW);
                    if(i==2) simtool_bt.setTextColor(Color.YELLOW);
                    if(i==3) simsim_bt.setTextColor(Color.YELLOW);
                } catch (Exception e) {
                    System.out.println("인터럽트로 인한 스레드 종료.");
                    return;
                }
            }
        });
    }


    public void delSimulFile(String stockcode) {
        List<String> filelist = new ArrayList<>();
        filelist = myexcel.readSimullist();
        int index = filelist.indexOf(stockcode);
        filelist.remove(index);
        myexcel.writeSimullist(filelist);
    }

    public void addSimulFile(String stockcode) {
        List<String> filelist = new ArrayList<>();
        filelist = myexcel.readSimullist();
        if(!filelist.contains(stockcode)) {
            // 중복된 파일이 없으면 추가해준다.
            // file이 없으면 size()가 0이다. 0이어도 add, 아니어도 add한다.
            filelist.add(stockcode);
            myexcel.writeSimullist(filelist);
        }
    }

    public void init_resource(View view) {

        simselect_bt = view.findViewById(R.id.sim_selectitem);;
        simdl_bt = view.findViewById(R.id.sim_dl);
        simtool_bt = view.findViewById(R.id.sim_tool);;
        simsim_bt = view.findViewById(R.id.sim_sim);
        noti_board = view.findViewById(R.id.noti_board);
        addnew_img = view.findViewById(R.id.sim_addnew);
    }

    public void simulation() {

        sim_stock = myexcel.readSimullist();
        noti_board.setText(code2name(sim_stock));
        if((sim_stock.size() <= 0) || (!checkTestFile(sim_stock))) {
            String info = "test파일이 없습니다\n"+"전력선택 버튼을 눌러주세요";
            noti_board.setText(info);
        }
        // simulation 재진입 시, 이전의 testlist를 지워준다
        // 아래처럼 testlist는 add만 있기 때문에 추가되면
        // 반드시 지워주고 rebuild해야 한다
        bbandtestlist.clear();
        List<Balance> balancelist = new ArrayList<Balance>();
        int size=sim_stock.size();
        for(int i =0;i<size;i++) {
            String code = sim_stock.get(i);
            PriceBox pricebox = new PriceBox(code);
            List<Float> closeprice = new ArrayList<>();
            closeprice = pricebox.getClose();
            BBandTest bbtest = new BBandTest(code,closeprice, 60);
            bbandtestlist.add(bbtest);
            Balance balance = new Balance(code,0);
            balancelist.add(balance);
        }

        // recycler view 준비
        recyclerView = view.findViewById(R.id.sim_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(null);
        //recyclerView.setItemViewCacheSize(10);
        //pf_adapter = new PortfolioAdapter(getActivity(), portfolioList);
        simul_adapter = new SimulAdapter(getActivity(), sim_stock);
        recyclerView.setAdapter(simul_adapter);
        simul_adapter.putBBandTestList(bbandtestlist);
        simul_adapter.putChartdata(balancelist);
        simul_adapter.refresh();
    }

    public void setDefaultTextColor() {
        simselect_bt.setTextColor(Color.LTGRAY);
        simdl_bt.setTextColor(Color.LTGRAY);
        simtool_bt.setTextColor(Color.LTGRAY);
        simsim_bt.setTextColor(Color.LTGRAY);
    }
}