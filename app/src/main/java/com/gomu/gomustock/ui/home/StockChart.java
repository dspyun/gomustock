package com.gomu.gomustock.ui.home;


import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import com.gomu.gomustock.MyStat;
import com.gomu.gomustock.R;
import com.gomu.gomustock.graph.MyChart;
import com.gomu.gomustock.stockengin.BBandTest;
import com.gomu.gomustock.stockengin.IchimokuTest;
import com.gomu.gomustock.stockengin.PriceBox;
import com.gomu.gomustock.stockengin.RSITest;
import com.gomu.gomustock.ui.format.FormatChart;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StockChart {

    MyStat mystat = new MyStat();

    public boolean isValidURL(String url) {

        try {
            new URL(url).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }

        return true;
    }

    /*
    public XYChart GetTodayChart(String stock_code, float input_target) {

        Color[] colors = {Color.RED, Color.BLUE,Color.BLACK, Color.GRAY, Color.LIGHT_GRAY,Color.BLUE};

        int hour = 60*4;
        float startprice;
        float nowprice;
        MyExcel myexcel = new MyExcel();
        List<String> dealprice = myexcel.readtodayprice(stock_code+"today","DEAL",-1,false);
        List<String> sellprice = myexcel.readtodayprice(stock_code+"today","SELL",-1,false);
        List<String> buyprice = myexcel.readtodayprice(stock_code+"today","BUY",-1,false);
        List<String> volume = myexcel.readtodayprice(stock_code+"today","VOLUME",-1,false);
        List<Float> kbband_deal = myexcel.string2float_fillpre(dealprice,1);
        List<Float> kbband_sell = myexcel.string2float_fillpre(sellprice,1);
        List<Float> kbband_buy = myexcel.string2float_fillpre(buyprice,1);
        List<Float> kbband_vol = myexcel.string2float_fillpre(volume,1);
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

        // Create Chart & add first data
        int xsize = kbband_deal.size();
        float linewidth=1.5f;
        XYChart chart  = new XYChartBuilder().width(300).height(200).build();
        XYSeries series_s = chart.addSeries("sell",kbband_sell);
        series_s.setLineWidth(linewidth);
        XYSeries series_b = chart.addSeries("buy",kbband_buy);
        series_b.setLineWidth(linewidth);
        float low_price = Collections.min(kbband_buy);
        MyStat mystat = new MyStat();
        List<Float> vol2 = mystat.scaling_float2(kbband_vol,low_price);
        XYSeries series_v = chart.addSeries("vol",vol2);
        series_v.setLineWidth(linewidth);

        XYSeries series_t = chart.addSeries("target",targetlist);
        series_t.setLineWidth(linewidth);
        chart.getStyler().setMarkerSize(0);
        chart.getStyler().setSeriesColors(colors);
        chart.getStyler().setLegendVisible(false);
        //chart.getStyler().setYAxisTicksVisible(true);

        Float diff_percent = 100*nowprice/startprice-100;
        String anntext = String.format("%.1f",diff_percent);
        anntext += "\n" + String.format("%.0f",nowprice);
        //AnnotationText maxText = new AnnotationText(anntext, series.getXMax(), nowprice*0.9, false);
        //chart.addAnnotation(maxText);
        chart.addAnnotation(
                new AnnotationTextPanel(anntext, xsize, startprice, false));
        chart.getStyler().setAnnotationTextPanelPadding(0);
        chart.getStyler().setAnnotationTextPanelFont(new Font("Verdana", Font.BOLD, 12));
        //chart.getStyler().setAnnotationTextPanelBackgroundColor(Color.RED);
        //chart.getStyler().setAnnotationTextPanelBorderColor(Color.BLUE);
        chart.getStyler().setAnnotationTextPanelFontColor(Color.BLACK);
        chart.getStyler().setAnnotationTextPanelBorderColor(Color.WHITE);

        return chart;
    }


     */

    public List<FormatChart> GetPeriodChart(String stock_code, int period) {

        float maxprice;
        float nowprice;

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

        List<FormatChart> chartlist = new ArrayList<FormatChart>();
        MyChart standard_chart = new MyChart();
        standard_chart.clearbuffer();
        chartlist = new ArrayList<FormatChart>();

        //Color[] colors = {Color.RED, Color.GRAY, Color.GRAY, Color.BLUE,Color.GREEN,Color.ORANGE,Color.BLUE};
        MyStat mystat = new MyStat();
        PriceBox kbbank = new PriceBox(stock_code);
        List<Float> kbband_close = kbbank.getClose(test_period);
        if(kbband_close.get(0)==0 || kbband_close.size() < test_period) {
            //XYChart chart  = new XYChartBuilder().width(300).height(200).build();
           // return chart;
        }
        BBandTest bbtest = new BBandTest(stock_code,kbband_close,test_period);
        RSITest rsitest = new RSITest(stock_code,kbband_close,test_period);
        List<Float> rsi_line = rsitest.test_line();
        maxprice = Collections.max(kbband_close);
        nowprice = kbband_close.get(kbband_close.size()-1);

        // Create Chart & add first data
        float linewidth=1.5f;
        int size = kbband_close.size();
        //List<Float> x = new ArrayList<>();
        //for(int i =0;i<size;i++) { x.add((float)i); }

        chartlist = standard_chart.adddata_float(kbband_close, stock_code, context.getColor(R.color.Red));
        standard_chart.adddata_float(bbtest.getUpperLine(), "upper_line", context.getColor(R.color.Red));
        standard_chart.adddata_float(bbtest.getLowLine(), "low_line", context.getColor(R.color.Red));
        List<Float> buyscore = bbtest.scaled_percentb();
        standard_chart.adddata_float(buyscore, "buysignal", context.getColor(R.color.Red));


        IchimokuTest ichi = new IchimokuTest(stock_code, kbband_close, test_period);
        List<Float> prospan1line = ichi.getProspan1();
        float extval = prospan1line.get(0);
        for(int j=0;j<26;j++) prospan1line.add(0,extval);
        standard_chart.adddata_float(mystat.leveling_float(prospan1line,scalelevel), "prospan1line", context.getColor(R.color.Blue));

        List<Float> prospan2line = ichi.getProspan2();
        extval = prospan2line.get(0);
        for(int j=0;j<26;j++) prospan2line.add(0,extval);
        chartlist = standard_chart.adddata_float(mystat.leveling_float(prospan2line,scalelevel), "prospan2line", context.getColor(R.color.Orange));

        /*
        Float diff_percent = 100*nowprice/maxprice;
        String anntext = String.format("%.1f",diff_percent);
        anntext += "\n" + String.format("%.0f",nowprice);
        //AnnotationText maxText = new AnnotationText(anntext, series.getXMax(), nowprice*0.9, false);
        //chart.addAnnotation(maxText);
        chart.addAnnotation(
                new AnnotationTextPanel(anntext, prospan2line.size(), nowprice*position, false));
        chart.getStyler().setAnnotationTextPanelPadding(0);
        chart.getStyler().setAnnotationTextPanelFont(new Font("Verdana", Font.BOLD, 12));
        //chart.getStyler().setAnnotationTextPanelBackgroundColor(Color.RED);
        //chart.getStyler().setAnnotationTextPanelBorderColor(Color.BLUE);
        chart.getStyler().setAnnotationTextPanelFontColor(Color.BLACK);
        chart.getStyler().setAnnotationTextPanelBorderColor(Color.WHITE);
        //chart.getStyler().setYAxisTicksVisible(true);
         */
        return chartlist;
    }

}
