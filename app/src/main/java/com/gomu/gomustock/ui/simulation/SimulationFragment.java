package com.gomu.gomustock.ui.simulation;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.gomu.gomustock.FullPopup_Option;
import com.gomu.gomustock.FulllPopup;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.R;
import com.gomu.gomustock.stockengin.StockDic;
import com.gomu.gomustock.ui.format.FormatStockInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SimulationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SimulationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public SimulationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SimulationFragment.
     */
    // TODO: Rename and change types and number of parameters

    private View view;
    private RecyclerView recyclerView;

    private SimulAdapter simul_adapter;
    private List<Integer> chart_data1 = new ArrayList<Integer>();
    private List<Integer> chart_data2 = new ArrayList<Integer>();
    private List<Integer> chart1_data1 = new ArrayList<Integer>();
    private List<Integer> chart1_data2 = new ArrayList<Integer>();


    private String open_api_data="empty";
    private String yesterday_price="empty";

    TextView simselect_bt, simdl_bt, simtool_bt, simsim_bt;
    TextView noti_board;
    ImageView addnew_img;

    private boolean stop_flag = false;

    private List<String> sim_stock = new ArrayList<>();
    Dialog dialog_listmanagery; // 커스텀 다이얼로그
    Dialog dialog_progress; // 커스텀 다이얼로그

    MyExcel myexcel = new MyExcel();

    WebView webView;
    String source;

    ImageView reload,review;

    List<String> latest_list = new ArrayList<>();
    List<String> namelist = new ArrayList<>();
    List<String> codelist = new ArrayList<>();
    List<Float> profitlist = new ArrayList<>();
    static int LIST_SIZE = 25;
    String FILENAME;

    public static SimulationFragment newInstance(String param1, String param2) {
        SimulationFragment fragment = new SimulationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_simulation, container, false);

        dialog_listmanagery = new Dialog(getActivity());       // Dialog 초기화
        dialog_listmanagery.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog_listmanagery.setContentView(R.layout.dialog_buy);

        dialog_progress = new Dialog(getActivity());       // Dialog 초기화
        dialog_progress.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog_progress.setContentView(R.layout.dialog_progress);


        reload = view.findViewById(R.id.reload);
        review = view.findViewById(R.id.review);

        webView = view.findViewById(R.id.webView);


        //WebView 자바스크립트 활성화
        webView.getSettings().setJavaScriptEnabled(true);
        // 자바스크립트인터페이스 연결
        // 이걸 통해 자바스크립트 내에서 자바함수에 접근할 수 있음.
        webView.addJavascriptInterface(new MyJavascriptInterface(), "Android");
        // 페이지가 모두 로드되었을 때, 작업 정의
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 자바스크립트 인터페이스로 연결되어 있는 getHTML를 실행
                // 자바스크립트 기본 메소드로 html 소스를 통째로 지정해서 인자로 넘김
                view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('body')[0].innerHTML);");
            }
            @Override
            public void onLoadResource(WebView view, String url)  {
                super.onPageFinished(view, url);
                // 자바스크립트 인터페이스로 연결되어 있는 getHTML를 실행
                // 자바스크립트 기본 메소드로 html 소스를 통째로 지정해서 인자로 넘김
                view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('body')[0].innerHTML);");
            }
        });
        //지정한 URL을 웹 뷰로 접근하기
        String URL = "https://comp.fnguide.com/SVO/WooriRenewal/inst.asp";
        //URL = "www.naver.com";
        webView.loadUrl(URL);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });

        review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<List<String>> multilist = new ArrayList<List<String>>();
                String stocklist="";

                if(codelist.size() > 0 && namelist.size() > 0) {
                    multilist.add(0, codelist);
                    multilist.add(1, namelist);
                    List<FormatStockInfo> empty = new ArrayList<>();
                    empty = myexcel.stock20format(multilist);
                    //myexcel.writestockinfoCustom(filename+".xls",empty);
                    myexcel.writestockinfoCustom(FILENAME, empty);
                    int size = codelist.size();
                    for (int i = 0; i < size ; i++) {
                        stocklist += latest_list.get(i) + "\n\r";
                        //stocklist += npslist.get(i).stock_code + " " + npslist.get(i).stock_name + "\r\n";
                    }

                    full_popup(stocklist);
                }
            }
        });

        return view;
    }
    public void full_popup(String stocklist) {
        //Log.d(TAG, "changeButtonText myLooper() " + Looper.myLooper());

        FullPopup_Option popup_option = new FullPopup_Option(stocklist);

        Intent intent = new Intent(getActivity(), FulllPopup.class);
        intent.putExtra("class",popup_option);
        getActivity().startActivity(intent);
    }
    public class MyJavascriptInterface {
        @JavascriptInterface
        public void getHtml(String html) {
            //위 자바스크립트가 호출되면 여기로 html이 반환됨
            StockDic stockdic = new StockDic();

            String result="";
            Map<String, String> npsmap = new HashMap<>();
            ArrayList<String> keyList;// = new ArrayList<>(npsmap.keySet());
            ArrayList<Float> profitkeyList;

            source = html;
            Document doc = Jsoup.parseBodyFragment(html);
            Elements tbodylist =doc.select("tbody");

            Elements tb_trlist = tbodylist.get(0).getElementsByTag("tr");
            String selected = tbodylist.toString();

            int size = tb_trlist.size();
            if(size > 0) {
                Elements theadlist =doc.select("thead");
                Elements th_thlist = theadlist.get(0).getElementsByTag("th");
                //String index6 = th_thlist.get(6).text();
                String index3 = th_thlist.get(3).text();
                Map<Float, String> profitmap = new HashMap<>();

                if( !index3.equals("우선주")) {
                    // 펀더멘탈, 수급종목은 이쪽으로 들어온다
                    latest_list.clear();
                    namelist.clear();
                    codelist.clear();
                    for (int i = 0; i < LIST_SIZE; i++) {
                        Elements tdlist = tb_trlist.get(i).getElementsByTag("td");
                        String name = tdlist.get(1).text();
                        Float profit = Float.parseFloat(tdlist.get(3).text().replace(",",""));
                        namelist.add(name);
                        codelist.add(stockdic.getStockcode(name));
                        profitmap.put(profit, name);
                        latest_list.add(codelist.get(i) + " " + namelist.get(i) + " " + profit);
                    }
                    profitkeyList = new ArrayList<>(profitmap.keySet());
                    profitkeyList.sort((s1, s2) -> s1.compareTo(s2));
                    // 최신 순으로 정렬한다
                    Collections.sort(profitkeyList, Collections.reverseOrder());

                    int i = 0;
                    latest_list.clear();
                    namelist.clear();
                    codelist.clear();
                    for (Float key : profitkeyList) {
                        // result = 시간 + 종목
                        //result += key + " " + npsmap.get(key)+"\n";
                        namelist.add(profitmap.get(key));
                        codelist.add(stockdic.getStockcode(profitmap.get(key)));
                        latest_list.add(key + " " + codelist.get(i) + " " + profitmap.get(key));
                        i++;
                        if (i >= LIST_SIZE) break;
                    }

                    index3 = th_thlist.get(3).text();
                    if(index3.equals("수익률(%)")) {
                        latest_list.add(0,"수급종목");
                        FILENAME = "fnguide_수급.xls";
                    } else {
                        latest_list.add(0,"펀더멘탈");
                        FILENAME = "fnguide_펀더멘탈.xls";
                    }
                } else {
                    // 국민연금은 여기로 들어온다
                    for (int i = 0; i < size; i++) {
                        Elements tdlist = tb_trlist.get(i).getElementsByTag("td");
                        String stock_name = tdlist.get(1).text();
                        //if(i < save_list_size) namelist.add(stock_name);
                        String buytime = tdlist.get(6).text();
                        npsmap.put(buytime, stock_name);
                    }
                    keyList = new ArrayList<>(npsmap.keySet());
                    keyList.sort((s1, s2) -> s1.compareTo(s2));
                    // 최신 순으로 정렬한다
                    Collections.sort(keyList, Collections.reverseOrder());

                    int i = 0;
                    latest_list.clear();
                    namelist.clear();
                    codelist.clear();
                    for (String key : keyList) {
                        // result = 시간 + 종목
                        //result += key + " " + npsmap.get(key)+"\n";
                        namelist.add(npsmap.get(key));
                        codelist.add(stockdic.getStockcode(npsmap.get(key)));
                        latest_list.add(key + " " + codelist.get(i) + " " + npsmap.get(key));
                        i++;
                        if (i >= LIST_SIZE) break;
                    }
                    latest_list.add(0,"국민연금");
                    FILENAME = "fnguide_국민연금.xls";
                }

            } else {
                //System.out.println( driver.toString());
            }
        }
    }

}