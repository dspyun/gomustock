package com.gomu.gomustock.ui.dashboard;


import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.MyStat;
import com.gomu.gomustock.R;
import com.gomu.gomustock.graph.MyChart;
import com.gomu.gomustock.stockengin.PriceBox;
import com.gomu.gomustock.stockengin.TAlib;
import com.gomu.gomustock.ui.format.FormatChart;
import com.gomu.gomustock.ui.format.FormatStockInfo;

import java.util.ArrayList;
import java.util.List;

public class BoardSubActivity extends AppCompatActivity {
    BoardSubOption suboption;
    FormatStockInfo basic_info = new FormatStockInfo();

    TextView mytext;
    ImageView pergragh;
    LineChart bbnandChart, adxChart, stochChart, rsiChart, macdChart;
    BarChart fognChart, agencyChart;

    TextView temp;
    WebView webView;

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
            mytext = findViewById(R.id.textView1);
            pergragh = findViewById(R.id.per_graph);

            String stock_name = suboption.getStockname();
            String stock_code = suboption.getStockcode();
            String stock_info = suboption.getStockinfo();

            fogn_chart(stock_code);
            agency_chart(stock_code);
            show_information(stock_name, stock_code, stock_info);

            String graghUrl ="https://cdn.fnguide.com/SVO2/chartImg/07_02/A"+stock_code+"_A_PER_D_FY1_07_02.png";
            Glide.with(context).load(graghUrl)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(pergragh);

            MyStat mystat = new MyStat();
            PriceBox pricebox = new PriceBox(stock_code);
            List<Float> closeprice = mystat.trim_float(pricebox.getClose(), 60);
            List<Float> highprice = mystat.trim_float(pricebox.getHigh(), 60);
            List<Float> lowprice = mystat.trim_float(pricebox.getLow(),60);
            // bband 결과값에는 price chart data가 포함되지 않았다
            // 아래처럼 별도로 하나 만들어서 추가해준다.

            MyChart bband_chart = new MyChart();

            bbnandChart = findViewById(R.id.bband_chart);
            List<List<Float>> bb_chart_list = new ArrayList<List<Float>>();
            List<FormatChart> bband_chartdata = new ArrayList<FormatChart>();
            bb_chart_list = mytalib.bbands(closeprice,60);
            bband_chart.adddata_float(closeprice, stock_code, Color.RED); // price chart data는 별도로 추가해준다
            bband_chart.adddata_float(bb_chart_list.get(0), "upper", Color.GRAY);
            bband_chart.adddata_float(bb_chart_list.get(1), "middle", Color.LTGRAY);
            bband_chart.adddata_float(bb_chart_list.get(2), "lower", Color.GRAY);
            bband_chartdata = bband_chart.adddata_float(mytalib.scaled_percentb(), "test", Color.BLUE);
            //bband_chart.setYMinmax(0, 0);
            bband_chart.multi_chart(bbnandChart, bband_chartdata, "볼린저밴드", false);
            //lineChart.invalidate();

            // rsi test
            MyChart rsi_chart = new MyChart();
            rsiChart = findViewById(R.id.rsi_chart);
            List<List<Float>> rsi_chartlist = new ArrayList<List<Float>>();
            List<FormatChart> rsi_chartdata = new ArrayList<FormatChart>();
            rsi_chartlist = mytalib.rsi(closeprice, 60);
            rsi_chart.adddata_float(rsi_chartlist.get(0), "RSI", Color.YELLOW);
            rsi_chartdata = rsi_chart.adddata_float(rsi_chartlist.get(1), "Interval", Color.WHITE);
            rsi_chart.multi_chart(rsiChart,rsi_chartdata,"RSI",false );

            // macd test
            MyChart macd_chart = new MyChart();
            macdChart = findViewById(R.id.macd_chart);
            List<List<Float>> macd_chart_list = new ArrayList<List<Float>>();
            List<FormatChart> macd_chartdata = new ArrayList<FormatChart>();
            macd_chart_list = mytalib.macd(closeprice,60);
            macd_chart.adddata_float(macd_chart_list.get(0), "fast", Color.YELLOW);
            macd_chart.adddata_float(macd_chart_list.get(1), "slow", Color.WHITE);
            macd_chartdata = macd_chart.adddata_float(macd_chart_list.get(2), "signal", Color.RED);
            //macd_chart.setYMinmax(0, 0);
            macd_chart.multi_chart(macdChart, macd_chartdata, "MACD", false);


            // adx test
            //MyChart adx_chart = new MyChart();
            //adxChart = findViewById(R.id.adx_chart);
            //List<Float> adx_chartdata = new ArrayList<>();
            //adx_chartdata = mytalib.adx(stock_code, 60);
            //rsi_chart.single_float(adxChart,adx_chartdata,"ADX",false );

            MyChart adx_chart = new MyChart();
            adxChart = findViewById(R.id.adx_chart);
            List<Float> adx_chartdata = new ArrayList<>();
            adx_chartdata = mytalib.mom(closeprice, 60);
            rsi_chart.single_float(adxChart,adx_chartdata,"MOM",false );

            // stoch test
            MyChart stoch_chart = new MyChart();
            stochChart = findViewById(R.id.stoch_chart);
            List<List<Float>> stoch_chart_list = new ArrayList<List<Float>>();
            List<FormatChart> stock_chartdata = new ArrayList<FormatChart>();
            stoch_chart_list = mytalib.stoch(closeprice,highprice,lowprice,60);
            stoch_chart.adddata_float(stoch_chart_list.get(0), "slow-K", Color.GRAY);
            stock_chartdata = stoch_chart.adddata_float(stoch_chart_list.get(1), "slow-D", Color.LTGRAY);
            //stoch_chart.setYMinmax(0, 0);
            stoch_chart.multi_chart(stochChart, stock_chartdata, "스토케스틱", false);

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

