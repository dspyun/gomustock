package com.gomu.gomustock.graph;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;
import static java.lang.Boolean.TRUE;

import android.graphics.Color;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.gomu.gomustock.R;
import com.gomu.gomustock.ui.format.FormatChart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyChart {
    private List<FormatChart> chartlist = new ArrayList<FormatChart>();
    private int Ymin, Ymax;
    private int Xmin, Xmax;
    public MyChart() {
        //mycontext = chart;
    }

    public List<FormatChart> adddata_int(List<Integer> data1, String name, int color) {

        FormatChart onechart = new FormatChart();
        onechart.intEntry(data1); // entry에 data를 넣는다.
        onechart.color = color;
        onechart.name = name;
        chartlist.add(onechart);

        return chartlist;
    }

    public List<FormatChart> adddata_float(List<Float> data1, String name, int color) {

        FormatChart onechart = new FormatChart();
        onechart.floatEntry(data1); // entry에 data를 넣는다.
        onechart.color = color;
        onechart.name = name;
        chartlist.add(onechart);

        return chartlist;
    }

    public void setYMinmax(int min, int max) {
        List<Integer> temp = new ArrayList<>();
        if(min==0) {
            int size = chartlist.size();
            for(int i = 0;i<size;i++) {
                temp.add((int) chartlist.get(i).min);
                temp.add((int) chartlist.get(i).max);
            }
            Ymin = Collections.min(temp);
            Ymax = Collections.max(temp);
        } else {
            Ymin = min;
            Ymax = max;
        }
    }

    public void clearbuffer() {
        chartlist.clear();
    }

    public void multi_chart(LineChart chart, List<FormatChart> chartlist, String description, boolean GRID_SHOW) {
        LineChart lineChart=chart;
        LineData lineData = new LineData();
        int size = chartlist.size();
        for(int j=0;j<size;j++) {
            LineDataSet linedate1 = setLinedata(chartlist.get(j).entries, chartlist.get(j).color, chartlist.get(j).name);
            lineData.addDataSet(linedate1);
        }
        // 라인을 차트와 연결시킨다
        lineChart.setData(lineData);
        //lineChart.setTouchEnabled(false);

        setxAaxis(lineChart, true, true);
        setyRAxis(lineChart, true, true);
        //setyLAxis(lineChart, true, chartlist.get(0).min, chartlist.get(0).max,true);
        //setyLAxis(lineChart, true, Ymin, Ymax,true);
        setyLAxis(lineChart, true, true);
        setlegend(lineChart, false); // 그래프를 좀 더 키우기 위해 legend는 off시킴 230813

        //lineChart.setBackgroundColor(Color.LTGRAY);
        setdescription(lineChart,true,description,  Color.YELLOW);

        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setDrawGridBackground(false);
        //lineChart.animateY(2000, Easing.EasingOption.EaseInCubic);
        lineChart.invalidate();
    }

    public void single_float(LineChart linechart, List<Float> chartdata, String description, boolean GRID_SHOW) {
        //LineChart lineChart = linechart;
        ArrayList<Entry> entrychart = new ArrayList<>();
        LineData linedata = new LineData();

        int size = chartdata.size();
        for(int i=0;i<size;i++ ) {
            entrychart.add(new Entry(i,chartdata.get(i)));
        }
        // 데이터가 담긴 Arraylist 를 BarDataSet 으로 변환한다.
        linedata.addDataSet(setLinedata(entrychart,Color.CYAN,description));
        /*
        LineDataSet lineDataSet = new LineDataSet(entrychart, "linedata");
        lineDataSet.setColor(Color.CYAN);
        lineDataSet.setValueTextColor(Color.WHITE);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);

        linedata.addDataSet(lineDataSet); // 해당 BarDataSet 을 적용될 차트에 들어갈 DataSet 에 넣는다.
        */
        linechart.setTouchEnabled(false);
        setdescription(linechart,true,description,  Color.YELLOW);

        linechart.setData(linedata); // 차트에 위의 DataSet 을 넣는다.
        setyRAxis(linechart,false,false);
        setxAaxis(linechart, true, false);
        //setyLAxis(barChart,false,false);
        YAxis yLAxis = linechart.getAxisLeft();
        //XAxis xBAxis = linechart.getXAxis();
        setlegend(linechart,false);
        yLAxis.setTextColor(Color.WHITE);
        //xBAxis.setTextColor(Color.YELLOW);
        linechart.invalidate(); // 차트 업데이트

    }

    public void single_int(LineChart linechart, List<Integer> chartdata, String description, boolean GRID_SHOW) {
        //LineChart lineChart = linechart;
        ArrayList<Entry> entrychart = new ArrayList<>();
        LineData linedata = new LineData();
        List<Integer> color = new ArrayList<>();

        // 1. entry에 chart data를 넣는다
        for(int i=0;i<chartdata.size();i++ ) {
            entrychart.add(new Entry(i,chartdata.get(i)));
        }
        LineDataSet lineDataSet = new LineDataSet(entrychart, "linedataset");

        for(int i=0;i<chartdata.size();i++) {
            if(chartdata.get(i)<0) color.add(ContextCompat.getColor(context,R.color.CharBlue));
            else color.add(ContextCompat.getColor(context,R.color.Salmon));
        }
        linedata.addDataSet(setLinedata(entrychart,Color.CYAN,description));
        lineDataSet.setColors(R.color.Red);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setValueTextColor(Color.WHITE);

        linedata.addDataSet(lineDataSet); // 해당 BarDataSet 을 적용될 차트에 들어갈 DataSet 에 넣는다.

        linechart.setTouchEnabled(false);
        linechart.setData(linedata); // 차트에 위의 DataSet 을 넣는다.
        setyRAxis(linechart,false,false);
        setxAaxis(linechart, false, false);
        //setyLAxis(barChart,false,false);
        YAxis yLAxis = linechart.getAxisLeft();
        setlegend(linechart,false);
        yLAxis.setTextColor(Color.WHITE);
        setdescription(linechart,false,description,Color.YELLOW);
        linechart.invalidate(); // 차트 업데이트
    }

    public void barchart_float(BarChart barchart, List<Float> chartdata, String description, boolean GRID_SHOW) {
        //BarChart barChart = barchart;
        ArrayList<BarEntry> entrychart = new ArrayList<>();
        BarData bardata = new BarData();
        List<Integer> color = new ArrayList<>();

        int size = chartdata.size();
        // 1. entry에 chart data를 넣는다
        for(int i=0;i<size;i++ ) {
            entrychart.add(new BarEntry(i,chartdata.get(i)));
        }
        BarDataSet barDataSet = new BarDataSet(entrychart, "bardataset");

        for(int i=0;i<size;i++) {
            if(chartdata.get(i)<0) color.add(ContextCompat.getColor(context, R.color.CharBlue));
            else color.add(ContextCompat.getColor(context,R.color.Salmon));
        }
        barDataSet.setColors(color);

        barDataSet.setValueTextColor(Color.WHITE);
        bardata.addDataSet(barDataSet); // 해당 BarDataSet 을 적용될 차트에 들어갈 DataSet 에 넣는다.

        barchart.setData(bardata); // 차트에 위의 DataSet 을 넣는다.
        setyRAxis(barchart,false,false);
        setxAaxis(barchart, false, false);
        //setyLAxis(barchart,false,false);
        YAxis yLAxis = barchart.getAxisLeft();
        setlegend(barchart,false);
        yLAxis.setTextColor(Color.WHITE);
        setdescription(barchart,false,description,Color.YELLOW);
        barchart.invalidate(); // 차트 업데이트
    }


    public void setlegend(LineChart lineChart, Boolean flag) {
        Legend l = lineChart.getLegend();
        l.setEnabled(flag);
        l.setTextColor(Color.YELLOW);
        l.setTextSize(12);
        l.setForm(Legend.LegendForm.LINE);
        l.setFormSize(15);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
    }

    public void setlegend(BarChart barChart, Boolean flag) {
        Legend l = barChart.getLegend();
        l.setEnabled(flag);
        l.setTextColor(Color.YELLOW);
        l.setTextSize(12);
        l.setForm(Legend.LegendForm.LINE);
        l.setFormSize(15);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
    }
    public void setdescription(LineChart lineChart, Boolean flag, String text, int color) {
        Description description = lineChart.getDescription();
        description.setEnabled(flag);
        description.setTextColor(color);
        description.setText(text);
        description.setTextSize(14);
    }
    public void setdescription(BarChart barChart, Boolean flag, String text, int color) {
        Description description = barChart.getDescription();
        description.setEnabled(flag);
        description.setTextColor(color);
        description.setText(text);
        description.setTextSize(14);
    }
    public void setxAaxis(LineChart lineChart, Boolean flag, Boolean GRID_SHOW) {
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setEnabled(flag);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        if(GRID_SHOW == TRUE ) {
            xAxis.enableGridDashedLine(8, 24, 0);
        }
    }
    public void setxAaxis(BarChart barChart, Boolean flag, Boolean GRID_SHOW) {
        XAxis xAxis = barChart.getXAxis();
        xAxis.setEnabled(flag);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        if(GRID_SHOW == TRUE ) {
            xAxis.enableGridDashedLine(8, 24, 0);
        }
    }
    public void setyLAxis(LineChart lineChart, Boolean flag, float min, float max, Boolean GRID_SHOW) {
        YAxis yLAxis = lineChart.getAxisLeft();
        yLAxis.setAxisMaximum(max);
        yLAxis.setAxisMinimum(min);
        yLAxis.setEnabled(flag);
        yLAxis.setTextColor(Color.WHITE);
        yLAxis.setDrawLabels(true);
        if(GRID_SHOW == TRUE ) {
            yLAxis.setDrawAxisLine(false);
            yLAxis.setDrawGridLines(false);
        }
    }
    public void setyLAxis(LineChart lineChart, Boolean flag, Boolean GRID_SHOW) {
        YAxis yLAxis = lineChart.getAxisLeft();
        yLAxis.setEnabled(flag);
        yLAxis.setTextColor(Color.WHITE);
        yLAxis.setDrawLabels(true);
        if(GRID_SHOW == TRUE ) {
            yLAxis.setDrawAxisLine(false);
            yLAxis.setDrawGridLines(false);
        }
    }
    public void setyLAxis(BarChart barChart, Boolean flag, float min, float max, Boolean GRID_SHOW) {
        YAxis yLAxis = barChart.getAxisLeft();
        yLAxis.setAxisMaximum(max);
        yLAxis.setAxisMinimum(min);
        yLAxis.setEnabled(flag);
        yLAxis.setTextColor(Color.WHITE);
        yLAxis.setDrawLabels(true);
        if(GRID_SHOW == TRUE ) {
            yLAxis.setDrawAxisLine(false);
            yLAxis.setDrawGridLines(false);
        }
    }
    public void setyRAxis(LineChart lineChart, Boolean flag, Boolean GRID_SHOW) {
        YAxis yRAxis = lineChart.getAxisRight();
        yRAxis.setEnabled(flag);
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);
    }
    public void setyRAxis(BarChart barChart, Boolean flag, Boolean GRID_SHOW) {
        YAxis yRAxis = barChart.getAxisRight();
        yRAxis.setEnabled(flag);
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);
    }
    public LineDataSet setLinedata(List<Entry> entries, int color, String name) {
        LineDataSet lineDataSet = new LineDataSet(entries, name);
        //lineDataSet.setValueTextColor(context.getColor(R.color.White));
        lineDataSet.setValueTextColor(color);
        lineDataSet.setColor(color);
        lineDataSet.setCircleRadius(6);
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);

        return lineDataSet;
    }
}
