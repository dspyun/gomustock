package com.gomu.gomustock.ui.notifications;

import static android.content.ContentValues.TAG;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.R;
import com.gomu.gomustock.databinding.FragmentNotificationsBinding;
import com.gomu.gomustock.graph.MyChart;
import com.gomu.gomustock.network.MyWeb;
import com.gomu.gomustock.network.YFDownload;
import com.gomu.gomustock.ui.format.FormatChart;
import com.gomu.gomustock.ui.format.FormatSector;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    View root;
    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;
    ImageView infochart11, infochart12, infochart21, infochart22;
    TextView tvDownload, tvUpdate,tvSignal,tvDummy;
    LineChart kospi, snp500,nasdaq, dow, sox;
    MyExcel myexcel = new MyExcel();
    List<Float> kospi_index = new ArrayList<>();
    List<Float> nasdaq_index = new ArrayList<>();
    List<Float> snp500_index = new ArrayList<>();
    List<Float> dow_index = new ArrayList<>();
    List<String> price_str = new ArrayList<>();
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        initResource();

        if(myexcel.file_check("^KS11.xls")) {
            show_chart();
        }


        FormatSector mysector = new FormatSector();
        mysector.init();
        List<FormatSector> sectorlist = new ArrayList<>();
        sectorlist.add(mysector);

        ListView sector_list = (ListView) root.findViewById(R.id.sector_listview);
        NotiSectorAdapter adapter = new NotiSectorAdapter(getActivity(),sectorlist);
        sector_list.setAdapter(adapter);

        sector_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getContext(),mid[position],Toast.LENGTH_SHORT).show();
            }
        });

        tvDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dl_yahoofinance_price();
            }
        });
        tvUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_chart();
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
    }

    void dl_yahoofinance_price() {
        MyWeb myweb = new MyWeb();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                new YFDownload("^KS11");
                new YFDownload("^GSPC");
                new YFDownload("^IXIC");
                new YFDownload("^DJI");
                new YFDownload("^SOX");
                notice_ok();
            }
        }).start();
    }

    void notice_ok() {
        Log.d(TAG, "changeButtonText myLooper() " + Looper.myLooper());

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1L); // 잠시라도 정지해야 함
                    //Toast.makeText(context, "home fragment", Toast.LENGTH_SHORT).show();
                    tvDownload.setTextColor(Color.YELLOW);
                } catch (Exception e) {
                    System.out.println("인터럽트로 인한 스레드 종료.");
                    return;
                }
            }
        });
    }

    public void show_chart() {
        List<Float> kospi_std = new ArrayList<>();
        List<Float> nasdaq_std = new ArrayList<>();
        List<Float> snp500_std = new ArrayList<>();
        List<Float> dow_std = new ArrayList<>();
        List<Float> sox_std = new ArrayList<>();

        int test_period = 120;
        List<Float> sox_index = new ArrayList<>();
        sox = (LineChart)root.findViewById(R.id.sox);
        List<String> price_str4 = myexcel.read_ohlcv("^SOX" , "CLOSE", test_period, false);
        sox_index = myexcel.string2float(price_str4,1);
        MyChart sox_chart = new MyChart();
        sox_chart.single_float(sox,sox_index,"필라델피아반도체", false);
        sox_std = myexcel.standardization_lib(sox_index);

        snp500 = (LineChart)root.findViewById(R.id.snp500);
        List<String> price_str1 = myexcel.read_ohlcv("^GSPC" , "CLOSE", test_period, false);
        snp500_index = myexcel.string2float(price_str1,1);
        MyChart snp500_chart = new MyChart();
        snp500_chart.single_float(snp500,snp500_index,"나스닥 IT 헬스", false);
        snp500_std = myexcel.standardization_lib(snp500_index);


        nasdaq = (LineChart)root.findViewById(R.id.nasdaq);
        List<String> price_str2 = myexcel.read_ohlcv("^IXIC" , "CLOSE", test_period, false);
        nasdaq_index = myexcel.string2float(price_str2,1);
        MyChart nasdaq_chart = new MyChart();
        nasdaq_chart.single_float(nasdaq,nasdaq_index,"SNP500 공업 금융", false);
        nasdaq_std = myexcel.standardization_lib(nasdaq_index);

        dow = (LineChart)root.findViewById(R.id.dow);
        List<String> price_str3 = myexcel.read_ohlcv("^DJI" , "CLOSE", test_period, false);
        dow_index = myexcel.string2float(price_str3,1);
        MyChart dow_chart = new MyChart();
        dow_chart.single_float(dow,dow_index,"다우 소재 산업재", false);
        dow_std = myexcel.standardization_lib(dow_index);


        kospi = (LineChart)root.findViewById(R.id.kospi);
        List<String> price_str = myexcel.read_ohlcv("^KS11" , "CLOSE", test_period, false);
        kospi_index = myexcel.string2float(price_str,1);
        MyChart kospi_chart = new MyChart();
        kospi_chart.single_float(kospi,kospi_index,"코스피", false);
        kospi_std = myexcel.standardization_lib(kospi_index);

        LineChart composit = (LineChart)root.findViewById(R.id.composit);
        int size = snp500_std.size();
        List<Float> comp = new ArrayList<>();
        for(int i =0;i<size;i++) {
            comp.add((nasdaq_std.get(i) + dow_std.get(i) + sox_std.get(i) + snp500_std.get(i))/4);
        }
        MyChart comp_chart = new MyChart();
        List<FormatChart> chartlist = new ArrayList<FormatChart>();
        comp_chart.adddata_float(comp,"미국4합성",context.getColor(R.color.White));
        chartlist = comp_chart.adddata_float(kospi_std,"코스피",context.getColor(R.color.Red));
        comp_chart.multi_chart(composit,chartlist,"코스피vs합성", false);
    }

    public void dl_SectroInfo() {
        MyWeb myweb = new MyWeb();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                myweb.getSectorInfo();
            }
        }).start();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}