package com.gomu.gomustock.ui.simulation;

import static android.content.ContentValues.TAG;
import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.MyStat;
import com.gomu.gomustock.R;
import com.gomu.gomustock.graph.MyChart;
import com.gomu.gomustock.network.MyOpenApi;
import com.gomu.gomustock.network.MyWeb;
import com.gomu.gomustock.stockdb.BuyStockDB;
import com.gomu.gomustock.stockdb.BuyStockDBData;
import com.gomu.gomustock.stockdb.StockDic;
import com.gomu.gomustock.stockengin.BBandTest;
import com.gomu.gomustock.stockengin.MyBalance;
import com.gomu.gomustock.ui.format.FormatChart;

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

    private SBSManager sim_bsmanager;

    private String open_api_data="empty";
    private String yesterday_price="empty";

    Button asset_bt, bench_bt;
    TextView simselect_bt, simdl_bt, simtool_bt, simsim_bt;
    ImageView refreshPrice;
    private BackgroundThread update_thread = new BackgroundThread();
    private boolean stop_flag = false;
    private int DelaySecond=1;
    private List<String> sim_stock = new ArrayList<>();
    public MyOpenApi myopenapi = new MyOpenApi();
    Dialog dialog_buy; // 커스텀 다이얼로그
    private List<Integer> chartcolor = new ArrayList<>();
    MyExcel myexcel = new MyExcel();
    StockDic stockdic = new StockDic();

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

        dialog_buy = new Dialog(getActivity());       // Dialog 초기화
        dialog_buy.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog_buy.setContentView(R.layout.dialog_buy);

        SimulationView(view);
        return view;
    }
    public void SimulationView(View view) {


        initialize_color();
        sim_stock = myexcel.readSimullist();
        if(sim_stock.size()==0) {
            no_simulation();
        } else {
            first_simulation();
            //no_simulation();
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
                dl_NaverPriceByday(sim_stock, 60);
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
                    BBandTest mybbtest = new BBandTest(code);
                    mybbtest.makeBackdata();
                }
            }
        });

        TextView buysell_history = view.findViewById(R.id.buysellhistory);
        simsim_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                simulation();
            }
        });

        ImageView addnew_img = view.findViewById(R.id.sim_addnew);
        addnew_img.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showDialog_buy();
                //app_restart();
            }
        });

        asset_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // 아래 내용 바꿀 것
            }
        });

        bench_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // 아래 내용 바꿀 것
                DelaySecond = 1;
                asset_bt.setText(simul_adapter.show_myaccount());
                simul_adapter.reload_curprice();
                simul_adapter.refresh();
            }
        });

        refreshPrice= (ImageView) view.findViewById(R.id.sim_refresh_price);
        refreshPrice.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DelaySecond = 1;
                asset_bt.setText(simul_adapter.show_myaccount());
                simul_adapter.reload_curprice();
                simul_adapter.refresh();
            }
        });
    }


    public void showDialog_buy(){

        dialog_buy.show(); // 다이얼로그 띄우기
        dialog_buy.findViewById(R.id.buyBtn).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // 원하는 기능 구현

                // dialog 화면에서 입력된 정보를 읽어온다
                EditText stock_name = dialog_buy.findViewById(R.id.stock_name);
                String name = stock_name.getText().toString();

                String stock_no = stockdic.getStockcode(name);
                if(stock_no.equals("")) {
                    Toast.makeText(context, "종목명 오류",Toast.LENGTH_SHORT).show();
                    return;
                }

                // db의 0번째에 매수데이터를 넣는다. 0번째가 가장 최신 데이터
                List<BuyStockDBData> simulist = new ArrayList<>();
                BuyStockDBData onebuy = new BuyStockDBData();
                onebuy.stock_code = stock_no;
                onebuy.stock_name = name;
                simulist = simul_adapter.getRecyclerList();
                simulist.add(onebuy);
                simul_adapter.putBuyList(simulist);

                //SBuyStock buystock = new SBuyStock();
                //buystock.insert2db(onebuy);

                List<String> filelist = new ArrayList<>();
                filelist = myexcel.readSimullist();
                if(!filelist.contains(stock_no)) {
                    // 중복된 파일이 없으면 추가해준다.
                    // file이 없으면 size()가 0이다. 0이어도 add, 아니어도 add한다.
                    filelist.add(stock_no);
                    myexcel.writeSimullist(filelist);
                }

                simul_adapter.refresh();
                dialog_buy.dismiss(); // 다이얼로그 닫기
            }
        });
        //  버튼
        dialog_buy.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 원하는 기능 구현
                dialog_buy.dismiss(); // 다이얼로그 닫기
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
        asset_bt.setText(simul_adapter.show_myaccount());
        simul_adapter.refresh();
        DelaySecond = 60;
    }

    public void top_board(View view) {

        simselect_bt = view.findViewById(R.id.sim_selectitem);;
        simdl_bt = view.findViewById(R.id.sim_dl);
        simtool_bt = view.findViewById(R.id.sim_tool);;
        simsim_bt = view.findViewById(R.id.sim_sim);

        asset_bt = view.findViewById(R.id.sim_asset_info);
        bench_bt = view.findViewById(R.id.sim_bench_info);

        asset_bt.setText("투자계정 총10,000\n"+"현9,000"+"평1,000");
        asset_bt.setBackgroundColor(context.getColor(R.color.MyBlack));
        asset_bt.setTextColor(context.getColor(R.color.MyGray));

        bench_bt.setText("벤치계정 \n총1억"+"\n현금 : 1억"+"\n이자 : 500");
        bench_bt.setBackgroundColor(context.getColor(R.color.MyBlack));
        bench_bt.setTextColor(context.getColor(R.color.MyGray));
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
        BuyStockDBData lastbuy = new BuyStockDBData();
        List<BuyStockDBData> lastbuylist = new ArrayList<BuyStockDBData>();
        List<MyBalance> balancelist = new ArrayList<>();

        simul_adapter.reload_curprice();

        // manager가 data를 생성해내고
        // balance가 data를 계산한다
        // 계산된 data는 차트가 보여준다
        for(int i=0;i<sim_stock.size();i++) {

            sim_bsmanager = new SBSManager();
            sim_bsmanager.buystock.reset(); // buydb를 비운다
            sim_bsmanager.sellstock.reset(); // selldb를 비운다

            sim_bsmanager.loadExcel2DB(sim_stock.get(i)); // 과거>현재 순의 시험데이터를 DB로 넣는다.
            sim_bsmanager.makeLastBuyList();
            lastbuy = sim_bsmanager.getLastBuy(); // 과거>현재 순으로  정렬된 데이터.
            lastbuylist.add(lastbuy);
            // 포트폴리오 정보와 가격 히스토리를 가지고
            // 수익변화차트 데이터를 만든다

            String stock_code = lastbuy.stock_code;
            MyBalance onebalance = new MyBalance(stock_code);
            onebalance.prepareDataset(sim_bsmanager.getBuyList(stock_code), sim_bsmanager.getSellList(stock_code));
            onebalance.makeBalancedata();
            balancelist.add(onebalance);
        }
        cacheChart(balancelist);
        stockChart(balancelist);

        // 계좌 정보 보여주기
        top_board(view);

        // recycler view 준비
        recyclerView = view.findViewById(R.id.sim_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //pf_adapter = new PortfolioAdapter(getActivity(), portfolioList);
        simul_adapter = new SimulAdapter(getActivity(), lastbuylist);
        recyclerView.setAdapter(simul_adapter);

        if(stop_flag!= true) {
            stop_flag=true;
            DelaySecond =1;
            //update_thread.start();
        }
    }

    public void no_simulation() {
        BuyStockDBData lastbuy = new BuyStockDBData();
        List<BuyStockDBData> lastbuylist = new ArrayList<BuyStockDBData>();
        // 매수, 매도 DB를 합쳐서
        // 최종 보유주식과 매매 히스토리를 만든다.
        SCache mycache = new SCache();
        mycache.initialize();
        sim_bsmanager = new SBSManager(getActivity());
        //sim_bsmanager.makeLastBuyList();
        //lastbuy = sim_bsmanager.getLastBuy();

        // 종목별 balance 결과 쭝 평가액변화를 차트에 보여준다.
        MyChart stock_chart = new MyChart();
        List<FormatChart> stockchart = new ArrayList<FormatChart>();
        List<Integer> onechartdata = new ArrayList<Integer>();
        for(int i = 0;i<30;i++) onechartdata.add(0);
        stockchart = stock_chart.buildChart_int(onechartdata, "", chartcolor.get(0));

        lineChart = (LineChart) view.findViewById(R.id.sim_stock_chart);
        stock_chart.multi_chart(lineChart, stockchart, "초기값", false);

        MyChart money_chart = new MyChart();
        money_chart.multi_chart(lineChart, stockchart, "초기값", false);

        // 계좌 정보 보여주기
        top_board(view);

        if(lastbuy != null) {
            // recycler view 준비
            recyclerView = view.findViewById(R.id.sim_recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            lastbuylist.add(sim_bsmanager.getPortfolio_dummy());
            simul_adapter = new SimulAdapter(getActivity(), lastbuylist);
            recyclerView.setAdapter(simul_adapter);
        }

        if(stop_flag!= true) {
            stop_flag=true;
            DelaySecond =1;
            //update_thread.start();
        }
    }
    public void first_simulation() {
        SCache mycache = new SCache();
        mycache.initialize();
        BuyStockDBData lastbuy = new BuyStockDBData();
        List<BuyStockDBData> lastbuylist = new ArrayList<BuyStockDBData>();
        List<MyBalance> balancelist = new ArrayList<>();

        //simul_adapter.reload_curprice();

        // manager가 data를 생성해내고
        // balance가 data를 계산한다
        // 계산된 data는 차트가 보여준다
        for(int i=0;i<sim_stock.size();i++) {

            sim_bsmanager = new SBSManager();
            sim_bsmanager.buystock.reset(); // buydb를 비운다
            sim_bsmanager.sellstock.reset(); // selldb를 비운다

            sim_bsmanager.loadExcel2DB(sim_stock.get(i)); // 과거>현재 순의 시험데이터를 DB로 넣는다.
            sim_bsmanager.makeLastBuyList();
            lastbuy = sim_bsmanager.getLastBuy(); // 과거>현재 순으로  정렬된 데이터.
            lastbuylist.add(lastbuy);
            // 포트폴리오 정보와 가격 히스토리를 가지고
            // 수익변화차트 데이터를 만든다

            String stock_code = lastbuy.stock_code;
            MyBalance onebalance = new MyBalance(stock_code);
            onebalance.prepareDataset(sim_bsmanager.getBuyList(stock_code), sim_bsmanager.getSellList(stock_code));
            onebalance.makeBalancedata();
            balancelist.add(onebalance);
        }
        cacheChart(balancelist);
        stockChart(balancelist);

        // 계좌 정보 보여주기
        top_board(view);

        // recycler view 준비
        recyclerView = view.findViewById(R.id.sim_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //pf_adapter = new PortfolioAdapter(getActivity(), portfolioList);
        simul_adapter = new SimulAdapter(getActivity(), lastbuylist);
        recyclerView.setAdapter(simul_adapter);

        if(stop_flag!= true) {
            stop_flag=true;
            DelaySecond =1;
            //update_thread.start();
        }

    }
    public void cacheChart(List<MyBalance> balancelist) {

        int size = balancelist.size();
        // 각 종목의 balance의 평가액과 현금을 총합한다.
        if(size != 0 ) {
            List<List<Integer>> remainmoney = new ArrayList<List<Integer>>();
            List<List<Integer>> estimmoney = new ArrayList<List<Integer>>();
            for (int i = 0; i < size ; i++) {
                remainmoney.add(balancelist.get(i).getRemaincache());
                estimmoney.add(balancelist.get(i).getEstimStock());
                int j = 0;
            }
            MyStat mystat = new MyStat();
            List<Integer> last_remainmoney = mystat.sumlist(remainmoney);
            List<Integer> last_estimmoney = mystat.sumlist(estimmoney);
            size = last_remainmoney.size();
            int temp;
            SCache mycache = new SCache();
            int firstcache = mycache.getFirstCache();
            for(int i=0;i<size;i++) {
                temp = last_remainmoney.get(i) +last_estimmoney.get(i)+ firstcache;
                last_remainmoney.set(i,temp);
            }

            // 총합된 평가액과 현금을 차트로 보여준다
            MyChart money_chart = new MyChart();
            // 합친내용을 차트에 보여준다
            List<FormatChart> chartlist = new ArrayList<FormatChart>();
            chartlist = money_chart.buildChart_int(last_remainmoney, "잔액", context.getColor(R.color.White));

            lineChart = (LineChart) view.findViewById(R.id.sim_cache_chart);
            //money_chart.setYMinmax(0, 0);
            money_chart.multi_chart(lineChart, chartlist, "자산증감", false);
        }
    }

    public void stockChart(List<MyBalance> balancelist) {
        // 종목별 balance 결과 중 평가액변화를 차트에 보여준다.
        MyChart stock_chart = new MyChart();
        List<FormatChart> stockchart = new ArrayList<FormatChart>();
        List<List<Integer>> estimmoney = new ArrayList<List<Integer>>();
        int size = balancelist.size();
        if(size != 0 ) {
            for (int i = 0; i < size; i++) {
                List<Integer> onechartdata = new ArrayList<Integer>();
                String stock_code = "";
                onechartdata = balancelist.get(i).getEstimStock();
                estimmoney.add(onechartdata); // 평가금액 총합 리스트에 개별종목 평가금액을 넣어준다.
                stock_code = balancelist.get(i).stock_code;
                stockchart = stock_chart.buildChart_int(onechartdata, stock_code, chartcolor.get(i));
            }
            MyStat mystat = new MyStat();
            List<Integer> last_estimmoney = mystat.sumlist(estimmoney); // 평가금액 총합리스트의 액수를 계산한다
            lineChart = (LineChart) view.findViewById(R.id.sim_stock_chart);
            //stock_chart.setYMinmax(0, 0);
            stock_chart.multi_chart(lineChart, stockchart, "종목별총액증감", false);
        }
    }

    public void initialize_color() {
        chartcolor.add( context.getColor(R.color.Red));
        chartcolor.add( context.getColor(R.color.Yellow));
        chartcolor.add( context.getColor(R.color.Burlywood));
        chartcolor.add( context.getColor(R.color.Brown));
        chartcolor.add( context.getColor(R.color.SeaGreen));
        chartcolor.add( context.getColor(R.color.LawnGreen));
    }

}