package com.gomu.gomustock.ui.dashboard;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gomu.gomustock.R;
import com.gomu.gomustock.network.MyWeb;
import com.gomu.gomustock.ui.format.FormatStockInfo;

public class BoardInfoActivity extends AppCompatActivity {
    BoardSubOption suboption;
    FormatStockInfo basic_info = new FormatStockInfo();

    TextView mytext, stock_news, company_info;
    String SHORT_NEWS;
    String COMPANY_INFO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.board_sub_activity);

        Intent intent = getIntent(); /*데이터 수신*/
        suboption = (BoardSubOption) intent.getSerializableExtra("class"); /*클래스*/

        setContentView(R.layout.board_sub_info);

        mytext = findViewById(R.id.bd_info_text);
        stock_news = findViewById(R.id.bd_news_text);
        company_info = findViewById(R.id.bd_company_info);

        String stock_name = suboption.getStockname();
        String stock_code = suboption.getStockcode();
        String stock_info = suboption.getStockinfo();
        String information = stock_name + " " + stock_code + "\n" +
                stock_info + "\n";
        mytext.setText(information);

        dl_shortnews(stock_code);
    }

    public void dl_shortnews(String stock_code) {
        MyWeb myweb = new MyWeb();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                SHORT_NEWS = myweb.getNaverStockNews(stock_code);
                COMPANY_INFO = myweb.getNaverCompanyInfo(stock_code);
                notice_ok();
            }
        }).start();
    }

    void notice_ok() {
        Log.d(TAG, "changeButtonText myLooper() " + Looper.myLooper());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1L); // 잠시라도 정지해야 함
                    //Toast.makeText(context, "home fragment", Toast.LENGTH_SHORT).show();
                    stock_news.setText(SHORT_NEWS);
                    company_info.setText(COMPANY_INFO);
                } catch (Exception e) {
                    System.out.println("인터럽트로 인한 스레드 종료.");
                    return;
                }
            }
        });
    }




}