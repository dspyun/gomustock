package com.gomu.gomustock.ui.notifications;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.gomu.gomustock.FullPopup_Option;
import com.gomu.gomustock.FulllPopup;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.MyStat;
import com.gomu.gomustock.R;
import com.gomu.gomustock.databinding.FragmentNotificationsBinding;
import com.gomu.gomustock.graph.MyChart;
import com.gomu.gomustock.network.Investing;
import com.gomu.gomustock.network.MyWeb;
import com.gomu.gomustock.network.Paxnet;
import com.gomu.gomustock.network.YFDownload;
import com.gomu.gomustock.stockengin.PriceBox;
import com.gomu.gomustock.ui.format.FormatChart;
import com.gomu.gomustock.ui.format.FormatMyStock;
import com.gomu.gomustock.ui.format.FormatStockInfo;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    View view;
    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;
    TextView tvDownload, tvUpdate,tvSignal,tvDummy,tvNews;
    ImageView etf_dlicon,etf_news,etf_sync;
    LineChart kospi, snp500,nasdaq, dow, sox;
    MyExcel myexcel = new MyExcel();
    List<Float> kospi_index = new ArrayList<>();
    List<Float> nasdaq_index = new ArrayList<>();
    List<Float> snp500_index = new ArrayList<>();
    List<Float> dow_index = new ArrayList<>();
    private RecyclerView recyclerView;
    private NotiAdapter noti_adapter;
    Dialog dialog_progress; // 커스텀 다이얼로그
    String SHORT_NEWS="";
    List<FormatMyStock> sectorinfo;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        view = binding.getRoot();

        dialog_progress = new Dialog(getActivity());       // Dialog 초기화
        dialog_progress.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog_progress.setContentView(R.layout.dialog_progress);

        // 파일리스트를 spinner에 넣어준다
        String[] filelist = {"etf1","etf2"};
        Spinner folderspinner = view.findViewById(R.id.etf_spinner);
        ArrayAdapter fileAdapter = new ArrayAdapter<String>(view.getContext(), R.layout.spinner_list, filelist);
        fileAdapter.setDropDownViewResource(R.layout.spinner_list);
        folderspinner.setAdapter(fileAdapter); //어댑터에 연결해줍니다.

        initResource();
        dl_stocknews();
        chart_ko();
        chart_na();

        sectorinfo = myexcel.readSectorinfo(false);
        int size = sectorinfo.size();
        for(int i =0;i<size;i++) {
            PriceBox pricebox = new PriceBox(sectorinfo.get(i).stock_code);
            sectorinfo.get(i).chartdata = pricebox.getClose(120);
        }
        recyclerView = view.findViewById(R.id.noti_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        noti_adapter = new NotiAdapter(getActivity(), sectorinfo);
        binding.notiRecyclerView.setAdapter(noti_adapter);

        etf_dlicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<String> stocklist = new ArrayList<>();
                int size = sectorinfo.size();
                for(int i =0;i<size;i++) {
                    stocklist.add(sectorinfo.get(i).stock_code);
                }
                //dl_shortnews();
                dl_stocknews();
                YFDownload_Dialog(stocklist);
                //dl_yahoofinance_price();
            }
        });
        etf_news.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                full_popup_news(SHORT_NEWS);
            }
        });
        etf_sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment_refresh();
            }
        });

        tvNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                full_popup_news(SHORT_NEWS);
            }
        });

        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });
        return view;
    }

    public void fragment_refresh() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

    public void initResource() {
        etf_dlicon= view.findViewById(R.id.etf_dlicon);
        etf_news = view.findViewById(R.id.etf_news);
        etf_sync = view.findViewById(R.id.etf_sync);

        tvNews = view.findViewById(R.id.short_news);
    }


    public void YFDownload_Dialog(List<String> stock_list){

        dialog_progress.show(); // 다이얼로그 띄우기
        dialog_progress.setCanceledOnTouchOutside(false);
        dialog_progress.setCancelable(false);

        ProgressBar dlg_bar = dialog_progress.findViewById(R.id.dialog_progressBar);

        Thread dlg_thread = new Thread(new Runnable() {
            MyWeb myweb = new MyWeb();
            MyExcel myexcel = new MyExcel();
            List<FormatStockInfo> web_stockinfo = new ArrayList<FormatStockInfo>();


            public void run() {
                try {
                    int max = stock_list.size();
                    dlg_bar.setProgress(0);
                    for(int i=0;i<stock_list.size();i++) {
                        dlg_bar.setProgress(100*(i+1)/max);
                        // 1. 주가를 다운로드 하고
                        new YFDownload(stock_list.get(i));
                    }
                    notice_ok(0);
                    dialog_progress.dismiss();
                } catch (Exception ex) {
                    Log.e("MainActivity", "Exception in processing mesasge.", ex);
                }
            }
        });

        dlg_thread.start();
    }


    void dl_yahoofinance_price() {
        MyWeb myweb = new MyWeb();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int size = sectorinfo.size();
                for(int i =0;i<size;i++ ) {
                    new YFDownload(sectorinfo.get(i).stock_code);
                }
                notice_ok(1);
            }
        }).start();
    }

    void notice_ok(int i) {
        Log.d(TAG, "changeButtonText myLooper() " + Looper.myLooper());

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1L); // 잠시라도 정지해야 함
                    //Toast.makeText(context, "home fragment", Toast.LENGTH_SHORT).show();
                    if(i==2) tvNews.setText(SHORT_NEWS);
                    noti_adapter.refresh();
                } catch (Exception e) {
                    System.out.println("인터럽트로 인한 스레드 종료.");
                    return;
                }
            }
        });
    }

    public void dl_shortnews() {
        MyWeb myweb = new MyWeb();
        Paxnet paxnet = new Paxnet();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                SHORT_NEWS = myweb.getNaverNews();
                //SHORT_NEWS += paxnet.getNews();
                notice_ok(2);
            }
        }).start();
    }

    public void dl_stocknews() {
        MyWeb myweb = new MyWeb();
        Investing investing = new Investing();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                SHORT_NEWS = myweb.getNaverNews();
                SHORT_NEWS += investing.getNews();
                notice_ok(2);
            }
        }).start();
    }

    public void full_popup_news(String news) {
        FullPopup_Option popup_option = new FullPopup_Option(news,"news");

        Intent intent = new Intent(getActivity(), FulllPopup.class);
        intent.putExtra("class", popup_option);
        getActivity().startActivity(intent);
    }

    public void chart_ko() {
        LineChart chart2,chart1;
        MyStat mystat = new MyStat();
        MyChart korea_chart = new MyChart();
        List<FormatChart> chartlist = new ArrayList<FormatChart>();

        chart1 = view.findViewById(R.id.kos_chart);
        chart2 = view.findViewById(R.id.money_chart);
        List<String> kospi = new ArrayList<String>();
        List<Integer> chart_kospi = new ArrayList<Integer>();;
        List<String> kosdaq = new ArrayList<String>();
        List<Integer> chart_kosdaq = new ArrayList<Integer>();;

        kospi = myexcel.read_ohlcv("^KS11", "CLOSE", 120, false);
        chart_kospi = mystat.string2int(kospi,1);
        kosdaq = myexcel.read_ohlcv("^KQ11", "CLOSE", 120, false);
        chart_kosdaq = mystat.string2int(kosdaq,3);

        korea_chart.adddata_int(chart_kospi, "kospi", getActivity().getColor(R.color.White));
        chartlist = korea_chart.adddata_int(chart_kosdaq, "kosdaq", getActivity().getColor(R.color.Red));

        korea_chart.multi_chart(chart1, chartlist, "W:kospi, R:kosdaq", false);
    }
    public void chart_na() {
        LineChart chart2;
        MyStat mystat = new MyStat();
        MyChart na_chart = new MyChart();
        List<FormatChart> chartlist = new ArrayList<FormatChart>();

        chart2 = view.findViewById(R.id.na_chart);
        List<String> snp = new ArrayList<String>();
        List<Integer> chart_snp = new ArrayList<Integer>();;
        List<String> dow = new ArrayList<String>();
        List<Integer> chart_dow = new ArrayList<Integer>();;

        snp = myexcel.read_ohlcv("^GSPC", "CLOSE", 120, false);
        chart_snp = mystat.string2int(snp,8);
        dow = myexcel.read_ohlcv("^DJI", "CLOSE", 120, false);
        chart_dow = mystat.string2int(dow,1);

        na_chart.adddata_int(chart_snp, "s&p500", getActivity().getColor(R.color.White));
        chartlist = na_chart.adddata_int(chart_dow, "dow", getActivity().getColor(R.color.Red));

        na_chart.multi_chart(chart2, chartlist, "W:s&p500, R:dow", false);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}