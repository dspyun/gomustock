package com.gomu.gomustock.ui.dashboard;

import static android.view.View.INVISIBLE;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.gomu.gomustock.FormatChart;
import com.gomu.gomustock.FormatStockInfo;
import com.gomu.gomustock.MyChart;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.MyStat;
import com.gomu.gomustock.MyWeb;
import com.gomu.gomustock.R;
import com.gomu.gomustock.portfolio.BuyStock;
import com.gomu.gomustock.portfolio.BuyStockDB;
import com.gomu.gomustock.portfolio.BuyStockDBData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.ViewHolder>
{

    private Activity context;
    private LineChart lineChart;

    private List<Float> chart_data1;
    private List<Float> chart_data2;
    MyChart standard_chart;

    List<String> pricelist;
    List<String> buycodelist = new ArrayList<String>();
    MyExcel myexcel;
    MyStat mystat;

    BoardAdapter bd_adapter;
    TextView myinform;
    int finger_position=0;
    List<FormatStockInfo> web_stockinfo = new ArrayList<FormatStockInfo>();
    List<FormatStockInfo> scoreinfo = new ArrayList<FormatStockInfo>();
    boolean stop_flag;
    public BoardAdapter(Activity context)
    {
        this.context = context;
        //this.dataList = dataList;
        myexcel = new MyExcel();
        mystat = new MyStat();
        ReadBuyList();
        initInfoArray();
    }


    public void initInfoArray() {
        web_stockinfo = myexcel.readStockinfo(false);
        if(web_stockinfo.size()==0) {
            // size가 0이면 파일이 없음. 초기화 해줘야 함
            // 초기화는 일단 code만 넣어주는 것으로
            for(int i =0;i<buycodelist.size();i++) {
                FormatStockInfo temp = new FormatStockInfo();
                temp.stock_code = buycodelist.get(i);
                web_stockinfo.add(temp);
                scoreinfo.add(temp);
            }
            int i=0;
        }
        for(int i =0;i<buycodelist.size();i++) {
            FormatStockInfo temp = new FormatStockInfo();
            temp.stock_code = buycodelist.get(i);
            scoreinfo.add(temp);
        }
    }

    public void refresh( ) {
        notifyDataSetChanged();
    }
    public void putScoreinfo(String stock_code, String score) {
        for(int i =0;i<scoreinfo.size();i++) {
            if(scoreinfo.get(i).stock_code.equals(stock_code)) {
                scoreinfo.get(i).score = score;
            }
        }
        int i =0;
    }

    public void ReadBuyList() {
        // buy code list를 읽어온다
        BuyStock mybuy = new BuyStock();
        buycodelist = mybuy.getBuyCodeList();
        int i = 0;
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
        String stock_code = buycodelist.get(position);

        // information text view에 종목번호를 넣는다
        MyExcel myexcel = new MyExcel();
        String stock_name = myexcel.find_stockname(stock_code);
        holder.tvStockinfo.setText(getStockinfo(stock_code,stock_name,position));

        // 차트에 코스피와 종목 데이터를 넣어준다
        chart_data1 = new ArrayList<Float>();
        chart_data2 = new ArrayList<Float>();
        pricelist = new ArrayList<String>();
        standard_chart = new MyChart();
        List<FormatChart> chartlist = new ArrayList<FormatChart>();
        pricelist = myexcel.oa_readItem(stock_code+".xls","CLOSE", false);
        chart_data1 = mystat.oa_standardization(pricelist);
        standard_chart.buildChart_float(chart_data1,stock_code,context.getColor(R.color.Red));

        pricelist = myexcel.oa_readItem("069500"+".xls","CLOSE", false);
        chart_data2 = mystat.oa_standardization(pricelist);
        chartlist = standard_chart.buildChart_float(chart_data2,"KODEX 200",context.getColor(R.color.White));
        standard_chart.setYMinmax(-3, 3);

        standard_chart.multi_chart(lineChart,chartlist,"표준화차트",false);
        //kospi_chart.single_chart(lineChart,chart_data1,color1,true);
    }

    public String getStockinfo(String stock_code, String stock_name, int position) {

        String stock_info="";
        stock_info = stock_code + " " + stock_name + "\n";
        MyExcel myexcel = new MyExcel();
        web_stockinfo.clear();
        // 파일에설 주식정보를 읽고
        web_stockinfo = myexcel.readStockinfo(false);
        // 주식정보에 전달받은 스코어정보를 넣고
        if(web_stockinfo.size() > 0) {
            /*
            for (int i = 0; i < scoreinfo.size(); i++) {
                if (scoreinfo.get(i).stock_code.equals(web_stockinfo.get(i).stock_code)) {
                    web_stockinfo.get(i).score = scoreinfo.get(i).score;
                }
            }
            */
            // 정보를 모두 string으로 변환
            for(int i=0;i<web_stockinfo.size();i++) {
                if(stock_code.equals( web_stockinfo.get(i).stock_code )) {
                    stock_info += web_stockinfo.get(position).toString();
                    return stock_info;
                }
            }
        }
        return stock_info;
    }

    public void dl_getStockinfo() {
        MyWeb myweb = new MyWeb();
        // 리스트버퍼를 초기화해서 이전에 남아있던 내용을 모두 없애준다
        web_stockinfo.clear();
        for(int i =0;i<buycodelist.size();i++) {
            FormatStockInfo temp = new FormatStockInfo();
            temp.stock_code = buycodelist.get(i);
            web_stockinfo.add(temp);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                FormatStockInfo result1;
                for(int i =0;i<buycodelist.size();i++) {
                    FormatStockInfo info = new FormatStockInfo();
                    info = myweb.getStockinfo(buycodelist.get(i));
                    info.stock_code = buycodelist.get(i);
                    info.stock_name = myexcel.find_stockname(buycodelist.get(i));
                    web_stockinfo.set(i,info);
                }
                FormatStockInfo info1 = new FormatStockInfo();
                info1.setHeader();
                web_stockinfo.add(0,info1);
                myexcel.writestockinfo(web_stockinfo);
            }
        }).start();
    }

    @Override
    public int getItemCount()
    {
        // buyList size를 adaper에 알려주면
        // size만큼 list갯수를 보여준다
        return buycodelist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvStockinfo;
        ImageView btEdit, btDelete;
        LinearLayout myboardlist;
        public ViewHolder(View view)
        {
            super(view);
            // 차트 두 개를 겹쳐보이게 할 수 있다
            // 일단 첫번째 차트는 감추고
            lineChart = (LineChart)view.findViewById(R.id.stock_chart);
            lineChart.setVisibility(INVISIBLE);
            lineChart = (LineChart)view.findViewById(R.id.kospi_chart);
            //tvStockinfo = view.findViewById(R.id.textView2);
            tvStockinfo = view.findViewById(R.id.bd_stockinfo);

            tvStockinfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    MyExcel myexcel = new MyExcel();
                    String stock_code = buycodelist.get(position);
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


}