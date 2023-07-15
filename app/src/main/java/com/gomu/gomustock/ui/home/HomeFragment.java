package com.gomu.gomustock.ui.home;

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
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.R;
import com.gomu.gomustock.databinding.FragmentHomeBinding;
import com.gomu.gomustock.graph.MyChart;
import com.gomu.gomustock.graph.MyTreeMap;
import com.gomu.gomustock.network.MyWeb;
import com.gomu.gomustock.network.YFDownload;
import com.gomu.gomustock.stockengin.PriceBox;
import com.gomu.gomustock.stockengin.StockDic;
import com.gomu.gomustock.ui.format.FormatMyStock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;

    private HomeAdapter home_adapter;

    Button tuja_bt, bench_bt;

    private boolean stop_flag = false;

    List<FormatMyStock> mystocklist = new ArrayList<FormatMyStock>();
    List<String> homestock_list = new ArrayList<>();
    TextView filedown, infodown, dummy, history;


    public MyExcel myexcel = new MyExcel();
    private List<Integer> chartcolor = new ArrayList<>();
    Dialog dialog_buy; // 커스텀 다이얼로그
    String latestOpenday; // 마지막 증시 오픈일, 오늘이 마지막증시 오픈일이 아니면 매도매수 안되게
    View root;

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
        top_board(root);

        // 매수, 매도 DB를 합쳐서
        // 최종 보유주식과 매매 히스토리를 만든다.
        Cache mycache = new Cache();
        mycache.initialize();

        mystocklist = myexcel.readMyStockList();
        homestock_list = makeStocklist(mystocklist);
        FillPriceInStocklist(homestock_list,120);

        // recycler view 준비
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        home_adapter = new HomeAdapter(getActivity(), mystocklist);
        binding.homeRecyclerView.setAdapter(home_adapter);

        dl_checkMarketOpen();
        //}

        if(stop_flag!= true) {
            stop_flag=true;
        }

        binding.filedownload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dl_yahoofinance_price(homestock_list);
            }
        });

        binding.infodownload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                LineChart homeChart = root.findViewById(R.id.home_moeny_chart);
                MyChart home_chart = new MyChart();

                TextView tvhome_money_info = root.findViewById(R.id.home_money_info);
                String money_info = home_adapter.update_top_board();
                tvhome_money_info.setText(money_info);
                List<Float> total_line = home_adapter.getMoneyLine();
                home_chart.single_float(homeChart,total_line,"moeny line",false );
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

        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //editHome.setText(s);
            }
        });
        return root;
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
                StockDic stockdic = new StockDic();

                String stock_no = stockdic.getStockcode(name);
                //String stock_no = myexcel.find_stockno(name);
                if(stock_no.equals("")) {
                    Toast.makeText(context, "종목명 오류",Toast.LENGTH_SHORT).show();
                    return;
                }

                Date buydate = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                String mybuydate = format.format(buydate);
                if(!latestOpenday.equals(mybuydate)) {
                    Toast.makeText(context, "오늘은 거래날짜가 아닙니다",Toast.LENGTH_SHORT).show();
                    return; // 오늘이 오픈일이 아니면 매도매수 안됨
                }

                // 1. 파일update하고
                // 2. adapter list update하고

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


    private BackgroundThread update_thread = new BackgroundThread();

    public void update_curprice() {
        stop_flag = true;
    }

    class BackgroundThread extends Thread {
        public void run() {
            while(stop_flag) {
                try {
                    Thread.sleep(1L); // 진입 시 잠시라도 정지해야 함
                    // 60초*60분마다 한 번씩 웹크롤링으로 현재가 update
                    updatePortfolioPrice();
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
                    filedown.setTextColor(Color.YELLOW);
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
    }

    public void top_board(View view) {




        filedown = view.findViewById(R.id.filedownload);
        infodown = view.findViewById(R.id.infodownload);
        dummy = view.findViewById(R.id.dummy);
        history = view.findViewById(R.id.buysellhistory);

    }

    public void initialize_color() {
        chartcolor.add( context.getColor(R.color.Red));
        chartcolor.add( context.getColor(R.color.Yellow));
        chartcolor.add( context.getColor(R.color.Burlywood));
        chartcolor.add( context.getColor(R.color.Brown));
        chartcolor.add( context.getColor(R.color.SeaGreen));
        chartcolor.add( context.getColor(R.color.LawnGreen));
    }

    List<String> makeStocklist(List<FormatMyStock> mystocklist ) {
        List<String> stock_list = new ArrayList<>();
        int size = mystocklist.size();
        for(int i =0;i<size;i++) {
            stock_list.add(mystocklist.get(i).stock_code);
        }
        return stock_list;
    }

    void FillPriceInStocklist(List<String> stock_list, int days) {
        int size = stock_list.size();
        for(int i =0;i<size;i++) {
            PriceBox pricebox = new PriceBox(stock_list.get(i));
            mystocklist.get(i).chartdata = pricebox.getClose(days);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}