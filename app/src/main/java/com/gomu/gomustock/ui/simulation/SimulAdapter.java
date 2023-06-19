package com.gomu.gomustock.ui.simulation;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.Dialog;
import android.os.Looper;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gomu.gomustock.MyOpenApi;
import com.gomu.gomustock.MyWeb;
import com.gomu.gomustock.R;
import com.gomu.gomustock.portfolio.BuyStockDBData;
import com.gomu.gomustock.ui.home.PortfolioData;

import java.util.List;

public class SimulAdapter extends RecyclerView.Adapter<SimulAdapter.ViewHolder>{

    public List<PortfolioData> PortfolioList;
    public static List<BuyStockDBData> buyList;
    private static Activity context;
    public String open_api_data="empty";

    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    // 직전에 클릭됐던 Item의 position
    private int prePosition = -1;
    MyOpenApi myopenapi = new MyOpenApi();
    private BackgroundThread update_thread = new BackgroundThread();
    private boolean stop_flag = false;
    private Dialog dialog_buy; // 커스텀 다이얼로그
    private Dialog dialog_sell; // 커스텀 다이얼로그
    public Button bt_tuja;


    public SimulAdapter(Activity context, List<BuyStockDBData> dataList)
    {
        this.context = context;
        this.buyList = dataList;
        stop_flag = true;
        update_thread.start();
        // getCurrentPrice();
        // 1시간마다 어제 주가를 불러온다(이건 의미 없으나 일단 구현)
        // openapi가 아니고 웹크롤링으로 구현필요
        // BackgroundThread thread = new BackgroundThread();
    }

    @Override
    protected void finalize() throws Throwable {
        update_thread.interrupt();//스레드 종료
        super.finalize();
    }
    public void refresh( ) {
        notifyDataSetChanged();
    }
    public void reload_curprice( ) {
        stop_flag=true;
    }
    public void getCurrentPrice() {
        // open api를 통해서 어제 종가를 가져와서
        // buy list에 넣는다
        // 이 후 adapter view를 update하면서
        // buiylist로 portfolio를 종가로 재평가해서 보여준다
        MyWeb myweb = new MyWeb();
        String price="";
        // TODO Auto-generated method stub
        for (int i = 0; i < buyList.size(); i++) {
            //System.out.println(Integer.toString(i) + "번째 종목번호 " + buyList.get(i).stock_no);
            price = myweb.getCurrentStockPrice(buyList.get(i).stock_code);
            String stokprice = price.replaceAll(",", "");
            int int_price = Integer.parseInt(stokprice);
            buyList.get(i).cur_price = int_price;
        }
    }
    class BackgroundThread extends Thread {
        public void run() {
            //여기서는 Toast를 비롯한 UI작업을 실행못함
            try {
                Thread.sleep(1000 * 2); // 1분에 한번씩 update
                getCurrentPrice();
                updatePortfolioPrice();
                stop_flag = false;
            } catch (InterruptedException e) {
                System.out.println("인터럽트로 인한 스레드 종료.");
                return;
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
    public SimulAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.simul_list_row, parent, false);

        dialog_buy = new Dialog(context);       // Dialog 초기화
        dialog_buy.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog_buy.setContentView(R.layout.dialog_buy);             // xml 레이아웃 파일과 연결

        dialog_sell = new Dialog(context);       // Dialog 초기화
        dialog_sell.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog_sell.setContentView(R.layout.dialog_sell);             // xml 레이아웃 파일과 연결

//        bt_tuja.setText(show_myaccount(buyList));

        return new SimulAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // postion을 써도 되는데 구글에서는 아래처럼 사용하는 것을 recommend 한다
        int finger_position = position;//holder.getAdapterPosition();
        BuyStockDBData buydata = buyList.get(position);

        int now_price = buyList.get(position).cur_price;
        PortfolioData data = estim_buystock(buydata, now_price);

        holder.tv_name.setText(data.stock_name);
        holder.tv_estim_profit.setText(Integer.toString(data.estim_profit));
        holder.tv_estim_price.setText(Integer.toString(data.estim_price));
        holder.tv_cur_price.setText(Integer.toString(data.cur_price));
        holder.tv_hold_quantity.setText(Integer.toString(data.hold_quantity));
        holder.tv_profit_rate.setText(String.format("%.2f",data.profit_rate) + "%");
        holder.tv_buy_price.setText(Integer.toString(data.buy_price));
        holder.tv_ave_price.setText(Integer.toString(data.ave_price));
        //holder.btexpand.setImageResource(R.drawable.circle_plus);
        // expandable list를 펼쳐준다
        holder.onBind(data, position, selectedItems);

        // expandable list에서 call이 되는 click listener
        // 리사이클러뷰의 리스트를 클릭하면 call된다
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


    // expandable view를 구현하기 위한 view holder
    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView tv_name,tv_estim_profit,tv_estim_price,tv_cur_price;
        TextView tv_hold_quantity,tv_profit_rate,tv_buy_price,tv_ave_price;
        ImageView btexpand,btpricerefresh;
        Button buybt, sellbt;
        //private View portfolio_list_view;
        LinearLayout portfolio_item;

        int finger_position;
        public ViewHolder(View view)
        {
            super(view);

            // recycler resource 초기화
            tv_name = view.findViewById(R.id.sim_stock_name);
            tv_estim_profit = view.findViewById(R.id.sim_estim_profit);
            tv_estim_price = view.findViewById(R.id.sim_estim_price);
            tv_cur_price = view.findViewById(R.id.sim_cur_price);
            tv_hold_quantity = view.findViewById(R.id.sim_hold_quantity);
            tv_profit_rate = view.findViewById(R.id.sim_profit_rate);
            tv_buy_price = view.findViewById(R.id.sim_buy_price);
            tv_ave_price = view.findViewById(R.id.sim_ave_price);

            // simulation의 top board 초기화
            bt_tuja = view.findViewById(R.id.sim_account_info);
            //portfolio_item = view.findViewById(R.id.portfolio_item);

        }
        void addItem(BuyStockDBData data) {
            // 외부에서 item을 추가시킬 함수입니다.
            buyList.add(data);
        }

        public void onBind(PortfolioData data, int position, SparseBooleanArray selectedItems) {
            finger_position = position;
        }

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
        total_cache = remain_cache + estim_price;

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
}
