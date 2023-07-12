package com.gomu.gomustock.ui.simulation;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
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
import com.gomu.gomustock.stockengin.StockDic;
import com.gomu.gomustock.stockengin.BBandTest;
import com.gomu.gomustock.stockengin.Balance;
import com.gomu.gomustock.stockengin.PriceBox;
import com.gomu.gomustock.ui.format.FormatChart;
import com.gomu.gomustock.ui.format.PortfolioData;

import java.util.ArrayList;
import java.util.List;

public class SimulAdapter extends RecyclerView.Adapter<SimulAdapter.ViewHolder>{


    List<String> simadaper_stock = new ArrayList<>();
    private static Activity context;

    public SparseBooleanArray selectedItems = new SparseBooleanArray();
    // 직전에 클릭됐던 Item의 position
    private int prePosition = -1;

    private boolean stop_flag = false;
    private Dialog dialog_buy; // 커스텀 다이얼로그
    private Dialog dialog_sell; // 커스텀 다이얼로그
    public Button bt_tuja;

    MyExcel myexcel = new MyExcel();
    Button buybt, sellbt;
    TableLayout simul_buysell_item;
    LineChart simulChart;
    StockDic stockdic = new StockDic();
    List<BBandTest> bbandtestlist = new ArrayList<>();
    BackgroundThread priceupdate_thread = new BackgroundThread();
    public SimulAdapter(Activity context, List<String> inputsimstock)
    {
        this.context = context;
        simadaper_stock = inputsimstock;
        loadbbtestlist(60);
        stop_flag = true;
        priceupdate_thread.start();
    }


    public void loadbbtestlist(int days) {
        for(int i =0;i<simadaper_stock.size();i++) {
            String code = simadaper_stock.get(i);
            //MyMagic01 mymagic01 = new MyMagic01(code, "069500");
            //mymagic01.makeBackdata();
            PriceBox pricebox = new PriceBox(code);
            List<Float> closeprice = pricebox.getClose();
            BBandTest bbtest = new BBandTest(code,closeprice,days);
            bbandtestlist.add(bbtest);
        }
    }

    public void refresh( ) {

        for(int i =0;i<simadaper_stock.size();i++) {
            notifyItemChanged(i);
        }
        //notifyDataSetChanged();
    }
    public void reload_curprice( ) {
        stop_flag=true;
        priceupdate_thread.start();
    }

    public void putBBandTestList(List<BBandTest> input_bbtestlist) {
        bbandtestlist = input_bbtestlist;
    }
    public void putStocklist(List<String> stocklist) {
        simadaper_stock = stocklist;
    }
    @Override
    public int getItemCount()
    {
        return simadaper_stock.size();
    }
    public void loadCurrentPrice() {
        // open api를 통해서 어제 종가를 가져와서
        // buy list에 넣는다
        // 이 후 adapter view를 update하면서
        // buiylist로 portfolio를 종가로 재평가해서 보여준다
        MyWeb myweb = new MyWeb();
        String price="";
        // TODO Auto-generated method stub

        for (int i = 0; i < bbandtestlist.size(); i++) {
            //System.out.println(Integer.toString(i) + "번째 종목번호 " + buyList.get(i).stock_no);
            //if(buyList.get(i).stock_code.equals("069500")) continue;
            price = myweb.getCurrentStockPrice(bbandtestlist.get(i).getStock_code());
            String stokprice = price.replaceAll(",", "");
            int int_price = Integer.parseInt(stokprice);
            bbandtestlist.get(i).putCurPrice(int_price);
        }
    }

    class BackgroundThread extends Thread {
        public void run() {
            //여기서는 Toast를 비롯한 UI작업을 실행못함
            while(stop_flag) {
                try {
                    // bbandtest를 마치고 adapter가 보여진 후,
                    // 현재가를 가져오도록 충분히 시간을 준다
                    Thread.sleep(3000);
                    loadCurrentPrice();
                    updateCurPrice();
                    stop_flag = false;
                } catch (InterruptedException e) {
                    System.out.println("인터럽트로 인한 스레드 종료.");
                    return;
                }
            }
        }
    }

