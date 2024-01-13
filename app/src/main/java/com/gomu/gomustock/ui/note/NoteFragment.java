package com.gomu.gomustock.ui.note;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.MyStat;
import com.gomu.gomustock.R;
import com.gomu.gomustock.graph.MyChart;
import com.gomu.gomustock.network.MyWeb;
import com.gomu.gomustock.stockengin.StockDic;
import com.gomu.gomustock.ui.format.FormatChart;
import com.gomu.gomustock.ui.format.FormatIFA;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NoteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NoteFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View view;
    ImageView editicon, saveicon,dicticon,portfolio;
    EditText mynote;

    FileControl filecontrol;
    String mymemo;
    String MEMOFILE = "memo.txt";
    private LineChart lineChart;
    Dialog dialog_progress; // 커스텀 다이얼로그
    MyExcel myexcel = new MyExcel();

    public NoteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NoteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NoteFragment newInstance(String param1, String param2) {
        NoteFragment fragment = new NoteFragment();
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

        view = inflater.inflate(R.layout.fragment_note, container, false);

        dialog_progress = new Dialog(getActivity());       // Dialog 초기화
        dialog_progress.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog_progress.setContentView(R.layout.dialog_progress);

        editicon = view.findViewById(R.id.memoedit);
        saveicon = view.findViewById(R.id.memosave);
        portfolio = view.findViewById(R.id.portfolio);
        dicticon = view.findViewById(R.id.newdic);
        mynote = view.findViewById(R.id.myeditor);
        lineChart = view.findViewById(R.id.ifa_chart);

        String[] filelist = {"memo1","memo2"};
        Spinner folderspinner = view.findViewById(R.id.memo_spinner);
        ArrayAdapter fileAdapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_list, filelist);
        fileAdapter.setDropDownViewResource(R.layout.spinner_list);
        folderspinner.setAdapter(fileAdapter); //어댑터에 연결해줍니다.

        filecontrol = new FileControl();

        mymemo = Read_Information(MEMOFILE);
        mynote.setText(mymemo);
        setUseableEditText(mynote, false);

        saveicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Save_Information(MEMOFILE);
                setUseableEditText(mynote, false);
                hideBottomNavigation(false);
            }
        });
        editicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideBottomNavigation(true);
                setUseableEditText(mynote, true);
                mymemo = Read_Information(MEMOFILE);
                mynote.setText(mymemo);
            }
        });
        portfolio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 2페이지 = 1달
                // 6페이지 = 3달
                NaverDownloadIFA_Dialog(6);
                //update_stockdic();
            }
        });
        dicticon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_chart();
                //update_stockdic();
            }
        });
        // Inflate the layout for this fragment
        return view;
    }
    public void hideBottomNavigation(Boolean bool) {
        BottomNavigationView bottomNavigation = getActivity().findViewById(R.id.nav_view);

        if (bool == true)
            bottomNavigation.setVisibility(View.GONE);
        else
            bottomNavigation.setVisibility(View.VISIBLE);
    }
    public String Read_Information(String filename) {
        String readfilename;
        readfilename = filename;//저장할 파일 이름설정
        //fname="/Label/"+"my-todo"+".txt";//저장할 파일 이름설정
        return filecontrol.ReadTextFile(readfilename);
    }

    public void Save_Information(String filename) {
        String savefilename;
        savefilename = filename;//저장할 파일 이름설정
        //Toast.makeText(getActivity().getApplicationContext(),"File save "+savefilename, Toast.LENGTH_SHORT).show();
        String content = mynote.getText().toString();
        filecontrol.WriteTextFile(savefilename, content);
    }

    private void setUseableEditText(EditText et, boolean useable) {
        et.setClickable(useable);
        et.setEnabled(useable);
        et.setFocusable(useable);
        et.setFocusableInTouchMode(useable);

    }

    public void NaverDownloadIFA_Dialog(int totalpage){
        dialog_progress.show(); // 다이얼로그 띄우기
        dialog_progress.setCanceledOnTouchOutside(false);
        dialog_progress.setCancelable(false);
        ProgressBar dlg_bar = dialog_progress.findViewById(R.id.dialog_progressBar);

        Thread dlg_thread = new Thread(new Runnable() {
            MyWeb myweb = new MyWeb();
            MyExcel myexcel = new MyExcel();
            List<FormatIFA> ifa_list_total = new ArrayList<FormatIFA>();
            List<FormatIFA> ifa_list_page = new ArrayList<FormatIFA>();
            public void run() {
                try {
                    int max = totalpage;
                    dlg_bar.setProgress(0);
                    for(int i=0;i<totalpage;i++) {
                        dlg_bar.setProgress(100*(i+1)/max);
                        // 1. 개인, 외국인, 기관 매매액 다운로드
                        ifa_list_page = myweb.getNaverPFA(Integer.toString(i+1));
                        int size = ifa_list_page.size();
                        for(int j =0;j<size ;j++) {
                            ifa_list_total.add(ifa_list_page.get(j));
                        }
                    }
                    myexcel.writeIFAinfo("ifa_info.xls",ifa_list_total);
                    dialog_progress.dismiss();
                } catch (Exception ex) {
                    Log.e("MainActivity", "Exception in processing mesasge.", ex);
                }
            }
        });

        dlg_thread.start();
    }

    MyChart three_chart = new MyChart();
    List<FormatChart> chartlist = new ArrayList<FormatChart>();
    private List<Integer> chart_indi = new ArrayList<Integer>();;
    private List<Integer> chart_foreign = new ArrayList<Integer>();;
    private List<Integer> chart_agency = new ArrayList<Integer>();;
    private List<Integer> chart_total = new ArrayList<Integer>();;
    public void show_chart() {
        three_chart.clearbuffer();
        chartlist = new ArrayList<FormatChart>();
        read_ifa();
        chartlist = three_chart.adddata_int(chart_indi, "indi", getActivity().getColor(R.color.White));
        chartlist = three_chart.adddata_int(chart_foreign, "foreign", getActivity().getColor(R.color.Red));
        chartlist = three_chart.adddata_int(chart_agency, "agency", getActivity().getColor(R.color.Yellow));
        chartlist = three_chart.adddata_int(chart_total, "total", getActivity().getColor(R.color.Blue));
        three_chart.multi_chart(lineChart, chartlist, "W:개인,R:외국인,Y:기관,B:총", false);
    }
    public void read_ifa() {
        MyStat mystat = new MyStat();
        List<String> indi = new ArrayList<>();
        List<String> foreign = new ArrayList<>();
        List<String> agency = new ArrayList<>();
        List<FormatIFA> ifa_list = new ArrayList<>();
        ifa_list = myexcel.readIFAinfo("ifa_info.xls");
        int size = ifa_list.size();
        for(int i =0;i<size;i++) {
            indi.add(ifa_list.get(i).indi);
            foreign.add(ifa_list.get(i).foreign);
            agency.add(ifa_list.get(i).agency);
        }
        chart_indi = mystat.string2int(indi,1);
        chart_foreign = mystat.string2int(foreign,1);
        chart_agency = mystat.string2int(agency,1);

        for(int i=0;i<size;i++) {
            chart_total.add(chart_indi.get(i)+chart_foreign.get(i)+chart_agency.get(i));
        }
    }

    public void update_stockdic() {
        Thread dlg_thread = new Thread(new Runnable() {
            StockDic stockdic = new StockDic();
            public void run() {
                try {
                    stockdic.reMakeDic();
                } catch (Exception ex) {
                    Log.e("MainActivity", "Exception in processing mesasge.", ex);
                }
            }
        });
    }
}
