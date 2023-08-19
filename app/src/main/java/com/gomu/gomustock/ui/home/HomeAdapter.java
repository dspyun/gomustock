package com.gomu.gomustock.ui.home;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Environment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.MyStat;
import com.gomu.gomustock.R;
import com.gomu.gomustock.graph.MyChart;
import com.gomu.gomustock.network.MyOpenApi;
import com.gomu.gomustock.network.MyWeb;
import com.gomu.gomustock.stockengin.BBandTest;
import com.gomu.gomustock.stockengin.PriceBox;
import com.gomu.gomustock.stockengin.RSITest;
import com.gomu.gomustock.stockengin.StockDic;
import com.gomu.gomustock.ui.format.FormatChart;
import com.gomu.gomustock.ui.format.FormatMyStock;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder>{

    private List<FormatMyStock> mystocklist;
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
    MyExcel myexcel = new MyExcel();
    private int recycler_size=0;
    String latestOpenday="";
    StockDic stockdic = new StockDic();
    ImageView btexpand,btpricerefresh;
    LineChart homeChart,todayChart;
    ImageView homeChartImg;
    TextView tvhome_money_info;
    TableLayout home_buysell_item;
    View view;
    String today_level="", period_level="";
    int imgbuf_with, imgbuf_height;

    public HomeAdapter(Activity context, List<FormatMyStock> input_mystocklist)
    {
        this.context = context;
        this.mystocklist = input_mystocklist;
        stop_flag = true;
        update_thread.start();
    }
    public HomeAdapter(Activity context)
    {
        this.context = context;
        recycler_size = recycler_size;
        stop_flag = true;
        update_thread.start();
        // getCurrentPrice();
        // 1시간마다 어제 주가를 불러온다(이건 의미 없으나 일단 구현)
        // openapi가 아니고 웹크롤링으로 구현필요
        // BackgroundThread thread = new BackgroundThread();
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    @Override
    public int getItemCount()
    {
        return mystocklist.size();
    }

    public void refresh( ) {
        notifyDataSetChanged();
    }
    public void reload_curprice( ) {
        stop_flag=true;
    }
    public void setOpenday(String openday) {
        latestOpenday = openday;
    }


    public void loadCurrentPrice() {
        // open api를 통해서 어제 종가를 가져와서
        // buy list에 넣는다
        // 이 후 adapter view를 update하면서
        // buiylist로 portfolio를 종가로 재평가해서 보여준다
        MyWeb myweb = new MyWeb();
        String price="";
        // TODO Auto-generated method stub
        for (int i = 0; i < mystocklist.size(); i++) {
            //System.out.println(Integer.toString(i) + "번째 종목번호 " + buyList.get(i).stock_no);
            price = myweb.getCurrentStockPrice(mystocklist.get(i).stock_code);
            String stokprice = price.replaceAll(",", "");
            int int_price = Integer.parseInt(stokprice);
            mystocklist.get(i).cur_price = int_price;
        }
    }

    class BackgroundThread extends Thread {
        public void run() {
            //여기서는 Toast를 비롯한 UI작업을 실행못함
            while(stop_flag) {
                try {
                    Thread.sleep(10); // 1분에 한번씩 update
                    loadCurrentPrice();
                    notice_ok();
                    stop_flag = false;
                } catch (InterruptedException e) {
                    System.out.println("인터럽트로 인한 스레드 종료.");
                    return;
                }
            }
        }
    }

    public void notice_ok() {

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10);
                    //Toast.makeText(context, "test thread", Toast.LENGTH_SHORT).show();
                    refresh();
                } catch (InterruptedException e) {
                    System.out.println("인터럽트로 인한 스레드 종료.");
                }
            }
        });
    }

    @Override
    public HomeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_list_row, parent, false);

        dialog_buy = new Dialog(context);       // Dialog 초기화
        dialog_buy.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog_buy.setContentView(R.layout.dialog_buy);             // xml 레이아웃 파일과 연결

        dialog_sell = new Dialog(context);       // Dialog 초기화
        dialog_sell.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dialog_sell.setContentView(R.layout.dialog_sell);             // xml 레이아웃 파일과 연결

