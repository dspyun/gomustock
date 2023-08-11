package com.gomu.gomustock.ui.notifications;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.gomu.gomustock.databinding.FragmentNotificationsBinding;
import com.gomu.gomustock.network.MyWeb;
import com.gomu.gomustock.network.YFDownload;
import com.gomu.gomustock.stockengin.PriceBox;
import com.gomu.gomustock.ui.format.FormatMyStock;
import com.gomu.gomustock.ui.format.FormatStockInfo;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    View root;
    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;
    TextView tvDownload, tvUpdate,tvSignal,tvDummy,tvNews;
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
        root = binding.getRoot();

        dialog_progress = new Dialog(getActivity());       // Dialog 초기화
        dialog_progress.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog_progress.setContentView(R.layout.dialog_progress);

        initResource();
        dl_shortnews();


        sectorinfo = myexcel.readSectorinfo(false);
        int size = sectorinfo.size();
        for(int i =0;i<size;i++) {
            PriceBox pricebox = new PriceBox(sectorinfo.get(i).stock_code);
            sectorinfo.get(i).chartdata = pricebox.getClose(120);
        }
        recyclerView = root.findViewById(R.id.noti_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        noti_adapter = new NotiAdapter(getActivity(), sectorinfo);
        binding.notiRecyclerView.setAdapter(noti_adapter);

        tvDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<String> stocklist = new ArrayList<>();
                int size = sectorinfo.size();
                for(int i =0;i<size;i++) {
                    stocklist.add(sectorinfo.get(i).stock_code);
                }
                YFDownload_Dialog(stocklist);
                //dl_yahoofinance_price();
            }
        });
        tvUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dl_shortnews();
            }
        });


        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });
        return root;
    }
    public void initResource() {
        tvDownload= root.findViewById(R.id.tv_noti_dl);
        tvUpdate = root.findViewById(R.id.tv_noti_update);
        tvSignal = root.findViewById(R.id.tv_noti_signal);
        tvDummy = root.findViewById(R.id.tv_noti_dummy);
        tvNews = root.findViewById(R.id.short_news);
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
                    if(i==1) tvDownload.setTextColor(Color.YELLOW);
                    else if(i==2) tvNews.setText(SHORT_NEWS);
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                SHORT_NEWS = myweb.getNaverNews();
                notice_ok(2);
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}