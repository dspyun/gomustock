package com.gomu.gomustock.ui.dashboard;


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
import com.gomu.gomustock.stockengin.TAlib;
import com.gomu.gomustock.graph.MyChart;
import com.gomu.gomustock.ui.format.FormatChart;
import com.gomu.gomustock.ui.format.FormatStockInfo;

import java.util.ArrayList;
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

    TAlib mytalib = new TAlib();
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
            bb_chart = mytalib.bbands_test(stock_code, 60);
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
            chart_data = mytalib.rsi_test(stock_code, 60);

            fogn_chart.single_float(adxChart,chart_data,"RSI",false );

            MyChart stoch1_chart = new MyChart();

            List<FormatChart> chartlist1 = new ArrayList<FormatChart>();
            stoch_chart_list = mytalib.macd_test(stock_code, 60);
            stoch1_chart.buildChart_float(stoch_chart_list.get(0), "fast", Color.YELLOW);
            stoch1_chart.buildChart_float(stoch_chart_list.get(1), "slow", Color.WHITE);
            chartlist1 = stoch1_chart.buildChart_float(stoch_chart_list.get(2), "signal", Color.RED);
            bband_chart.setYMinmax(0, 0);
            stoch1_chart.multi_chart(stochChart, chartlist1, "MACD", false);

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

        webView.clearCache(true);
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

}

