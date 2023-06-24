package com.gomu.gomustock.ui.dashboard;

import static android.content.ContentValues.TAG;
import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gomu.gomustock.MySignal;
import com.gomu.gomustock.R;
import com.gomu.gomustock.databinding.FragmentDashboardBinding;


public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;

    Button btAdd, btReset, btDownload, btUpdate,btSignal,btDummy;
    RecyclerView recyclerView;

    BoardAdapter bd_adapter;

    MySignal mysignal;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initResource(root);
        mysignal = new MySignal(getActivity());
        scoring_thread.start();

        ImageView na_zumimage =  binding.zumchartNa;
        ImageView kr_zumimage = binding.zumchartKr;
        // zoom image link는 스마트폰 > 줌투자 > 새탭에서 이미지열기 > 링크 복사를 해서 사용한다

        String kr_imageUrl = "https://ssl.pstatic.net/imgfinance/chart/sise/siseMainKOSPI.png?sid=1686905920750";
        Glide.with(context).load(kr_imageUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(kr_zumimage);
        kr_zumimage.setScaleType(ImageView.ScaleType.FIT_XY);

        String na_imageUrl = "https://ssl.pstatic.net/imgfinance/chart/world/continent/SPI@SPX.png?1687441130060";
        Glide.with(context).load(na_imageUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(na_zumimage);
        na_zumimage.setScaleType(ImageView.ScaleType.FIT_XY);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        bd_adapter = new BoardAdapter(getActivity());
        binding.recyclerView.setAdapter(bd_adapter);

        na_zumimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 전체화면으로 보여준다
                Intent intent = new Intent(getActivity(), BoardSubActivity.class);
                BoardSubOption suboption = new BoardSubOption("popup","oversea");
                intent.putExtra("class",suboption);
                startActivity(intent);
            }
        });
        kr_zumimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 전체화면으로 보여준다
                Intent intent = new Intent(getActivity(), BoardSubActivity.class);
                BoardSubOption suboption = new BoardSubOption("popup","domestic");
                intent.putExtra("class",suboption);
                startActivity(intent);

            }
        });

        btDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myscoring2();
                //scoring_thread.start();
            }
        });

        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //bd_adapter.adapter_refresh();
                mysignal.calcScore();
                int size = mysignal.scorebox.size();
                for(int i =0; i< size;i++) {
                    String stock_code = mysignal.scorebox.get(i).stock_code;
                    if(stock_code.equals("코덱스 200")) continue;
                    int score = mysignal.scorebox.get(i).score;
                    bd_adapter.putScoreinfo(stock_code, String.valueOf(score));
                }
                bd_adapter.refresh();
            }
        });
        return root;
    }

    public void initResource(View v) {
        btDownload= v.findViewById(R.id.bt_dl);
        btUpdate = v.findViewById(R.id.bt_update);
        btSignal = v.findViewById(R.id.bt_signal);
        btDummy = v.findViewById(R.id.bt_dummy);
        recyclerView = v.findViewById(R.id.recycler_view);
    }


    boolean stop_flag;
    private BackgroundThread scoring_thread = new BackgroundThread();
    class BackgroundThread extends Thread {
        public void run() {
            try {
                mysignal.addCurprice2Scorebox();
                Thread.sleep(1000L);
                //myscoring2();
            } catch (InterruptedException e) {
                System.out.println("인터럽트로 인한 스레드 종료.");
                return;
            }
        }
    }
    public void myscoring() {
        Log.d(TAG, "changeButtonText myLooper() " + Looper.myLooper());

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10L);
                    //Toast.makeText(context, "test thread", Toast.LENGTH_SHORT).show();
                    mysignal.calcScore();
                    int size = mysignal.scorebox.size();
                    for(int i =0; i< size;i++) {
                        String stock_code = mysignal.scorebox.get(i).stock_code;
                        if(stock_code.equals("코덱스 200")) continue;
                        int score = mysignal.scorebox.get(i).score;
                        bd_adapter.putScoreinfo(stock_code, String.valueOf(score));
                    }
                    bd_adapter.setScorebox(mysignal.scorebox);
                    bd_adapter.refresh();
                } catch (InterruptedException e) {
                    System.out.println("인터럽트로 인한 스레드 종료.");
                    return;
                }
            }
        });
    }

    public void myscoring2() {
        // 60일치 데이터로 스코어링했는 결과를 리사이클러뷰에 던져준고 refresh한다.
        // thread가 현재가격 불러왔으면 현재가격 주가해서 스코어링 데이터를 던져준다.
        // 불러오지 않았으면 60일치 결과로 계속 던져준다
        mysignal.calcScore();
        bd_adapter.setScorebox(mysignal.scorebox);
        bd_adapter.refresh();
    }

    public void mywebview(WebView webView, String myurl) {

        webView.setWebViewClient(new WebViewClient());  // 새 창 띄우기 않기
        webView.setWebChromeClient(new WebChromeClient());
        //webView.setDownloadListener(new DownloadListener(){...});  // 파일 다운로드 설정

        //webView.getSettings().setLoadWithOverviewMode(true);  // WebView 화면크기에 맞추도록 설정 - setUseWideViewPort 와 같이 써야함
        webView.getSettings().setUseWideViewPort(true);  // wide viewport 설정 - setLoadWithOverviewMode 와 같이 써야함

        webView.getSettings().setSupportZoom(false);  // 줌 설정 여부
        webView.getSettings().setBuiltInZoomControls(false);  // 줌 확대/축소 버튼 여부

        webView.getSettings().setJavaScriptEnabled(true); // 자바스크립트 사용여부
//        webview.addJavascriptInterface(new AndroidBridge(), "android");
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true); // javascript가 window.open()을 사용할 수 있도록 설정
        webView.getSettings().setSupportMultipleWindows(true); // 멀티 윈도우 사용 여부

        webView.getSettings().setDomStorageEnabled(true);  // 로컬 스토리지 (localStorage) 사용여부

        //웹페이지 호출
//        webView.loadUrl("http://www.naver.com");
        webView.loadUrl(myurl);;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}