    public void updateCurPrice() {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10);
                    //Toast.makeText(context, "test thread", Toast.LENGTH_SHORT).show();
                    //notifyDataSetChanged();
                    for(int i =0;i<simadaper_stock.size();i++) {
                        notifyItemChanged(i);
                    }
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
    MyChart simul_chart = new MyChart();
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pposition) {

        // postion을 써도 되는데 구글에서는 아래처럼 사용하는 것을 recommend 한다
        int position = holder.getAdapterPosition();
        int finger_position = position;

        BBandTest bbtest = bbandtestlist.get(position);
        String stock_code = bbtest.getStock_code();

        MyChart simul_chart = new MyChart();
        List<FormatChart> datalist = new ArrayList<FormatChart>();
        List<Float> pricelist = new ArrayList<>();
        pricelist = bbtest.getClosePirce();
        //pricelist = bbtest.trim(pricelist,120);
        simul_chart.adddata_float(pricelist,stock_code,context.getColor(R.color.Red));

        List<Float> buyscore = bbtest.chartdata_buyscore();
        //buyscore = bbtest.trim(buyscore,120);
        datalist = simul_chart.adddata_float(buyscore, stock_code, context.getColor(R.color.Blue));
        simul_chart.multi_chart(simulChart, datalist, "1년차트", false);

        Balance balance = new Balance(stock_code,0);
        float profitrate = balance.getProfitRate();
        int returnmoney = balance.getSellPrice();
        int averprice = balance.getAVERPrice();
        int total_buy_price = balance.getTotalBuyCost();
        int hold_quantity = balance.getHoldQuantity();
        int estimprice = balance.getEstimPrice();

        String simul_info =
                "투자액 " + Integer.toString(total_buy_price) + "\n" +
                "회수액 " + Integer.toString(returnmoney) + "\n"+
                "잔량 " + Integer.toString(hold_quantity) + ", 평단가 " +  Integer.toString(averprice)+"\n" +
                "수익률 " + String.format("%.2f",profitrate) + "%" + "\n" +
                "평가액 " + Integer.toString(estimprice) + "\n";
        holder.tvSimulinfo.setText(simul_info);

        StockDic stockdic = new StockDic();
        String stock_name = stockdic.getStockname(stock_code);
        String head_info =
                stock_name + "(" + stock_code +"}"+"\n"+
                "현재가 " + bbtest.getCurPrice();
        holder.tvHeadinfo.setText(head_info);

        holder.onBind(position, selectedItems);
        // 일단 default로 + 아이콘을 뿌려준다.

        holder.setOnViewHolderItemClickListener(new OnViewHolderItemClickListener() {
            @Override
            public void onViewHolderItemClick() {

                String bool;
                /*
                for(int i =0;i<buyList.size();i++)  {
                    selectedItems.delete(i);
                }
                selectedItems.put(position, true);
                prePosition = position;
                if (prePosition != -1) notifyItemChanged(prePosition);
                notifyItemChanged(position);
                //System.out.println("event position is " + Integer.toString(position));
                 */
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
                //buyList.add(onebuy);

                //SBuyStock buystock = new SBuyStock();
                //buystock.insert2db(onebuy);

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
        TextView tvHeadinfo,tvSimulinfo;
        LinearLayout simul_list_item;

        public ViewHolder(View view)
        {
            super(view);

            // recycler resource 초기화
            simulChart = (LineChart)view.findViewById(R.id.simul_chart);
            //tvStockinfo = view.findViewById(R.id.textView2);
            tvHeadinfo = view.findViewById(R.id.stock_info);
            tvSimulinfo = view.findViewById(R.id.simul_info);
            tvHeadinfo.setBackgroundColor(Color.DKGRAY);

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
            // buyList.add(data);
        }
        public void onBind(int position, SparseBooleanArray selectedItems) {
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
        /*
        for(int i=0;i<buyList.size();i++) {
            estim_price += buyList.get(i).cur_price*buyList.get(i).buy_quantity;
        }

         */
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
}
