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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.MyStat;
import com.gomu.gomustock.R;
import com.gomu.gomustock.databinding.FragmentHomeBinding;
import com.gomu.gomustock.graph.MyChart;
import com.gomu.gomustock.network.InfoDownload;
import com.gomu.gomustock.network.MyWeb;
import com.gomu.gomustock.network.YFDownload;
import com.gomu.gomustock.stockengin.BBandTest;
import com.gomu.gomustock.stockengin.PriceBox;
import com.gomu.gomustock.stockengin.RSITest;
import com.gomu.gomustock.stockengin.StockDic;
import com.gomu.gomustock.ui.format.FormatChart;
import com.gomu.gomustock.ui.format.FormatMyStock;
import com.gomu.gomustock.ui.format.FormatStockInfo;

import java.util.ArrayList;
import java.util.Collections;
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


    public MyWeb myweb = new MyWeb();
    public MyExcel myexcel = new MyExcel();
    private List<Integer> chartcolor = new ArrayList<>();
    Dialog dialog_buy; // 커스텀 다이얼로그
    Dialog dialog_progress; // 커스텀 다이얼로그
    String latestOpenday; // 마지막 증시 오픈일, 오늘이 마지막증시 오픈일이 아니면 매도매수 안되게
    View root;
    String g_today_level="", g_period_level="";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        dialog_buy = new Dialog(getActivity());       // Dialog 초기화
        dialog_buy.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog_buy.setContentView(R.layout.dialog_buy);

        dialog_progress = new Dialog(getActivity());       // Dialog 초기화
        dialog_progress.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog_progress.setContentView(R.layout.dialog_progress);

        recyclerView = root.findViewById(R.id.home_recycler_view);
        initialize_color();
        top_board(root);

        // 매수, 매도 DB를 합쳐서
        // 최종 보유주식과 매매 히스토리를 만든다.
        Cache mycache = new Cache();
        mycache.initialize();

        mystocklist = myexcel.readMyStock("mystock");
        homestock_list = makeStocklist(mystocklist);
        FillPriceInStocklist(homestock_list,120);
        fill_chartdata();

        // recycler view 준비
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        home_adapter = new HomeAdapter(getActivity(), mystocklist);
        binding.homeRecyclerView.setAdapter(home_adapter);

        dl_checkMarketOpen();

        if(stop_flag!= true) {
            stop_flag=true;
        }

        binding.filedownload.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                YFDownload_Dialog(homestock_list,"All");
                home_adapter.refresh();
                fragment_refresh();
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
                home_chart.single_float(homeChart,total_line,"총자산변화",false );
            }
        });

        binding.dummy.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                YFDownload_Dialog(homestock_list,"TODAY");
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


    public void fragment_refresh() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
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
;
                StockDic stockdic = new StockDic();

                String code = stockdic.getStockcode(name);
                //String stock_no = myexcel.find_stockno(name);
                if(code.equals("")) {
                    EditText stock_code = dialog_buy.findViewById(R.id.stock_code);
                    code = stock_code.getText().toString();
                    name = stockdic.getStockname(code);
                    if(name.equals("")) {
                        Toast.makeText(context, code + " / " + name + " : 코드/종목명 오류", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                /* 거래날짜 체크 : 일단 막아놓는다
                Date buydate = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                String mybuydate = format.format(buydate);
                if(!latestOpenday.equals(mybuydate)) {
                    Toast.makeText(context, "오늘은 거래날짜가 아닙니다",Toast.LENGTH_SHORT).show();
                    return; // 오늘이 오픈일이 아니면 매도매수 안됨
                }
                */
                // 파일에 저장한다
                MyExcel myexcel = new MyExcel();
                List<FormatMyStock> mystock = new ArrayList<>();
                mystock = myexcel.readMyStock("mystock");
                FormatMyStock newstock = new FormatMyStock();
                newstock.stock_code = code;
                newstock.stock_name = name;
                mystock.add(newstock);
                myexcel.writeMyStock(mystock);

                dialog_buy.dismiss(); // 다이얼로그 닫기
            }
        });
        //  버튼
        dialog_buy.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText stock_name = dialog_buy.findViewById(R.id.stock_name);
                String name = stock_name.getText().toString();

                StockDic stockdic = new StockDic();

                String code = stockdic.getStockcode(name);
                //String stock_no = myexcel.find_stockno(name);
                if(code.equals("")) {
                    EditText stock_code = dialog_buy.findViewById(R.id.stock_code);
                    code = stock_code.getText().toString();
                    name = stockdic.getStockname(code);
                    if(name.equals("")) {
                        Toast.makeText(context, code + " / " + name + " : 코드/종목명 오류", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                /* 거래날짜 체크 : 일단 막아놓는다
                Date buydate = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                String mybuydate = format.format(buydate);
                if(!latestOpenday.equals(mybuydate)) {
                    Toast.makeText(context, "오늘은 거래날짜가 아닙니다",Toast.LENGTH_SHORT).show();
                    return; // 오늘이 오픈일이 아니면 매도매수 안됨
                }
                */
                // 파일에 저장한다
                MyExcel myexcel = new MyExcel();
                List<FormatMyStock> mystock = new ArrayList<>();
                mystock = myexcel.readMyStock("mystock");
                int size = mystock.size();
                for(int i =0;i<size;i++) {
                    if(mystock.get(i).stock_code.equals(code)) {
                        mystock.remove(i);
                        break;
                    }
                }
                myexcel.writeMyStock(mystock);

                dialog_buy.dismiss(); // 다이얼로그 닫기
            }
        });
    }

    public void downloadNowPrice(List<String> stock_list, int hour) {
        int size = stock_list.size();
        String sizestr = Integer.toString(size);
        for(int i =0;i<size;i++) {
            String stock_code = stock_list.get(i);
            //_cb.callback("오늘가격" + "\n"+"다운로드" + "\n"+ Integer.toString(i)+ "/"+ sizestr+"\n"+ stock_code);
            myweb.getNaverpriceByToday(stock_code, 6 * hour); // 1시간을 읽어서 저장한다
        }
    }

    public void YFDownload_Dialog(List<String> stock_list, String guide){
        dialog_progress.show(); // 다이얼로그 띄우기
        dialog_progress.setCanceledOnTouchOutside(false);
        dialog_progress.setCancelable(false);
        ProgressBar dlg_bar = dialog_progress.findViewById(R.id.dialog_progressBar);

        Thread dlg_thread = new Thread(new Runnable() {
            MyWeb myweb = new MyWeb();
            MyExcel myexcel = new MyExcel();
            List<FormatStockInfo> web_stockinfo = new ArrayList<FormatStockInfo>();
            FormatStockInfo oneinfo = new FormatStockInfo();
            public void run() {
                try {
                    int hour = 4;
                    int max = stock_list.size();
                    InfoDownload info = new InfoDownload();
                    dlg_bar.setProgress(0);
                    for(int i=0;i<stock_list.size();i++) {
                        dlg_bar.setProgress(100*(i+1)/max);
                        // 1. 주가를 다운로드 하고
                        if(guide.equals("All")) {
                            new YFDownload(stock_list.get(i)); // 1년치를 다운로드 받고
                            myweb.getNaverpriceByToday(stock_list.get(i), 6 * hour); // hour시간을 읽어서 저장한다.
                            oneinfo = info.downloadStockInfoOne(stock_list.get(i));
                            web_stockinfo.add(oneinfo);
                        } else {
                            // "TODAY"
                            myweb.getNaverpriceByToday(stock_list.get(i), 6 * hour); // hour시간을 읽어서 저장한다.
                        }
                    }
                    if(guide.equals("All")) myexcel.writestockinfoCustom("mystock",web_stockinfo);
                    notice_ok();
                    dialog_progress.dismiss();
                } catch (Exception ex) {
                    Log.e("MainActivity", "Exception in processing mesasge.", ex);
                }
            }
        });

        dlg_thread.start();
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

    float lastprice;
    public List<FormatChart> GetPeriodChart(String stock_code, int period) {

        List<FormatChart> chartlist = new ArrayList<FormatChart>();
        float maxprice,minprice;


        float position;
        int test_period = period;
        float scalelevel=0;
        if(period <=60) {
            scalelevel = 0.05f;
            position = 0.95f;
        }
        else {
            scalelevel = 0.15f;
            position = 0.8f;
        }

        if(!myexcel.file_check(stock_code)) {
            return chartlist;
        }

        MyChart standard_chart = new MyChart();
        standard_chart.clearbuffer();
        chartlist = new ArrayList<FormatChart>();

        //Color[] colors = {getColor(view,R.color.Red), Color.GRAY, Color.GRAY, Color.BLUE,Color.GREEN,Color.CYAN,Color.BLUE};
        MyStat mystat = new MyStat();
        PriceBox kbbank = new PriceBox(stock_code);
        List<Float> kbband_close = kbbank.getClose(test_period);
        if(kbband_close.get(0)==0 || kbband_close.size() < test_period || kbbank.checkFile()==false) {
            //XYChart chart  = new XYChartBuilder().width(300).height(200).build();
            return chartlist;
        }
        BBandTest bbtest = new BBandTest(stock_code,kbband_close,test_period);
        RSITest rsitest = new RSITest(stock_code,kbband_close,test_period);
        List<Float> rsi_line = rsitest.test_line();
        maxprice = Collections.max(kbband_close);
        minprice = Collections.min(kbband_close);
        lastprice = kbband_close.get(kbband_close.size()-1);

        // Create Chart & add first data
        float linewidth=1.5f;
        int size = kbband_close.size();
        //List<Float> x = new ArrayList<>();
        //for(int i =0;i<size;i++) { x.add((float)i); }

        chartlist = standard_chart.adddata_float(kbband_close, "", context.getColor(R.color.Red));
        standard_chart.adddata_float(bbtest.getUpperLine(), "", context.getColor(R.color.LightGray));
        standard_chart.adddata_float(bbtest.getLowLine(), "", context.getColor(R.color.LightGray));
        List<Float> buyscore = bbtest.scaled_percentb();
        chartlist = standard_chart.adddata_float(buyscore, "", context.getColor(R.color.Yellow));


        Float diff_percent = 100*(lastprice-minprice)/(maxprice-minprice);
        g_period_level = String.format("%.0f",lastprice);
        g_period_level += " / " + String.format("%.1f",diff_percent);

        return chartlist;
    }

    public List<FormatChart> GetTodayChart(String stock_code, float input_target) {

        int hour = 60*4;
        float startprice;
        float nowprice;
        List<FormatChart> chartlist = new ArrayList<FormatChart>();

        MyExcel myexcel = new MyExcel();
        if(!myexcel.file_check(stock_code+"today")) {
            return chartlist;
        }

        List<String> dealprice = myexcel.readtodayprice(stock_code+"today","DEAL",-1,false);
        List<String> sellprice = myexcel.readtodayprice(stock_code+"today","SELL",-1,false);
        List<String> buyprice = myexcel.readtodayprice(stock_code+"today","BUY",-1,false);
        List<String> volume = myexcel.readtodayprice(stock_code+"today","VOLUME",-1,false);
        List<Float> kbband_deal = myexcel.string2float_fillpre(dealprice,1);
        List<Float> kbband_sell = myexcel.string2float_fillpre(sellprice,1);
        List<Float> kbband_buy = myexcel.string2float_fillpre(buyprice,1);
        List<Float> kbband_vol = myexcel.string2float_fillpre(volume,1);

        if(kbband_deal.size() == 0 || kbband_sell.size()==0 ||kbband_buy.size() == 0 ||  kbband_vol.size()==0) {
            return chartlist;
        }
        List<Float> targetlist = new ArrayList<>();
        startprice = kbband_deal.get(0);
        nowprice = kbband_deal.get(kbband_deal.size()-1);

        float target;
        if(input_target==1) target = kbband_buy.get(0);
        else target = input_target;
        int size = kbband_buy.size();

        for(int i =0;i<size;i++) {
            targetlist.add(target);
        }

        MyChart standard_chart = new MyChart();
        standard_chart.clearbuffer();
        chartlist = new ArrayList<FormatChart>();

        chartlist = standard_chart.adddata_float(kbband_sell, "", context.getColor(R.color.Red));
        standard_chart.adddata_float(kbband_buy, "", context.getColor(R.color.LightGray));

        float low_price = Collections.min(kbband_buy);
        MyStat mystat = new MyStat();
        List<Float> vol2 = mystat.scaling_float2(kbband_vol,low_price);
        standard_chart.adddata_float(vol2, "", context.getColor(R.color.SeaGreen));

        chartlist = standard_chart.adddata_float(targetlist, "", context.getColor(R.color.Yellow));

        Float diff_percent = 100*nowprice/lastprice-100;
        g_today_level = String.format("%.0f",nowprice);
        g_today_level += " / " + String.format("%.1f",diff_percent);


        return chartlist;
    }

    public void fill_chartdata() {
        int size = mystocklist.size();
        for(int i =0;i<size;i++) {
            String stock_code = mystocklist.get(i).stock_code;
            mystocklist.get(i).chartlist1 = new ArrayList<FormatChart>();
            mystocklist.get(i).chartlist2 = new ArrayList<FormatChart>();
            mystocklist.get(i).chartlist1 = GetPeriodChart(stock_code, 120);
            mystocklist.get(i).period_level = g_period_level;
            mystocklist.get(i).chartlist2 = GetTodayChart(stock_code, 1);
            mystocklist.get(i).today_level = g_today_level;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}