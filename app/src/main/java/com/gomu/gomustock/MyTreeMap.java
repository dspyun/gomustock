package com.gomu.gomustock;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MyTreeMap {

    final String ASSET_PATH = "file:///android_asset/";
    //public static final String ASSET_PATH = "file:///data/com.gomu.myroom/";
    ProgressBar progressBar;
    WebView webView;
    Context context;
    public MyTreeMap(Context inputcontext) {
        this.context = inputcontext;
    }

    public void webviewTreemap(WebView webview, String region) {

        //progressBar =(ProgressBar)findViewById(R.id.progressBar);

        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webview.getSettings().
                setJavaScriptCanOpenWindowsAutomatically(true);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading (WebView view, WebResourceRequest request){
                loadChart(webview);
                //view.loadUrl(url);
                final Uri uri = request.getUrl();
                return handleUri(uri);
            }
            private boolean handleUri(final Uri uri) {
                //Log.i(TAG, "Uri =" + uri);
                final String host = uri.getHost();
                final String scheme = uri.getScheme();

                final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intent);
                return true;
            }
            @Override
            public void onPageStarted (WebView view, String url, android.graphics.Bitmap favicon){
                super.onPageStarted(view, url, favicon);
                //progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished (WebView view, String url){
                super.onPageFinished(view, url);
                //progressBar.setVisibility(View.INVISIBLE);
            }
        });
        webview.loadUrl("http://");
    }
    private void loadChart(WebView webview) {

        String content = null;
        try {
            AssetManager assetManager = context.getAssets();
            InputStream in = assetManager.open("category.html");
            byte[] bytes = readFully(in);
            content = new String(bytes, "UTF-8");
        } catch (IOException e) {
        }

        String formattedContent = String.format(content);
        webview.loadDataWithBaseURL(ASSET_PATH, formattedContent, "text/html", "utf-8", null);
        webview.requestFocusFromTouch();
    }


    private static byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1;) {
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }

    public void treemap(WebView webView, String region) {

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

        String STOCKDIR = Environment.getExternalStorageDirectory().getPath() + "/gomustock/";;
        String PathFile = STOCKDIR+"treemap"+".html";;

        if(region.equals("domestic"))  webView.loadUrl("file:///android_asset/category.html");
        else if(region.equals("oversea")) webView.loadUrl("file:///android_asset/category.html");

        //webView.loadUrl("file:///android_asset/treemap.html");
    }

    public void Treemapfile() {
        MyExcel myexcel = new MyExcel();
        String data = myexcel.readTreemap();
        String fullfile = header + data + tail;
        myexcel.writehtml(fullfile);
    }

    String header = "<html>\n" +
            "<head>\n" +
            "    <script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>\n" +
            "    <script type=\"text/javascript\">\n" +
            "      google.charts.load('current', {'packages':['treemap']});\n" +
            "      google.charts.setOnLoadCallback(drawChart);\n" +
            "      function drawChart() {\n" +
            "        var data = google.visualization.arrayToDataTable([\n" +
            "          ['Location', 'Parent', 'Market trade volume (size)', 'Market increase/decrease (color)'],\n";

    String tail = "\n" +
            "        tree = new google.visualization.TreeMap(document.getElementById('chart_div'));\n" +
            "\n" +
            "        tree.draw(data, {\n" +
            "          minColor: '#f00',\n" +
            "          midColor: '#ddd',\n" +
            "          maxColor: '#0d0',\n" +
            "          headerHeight: 15,\n" +
            "          fontColor: 'black',\n" +
            "          showScale: true,\n" +
            "          fontSize: 24\n" +
            "        });\n" +
            "\n" +
            "      }\n" +
            "    </script>\n" +
            "  </head>\n" +
            "  <body bgcolor=\"000000\">\n" +
            "    <div id=\"chart_div\" style=\"width: 900px; height: 500px;\"></div>\n" +
            "  </body>\n" +
            "</html>\n";
}
