package com.gomu.gomustock.ui.simulation;

import static android.content.ContentValues.TAG;
import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
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

import com.github.mikephil.charting.charts.LineChart;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.R;
import com.gomu.gomustock.network.MyOpenApi;
import com.gomu.gomustock.network.MyWeb;
import com.gomu.gomustock.network.YFDownload;
import com.gomu.gomustock.stockdb.BuyStockDB;
import com.gomu.gomustock.stockdb.BuyStockDBData;
import com.gomu.gomustock.stockengin.BBandTest;
import com.gomu.gomustock.stockengin.Balance;
import com.gomu.gomustock.stockengin.PriceBox;
import com.gomu.gomustock.stockengin.StockDic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    private LineChart lineChart;

    private SimulAdapter simul_adapter;
    private List<Integer> chart_data1 = new ArrayList<Integer>();
    private List<Integer> chart_data2 = new ArrayList<Integer>();
    private List<Integer> chart1_data1 = new ArrayList<Integer>();
    private List<Integer> chart1_data2 = new ArrayList<Integer>();

    private BSManager sim_bsmanager;

    private String open_api_data="empty";
    private String yesterday_price="empty";

    TextView simselect_bt, simdl_bt, simtool_bt, simsim_bt;
    TextView noti_board;
    ImageView refreshPrice;
    ImageView addnew_img;

    private BackgroundThread update_thread = new BackgroundThread();
    private boolean stop_flag = false;
    private int DelaySecond=1;
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

        sim_stock = myexcel.readSimullist();

        init_resource(view);
        noti_board.setText(code2name(sim_stock));
        if(sim_stock.size()==0) {
            no_simulation();
        } else {
            if(!checkTestFile(sim_stock)) {
                String info = "test파일이 없습니다\n"+"전력선택 버튼을 눌러주세요";
                noti_board.setText(info);
                no_simulation();
            }
            else {
                simulation();
            }
        }

        simselect_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sim_stock = myexcel.readSimullist();
            }
        });

        simdl_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
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
            }
        });

        simsim_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                simulation();
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

        refreshPrice= (ImageView) view.findViewById(R.id.sim_refresh_price);
        refreshPrice.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DelaySecond = 1;
                //simul_adapter.reload_curprice();
                //simul_adapter.putStocklist(sim_stock);
                //simul_adapter.refresh();
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
                EditText stock_name = dialog_listmanagery.findViewById(R.id.stock_name);
                String name = stock_name.getText().toString();

                String stock_no = stockdic.getStockcode(name);
                if(stock_no.equals("")) {
                    Toast.makeText(context, "종목명 오류",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(!myexcel.file_check(stock_no+".xls")) {
                    sim_stock.add(stock_no);
                    //dl_NaverPriceByday(sim_stock, 240);
                    dl_yahoofinance_price(sim_stock);
                }

                addSimulFile(stock_no);
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
                simul_adapter.putStocklist(sim_stock);
                simul_adapter.refresh();
                dialog_listmanagery.dismiss(); // 다이얼로그 닫기
            }
        });
    }


    public void dl_NaverPriceByday(List<String> stocklist, int day) {
        MyWeb myweb = new MyWeb();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                for(int i=0;i<stocklist.size();i++) {
                    myweb.getNaverpriceByday(stocklist.get(i),day);
                }
            }
        }).start();
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
                notice_ok();
            }
        }).start();
    }

    public void notice_ok() {
        Log.d(TAG, "changeButtonText myLooper() " + Looper.myLooper());

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1L); // 잠시라도 정지해야 함
                    //Toast.makeText(context, "home fragment", Toast.LENGTH_SHORT).show();
                    simdl_bt.setTextColor(Color.YELLOW);
                } catch (Exception e) {
                    System.out.println("인터럽트로 인한 스레드 종료.");
                    return;
                }
            }
        });
    }

    public void dl_PriceHistory1Y(List<String> itemlist) {
        MyOpenApi myopenapi = new MyOpenApi();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                myopenapi.getPriceHistory1Y(itemlist);
            }
        }).start();
    }

    public void dl_IndexHistory1Y(String index_name) {
        MyOpenApi myopenapi = new MyOpenApi();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                myopenapi.getIndexHistory1Y(index_name);
            }
        }).start();
    }

    public void dl_PriceNIndexhistory3M(List<String> itemlist) {
        MyOpenApi myopenapi = new MyOpenApi();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                myopenapi.dl_Pricehistory3M(itemlist);
                myopenapi.dl_Indexhistory3M("코스피 200");
            }
        }).start();
    }

    public void dl_AgencyForeigne(List<String> buylist) {
        MyWeb myweb = new MyWeb();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                myweb.dl_fogninfo(buylist);
            }
        }).start();
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

    class BackgroundThread extends Thread {
        public void run() {
                try {
                    Thread.sleep(1L); // 진입 시 잠시라도 정지해야 함
                    // 60초*60분마다 한 번씩 웹크롤링으로 현재가 update
                    updatePortfolioPrice();
                    Thread.sleep(1000*DelaySecond); //  1초 대기
                } catch (InterruptedException e) {
                    System.out.println("인터럽트로 인한 스레드 종료.");
                    return;
                }
                //여기서는 Toast를 비롯한 UI작업을 실행못함
        }
    }

    public void updatePortfolioPrice() {
        Log.d(TAG, "changeButtonText myLooper() " + Looper.myLooper());

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1L); // 잠시라도 정지해야 함
                    //Toast.makeText(context, "home fragment", Toast.LENGTH_SHORT).show();
                    update_account();
                } catch (Exception e) {
                    System.out.println("인터럽트로 인한 스레드 종료.");
                    return;
                }
            }
        });
    }

    void update_account() {
        simul_adapter.refresh();
        DelaySecond = 60;
    }

    public void init_resource(View view) {

        simselect_bt = view.findViewById(R.id.sim_selectitem);;
        simdl_bt = view.findViewById(R.id.sim_dl);
        simtool_bt = view.findViewById(R.id.sim_tool);;
        simsim_bt = view.findViewById(R.id.sim_sim);
        noti_board = view.findViewById(R.id.noti_board);
        addnew_img = view.findViewById(R.id.sim_addnew);
    }

    public List<String> ReadBuyList() {
        BuyStockDB buystock_db;
        List<BuyStockDBData> buystockList = new ArrayList<>();
        List<String> buylist = new ArrayList<>();
        buystock_db = BuyStockDB.getInstance(context);
        buystockList = buystock_db.buystockDao().getAll();
        // buy stock list에서 중복된 종목을 제거한다
        Set<String> set = new HashSet<String>();
        for(int i =0;i < buystockList.size();i++ ) {
            set.add(buystockList.get(i).stock_code);
        }
        Iterator<String> iter = set.iterator();

        // 중복 종목명이 제거된 결과를 buylist에 저장한다
        while(iter.hasNext()) { //iter에 다음 읽을 데이터가 있다면
            buylist.add(iter.next());
        }
        return buylist;
    }

    public void simulation() {

        SCache mycache = new SCache();
        mycache.initialize();

        List<Balance> balancelist = new ArrayList<Balance>();
        for(int i =0;i<sim_stock.size();i++) {
            String code = sim_stock.get(i);
            //MyMagic01 mymagic01 = new MyMagic01(code, "069500");
            //mymagic01.makeBackdata();
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

        if(stop_flag!= true) {
            stop_flag=true;
            DelaySecond =1;
            //update_thread.start();
        }
    }

    public void no_simulation() {
        BuyStockDBData lastbuy = new BuyStockDBData();

        sim_bsmanager = new BSManager(context,"");

        if(lastbuy != null) {
            // recycler view 준비
            recyclerView = view.findViewById(R.id.sim_recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            //lastbuylist.add(sim_bsmanager.getPortfolio_dummy());
            simul_adapter = new SimulAdapter(getActivity(), sim_stock);
            recyclerView.setAdapter(simul_adapter);
        }

        if(stop_flag!= true) {
            stop_flag=true;
            DelaySecond =1;
            //update_thread.start();
        }
    }
}