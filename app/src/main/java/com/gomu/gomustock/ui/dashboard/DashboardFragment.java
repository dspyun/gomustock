package com.gomu.gomustock.ui.dashboard;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.R;
import com.gomu.gomustock.databinding.FragmentDashboardBinding;
import com.gomu.gomustock.network.MyWeb;
import com.gomu.gomustock.network.YFDownload;
import com.gomu.gomustock.stockengin.MyScore;
import com.gomu.gomustock.stockengin.StockDic;
import com.gomu.gomustock.ui.format.FormatStockInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;

    ImageView imgAddlist;
    TextView tvDownload, tvUpdate,tvSignal,tvDummy;
    RecyclerView recyclerView;

    BoardAdapter bd_adapter;

    MyScore myscore;
    Dialog dialog_buy; // 커스텀 다이얼로그
    MyExcel myexcel = new MyExcel();
    StockDic stockdic = new StockDic();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        dialog_buy = new Dialog(getActivity());       // Dialog 초기화
        dialog_buy.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog_buy.setContentView(R.layout.dialog_buy);

        initResource(root);

        ImageView na_zumimage =  binding.zumchartNa;
        ImageView kr_zumimage = binding.zumchartKr;
        // zoom image link는 스마트폰 > 줌투자 > 새탭에서 이미지열기 > 링크 복사를 해서 사용한다

        Date todaydate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String today = format.format(todaydate);
        String kr_imageUrl ="https://t1.daumcdn.net/finance/chart/kr/stock/m3/KGG01P.png?timestamp="+today;
        Glide.with(context).load(kr_imageUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(kr_zumimage);
        kr_zumimage.setScaleType(ImageView.ScaleType.FIT_XY);
        String na_imageUrl = "https://t1.daumcdn.net/finance/chart/us/stock/m3/SP500.png?timestamp="+today;
        Glide.with(context).load(na_imageUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(na_zumimage);
        na_zumimage.setScaleType(ImageView.ScaleType.FIT_XY);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        bd_adapter = new BoardAdapter(getActivity());
        binding.recyclerView.setAdapter(bd_adapter);

        // adapter초기화 후 생성된 recyclerlist를
        // signal에 입력시키고 현재가격을 불러오는 thread를 시작하여
        // scoring을 할 준비를 한다
        // 나중에 사용자가 update버튼으로 score를 수동 update한다.
        myscore = new MyScore(bd_adapter.getRecyclerList(), "069500");
        myscore.getPriceThreadStart();

        na_zumimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 전체화면으로 보여준다
                Intent intent = new Intent(getActivity(), BoardChartActivity.class);
                BoardSubOption suboption = new BoardSubOption("popup","oversea");
                intent.putExtra("class",suboption);
                startActivity(intent);
            }
        });
        kr_zumimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 전체화면으로 보여준다
                Intent intent = new Intent(getActivity(), BoardChartActivity.class);
                BoardSubOption suboption = new BoardSubOption("popup","domestic");
                intent.putExtra("class",suboption);
                startActivity(intent);

            }
        });

        tvDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyWeb myweb = new MyWeb();
                List<String> recyclerlist = bd_adapter.getRecyclerList();
                dl_getStockinfo(recyclerlist);
                dl_AgencyForeigne(recyclerlist);
                dl_yahoofinance_price(recyclerlist);
            }
        });

        tvUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<String> recyclerlist = bd_adapter.getRecyclerList();
                scoringstock(recyclerlist);
            }
        });
        tvDummy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //bd_adapter.adapter_refresh();

            }
        });
        binding.dashAddnew.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showDialog_buy();
                //app_restart();
            }
        });

        return root;
    }

    public void showDialog_buy(){

        dialog_buy.show(); // 다이얼로그 띄우기
        dialog_buy.findViewById(R.id.buyBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 원하는 기능 구현
                // dialog 화면에서 입력된 정보를 읽어온다
                EditText stock_name = dialog_buy.findViewById(R.id.stock_name);
                String name = stock_name.getText().toString();

                String stock_code = stockdic.getStockcode(name);
                if(stock_code.equals("")) {
                    Toast.makeText(context, "종목명 오류",Toast.LENGTH_SHORT).show();
                    return;
                }

                // adapter list에 종목코드가 없으면 update사켜주고
                List<String> adapterList = bd_adapter.getRecyclerList();
                if(!adapterList.contains(stock_code)) {
                    adapterList.add(stock_code);
                    // 파일에도 추가해주고..info는 추가로 불러와야 함
                    addInfoFile(stock_code);
                    bd_adapter.putRecyclerList(adapterList);
                }

                bd_adapter.refresh();
                dialog_buy.dismiss(); // 다이얼로그 닫기
            }
        });

        dialog_buy.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // dialog 화면에서 입력된 정보를 읽어온다
                EditText stock_name = dialog_buy.findViewById(R.id.stock_name);
                String name = stock_name.getText().toString();

                String stock_code = stockdic.getStockcode(name);
                if(stock_code.equals("")) {
                    Toast.makeText(context, "종목명 오류",Toast.LENGTH_SHORT).show();
                    return;
                }

                // adapter list update사켜주고
                List<String> adapterList = bd_adapter.getRecyclerList();
                for(int i =0;i<adapterList.size();i++) {
                    if (adapterList.get(i).equals(stock_code)) {
                        adapterList.remove(i);
                        break;
                    }
                }
                // 파일에도 추가해주고..info는 추가로 불러와야 함
                delInfoFile(stock_code);
                bd_adapter.putRecyclerList(adapterList);
                bd_adapter.refresh();
                dialog_buy.dismiss(); // 다이얼로그 닫기

            }
        });
    }

    public void initResource(View v) {
        tvDownload= v.findViewById(R.id.tv_dl);
        tvUpdate = v.findViewById(R.id.tv_update);
        tvSignal = v.findViewById(R.id.tv_signal);
        tvDummy = v.findViewById(R.id.tv_dummy);
        imgAddlist = v.findViewById(R.id.dash_addnew);
        recyclerView = v.findViewById(R.id.recycler_view);
    }

    boolean stop_flag;
    private BackgroundThread scoring_thread = new BackgroundThread();
    public class BackgroundThread extends Thread {
        public void run() {
            try {
                myscore.addCurprice2Scorebox();
                Thread.sleep(1000L);
                //myscoring2();
            } catch (InterruptedException e) {
                System.out.println("인터럽트로 인한 스레드 종료.");
                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void scoringstock(List<String> recyclerlist) {
        // 60일치 데이터로 스코어링했는 결과를 리사이클러뷰에 던져준고 refresh한다.
        // 초기에 실행된 sigmal.thread가 현재가격 불러왔으면 현재가격 주가해서 스코어링 데이터를 던져준다.
        // 불러오지 않았으면 60일치 결과로 계속 던져준다
        //mysignal = new MySignal(recyclerlist);
        //mysignal.getPriceThreadStart();

        myscore.calcScore();
        bd_adapter.setScorebox( myscore.getScorebox());
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

    public void addInfoFile(String stockcode) {
        List<String> codelist = new ArrayList<>();
        List<FormatStockInfo> infolist = myexcel.readStockinfo(false);
        // infofile을 읽어서 stockcode 정보가 있는지 검사한다.
        // 없으면 추가, 있으면 건너뛰기
        int size = infolist.size();
        for(int i =0;i<size;i++) {
            codelist.add(infolist.get(i).stock_code);
        }
        // 코드리스트에 stockcode가 없으면 추가한다
        if(!codelist.contains(stockcode)) {
            FormatStockInfo newcode = new FormatStockInfo();
            newcode.addStockcode(stockcode);
            infolist.add(newcode);
        }
        myexcel.writestockinfo(infolist);
    }
    public void delInfoFile(String stockcode) {
        List<String> codelist = new ArrayList<>();
        List<FormatStockInfo> infolist = myexcel.readStockinfo(false);
        // infofile을 읽어서 stockcode 정보가 있는지 검사한다.
        // 있으면 삭제
        int size = infolist.size();
        for(int i =0;i<size;i++) {
            if(infolist.get(i).stock_code.equals(stockcode)) {
                infolist.remove(i);
                break;
            }
        }
        myexcel.writestockinfo(infolist);
    }


    void dl_yahoofinance_price(List<String> stocklist) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                for(int i=0;i<stocklist.size();i++) {
                    new YFDownload(stocklist.get(i));
                }
                notice_ok();
            }
        }).start();
    }
    void notice_ok() {

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

    public void dl_AgencyForeigne(List<String> buylist) {
        MyWeb myweb = new MyWeb();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                myweb.dl_fogninfo(buylist);
            }
        }).start();
    }

    public void dl_getStockinfo(List<String> recyclerList) {
        MyWeb myweb = new MyWeb();
        MyExcel myexcel = new MyExcel();
        List<FormatStockInfo> web_stockinfo = new ArrayList<FormatStockInfo>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int size = recyclerList.size();
                for(int i =0;i<size;i++) {
                    FormatStockInfo info = new FormatStockInfo();
                    if(recyclerList.get(i).equals("069500")) {
                        // ETF이기 때문에 수익률 같은 것은 불러올 수 없다
                        info.init();
                        info.stock_code = "069500";
                        info.stock_name = "KODEX 200";
                    } else {
                        info = myweb.getStockinfo(recyclerList.get(i));
                        info.stock_code = recyclerList.get(i);
                    }
                    web_stockinfo.add(i,info);
                }
                myexcel.writestockinfo(web_stockinfo);
                notice_ok();
            }
        }).start();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}