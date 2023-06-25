package com.gomu.gomustock.ui.dashboard;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.gomu.gomustock.ui.format.FormatChart;
import com.gomu.gomustock.ui.format.FormatScore;
import com.gomu.gomustock.ui.format.FormatStockInfo;
import com.gomu.gomustock.graph.MyChart;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.R;
import com.gomu.gomustock.ui.home.BuyStock;

import java.util.ArrayList;
import java.util.List;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.ViewHolder>
{

    private Activity context;
    private LineChart lineChart;

    private List<Float> chart_data1 = new ArrayList<Float>();;
    private List<Float> chart_data2 =new ArrayList<Float>();;
    MyChart standard_chart = new MyChart();
    public List<String> pricelist = new ArrayList<String>();

    int finger_position=0;
    List<FormatStockInfo> web_stockinfo = new ArrayList<FormatStockInfo>();
    List<FormatStockInfo> scoreinfo = new ArrayList<FormatStockInfo>();
    boolean stop_flag;
    MyExcel myexcel = new MyExcel();
    List<FormatChart> chartlist = new ArrayList<FormatChart>();
    public List<FormatScore> scorebox = new ArrayList<>();
    List<String> recycler_list = new ArrayList<>();

    private List<String> stock_name_list = new ArrayList<>();
    private List<String> stock_code_list = new ArrayList<>();
    public BoardAdapter(Activity context)
    {
        this.context = context;
        //this.dataList = dataList;
        loadDictionary.start();
        loadRecyclerList();

    }

    public void refresh( ) {
        notifyDataSetChanged();
    }

    public void putScoreinfo(String stock_code, String score) {
        int size = scoreinfo.size();
        for(int i =0;i< size ;i++) {
            if(scoreinfo.get(i).stock_code.equals(stock_code)) {
                scoreinfo.get(i).score = score;
            }
        }
        int i =0;
    }

    public void loadBuylist2RecyclerList() {
        // buy code list를 읽어온다
        BuyStock mybuy = new BuyStock();
        List<String> buycodelist = mybuy.getBuyCodeList();
        recycler_list.addAll(buycodelist);
    }

    public void loadRecyclerList() {
        web_stockinfo = myexcel.readStockinfo(false);
        if(web_stockinfo !=null) {
            for (int i = 0; i < web_stockinfo.size(); i++) {
                recycler_list.add(web_stockinfo.get(i).stock_code);
            }
        } else {
            loadBuylist2RecyclerList();
        }
    }

    public String getStockInfo(String stock_code) {
        String result="";
        int size = web_stockinfo.size();
        for(int i =0;i<size;i++)
        {
            if(stock_code.equals(web_stockinfo.get(i).stock_code)) {
                result = web_stockinfo.get(i).toString();
                return result;
            }
        }
        return result;
    }
    public String getStockname(String stock_code) {
        String result="";
        int size = web_stockinfo.size();
        for(int i =0;i<size;i++)
        {
            if(stock_code.equals(web_stockinfo.get(i).stock_code)) {
                result = web_stockinfo.get(i).stock_name;
                return result;
            }
        }
        return result;
    }
    public List<String> getRecyclerList() {
        return recycler_list;
    }
    public void putRecyclerList(List<String> input_list) {
        recycler_list =input_list;
    }

    public void setScorebox(List<FormatScore> input_scorebox) {
        scorebox = input_scorebox;
    }

    public String getScore(String stock_code) {
        String result="";
        int size = scorebox.size();
        for(int i =0;i<size;i++) {
            if(scorebox.get(i).stock_code.equals(stock_code)) {
                result = String.valueOf(scorebox.get(i).score);
                result += " : " + scorebox.get(i).cur_price;
                return result;
            }
        }
        return result;
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.board_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        finger_position = position;
        //final BoardData data = dataList.get(position);
        String stock_code = recycler_list.get(position);

        if(stock_code.equals("069500")) return;
        // information text view에 종목번호를 넣는다

        String stock_name = dashfind_stockname(stock_code);
        //holder.tvStockinfo.setText(getStockinfo(stock_code,stock_name,position));
        holder.tvStockinfo.setText(getStockInfo(stock_code));
        holder.tvscoreboard.setText(stock_name + " score is " + getScore(stock_code));
        // 차트에 코스피와 종목 데이터를 넣어준다

        standard_chart = new MyChart();
        chartlist = new ArrayList<FormatChart>();
        pricelist = myexcel.oa_readItem(stock_code+".xls","CLOSE", false);
        pricelist = myexcel.arrangeRev_string(pricelist);
        chart_data1 = myexcel.oa_standardization(pricelist);
        standard_chart.buildChart_float(chart_data1,stock_code,context.getColor(R.color.Red));

        if(chart_data2.size() == 0) {
            pricelist = myexcel.oa_readItem("069500" + ".xls", "CLOSE", false);
            pricelist = myexcel.arrangeRev_string(pricelist);
            chart_data2 = myexcel.oa_standardization(pricelist);
        }
        chartlist = standard_chart.buildChart_float(chart_data2,"KODEX 200",context.getColor(R.color.White));
        standard_chart.setYMinmax(-3, 3);

        standard_chart.multi_chart(lineChart,chartlist,"표준화차트",false);
        //kospi_chart.single_chart(lineChart,chart_data1,color1,true);
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    @Override
    public int getItemCount()
    {
        // buyList size를 adaper에 알려주면
        // size만큼 list갯수를 보여준다
        return recycler_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvStockinfo;
        ImageView btEdit, btDelete;
        LinearLayout myboardlist;
        TextView tvscoreboard;
        public ViewHolder(View view)
        {
            super(view);
            // 차트 두 개를 겹쳐보이게 할 수 있다
            // 일단 첫번째 차트는 감추고
            lineChart = (LineChart)view.findViewById(R.id.stock_chart);
            //tvStockinfo = view.findViewById(R.id.textView2);
            tvStockinfo = view.findViewById(R.id.bd_stockinfo);
            tvscoreboard = view.findViewById(R.id.scoreboard);
            tvscoreboard.setBackgroundColor(Color.DKGRAY);

            tvStockinfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    MyExcel myexcel = new MyExcel();
                    String stock_code = recycler_list.get(position);
                    String stock_name = myexcel.find_stockname(stock_code);
                    System.out.println("boardview " + stock_code + " " + stock_name + "finger " + String.valueOf(finger_position));

                    String stock_info = web_stockinfo.get(position).toString();
                    BoardSubOption suboption = new BoardSubOption("inform",stock_code, stock_name, stock_info);

                    Intent intent = new Intent(context, BoardSubActivity.class);
                    intent.putExtra("class",suboption);
                    context.startActivity(intent);
                }
            });
        }
        public void textview_refresh() {
            tvStockinfo.invalidate();
        }
    }

    public bgDicThread loadDictionary = new bgDicThread();
    class bgDicThread extends Thread {
        public void run() {
            try {
                Thread.sleep(10L);
                List<List<String>> diclist = new ArrayList<List<String>>();
                MyExcel myexcel = new MyExcel();
                diclist = myexcel.readStockDic();
                stock_name_list = diclist.get(1);
                stock_code_list = diclist.get(0);
                //myscoring2();
            } catch (InterruptedException e) {
                System.out.println("인터럽트로 인한 스레드 종료.");
                return;
            }
        }
    }

    public String dashfind_stockno(String name) {

        int index = stock_name_list.indexOf(name);
        if(index == -1) return "";
        return stock_code_list.get(index);
    }

    public String dashfind_stockname(String number) {

        int index = stock_code_list.indexOf(number);
        if(index == -1) return "";
        return stock_name_list.get(index);
    }

}