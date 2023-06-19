package com.gomu.gomustock.ui.simulation;

import static android.content.ContentValues.TAG;
import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.gomu.gomustock.FormatChart;
import com.gomu.gomustock.MyBalance;
import com.gomu.gomustock.MyChart;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.MyMagic01;
import com.gomu.gomustock.MyOpenApi;
import com.gomu.gomustock.MyStat;
import com.gomu.gomustock.MyWeb;
import com.gomu.gomustock.R;
import com.gomu.gomustock.portfolio.BuyStockDB;
import com.gomu.gomustock.portfolio.BuyStockDBData;

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

    private SPortfolio sim_portfolio;

    private String open_api_data="empty";
    private String yesterday_price="empty";

    Button tuja_bt, bench_bt, simstock_bt, simdl_bt, simtool_bt, simsim_bt;
    ImageView refreshPrice;
    private BackgroundThread update_thread = new BackgroundThread();
    private boolean stop_flag = false;
    private int DelaySecond=1;
    private List<String> sim_stock = new ArrayList<>();
    public MyOpenApi myopenapi = new MyOpenApi();

    private List<Integer> chartcolor = new ArrayList<>();
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
        SimulationView(view);
        return view;
    }
    public void SimulationView(View view) {
        List<BuyStockDBData> lastbuylist = new ArrayList<BuyStockDBData>();
        initialize_color();

        // 매수, 매도 DB를 합쳐서
        // 최종 보유주식과 매매 히스토리를 만든다.
        SCache mycache = new SCache();
        mycache.initialize();
        sim_portfolio = new SPortfolio(getActivity());
        //List<String> testdata = new ArrayList<>();
        //testdata.add("000660_testset.xls");
        //testdata.add("005930_testset.xls");
        //myportfolio.loadExcel2DB(testdata);
        //myportfolio.loadDB2Portfolio();
        lastbuylist = sim_portfolio.getPortfolio();

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

        // recycler view 준비
        recyclerView = view.findViewById(R.id.sim_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //pf_adapter = new PortfolioAdapter(getActivity(), portfolioList);
        if(lastbuylist.size()==0) lastbuylist = sim_portfolio.getPortfolio_dummy();
        simul_adapter = new SimulAdapter(getActivity(), lastbuylist);
        recyclerView.setAdapter(simul_adapter);

        if(stop_flag!= true) {
            stop_flag=true;
            DelaySecond =1;
            //update_thread.start();
        }

        simstock_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(sim_stock.size() ==0) {
                    sim_stock.add("005930");
                    sim_stock.add("000660");
                    sim_stock.add("069500");
                }
            }
        });

        simdl_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dl_NaverPrice30(sim_stock);
            }
        });

        simtool_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String code, name;
                MyExcel myexcel = new MyExcel();
                // backtest용 data를 만들어 excel에 저장한다
                for(int i =0;i<sim_stock.size();i++) {
                    code = sim_stock.get(i);
                    MyMagic01 mymagic01 = new MyMagic01(code, "069500");
                    mymagic01.makeBackdata();
                }


            }
        });

        Button buysell_history = (Button)view.findViewById(R.id.buysell_history);
        simsim_bt.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                simulation();
            }
        });

        tuja_bt.setOnClickListener(new View.OnClickListener()
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
                tuja_bt.setText(simul_adapter.show_myaccount());
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
                tuja_bt.setText(simul_adapter.show_myaccount());
                simul_adapter.reload_curprice();
                simul_adapter.refresh();
            }
        });
    }

    public void dl_NaverPrice30(List<String> stocklist) {
        MyWeb myweb = new MyWeb();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                for(int i=0;i<stocklist.size();i++) {
                    myweb.getNaverprice30(stocklist.get(i));
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
        tuja_bt.setText(simul_adapter.show_myaccount());
        simul_adapter.refresh();
        DelaySecond = 60;
    }

    public void top_board(View view) {

        simstock_bt = view.findViewById(R.id.sim_stock);
        simstock_bt.setText("종목선택");
        simdl_bt = view.findViewById(R.id.sim_dl);
        simdl_bt.setText("DataDL");
        simtool_bt = view.findViewById(R.id.sim_tool);
        simtool_bt.setText("전략선택");
        simsim_bt = view.findViewById(R.id.sim_sim);
        simtool_bt.setText("평가");

        tuja_bt = view.findViewById(R.id.sim_account_info);
        bench_bt = view.findViewById(R.id.sim_bench_info);

        tuja_bt.setText("투자계정 총10,000\n"+"현9,000"+"평1,000");
        tuja_bt.setBackgroundColor(context.getColor(R.color.MyBlack));
        tuja_bt.setTextColor(context.getColor(R.color.MyGray));

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
        List<BuyStockDBData> lastbuylist = new ArrayList<BuyStockDBData>();
        SCache mycache = new SCache();
        mycache.initialize();
        sim_portfolio = new SPortfolio(getActivity());
        List<String> testdata = new ArrayList<>();
        /*
        for(int i =0;i<sim_stock.size();i++) {
            testdata.add(sim_stock.get(i));
        }
         */
        testdata.add(sim_stock.get(0));
        sim_portfolio.loadExcel2DB2(testdata);
        sim_portfolio.loadDB2Portfolio();
        lastbuylist = sim_portfolio.getPortfolio();

        // 포트폴리오 정보와 가격 히스토리를 가지고
        // 수익변화차트 데이터를 만든다
        List<MyBalance> balancelist = new ArrayList<>();
        for(int i =0;i<lastbuylist.size();i++) {
            MyBalance onebalance = new MyBalance(lastbuylist.get(i).stock_code);
            onebalance.prepareDataset(sim_portfolio.getBuyList(),sim_portfolio.getSellList());
            onebalance.makeBalancedata();
            balancelist.add(onebalance);
        }

        // 종목별 balance 결과 중 평가액변화를 차트에 보여준다.
        MyChart stock_chart = new MyChart();
        List<FormatChart> stockchart = new ArrayList<FormatChart>();
        int size = balancelist.size();
        if(size != 0 ) {
            for (int i = 0; i < size; i++) {
                List<Integer> onechartdata = new ArrayList<Integer>();
                String stock_code = "";
                onechartdata = balancelist.get(i).getEstimStock();
                stock_code = balancelist.get(i).stock_code;
                stockchart = stock_chart.buildChart_int(onechartdata, stock_code, context.getColor(R.color.Red));
            }
            lineChart = (LineChart) view.findViewById(R.id.sim_stock_chart);
            stock_chart.setYMinmax(0, 0);
            stock_chart.multi_chart(lineChart, stockchart, "종목별총액증감", false);
        }

        // 각 종목의 balance의 평가액과 현금을 총합한다.
        if(size != 0 ) {
            List<List<Integer>> estimmoney = new ArrayList<List<Integer>>();
            List<List<Integer>> remainmoney = new ArrayList<List<Integer>>();
            for (int i = 0; i < balancelist.size(); i++) {
                estimmoney.add(balancelist.get(i).getEstimStock());
                remainmoney.add(balancelist.get(0).getRemaincache());
            }
            MyStat mystat = new MyStat();
            List<Integer> last_estimmoney = mystat.sumlist(estimmoney);
            List<Integer> last_remainmoney = mystat.sumlist(remainmoney);

            // 총합된 평가액과 현금을 차트로 보여준다
            MyChart money_chart = new MyChart();
            // 합친내용을 차트에 보여준다
            List<FormatChart> chartlist = new ArrayList<FormatChart>();
            //money_chart.buildChart_int(last_remainmoney, "잔액", context.getColor(R.color.White));
            chartlist = money_chart.buildChart_int(last_estimmoney, "평가액", context.getColor(R.color.Red));

            lineChart = (LineChart) view.findViewById(R.id.sim_account_chart);
            money_chart.setYMinmax(0, 0);
            money_chart.multi_chart(lineChart, chartlist, "자산증감", false);
        }
        // 계좌 정보 보여주기
        top_board(view);

        // recycler view 준비
        recyclerView = view.findViewById(R.id.sim_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //pf_adapter = new PortfolioAdapter(getActivity(), portfolioList);
        if(lastbuylist.size()==0) lastbuylist = sim_portfolio.getPortfolio_dummy();
        simul_adapter = new SimulAdapter(getActivity(), lastbuylist);
        recyclerView.setAdapter(simul_adapter);

        if(stop_flag!= true) {
            stop_flag=true;
            DelaySecond =1;
            //update_thread.start();
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