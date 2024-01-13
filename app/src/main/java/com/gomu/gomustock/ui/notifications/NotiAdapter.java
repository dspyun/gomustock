package com.gomu.gomustock.ui.notifications;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.gomu.gomustock.FullPopup_Option;
import com.gomu.gomustock.FulllPopup;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.R;
import com.gomu.gomustock.graph.MyChart;
import com.gomu.gomustock.network.MyWeb;
import com.gomu.gomustock.ui.format.FormatMyStock;
import com.gomu.gomustock.ui.format.FormatStockInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotiAdapter extends RecyclerView.Adapter<NotiAdapter.ViewHolder>{

    private String[] localDataSet;
    List<FormatMyStock> sectorinfo;
    private static Activity context;
    private LineChart chart2,chart1;
    String get_stock_code;

    getAssetThread getassetthread;

    public NotiAdapter(Activity context, List<FormatMyStock> input_mystocklist) {
        this.context = context;
        this.sectorinfo = input_mystocklist;
    }

    public void refresh() {
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return sectorinfo.size()/2;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(View view) {
            super(view);

            // if(getAdapterPosition() < 0) return;
            // Define click listener for the ViewHolder's View
            chart1 = view.findViewById(R.id.sector01);
            chart2 = view.findViewById(R.id.sector02);

            chart1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FormatMyStock onesector1 = new FormatMyStock();
                    int position = getAdapterPosition();
                    int index = position*2;
                    onesector1 = sectorinfo.get(index);

                    get_stock_code = onesector1.stock_code;
                    //dlSectorAsset(onesector1.stock_code);
                    getassetthread = new getAssetThread();
                    getassetthread.start();
                }
            });

            chart2.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    FormatMyStock onesector1 = new FormatMyStock();
                    int position = getAdapterPosition();
                    int index = position*2;
                    onesector1 = sectorinfo.get(index+1);
                    get_stock_code = onesector1.stock_code;
                    //dlSectorAsset(onesector1.stock_code);
                    getassetthread = new getAssetThread();
                    getassetthread.start();
                }
            });

        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NotiAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.noti_list_row, viewGroup, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FormatMyStock onesector1 = new FormatMyStock();
        FormatMyStock onesector2 = new FormatMyStock();
        int index = position * 2;
        int maxlist = sectorinfo.size();
        onesector1 = sectorinfo.get(index);
        if ((index + 1) < maxlist) onesector2 = sectorinfo.get(index + 1);

        if (onesector1.chartdata.size() > 0) {
            String sector_info = onesector1.stock_name;
            float min = Collections.min(onesector1.chartdata);
            float max = Collections.max(onesector1.chartdata);
            float last = onesector1.chartdata.get((onesector1.chartdata.size() - 1));
            sector_info += " " + String.format("%.0f", last) + "/" + String.format("%.0f", 100 * (last - min) / (max - min));
            MyChart noti_chart1 = new MyChart();
            noti_chart1.single_float(chart1, onesector1.chartdata, sector_info, false);
            chart1.setTouchEnabled(true);
        }

        if (onesector2.chartdata.size() > 0 && (index + 1) < maxlist) {
            String sector_info2 = onesector2.stock_name;
            float min = Collections.min(onesector2.chartdata);
            float max = Collections.max(onesector2.chartdata);
            float last = onesector2.chartdata.get((onesector2.chartdata.size() - 1));
            sector_info2 += " " + String.format("%.0f", last) + "/" + String.format("%.0f", 100 * (last - min) / (max - min));
            MyChart noti_chart2 = new MyChart();
            noti_chart2.single_float(chart2, onesector2.chartdata, sector_info2, false);
            chart2.setTouchEnabled(true);
        }
    }
    // Return the size of your dataset (invoked by the layout manager)


    class getAssetThread extends Thread {
        public void run() {

            try {
                Thread.sleep(100L); // 진입 시 잠시라도 정지해야 함
                // 60초*60분마다 한 번씩 웹크롤링으로 현재가 update
                MyWeb myweb = new MyWeb();
                MyExcel myexcel = new MyExcel();
                String filename="";
                filename = myweb.getNaverETFAsset(get_stock_code);
                List<FormatStockInfo> etflist = new ArrayList<>();
                etflist = myexcel.readStockinfoCustomxls(filename+".xls");
                int size = etflist.size();
                String stocklist="";
                for(int i =0;i<size;i++) {
                    stocklist += etflist.get(i).stock_name + "\r\n";
                }
                full_popup(stocklist);
            } catch (InterruptedException e) {
                System.out.println("인터럽트로 인한 스레드 종료.");
                return;
            }
        }
    }
    public void full_popup(String stocklist) {
        //Log.d(TAG, "changeButtonText myLooper() " + Looper.myLooper());

        FullPopup_Option popup_option = new FullPopup_Option(stocklist);

        Intent intent = new Intent(context, FulllPopup.class);
        intent.putExtra("class",popup_option);
        context.startActivity(intent);
    }
}
