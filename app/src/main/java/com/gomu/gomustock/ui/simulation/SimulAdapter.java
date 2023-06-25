package com.gomu.gomustock.ui.simulation;

import static android.content.ContentValues.TAG;

import android.animation.ValueAnimator;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.network.MyWeb;
import com.gomu.gomustock.R;
import com.gomu.gomustock.stockdb.BuyStockDBData;
import com.gomu.gomustock.ui.format.PortfolioData;

import java.util.ArrayList;
import java.util.List;

public class SimulAdapter extends RecyclerView.Adapter<SimulAdapter.ViewHolder>{

    public List<BuyStockDBData> buyList;
    private static Activity context;

    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    // 직전에 클릭됐던 Item의 position
    private int prePosition = -1;
    private BackgroundThread update_thread = new BackgroundThread();
    private boolean stop_flag = false;
    private Dialog dialog_buy; // 커스텀 다이얼로그
    private Dialog dialog_sell; // 커스텀 다이얼로그
    public Button bt_tuja;

    private List<String> stock_name_list = new ArrayList<>();
    private List<String> stock_code_list = new ArrayList<>();
    MyExcel myexcel = new MyExcel();
    Button buybt, sellbt;
    public SimulAdapter(Activity context, List<BuyStockDBData> dataList)
    {
        this.context = context;
        this.buyList = dataList;
        stop_flag = true;
        update_thread.start();
        loadDictionary.start();
        //getCurrentPrice();
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
    public  List<BuyStockDBData> getRecyclerList() {
        return buyList;
    }
    public void putBuyList(List<BuyStockDBData> input_buylist) {
        buyList = input_buylist;
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
        holder.onBind(position, selectedItems);

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

    public void showDialog_buy(){
        dialog_buy.show(); // 다이얼로그 띄우기

        dialog_buy.findViewById(R.id.buyBtn).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // 원하는 기능 구현

                // dialog 화면에서 입력된 정보를 읽어온다
                EditText stock_name = dialog_buy.findViewById(R.id.stock_name);
                String name = stock_name.getText().toString();

                String stock_code = myexcel.find_stockno(name);
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
        TextView tv_name,tv_estim_profit,tv_estim_price,tv_cur_price;
        TextView tv_hold_quantity,tv_profit_rate,tv_buy_price,tv_ave_price;
        ImageView btexpand,btpricerefresh;

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
            bt_tuja = view.findViewById(R.id.sim_asset_info);
            //portfolio_item = view.findViewById(R.id.portfolio_item);

            btexpand = view.findViewById(R.id.sim_button_expanable);
            buybt = view.findViewById(R.id.sim_buy_stock);
            sellbt = view.findViewById(R.id.sim_sell_stock);
            // 펼쳐진 부분이 클릭되면 접히게 하는 click listener

            portfolio_item = view.findViewById(R.id.portfolio_item);
            btexpand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(context, "click expandable icon", Toast.LENGTH_SHORT).show();
                    //onViewHolderItemClickListener.onViewHolderItemClick();
                    if (selectedItems.get(finger_position)) {
                        // 펼쳐진 Item을 클릭 시
                        selectedItems.delete(finger_position);
                        btexpand.setImageResource(R.drawable.minus48jpx);

                    } else {
                        // 직전의 클릭됐던 Item의 클릭상태를 지움
                        selectedItems.delete(prePosition);
                        // 클릭한 Item의 position을 저장
                        selectedItems.put(finger_position, true);
                        btexpand.setImageResource(R.drawable.plus48px);
                    }
                    // 해당 포지션의 변화를 알림
                    String prepos = Integer.toString(prePosition);
                    String fingerpos = Integer.toString(finger_position);
                    Toast.makeText(context, "pre: "+prepos+" finger "+fingerpos, Toast.LENGTH_SHORT).show();
                    if (prePosition != -1) notifyItemChanged(prePosition);
                    notifyItemChanged(finger_position);
                    // 클릭된 position 저장

                    prePosition = finger_position;
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
        void addItem(BuyStockDBData data) {
            // 외부에서 item을 추가시킬 함수입니다.
            buyList.add(data);
        }

        public void onBind(int position, SparseBooleanArray selectedItems) {
            finger_position = position;
            changeVisibility(selectedItems.get(position));
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

                buybt.getLayoutParams().height = (int) animation.getAnimatedValue();
                buybt.requestLayout();
                // imageView가 실제로 사라지게하는 부분
                buybt.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

                sellbt.getLayoutParams().height = (int) animation.getAnimatedValue();
                sellbt.requestLayout();
                // imageView가 실제로 사라지게하는 부분
                sellbt.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
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
    public String simfind_stockno(String name) {

        int index = stock_name_list.indexOf(name);
        if(index == -1) return "";
        return stock_code_list.get(index);
    }

    public String simfind_stockname(String number) {

        int index = stock_code_list.indexOf(number);
        if(index == -1) return "";
        return stock_name_list.get(index);
    }
}
