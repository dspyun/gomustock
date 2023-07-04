package com.gomu.gomustock.ui.simulation;

import static android.content.ContentValues.TAG;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Looper;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.R;
import com.gomu.gomustock.graph.MyChart;
import com.gomu.gomustock.network.MyWeb;
import com.gomu.gomustock.stockdb.BuyStockDBData;
import com.gomu.gomustock.stockdb.StockDic;
import com.gomu.gomustock.ui.format.FormatChart;
import com.gomu.gomustock.ui.format.PortfolioData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimulAdapter extends RecyclerView.Adapter<SimulAdapter.ViewHolder>{

    private List<BuyStockDBData> buyList;
    private static Activity context;

    public SparseBooleanArray selectedItems = new SparseBooleanArray();
    // 직전에 클릭됐던 Item의 position
    private int prePosition = -1;
    private BackgroundThread priceupdate_thread = new BackgroundThread();
    private boolean stop_flag = false;
    private Dialog dialog_buy; // 커스텀 다이얼로그
    private Dialog dialog_sell; // 커스텀 다이얼로그
    public Button bt_tuja;

    MyExcel myexcel = new MyExcel();
    Button buybt, sellbt;
    TableLayout simul_buysell_item;
    LineChart simulChart;
    StockDic stockdic = new StockDic();
    public SimulAdapter(Activity context, List<BuyStockDBData> dataList)
    {
        this.context = context;
        this.buyList = dataList;
        stop_flag = true;
        priceupdate_thread.start();
        // 1시간마다 어제 주가를 불러온다(이건 의미 없으나 일단 구현)
        // openapi가 아니고 웹크롤링으로 구현필요
        // BackgroundThread thread = new BackgroundThread();
    }

    @Override
    protected void finalize() throws Throwable {
        priceupdate_thread.interrupt();//스레드 종료
        super.finalize();
    }
    public void refresh( ) {
        notifyDataSetChanged();
    }
    public void reload_curprice( ) {
        //priceupdate_thread.start();
        stop_flag=true;
    }
    public boolean checksamefile(String stock_code) {
        int size = buyList.size();
        for(int i=0;i<size;i++) {
            if(stock_code.equals(buyList.get(i).stock_code)) return true;
        }
        return false;
    }
    public  List<BuyStockDBData> getRecyclerList() {
        return buyList;
    }
    public void putBuyList(List<BuyStockDBData> input_buylist) {
        buyList = input_buylist;
    }
    public void loadCurrentPrice() {
        // open api를 통해서 어제 종가를 가져와서
        // buy list에 넣는다
        // 이 후 adapter view를 update하면서
        // buiylist로 portfolio를 종가로 재평가해서 보여준다
        MyWeb myweb = new MyWeb();
        String price="";
        // TODO Auto-generated method stub
        for (int i = 0; i < buyList.size(); i++) {
            //System.out.println(Integer.toString(i) + "번째 종목번호 " + buyList.get(i).stock_no);
            //if(buyList.get(i).stock_code.equals("069500")) continue;
            price = myweb.getCurrentStockPrice(buyList.get(i).stock_code);
            String stokprice = price.replaceAll(",", "");
            int int_price = Integer.parseInt(stokprice);
            buyList.get(i).cur_price = int_price;
        }
    }
    class BackgroundThread extends Thread {
        public void run() {
            //여기서는 Toast를 비롯한 UI작업을 실행못함
            while(stop_flag) {
                try {
                    Thread.sleep(1000); // 1분에 한번씩 update
                    loadCurrentPrice();
                    updatePortfolioPrice();
                    stop_flag = false;
                } catch (InterruptedException e) {
                    System.out.println("인터럽트로 인한 스레드 종료.");
                    return;
                }
            }
        }
    }

    public void updatePortfolioPrice() {
        Log.d(TAG, "changeButtonText myLooper() " + Looper.myLooper());

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    //Toast.makeText(context, "test thread", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                } catch (InterruptedException e) {
                    System.out.println("인터럽트로 인한 스레드 종료.");
                    return;
                }
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simul_list_row, parent, false);

        dialog_buy = new Dialog(context);       // Dialog 초기화
        dialog_buy.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog_buy.setContentView(R.layout.dialog_buy);             // xml 레이아웃 파일과 연결

        dialog_sell = new Dialog(context);       // Dialog 초기화
        dialog_sell.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog_sell.setContentView(R.layout.dialog_sell);             // xml 레이아웃 파일과 연결