//        bt_tuja.setText(show_myaccount(buyList));

        return new HomeAdapter.ViewHolder(view);
    }

    int finger_position;
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // postion을 써도 되는데 구글에서는 아래처럼 사용하는 것을 recommend 한다
        //int position = holder.getAdapterPosition();
        finger_position = position;
        FormatMyStock mystock = mystocklist.get(position);

        int now_price = mystocklist.get(position).cur_price;

        homeChart.setVisibility(VISIBLE);
        homeChartImg.setVisibility(INVISIBLE);

        String stock_name = mystock.stock_name;
        String stock_code =  mystock.stock_code;
        MyChart home_chart = new MyChart();
        //home_chart.single_float(homeChart,mystock.chartdata,period_level,false );

        //ImageView homeChartImg = view.findViewById(R.id.home_chart_img);
        /*
        String STOCKDIR = Environment.getExternalStorageDirectory().getPath() + "/gomustock/";;
        if(mystocklist.get(position).img_buf_1 != null) {
            homeChart.setVisibility(INVISIBLE);
            homeChartImg.setVisibility(VISIBLE);
            Bitmap bitmap = Bitmap.createBitmap(imgbuf_with, imgbuf_height, Bitmap.Config.ARGB_8888);
            ByteBuffer buffer = ByteBuffer.wrap(mystocklist.get(position).img_buf_1.array());
            buffer.rewind();
            bitmap.copyPixelsFromBuffer(buffer);
            homeChartImg.setImageBitmap(bitmap);

        } else {
            homeChart.setVisibility(VISIBLE);
            homeChartImg.setVisibility(INVISIBLE);
            List<FormatChart> chartlist = new ArrayList<>();
            chartlist = GetPeriodChart(stock_code, 120);;
            home_chart.multi_chart(homeChart, chartlist, "복합차트", false);
        }
        */
        homeChart.setVisibility(VISIBLE);
        homeChartImg.setVisibility(INVISIBLE);
        List<FormatChart> chartlist1 = new ArrayList<>();
        //chartlist = GetPeriodChart(stock_code, 120);
        chartlist1 = mystock.chartlist1;
        period_level = mystock.period_level;
        if(chartlist1.size() <= 0) return;
        home_chart.multi_chart(homeChart, chartlist1, period_level, false);

        MyChart day_chart = new MyChart();
        List<FormatChart> chartlist2 = new ArrayList<>();
        //chartlist1 = GetTodayChart(stock_code,1);
        chartlist2 = mystock.chartlist2;
        today_level = mystock.today_level;
        if(chartlist2.size() <= 0) return;
        day_chart.multi_chart(todayChart, chartlist2, today_level, false);

        float cur_price = mystock.cur_price;
        if(cur_price<=0) {
            int size = mystock.chartdata.size();
            cur_price = mystock.chartdata.get(size-1);
        }
        //int returnmoney = balance.getSellPrice();
        /*
        float total_buy_price = mystock.buy_price;
        int hold_quantity = mystock.quantity;
        float averprice = total_buy_price/hold_quantity;
        float estimprice = hold_quantity*cur_price;
        float profitrate = (estimprice/total_buy_price)*100-100;

        String simul_info =
                "투자액 " + String.format("%.0f",total_buy_price/10000) + "만원\n" +
                "잔량 " + Integer.toString(hold_quantity) + "\n" +
                "평단가 " + String.format("%.0f",averprice)+"\n" +
                "평가액 " + String.format("%.0f",estimprice/10000) + "만원\n" +
                "수익률 " + String.format("%.2f",profitrate) + "%" + "\n" +
                "수익액 " + String.format("%.0f",(estimprice-total_buy_price)/10000) + "만원\n";

        holder.tvbody_info.setText(simul_info);
        */


        String head_info =
                Integer.toString(position) + " " +
                stock_name + "(" + stock_code +"}"+" "+
                        "현재가 " + mystock.cur_price;
        holder.tvhead_info.setText(head_info);

        //holder.btexpand.setImageResource(R.drawable.circle_plus);
        // expandable list를 펼쳐준다
        holder.onBind(position, selectedItems);

        holder.setOnViewHolderItemClickListener(new OnViewHolderItemClickListener() {
            @Override
            public void onViewHolderItemClick() {

                String bool;

                for(int i =0;i<mystocklist.size();i++)  {
                    selectedItems.delete(i);
                }
                selectedItems.put(position, true);
                prePosition = position;
                if (prePosition != -1) notifyItemChanged(prePosition);
                notifyItemChanged(position);
                //System.out.println("event position is " + Integer.toString(position));
            }
        });

        /*
        homeChart.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(mystocklist.get(position).img_buf_1 == null) {
                    System.out.println("[view1의 크기] 가로 :" + homeChart.getWidth() + "   세로 : " + homeChart.getHeight());
                    //homeChart.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    homeChart.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    Bitmap b = Bitmap.createBitmap(homeChart.getMeasuredWidth(), homeChart.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                    imgbuf_with = homeChart.getMeasuredWidth();
                    imgbuf_height=homeChart.getMeasuredHeight();
                    Canvas c = new Canvas(b);
                    homeChart.layout(homeChart.getLeft(), homeChart.getTop(), homeChart.getRight(), homeChart.getBottom());
                    homeChart.draw(c);
                    int size = b.getByteCount();
                    mystocklist.get(position).img_buf_1 = ByteBuffer.allocate(size);
                    b.copyPixelsToBuffer(mystocklist.get(position).img_buf_1);
                }
            }
        });
        */
    }

    public void showDialog_buy(){
        EditText stock_name = dialog_buy.findViewById(R.id.stock_name);
        stock_name.setText(mystocklist.get(finger_position).stock_name);
        EditText stock_code = dialog_buy.findViewById(R.id.stock_code);

        dialog_buy.show(); // 다이얼로그 띄우기

        dialog_buy.findViewById(R.id.buyBtn).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // 원하는 기능 구현

                // dialog 화면에서 입력된 정보를 읽어온다
                EditText stock_name = dialog_buy.findViewById(R.id.stock_name);
                String name = stock_name.getText().toString();


                String stock_no = stockdic.getStockcode(name);
                if(stock_no.equals("")) {
                    Toast.makeText(context, "종목명 오류",Toast.LENGTH_SHORT).show();
                    return;
                }
                //dl_NaverPriceByday(stock_no,60);

                Date buydate = new Date();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
                String mybuydate = format.format(buydate);
                if(!latestOpenday.equals(mybuydate)) {
                    Toast.makeText(context, "오늘은 거래날짜가 아닙니다",Toast.LENGTH_SHORT).show();
                    return; // 오늘이 오픈일이 아니면 매도매수 안됨
                }

                refresh();
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
        TextView tv_name,tv_buy_price;
        TextView tvhead_info,tvbody_info;


        Button buybt, sellbt;
        //private View portfolio_list_view;
        LinearLayout home_list_item;

        int finger_position;
        public ViewHolder(View view)
        {
            super(view);

            tvhome_money_info = view.findViewById(R.id.home_money_info);

            homeChart = view.findViewById(R.id.home_chart);
            tv_name = view.findViewById(R.id.stock_name);
            homeChartImg = view.findViewById(R.id.home_chart_img);

            tvhead_info = view.findViewById(R.id.home_info_header);
            //tvbody_info = view.findViewById(R.id.home_info_body);
            todayChart = view.findViewById(R.id.today_chart);

            // 펼쳐진 부분이 클릭되면 접히게 하는 click listener
            home_list_item = view.findViewById(R.id.home_list_layout);
            home_buysell_item =  view.findViewById(R.id.home_buysell_layout);

            home_list_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onViewHolderItemClickListener.onViewHolderItemClick();
                }
            });
            /*
            buybt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "go to buy", Toast.LENGTH_SHORT).show();
                    showDialog_buy();
                    //onViewHolderItemClickListener.onViewHolderItemClick();
                }
            });
            */

        }
        public void setOnViewHolderItemClickListener(OnViewHolderItemClickListener onViewHolderItemClickListener)
        {
            this.onViewHolderItemClickListener = onViewHolderItemClickListener;
        }

        public void onBind(int position, SparseBooleanArray selectedItems) {
            home_buysell_item.setVisibility(selectedItems.get(position) ? VISIBLE : View.GONE);
            //finger_position = position;
            //changeVisibility(selectedItems.get(position));
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
                    buybt.setVisibility(isExpanded ? VISIBLE : View.GONE);

                    sellbt.getLayoutParams().height = (int) animation.getAnimatedValue();
                    sellbt.requestLayout();
                    // imageView가 실제로 사라지게하는 부분
                    sellbt.setVisibility(isExpanded ? VISIBLE : View.GONE);
                }
            });
            // Animation start
            va.start();
        }
    }

    public String show_myaccount() {

        Cache mycache = new Cache();
        int first_cache = mycache.getFirstCache();
        int remain_cache= mycache.getRemainCache();
        int estim_price=0;

        if(first_cache ==0) return "";
        int size = mystocklist.size();
        for(int i=0;i<size;i++) {
            String quantity = mystocklist.get(i).quantity;
            estim_price += Float.parseFloat(quantity)* mystocklist.get(i).cur_price;
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

    public String update_top_board() {

        float totol_invest_money=0, total_estim_money=0;
        float profit_rate=0, total_earn_money=0;

        int size = mystocklist.size();
        for(int i =0;i<size;i++) {
            String price = mystocklist.get(i).buy_price;
            String quantity = mystocklist.get(i).quantity;
            totol_invest_money += Float.parseFloat(price);
            total_estim_money += Float.parseFloat(quantity)*Float.parseFloat(price);;
        }
        profit_rate = (total_estim_money/totol_invest_money)*100-100;
        total_earn_money = total_estim_money-totol_invest_money;
        String total_money_info =
                "투자액 = " + String.format("%.0f",totol_invest_money/10000) + "만원\n" +
                "평가액 = " + String.format("%.0f",total_estim_money/10000) + "만원\n" +
                "수익률 = " + String.format("%.2f", profit_rate) +"%\n" +
                "수익액 = " + String.format("%.0f",total_earn_money/10000) + "만원\n";
        return  total_money_info;
    }

    public List<Float> getMoneyLine() {
        List<Float> sumline = new ArrayList<>();
        int days = mystocklist.get(0).chartdata.size();
        for(int k=0;k<days;k++) {
            sumline.add(0f);
        }
        int size = mystocklist.size();
        for(int i =0;i<size;i++) {
            List<Float> oneline = new ArrayList<>();
            oneline = mystocklist.get(i).chartdata;
            int quantity = Integer.parseInt(mystocklist.get(i).quantity);
            days = oneline.size();
            for(int j =0;j<days;j++) {
                float sum = sumline.get(j) + oneline.get(j)*quantity;
                sumline.set(j,sum);
            }
        }
        for(int k=0;k<days;k++) {
            sumline.set(k,sumline.get(k)/10000);
        }
        return sumline;
    }

}
