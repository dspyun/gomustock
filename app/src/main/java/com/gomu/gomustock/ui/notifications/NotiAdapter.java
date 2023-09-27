package com.gomu.gomustock.ui.notifications;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.gomu.gomustock.R;
import com.gomu.gomustock.graph.MyChart;
import com.gomu.gomustock.ui.format.FormatMyStock;

import java.util.Collections;
import java.util.List;

public class NotiAdapter extends RecyclerView.Adapter<NotiAdapter.ViewHolder>{

    private String[] localDataSet;
    List<FormatMyStock> sectorinfo;
    Context context;
    static LineChart chart2,chart1;

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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            chart1 = view.findViewById(R.id.sector01);
            chart2 = view.findViewById(R.id.sector02);
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
            float last = onesector1.chartdata.get((onesector1.chartdata.size()-1));
            sector_info += " " + String.format("%.0f", last) + "/" + String.format("%.0f",100*(last-min)/(max-min));
            MyChart noti_chart1 = new MyChart();
            noti_chart1.single_float(chart1, onesector1.chartdata, sector_info, false);
        }

        if (onesector2.chartdata.size() > 0 && (index + 1) < maxlist) {
            String sector_info2 = onesector2.stock_name;
            float min = Collections.min(onesector2.chartdata);
            float max = Collections.max(onesector2.chartdata);
            float last = onesector2.chartdata.get((onesector2.chartdata.size()-1));
            sector_info2 += " " + String.format("%.0f",last) + "/" + String.format("%.0f",100*(last-min)/(max-min));
            MyChart noti_chart2 = new MyChart();
            noti_chart2.single_float(chart2, onesector2.chartdata, sector_info2, false);
        }
    }
    // Return the size of your dataset (invoked by the layout manager)

}