//        bt_tuja.setText(show_myaccount(buyList));

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // postion을 써도 되는데 구글에서는 아래처럼 사용하는 것을 recommend 한다
        int finger_position = position;//holder.getAdapterPosition();
        BuyStockDBData buydata = buyList.get(position);
        String stock_code = buydata.stock_code;
        int now_price = buyList.get(position).cur_price;

        MyChart simul_chart = new MyChart();
        List<FormatChart> chartlist = new ArrayList<FormatChart>();
        chartlist = new ArrayList<FormatChart>();

        List<String> pricelist_str = new ArrayList<>();
        pricelist_str = myexcel.readhistory(stock_code+".xls","CLOSE", 60,false);
        pricelist_str = myexcel.arrangeRev_string(pricelist_str);
        List<Integer> pricelist = myexcel.string2int(pricelist_str, 1);
        simul_chart.buildChart_int(pricelist,stock_code,context.getColor(R.color.Red));
        int maxprice = Collections.max(pricelist);

        BSManager mybsmanager = new BSManager(context,stock_code );
        List<Integer> buyhistory = mybsmanager.getBuyQuantityList();
        int size = buyhistory.size();
        for(int i =0;i<size;i++) {
            int scale_data = buyhistory.get(i);
            scale_data = (int)(maxprice + maxprice*0.01*scale_data);
            buyhistory.set(i,scale_data);
        }
        if(pricelist.size() > 0 && buyhistory.size() > 0) {
            // 리스트에 첫번째로 추가되면 buyhistory가 없다.
            // 이 때는 그냥 넘어간다
            chartlist = simul_chart.buildChart_int(buyhistory, stock_code, context.getColor(R.color.Blue));
            simul_chart.multi_chart(simulChart, chartlist, "표준화차트", false);
        }
        BuyStockDBData currentstockinfo = new BuyStockDBData();
        currentstockinfo =mybsmanager.CurrentStockInfo();
        PortfolioData data= estim_buystock(currentstockinfo, now_price);

        String stock_info = data.stock_name + " " + Integer.toString(data.hold_quantity)+"주"
                + " " + "현재가 " +Integer.toString(data.cur_price);
        String simul_info = "평가액 " + Integer.toString(data.estim_price) + "\n"+
                "수익률 " + String.format("%.2f",data.profit_rate) + "%" + "\n" +
                "매수액 " + Integer.toString(data.buy_price) + "\n" +
                "평단가 " + Integer.toString(data.estim_profit);
        holder.tvStockinfo.setText(stock_info);
        holder.tvSimulinfo.setText(simul_info);

        holder.onBind(data, position, selectedItems);
        // 일단 default로 + 아이콘을 뿌려준다.

        holder.setOnViewHolderItemClickListener(new OnViewHolderItemClickListener() {
            @Override
            public void onViewHolderItemClick() {

                String bool;

                for(int i =0;i<buyList.size();i++)  {
                    selectedItems.delete(i);
                }
                selectedItems.put(position, true);
                prePosition = position;
                if (prePosition != -1) notifyItemChanged(prePosition);
                notifyItemChanged(position);
                //System.out.println("event position is " + Integer.toString(position));

            }
        });
    }

    public PortfolioData estim_buystock(BuyStockDBData buystock, int cur_price) {
        String stock_name; // 종목명
        int estim_profit, estim_price; // 평가손익
        int hold_quantity,unit_price,ave_price;
        double profit_rate;
        PortfolioData screen_info = new PortfolioData();

        unit_price = buystock.getPrice();
        hold_quantity = buystock.getQuantity();

        estim_profit = (cur_price - unit_price) * hold_quantity;
        estim_price = cur_price * hold_quantity;
        profit_rate = ((cur_price*1.0)/(unit_price*1.0)-1)*100;
        ave_price = unit_price;

        screen_info.transaction_type = "buy";
        screen_info.stock_name = buystock.stock_name;
        screen_info.estim_profit = estim_profit;
        screen_info.estim_price = estim_price;
        screen_info.cur_price = cur_price;
        screen_info.hold_quantity = hold_quantity;
        screen_info.profit_rate = profit_rate;
        screen_info.buy_price = unit_price*hold_quantity;
        screen_info.ave_price = ave_price;
        //Toast.makeText(context, Double.toString(profit_rate), Toast.LENGTH_SHORT).show();
        return screen_info;
    }

    @Override
    public int getItemCount()
    {
        return buyList.size();
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

                BuyStockDBData onebuy = new BuyStockDBData();
                onebuy.stock_name = name;
                onebuy.stock_code = stock_code;
                buyList.add(onebuy);

                SBuyStock buystock = new SBuyStock();
                buystock.insert2db(onebuy);

                notifyDataSetChanged();
                dialog_buy.dismiss(); // 다이얼로그 닫기
            }
        });
        //  버튼
        dialog_buy.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 원하는 기능 구현
                dialog_buy.dismiss(); // 다이얼로그 닫기
            }
        });
    }

    // expandable view를 구현하기 위한 view holder
    public class ViewHolder extends RecyclerView.ViewHolder
    {
        OnViewHolderItemClickListener onViewHolderItemClickListener;
        TextView tvStockinfo,tvSimulinfo;
        LinearLayout simul_list_item;


        public ViewHolder(View view)
        {
            super(view);

            // recycler resource 초기화
            simulChart = (LineChart)view.findViewById(R.id.simul_chart);
            //tvStockinfo = view.findViewById(R.id.textView2);
            tvStockinfo = view.findViewById(R.id.stock_info);
            tvSimulinfo = view.findViewById(R.id.simul_info);
            tvStockinfo.setBackgroundColor(Color.DKGRAY);

            // simulation의 top board 초기화
            bt_tuja = view.findViewById(R.id.sim_asset_info);
            //portfolio_item = view.findViewById(R.id.portfolio_item);

            buybt = view.findViewById(R.id.sim_buy_stock);
            sellbt = view.findViewById(R.id.sim_sell_stock);
            // 펼쳐진 부분이 클릭되면 접히게 하는 click listener

            simul_list_item = view.findViewById(R.id.simul_list_layout);
            simul_buysell_item =  view.findViewById(R.id.simul_buysell_layout);
            // expandable list에서 call이 되는 click listener
            // 리사이클러뷰의 리스트를 클릭하면 call된다

            simul_list_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onViewHolderItemClickListener.onViewHolderItemClick();
                }
            });

            buybt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "add item to simul list", Toast.LENGTH_SHORT).show();
                    showDialog_buy();
                    //onViewHolderItemClickListener.onViewHolderItemClick();
                }
            });
            sellbt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "not support selldialog", Toast.LENGTH_SHORT).show();
                    //onViewHolderItemClickListener.onViewHolderItemClick();
                }
            });
        }
        public void setOnViewHolderItemClickListener(OnViewHolderItemClickListener onViewHolderItemClickListener)
        {
            this.onViewHolderItemClickListener = onViewHolderItemClickListener;
        }
        void addItem(BuyStockDBData data) {
            // 외부에서 item을 추가시킬 함수입니다.
            buyList.add(data);
        }
        public void onBind(PortfolioData data, int position, SparseBooleanArray selectedItems) {
            //System.out.println("position is " + Integer.toString(position));
            simul_buysell_item.setVisibility(selectedItems.get(position) ? View.VISIBLE : View.GONE);
            //changeVisibility(selectedItems.get(position));
        }
    }

    /**
     * 클릭된 Item의 상태 변경
     * @param isExpanded Item을 펼칠 것인지 여부
     */
    private void changeVisibility(final boolean isExpanded) {
        // ValueAnimator.ofInt(int... values)는 View가 변할 값을 지정, 인자는 int 배열
        // 600 : expandable list의 놎이값
        ValueAnimator va = isExpanded ? ValueAnimator.ofInt(0, 150) : ValueAnimator.ofInt(150, 0);
        // Animation이 실행되는 시간, n/1000초
        va.setDuration(500);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {


                simul_buysell_item.getLayoutParams().height = (int) animation.getAnimatedValue();
                simul_buysell_item.requestLayout();
                // imageView가 실제로 사라지게하는 부분
                simul_buysell_item.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
/*
                buybt.getLayoutParams().height = (int) animation.getAnimatedValue();
                buybt.requestLayout();
                // imageView가 실제로 사라지게하는 부분
                buybt.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

                sellbt.getLayoutParams().height = (int) animation.getAnimatedValue();
                sellbt.requestLayout();
                // imageView가 실제로 사라지게하는 부분
                sellbt.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
*/
            }
        });
        // Animation start
        va.start();
    }

    public String show_myaccount() {

        SCache mycache = new SCache();
        int first_cache = mycache.getFirstCache();
        int remain_cache= mycache.getRemainCache();
        int estim_price=0;

        if(first_cache ==0) return "";

        for(int i=0;i<buyList.size();i++) {
            estim_price += buyList.get(i).cur_price*buyList.get(i).buy_quantity;
        }
        int total_cache =0;
        total_cache = first_cache + remain_cache + estim_price;

        String total  = Integer.toString(total_cache);
        String remain = Integer.toString(remain_cache);
        String estim = Integer.toString(estim_price);
        String profitrate = String.format("%.2f",100*(((float)total_cache-first_cache)/first_cache));
        String account_info = "투자계정수익률 "+profitrate+"\n총 : "+total+"\n현금 : "+remain+"\n주식평가 : "+estim;

        //Toast.makeText(context, account_info, Toast.LENGTH_SHORT).show();
        return account_info;
    }

    public void updatebt() {
        //bt_tuja = context.findViewById(R.id.sim_account_info);
        bt_tuja.setText(show_myaccount());
    }
    public  List<BuyStockDBData> getBuylist() {
        return buyList;
    }
    public void putbuyList(List<BuyStockDBData> input_buylist) {
        buyList = input_buylist;
    }

}
