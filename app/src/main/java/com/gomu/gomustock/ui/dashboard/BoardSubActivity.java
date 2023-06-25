package com.gomu.gomustock.ui.dashboard;


import static com.tictactec.ta.lib.MAType.Sma;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.R;
import com.gomu.gomustock.graph.MyChart;
import com.gomu.gomustock.ui.format.FormatChart;
import com.gomu.gomustock.ui.format.FormatStockInfo;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoardSubActivity extends AppCompatActivity {
    BoardSubOption suboption;
    FormatStockInfo basic_info = new FormatStockInfo();
    private List<Float> chart1_data1 = new ArrayList<Float>();
    private List<Float> chart1_data2 = new ArrayList<Float>();
    private List<List<Float>> bb_chart = new ArrayList<List<Float>>();
    private List<List<Float>> stoch_chart_list = new ArrayList<List<Float>>();
    TextView mytext;
    ImageView myimage;
    LineChart bbnandChart, adxChart, stochChart;
    BarChart fognChart, agencyChart;

    TextView temp;
    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.board_sub_activity);

        Intent intent = getIntent(); /*데이터 수신*/
        suboption = (BoardSubOption) intent.getSerializableExtra("class"); /*클래스*/

        if (suboption.layout.equals("popup")) {
            setContentView(R.layout.board_sub_popup);

            webView = (WebView) findViewById(R.id.category_web_view);
            show_popup_treemap(webView, suboption.getRegion());
            // 이래는 트리맵차트 시험용...계속 개발 중
            //MyTreeMap mytree = new MyTreeMap(context);
            //mytree.treemap(webView, suboption.getRegion());;

        } else {
            setContentView(R.layout.board_sub_info);

            fognChart = findViewById(R.id.fogn_chart);
            agencyChart = findViewById(R.id.agency_chart);
            bbnandChart = findViewById(R.id.bband_chart);
            stochChart = findViewById(R.id.stoch1_chart);
            adxChart = findViewById(R.id.sub_chart);
            mytext = findViewById(R.id.textView1);

            String stock_name = suboption.getStockname();
            String stock_code = suboption.getStockcode();
            String stock_info = suboption.getStockinfo();

            fogn_chart(stock_code);
            agency_chart(stock_code);

            show_information(stock_name, stock_code, stock_info);

            MyExcel myexcel = new MyExcel();
            MyChart bband_chart = new MyChart();

            List<FormatChart> chartlist = new ArrayList<FormatChart>();
            List<String> temp = new ArrayList<>();
            temp = myexcel.oa_readItem(stock_code + ".xls","CLOSE", false);
            temp = myexcel.arrangeRev_string(temp);
            chart1_data1 = myexcel.string2float(temp, 1);
            bband_chart.buildChart_float(chart1_data1, stock_code, Color.RED);
            bb_chart = bbands_test(stock_code, 60);
            bband_chart.buildChart_float(bb_chart.get(0), "upper", Color.GRAY);
            bband_chart.buildChart_float(bb_chart.get(1), "middle", Color.LTGRAY);
            bband_chart.buildChart_float(bb_chart.get(2), "lower", Color.GRAY);
            chartlist = bband_chart.buildChart_float(bb_chart.get(3), "test", Color.BLUE);
            bband_chart.setYMinmax(0, 0);
            bband_chart.multi_chart(bbnandChart, chartlist, "볼린저밴드", false);
            //lineChart.invalidate();

            //adx_chart(stock_code, 60);
            MyChart fogn_chart = new MyChart();
            List<Float> chart_data = new ArrayList<>();
            chart_data = adx_test(stock_code, 60);

            fogn_chart.single_float(adxChart,chart_data,"ADX",false );

            MyChart stoch1_chart = new MyChart();

            List<Float> outk = new ArrayList<>();
            List<Float> outd = new ArrayList<>();
            for(int i = 0;i<60;i++) {
                outk.add(0f);
                outd.add(0f);
            }
            List<FormatChart> chartlist1 = new ArrayList<FormatChart>();
            stoch_chart_list = stoch_test(stock_code, 60,outk,outd);
            stoch1_chart.buildChart_float(stoch_chart_list.get(0), "slow-K", Color.LTGRAY);
            chartlist1 = stoch1_chart.buildChart_float(stoch_chart_list.get(1), "slow-D", Color.GRAY);
            bband_chart.setYMinmax(0, 0);
            stoch1_chart.multi_chart(stochChart, chartlist1, "스토케스틱", false);

            mytext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bbnandChart.invalidate();
                }
            });
        }

    }

    public String getinformation(String stock_code, String stock_name, String stockinfo) {
        System.out.println("stock_code =" + stock_code + "stock name = " + stock_name);
        String stock_info = "";
        stock_info = stock_code + " " + stock_name + "\n";
        stock_info += stockinfo;

        return stock_info;
    }

    public void show_information(String stockname, String stockcode, String stockinfo) {

        mytext.setTextSize(14);
        mytext.setText(getinformation(stockname, stockcode, stockinfo));

    }


    public void show_popup_treemap(WebView webView, String region) {

        webView.setWebViewClient(new WebViewClient());  // 새 창 띄우기 않기
        webView.setWebChromeClient(new WebChromeClient());
        //webView.setDownloadListener(new DownloadListener(){...});  // 파일 다운로드 설정

        webView.getSettings().setLoadWithOverviewMode(true);  // WebView 화면크기에 맞추도록 설정 - setUseWideViewPort 와 같이 써야함
        webView.getSettings().setUseWideViewPort(true);  // wide viewport 설정 - setLoadWithOverviewMode 와 같이 써야함

        webView.getSettings().setSupportZoom(false);  // 줌 설정 여부
        webView.getSettings().setBuiltInZoomControls(false);  // 줌 확대/축소 버튼 여부

        webView.getSettings().setJavaScriptEnabled(true); // 자바스크립트 사용여부
//        webview.addJavascriptInterface(new AndroidBridge(), "android");
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true); // javascript가 window.open()을 사용할 수 있도록 설정
        webView.getSettings().setSupportMultipleWindows(true); // 멀티 윈도우 사용 여부

        webView.getSettings().setDomStorageEnabled(true);  // 로컬 스토리지 (localStorage) 사용여부

        if(region.equals("domestic"))  webView.loadUrl("https://m.invest.zum.com/domestic?mekko=1");
        else if(region.equals("oversea")) webView.loadUrl("https://m.invest.zum.com/global?mekko=1");
    }


    public List<List<Float>> bbands_test(String stock_code, int total_period) {

        List<List<Float>> threechart = new ArrayList<List<Float>>();

        // The total number of periods to generate data for.
        final int TOTAL_PERIODS = total_period;

        // The number of periods to average together.
        final int PERIODS_AVERAGE = 5;

        double[] closePrice = new double[TOTAL_PERIODS];
        double[] outRealUpperBand = new double[TOTAL_PERIODS];
        double[] outRealMiddleBand = new double[TOTAL_PERIODS];
        double[] outRealLowerBand = new double[TOTAL_PERIODS];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double optInNbDevUp = 2; // 상한선 = 표준편차*2
        double optInNbDevDn = 2; // 하한선 = 표준편차*2
        MAType optInMAType = Sma; // 단순이동평균

        MyExcel myexcel = new MyExcel();
        List<String> close_str = new ArrayList<>();
        close_str = myexcel.oa_readItem(stock_code+".xls","CLOSE",false);
        close_str = myexcel.arrangeRev_string(close_str);
        List<Double> closedata = myexcel.string2double(close_str,1);
        for (int i =  0; i < closePrice.length; i++) {
            closePrice[i] = (double) closedata.get(i);
        }

        Core c = new Core();
        //RetCode retCode = c.sma(0, closePrice.length - 1, closePrice, PERIODS_AVERAGE, begin, length, out);
        RetCode retCode = c.bbands(0, closePrice.length - 1, closePrice, PERIODS_AVERAGE,
                optInNbDevUp, optInNbDevDn, optInMAType,
                begin, length, outRealUpperBand, outRealMiddleBand, outRealLowerBand);

        if (retCode == RetCode.Success) {
            System.out.println("Output Start Period: " + begin.value);
            System.out.println("Output End Period: " + (begin.value + length.value - 1));
            int start = begin.value;
            int end = (begin.value + length.value);
            // 결과를 float 리스트로 패킹해서 전달한다
            int length2 = outRealUpperBand.length;
            // 시작이 4부터임. 그래서 0~3까지 초기값으로 채워넣음
            List<Float> value = new ArrayList<Float>();
            for(int i = 0;i<start;i++) {
                value.add((float)outRealUpperBand[0]);
            }
            for(int i = 0;i < end-start;i++ ) {
                value.add((float)outRealUpperBand[i]);
            }
            threechart.add(value);

            List<Float> value1 = new ArrayList<Float>();
            for(int i = 0;i<start;i++) {
                value1.add((float)outRealMiddleBand[0]);
            }
            for(int i = 0; i <end-start;i++ ) {
                value1.add((float)outRealMiddleBand[i]);
            }
            threechart.add(value1);

            List<Float> value2 = new ArrayList<Float>();
            for(int i = 0;i<start;i++) {
                value2.add((float)outRealLowerBand[0]);
            }
            for(int i = 0; i <end-start;i++ ) {
                value2.add((float)outRealLowerBand[i]);
            }
            threechart.add(value2);

            double pricemax = Collections.max(closedata);
            List<Float> value3 = new ArrayList<Float>();
            for(int i = 0;i<start;i++) {
                value3.add((float)pricemax);
            }
            for(int i = 0; i <end-start;i++ ) {
                double temp1 = closePrice[start+i]-outRealMiddleBand[i];
                double temp2 = closePrice[start+i]-outRealLowerBand[i];
                double temp3 = 0.2*(outRealMiddleBand[i]-outRealLowerBand[i]);
                if((temp1 <= 0) && (temp2 <= temp3)) {
                    value3.add((float)(pricemax+pricemax*0.02));
                }
                else {
                    value3.add((float)pricemax);
                }
            }
            threechart.add(value3);
        }
        else {
            System.out.println("Error");
        }

        return threechart;
    }

    public List<Float> adx_test(String stock_code, int total_period) {


        // The total number of periods to generate data for.
        final int TOTAL_PERIODS = total_period;

        double[] closePrice = new double[TOTAL_PERIODS];
        double[] highPrice = new double[TOTAL_PERIODS];
        double[] lowPrice = new double[TOTAL_PERIODS];
        double[] outReal = new double[TOTAL_PERIODS];
        MInteger outBegIdx = new MInteger();;
        MInteger outNBElement = new MInteger();
        MInteger begin = new MInteger();;
        MInteger length = new MInteger();
        int optInTimePeriod = 14;

        List<Float> value = new ArrayList<Float>();

        MyExcel myexcel = new MyExcel();
        List<String> close_str = new ArrayList<>();
        close_str = myexcel.oa_readItem(stock_code+".xls","CLOSE",false);
        close_str = myexcel.arrangeRev_string(close_str);
        List<Double> closedata = myexcel.string2double(close_str,1);
        for (int i =  0; i < closePrice.length; i++) {
            closePrice[i] = (double) closedata.get(i);
        }
        List<String> high_str = new ArrayList<>();
        high_str = myexcel.oa_readItem(stock_code+".xls","HIGH",false);
        high_str = myexcel.arrangeRev_string(high_str);
        List<Double> highdata = myexcel.string2double(high_str,1);
        for (int i =  0; i < highPrice.length; i++) {
            highPrice[i] = (double) highdata.get(i);
        }
        List<String> low_str = new ArrayList<>();
        low_str = myexcel.oa_readItem(stock_code+".xls","LOW",false);
        low_str = myexcel.arrangeRev_string(low_str);
        List<Double> lowdata = myexcel.string2double(low_str,1);
        for (int i =  0; i < lowPrice.length; i++) {
            lowPrice[i] = (double) lowdata.get(i);
        }

        Core c = new Core();
        //RetCode retCode = c.sma(0, closePrice.length - 1, closePrice, PERIODS_AVERAGE, begin, length, out);
        RetCode retCode = c.adx( 0, closePrice.length -1,  highPrice, lowPrice, closePrice,
           optInTimePeriod, begin, length,  outReal);

        if (retCode == RetCode.Success) {
            System.out.println("Output Start Period: " + begin.value);
            System.out.println("Output Period length : " +length.value);
            int start = begin.value;
            int end = (begin.value + length.value);
            // 결과를 float 리스트로 패킹해서 전달한다
            int length2 = outReal.length;
            // 시작이 4부터임. 그래서 0~3까지 초기값으로 채워넣음

            for(int i = 0;i<start;i++) {
                value.add((float)outReal[0]);
            }
            for(int i = 0;i < end-start;i++ ) {
                value.add((float)outReal[i]);
            }

        }
        else {
            System.out.println("Error");
        }

        return value;
    }


    public List<List<Float>> stoch_test(String stock_code, int total_period, List<Float> outSlowK_list, List<Float> outSlowD_list ) {

        // The total number of periods to generate data for.
        int TOTAL_PERIODS = total_period;

        double[] closePrice = new double[TOTAL_PERIODS];
        double[] highPrice = new double[TOTAL_PERIODS];
        double[] lowPrice = new double[TOTAL_PERIODS];
        double[] outSlowK = new double[TOTAL_PERIODS];
        double[] outSlowD = new double[TOTAL_PERIODS];
        MInteger outBegIdx = new MInteger();;
        MInteger outNBElement = new MInteger();
        MInteger begin = new MInteger();;
        MInteger length = new MInteger();
        int optInTimePeriod = 14;
        MAType optInMAType = Sma; // 단순이동평균
        int optInFastK_Period = 5;
        int optInSlowK_Period = 3;
        int optInSlowD_Period = 3;
        MAType optInSlowK_MAType = Sma;
        MAType optInSlowD_MAType = Sma;

        MyExcel myexcel = new MyExcel();
        List<String> close_str = new ArrayList<>();
        close_str = myexcel.oa_readItem(stock_code+".xls","CLOSE",false);
        close_str = myexcel.arrangeRev_string(close_str);
        List<Double> closedata = myexcel.string2double(close_str,1);
        for (int i =  0; i < closePrice.length; i++) {
            closePrice[i] = (double) closedata.get(i);
        }
        List<String> high_str = new ArrayList<>();
        high_str = myexcel.oa_readItem(stock_code+".xls","HIGH",false);
        high_str = myexcel.arrangeRev_string(high_str);
        List<Double> highdata = myexcel.string2double(high_str,1);
        for (int i =  0; i < highPrice.length; i++) {
            highPrice[i] = (double) highdata.get(i);
        }
        List<String> low_str = new ArrayList<>();
        low_str = myexcel.oa_readItem(stock_code+".xls","LOW",false);
        low_str = myexcel.arrangeRev_string(low_str);
        List<Double> lowdata = myexcel.string2double(low_str,1);
        for (int i =  0; i < lowPrice.length; i++) {
            lowPrice[i] = (double) lowdata.get(i);
        }

        // outNBElement : 생성된 자료의 배술 수. = output 길이
        // 결과가 배열로 반환되기 때문에 길이정보도 같이 반환되어야 함
        Core c = new Core();
        RetCode retCode  = c.stoch( 0, closePrice.length -1, highPrice, lowPrice, closePrice,
        optInFastK_Period, optInSlowK_Period, optInSlowK_MAType, optInSlowD_Period, optInSlowD_MAType,
        outBegIdx, outNBElement, outSlowK, outSlowD );

        List<List<Float>> threechart = new ArrayList<List<Float>>();

        outSlowK_list = new ArrayList<Float>();
        int start = outBegIdx.value;
        int end = (outBegIdx.value + outNBElement.value);

        for(int i = 0;i<start;i++) {
            outSlowK_list.add((float)outSlowK[0]);
        }
        for(int i = 0;i < end-start;i++ ) {
            outSlowK_list.add((float)outSlowK[i]);
        }
        threechart.add(outSlowK_list);

        outSlowD_list = new ArrayList<Float>();
        start = outBegIdx.value;
        end = (outBegIdx.value + outNBElement.value);

        for(int i = 0;i<start;i++) {
            outSlowD_list.add((float)outSlowD[0]);
        }
        for(int i = 0;i < end-start;i++ ) {
            outSlowD_list.add((float)outSlowD[i]);
        }
        threechart.add(outSlowD_list);

        return threechart;
    }

    public void fogn_chart(String stock_code) {
        MyChart fogn_chart = new MyChart();
        List<Float> chart_data = new ArrayList<>();
        MyExcel myexcel = new MyExcel();
        // 파일을 읽어서 int data로 변환
        List<String> temp = myexcel.readFogninfo(stock_code,"FOGN", false);
        chart_data = myexcel.string2float(temp, 1000);

        fogn_chart.barchart_float(fognChart,chart_data,"외국인",false );
    }

    public void agency_chart(String stock_code) {
        MyChart fogn_chart = new MyChart();
        List<Float> chart_data = new ArrayList<>();
        MyExcel myexcel = new MyExcel();
        // 파일을 읽어서 int data로 변환
        List<String> temp = myexcel.readFogninfo(stock_code,"AGENCY", false);
        chart_data = myexcel.string2float(temp, 1000);

        fogn_chart.barchart_float(agencyChart,chart_data,"기관",false );
    }

    public void adx_chart(String stock_code, int total_period) {
        MyChart fogn_chart = new MyChart();
        List<Float> chart_data = new ArrayList<>();
        chart_data = adx_test(stock_code, total_period);

        fogn_chart.single_float(adxChart,chart_data,"ADX",false );
    }


}