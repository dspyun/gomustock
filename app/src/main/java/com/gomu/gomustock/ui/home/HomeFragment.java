package com.gomu.gomustock.ui.home;

import static android.content.ContentValues.TAG;
import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.gomu.gomustock.FormatChart;
import com.gomu.gomustock.MyBalance;
import com.gomu.gomustock.MyChart;
import com.gomu.gomustock.MyOpenApi;
import com.gomu.gomustock.MyStat;
import com.gomu.gomustock.MyTreeMap;
import com.gomu.gomustock.MyWeb;
import com.gomu.gomustock.R;
import com.gomu.gomustock.databinding.FragmentHomeBinding;
import com.gomu.gomustock.portfolio.BuyStockDB;
import com.gomu.gomustock.portfolio.BuyStockDBData;
import com.gomu.gomustock.portfolio.Cache;
import com.gomu.gomustock.portfolio.Portfolio;
import com.gomu.gomustock.portfolio.SellStockDBData;

import java.util.ArrayList;
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
    private List<BuyStockDBData> portfolio = new ArrayList<BuyStockDBData>();
    private Portfolio myportfolio;

    private String open_api_data="empty";
    private String yesterday_price="empty";

    Button tuja_bt, bench_bt;
    private BackgroundThread update_thread = new BackgroundThread();
    private boolean stop_flag = false;
    private int DelaySecond=1;

    public MyOpenApi myopenapi = new MyOpenApi();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = root.findViewById(R.id.home_recycler_view);

        // 매수, 매도 DB를 합쳐서
        // 최종 보유주식과 매매 히스토리를 만든다.
        Cache mycache = new Cache();
        mycache.initialize();
        myportfolio = new Portfolio(getActivity());
        myportfolio.loadDB2Portfolio();
        portfolio = myportfolio.getPortfolio();

        // 포트폴리오 정보와 가격 히스토리를 가지고
        // 종목펼 수익변화차트 데이터를 만든다
        List<MyBalance> balancelist = new ArrayList<>();
        for(int i =0;i<portfolio.size();i++) {
            MyBalance onebalance = new MyBalance(portfolio.get(i).stock_code);
            onebalance.prepareDataset(myportfolio.getBuyList(),myportfolio.getSellList());
            onebalance.makeBalancedata();
            balancelist.add(onebalance);
        }
        int size = balancelist.size();


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
            chartlist = money_chart.buildChart_int(last_estimmoney, "평가액", context.getColor(R.color.Red));
            //chartlist = money_chart.buildChart_int(last_remainmoney, "잔액", context.getColor(R.color.White));

            lineChart = (LineChart) root.findViewById(R.id.tuja_chart);
            //money_chart.setYMinmax(0, 0);
            money_chart.multi_chart(lineChart, chartlist, "자산증감", false);
        }

        // 종목별 balance 결과 쭝 평가액변화를 차트에 보여준다.
        MyChart stock_chart = new MyChart();
        List<FormatChart> stockchart = new ArrayList<FormatChart>();

        if(size != 0 ) {
            for (int i = 0; i < size; i++) {
                List<Integer> onechartdata = new ArrayList<Integer>();
                String stock_code = "";
                onechartdata = balancelist.get(i).getEstimStock();
                stock_code = balancelist.get(i).stock_code;
                stockchart = stock_chart.buildChart_int(onechartdata, stock_code, context.getColor(R.color.Red));
            }
            lineChart = (LineChart) root.findViewById(R.id.tuja_detail_chart);
            //stock_chart.setYMinmax(0, 0);
            stock_chart.multi_chart(lineChart, stockchart, "종목별총액증감", false);
        }

        // 계좌 정보 보여주기
        top_board(root);

        // recycler view 준비
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //pf_adapter = new PortfolioAdapter(getActivity(), portfolioList);
        if(portfolio.size()==0) portfolio = myportfolio.getPortfolio_dummy();
        home_adapter = new HomeAdapter(getActivity(), portfolio);
        binding.homeRecyclerView.setAdapter(home_adapter);

        if(stop_flag!= true) {
            stop_flag=true;
            DelaySecond =1;
            update_thread.start();
        }

        binding.yfDownload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                List<String> buylist = new ArrayList<>();
                buylist = ReadBuyList();
                //dl_PriceNIndexhistory3M(buylist);
                dl_AgencyForeigne(buylist);
                dl_NaverPrice30();
                //dl_IndexHistory1Y("코스피 200");
            }
        });

        binding.realChart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                binding.realChart.setText("Data2DB");
                // backtest excel을 읽어 buydb, selldb에 나누어 저장한다
                // myportfolio.loadExcel2DB("005930_testset.xls");
            }
        });

        binding.standardChart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                binding.standardChart.setText("빽데이터");
                MyTreeMap mytree = new MyTreeMap(context);
                mytree.Treemapfile();
                // backtest용 data를 만들어 excel에 저장한다
            }
        });

        Button buysell_history = (Button)root.findViewById(R.id.buysell_history);
        binding.buysellHistory.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(context, SubActivity.class);

                List<BuyStockDBData> mybuylist = myportfolio.getBuyList();
                List<SellStockDBData> myselllist = myportfolio.getSellList();
                SubOption mysuboption = new SubOption(mybuylist,myselllist);

                intent.putExtra("class",mysuboption);
                startActivity(intent);
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
    public void dl_NaverPrice30() {
        MyWeb myweb = new MyWeb();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                myweb.getNaverprice30("005930");
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        update_thread.interrupt();
        stop_flag=true;
        DelaySecond = 1;
        binding = null;
    }

}