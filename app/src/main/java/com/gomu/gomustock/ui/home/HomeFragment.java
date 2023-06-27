package com.gomu.gomustock.ui.home;

import static android.content.ContentValues.TAG;
import static com.gomu.gomustock.ui.home.HomeAdapter.buyList;
import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.gomu.gomustock.MyDate;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.MyStat;
import com.gomu.gomustock.R;
import com.gomu.gomustock.databinding.FragmentHomeBinding;
import com.gomu.gomustock.graph.MyChart;
import com.gomu.gomustock.graph.MyTreeMap;
import com.gomu.gomustock.network.MyOpenApi;
import com.gomu.gomustock.network.MyWeb;
import com.gomu.gomustock.stockdb.BuyStockDB;
import com.gomu.gomustock.stockdb.BuyStockDBData;
import com.gomu.gomustock.stockdb.StockDic;
import com.gomu.gomustock.stockengin.MyBalance;
import com.gomu.gomustock.ui.format.FormatChart;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private LineChart lineChart;

    private HomeAdapter home_adapter;
    private List<Integer> chart_data1 = new ArrayList<Integer>();
    private List<Integer> chart_data2 = new ArrayList<Integer>();
    private List<Integer> chart1_data1 = new ArrayList<Integer>();
    private List<Integer> chart1_data2 = new ArrayList<Integer>();
    private List<BuyStockDBData> lastbuylist = new ArrayList<BuyStockDBData>();
    private HBSManager hbsmanager;

    private String open_api_data="empty";
    private String yesterday_price="empty";

    Button tuja_bt, bench_bt;
    private BackgroundThread update_thread = new BackgroundThread();
    private boolean stop_flag = false;
    private int DelaySecond=1;
    List<MyBalance> balancelist = new ArrayList<>();

    public MyOpenApi myopenapi = new MyOpenApi();
    public MyExcel myexcel = new MyExcel();
    private List<Integer> chartcolor = new ArrayList<>();
    Dialog dialog_buy; // 커스텀 다이얼로그
    String latestOpenday; // 마지막 증시 오픈일, 오늘이 마지막증시 오픈일이 아니면 매도매수 안되게
    View root;

    StockDic stockdic = new StockDic();
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        dialog_buy = new Dialog(getActivity());       // Dialog 초기화
        dialog_buy.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog_buy.setContentView(R.layout.dialog_buy);

        recyclerView = root.findViewById(R.id.home_recycler_view);
        initialize_color();

        // 매수, 매도 DB를 합쳐서
        // 최종 보유주식과 매매 히스토리를 만든다.
        Cache mycache = new Cache();
        mycache.initialize();

        balancelist.clear();
        hbsmanager = new HBSManager(getActivity());
        hbsmanager.makeLastBuyList();
        lastbuylist = hbsmanager.getLastBuyList();

        if((lastbuylist.size() != 0 ) && (-1 != myexcel.checkExcelfile(lastbuylist))) {
            // 포트폴리오를 가지고 밸런스를 계산한다.
            calcTodayBalance(hbsmanager);
            // 별런스 결과로 차트를 보여준다
            cashChart();
            stockChart();
        }
        // 계좌 정보 보여주기
        top_board(root);

        // recycler view 준비
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        home_adapter = new HomeAdapter(getActivity(), lastbuylist);
        binding.homeRecyclerView.setAdapter(home_adapter);

        dl_checkMarketOpen();
        //}

        if(stop_flag!= true) {
            stop_flag=true;
            DelaySecond =1;
            //update_thread.start();
        }

        binding.filedownload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                List<String> buylist = hbsmanager.getOnlyBuyCode();
                buylist.add("069500"); // 코덱스 200은 무조건 다운로드 해줌
                dl_AgencyForeigne(buylist);
                dl_NaverPriceByday(buylist, 60);
                //dl_IndexHistory1Y("코스피 200");
                //home_adapter.app_restart();
            }
        });

        binding.infodownload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });

        binding.dummy.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //binding.dummy.setText("빽데이터");
                MyTreeMap mytree = new MyTreeMap(context);
                mytree.Treemapfile();
                // backtest용 data를 만들어 excel에 저장한다
            }
        });

        TextView buysell_history = root.findViewById(R.id.buysellhistory);
        binding.buysellhistory.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(context, SubActivity.class);


                //List<BuyStockDBData> mybuylist = hbsmanager.getBuyList();
                //List<SellStockDBData> myselllist = hbsmanager.getSellList();
                //SubOption mysuboption = new SubOption(mybuylist,myselllist);

                //intent.putExtra("class",mysuboption);
                //startActivity(intent);
            }
        });
        binding.dummy.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dl_checkMarketOpen();
            }
        });
        binding.buynew.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showDialog_buy();
                //app_restart();
            }
        });

        Button tuja_bt = (Button)root.findViewById(R.id.account_tuja);
        binding.accountTuja.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //
            }
        });

        //Button  bt_refresh= (Button)root.findViewById(R.id.refresh_price);
        binding.refreshPrice.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DelaySecond = 1;
                tuja_bt.setText(home_adapter.show_myaccount());
                home_adapter.reload_curprice();

                hbsmanager = new HBSManager(getActivity());
                hbsmanager.makeLastBuyList();
                lastbuylist = hbsmanager.getLastBuyList();

                calcTodayBalance(hbsmanager);
                cashChart();
                stockChart();
                home_adapter.refresh();
            }
        });

        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //editHome.setText(s);
            }
        });
        return root;
    }

    public void cashChart() {

        lineChart = (LineChart) root.findViewById(R.id.tuja_chart);
        MyChart money_chart = new MyChart();
        MyStat mystat = new MyStat();
        List<FormatChart> chartlist = new ArrayList<FormatChart>();

        // 각 종목의 balance의 평가액과 현금을 총합한다.
        int size = balancelist.size();
        if (size != 0) {
            List<List<Integer>> remainmoney = new ArrayList<List<Integer>>();;
            for (int i = 0; i < balancelist.size(); i++) {
                //remainmoney.add(balancelist.get(i).getRemaincache());
                remainmoney.add(balancelist.get(i).getTotalAsset());
            }
            List<Integer> last_remainmoney = mystat.sumlist(remainmoney);

            // 보유현금을 차트빌더에 넣는다
            chartlist = money_chart.buildChart_int(last_remainmoney, "잔액", chartcolor.get(0));
            //money_chart.setYMinmax(0, 0);
            money_chart.multi_chart(lineChart, chartlist, "자산증감", false);
        }
    }
    public void stockChart() {
        MyChart stock_chart = new MyChart();
        lineChart = (LineChart) root.findViewById(R.id.tuja_detail_chart);
        List<FormatChart> chartlist = new ArrayList<FormatChart>();
        MyStat mystat = new MyStat();

        int size = balancelist.size();
        if(size > 0) {
            List<List<Integer>> estimmoney= new ArrayList<List<Integer>>();
            for (int i = 0; i < balancelist.size(); i++) {
                estimmoney.add(balancelist.get(i).getEstimStock());
            }
            List<Integer> last_estimmoney = mystat.sumlist(estimmoney);

            // 총평가액을 차트에 보여준다
            chartlist = stock_chart.buildChart_int(last_estimmoney, "잔액", chartcolor.get(1));
            //stock_chart.setYMinmax(0, 0);
            stock_chart.multi_chart(lineChart, chartlist, "종목별총액증감", false);
        }
    }
    public void eachstockChart() {
        // 종목별 balance 결과 쭝 평가액변화를 차트에 보여준다.
        MyChart stock_chart = new MyChart();
        List<FormatChart> stockchart = new ArrayList<FormatChart>();
        lineChart = (LineChart) root.findViewById(R.id.tuja_detail_chart);

        int size = balancelist.size();
        if (size != 0) {
            // 종목별 평가액을 차트빌더에 넣는다.
            for (int i = 0; i < size; i++) {
                List<Integer> onechartdata = new ArrayList<Integer>();
                String stock_code = "";
                onechartdata = balancelist.get(i).getEstimStock();
                stock_code = balancelist.get(i).stock_code;
                stockchart = stock_chart.buildChart_int(onechartdata, stock_code, chartcolor.get(i));
            }

            //stock_chart.setYMinmax(0, 0);
            stock_chart.multi_chart(lineChart, stockchart, "종목별총액증감", false);
        }
    }

    public void calcTodayBalance(HBSManager mybsmanager) {
        List<BuyStockDBData> lastbuyli = new ArrayList<BuyStockDBData>();
        MyDate mydate = new MyDate();
        BuyStock buystock = new BuyStock();
        SellStock sellstock = new SellStock();
        lastbuyli = mybsmanager.getLastBuyList();

        balancelist.clear();
        String today = mydate.getToday();

        int size = lastbuyli.size();
        for (int i = 0; i < size; i++) {
            String stock_code = lastbuyli.get(i).stock_code;
            MyBalance onebalance = new MyBalance(stock_code);
            onebalance.prepareDataset(mybsmanager.getBuyList(stock_code), mybsmanager.getSellList(stock_code));
            //------------------------------------------------------
            // 오늘 현재가를 price table에 update해줘야 완벽하다.
            if(home_adapter != null ) {
                int cur_price = home_adapter.getCurrentPrice(stock_code);
                onebalance.putTodayPrice(today, cur_price);
            }
            // 시뮬데이션 대비 추가되는 내용 > 오늘 정보를 넣어주고 balance를 계산한다
            // 준비한 balance dataset에 데이터를 넣어준다.
            int todaybuy = buystock.getTodayBuyQuan(stock_code);
            int todaysell = sellstock.getTodaySellQuan(stock_code);
            onebalance.putTodayBuySellData(today,todaybuy,todaysell);
            //------------------------------------------------------
            onebalance.makeBalancedata();
            balancelist.add(onebalance);
        }
    }

    public void app_restart() {

        Intent intent = getActivity().getIntent();
        getActivity().overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().finish();

        getActivity().overridePendingTransition(0, 0);
        startActivity(intent);
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
                EditText stock_price = dialog_buy.findViewById(R.id.buy_price);
                String price = stock_price.getText().toString();
                EditText stock_quantity = dialog_buy.findViewById(R.id.buy_quantity);
                String quantity = stock_quantity.getText().toString();

                String stock_no = stockdic.getStockcode(name);
                //String stock_no = myexcel.find_stockno(name);
                if(stock_no.equals("")) {
                    Toast.makeText(context, "종목명 오류",Toast.LENGTH_SHORT).show();
                    return;
                }
                //dl_NaverPriceByday(stock_no,60);

                Date buydate = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                String mybuydate = format.format(buydate);
                if(!latestOpenday.equals(mybuydate)) {
                    Toast.makeText(context, "오늘은 거래날짜가 아닙니다",Toast.LENGTH_SHORT).show();
                    return; // 오늘이 오픈일이 아니면 매도매수 안됨
                }

                // db의 0번째에 매수데이터를 넣는다. 0번째가 가장 최신 데이터
                BuyStockDBData onebuy = new BuyStockDBData();
                onebuy.stock_code = stock_no;
                onebuy.stock_name = name;
                onebuy.buy_date = mybuydate;
                onebuy.buy_quantity = Integer.parseInt(quantity);
                onebuy.buy_price = Integer.parseInt(price);
                buyList.add(onebuy);

                BuyStock buystock = new BuyStock();
                buystock.insert2db(onebuy);

                Cache mycache = new Cache();
                int buymoney = Integer.parseInt(quantity)*Integer.parseInt(price)*-1;
                mycache.update_cache(buymoney);
                //app_restart();
                home_adapter.refresh();
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
    public void dl_NaverPriceByday(List<String> stock_code, int day) {
        MyWeb myweb = new MyWeb();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                for(int i=0;i<stock_code.size();i++) {
                    myweb.getNaverpriceByday(stock_code.get(i), day);
                }
                myweb.getNaverpriceByday("069500", day); // kodex 200 상품
                //updatePortfolioPrice();
            }
        }).start();
    }

    public void dl_checkMarketOpen() {
        MyWeb myweb = new MyWeb();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                latestOpenday = myweb.checkOpenday(); // 네이버금융을 체크해서 가장 마지막 영업일을 가져온다.
                home_adapter.setOpenday(latestOpenday);
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
            while(stop_flag) {
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
                    //cashChart();
                    //stockChart();
                } catch (Exception e) {
                    System.out.println("인터럽트로 인한 스레드 종료.");
                    return;
                }
            }
        });
    }

    void update_account() {
        tuja_bt.setText(home_adapter.show_myaccount());
        home_adapter.refresh();
        DelaySecond = 60;
    }

    public void top_board(View view) {
        tuja_bt = view.findViewById(R.id.account_tuja);
        bench_bt = view.findViewById(R.id.account_bench);

        tuja_bt.setText("투자계정 총10,000\n"+"현9,000"+"평1,000");
        tuja_bt.setBackgroundColor(context.getColor(R.color.MyBlack));
        tuja_bt.setTextColor(context.getColor(R.color.MyGray));

        bench_bt.setText("벤치계정 \n총1억"+"\n현금 : 1억"+"\n이자 : 500");
        bench_bt.setBackgroundColor(context.getColor(R.color.MyBlack));
        bench_bt.setTextColor(context.getColor(R.color.MyGray));
    }

    public List<String> Read_BuyList() {
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


    public void initialize_color() {
        chartcolor.add( context.getColor(R.color.Red));
        chartcolor.add( context.getColor(R.color.Yellow));
        chartcolor.add( context.getColor(R.color.Burlywood));
        chartcolor.add( context.getColor(R.color.Brown));
        chartcolor.add( context.getColor(R.color.SeaGreen));
        chartcolor.add( context.getColor(R.color.LawnGreen));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        update_thread.interrupt();
        stop_flag=true;
        DelaySecond = 1;
        binding = null;
    }

}