package com.gomu.gomustock.ui.home;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.gomu.gomustock.R;
import com.gomu.gomustock.portfolio.BuyStockDBData;
import com.gomu.gomustock.portfolio.SellStockDBData;

import java.util.List;

public class SubActivity extends AppCompatActivity {

    TextView tx1, tx2, tx3, tx4, tx5;
    List<BuyStockDBData> mybuylist;
    List<SellStockDBData> myselllist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        tx1 = (TextView)findViewById(R.id.textView1); /*TextView선언*/
        tx2 = (TextView)findViewById(R.id.textView2);
        tx3 = (TextView)findViewById(R.id.textView3);
        tx4 = (TextView)findViewById(R.id.textView4);
        tx5 = (TextView)findViewById(R.id.textView5);

        Intent intent = getIntent(); /*데이터 수신*/

        //show_example_data(intent);

        SubOption suboption = (SubOption)intent.getSerializableExtra("class"); /*클래스*/
        mybuylist = suboption.getBuyList();
        myselllist = suboption.getSellList();
        String selllist = make_selllist_data(myselllist);
        String buylist = make_buylist_data(mybuylist);
        tx1.setTextSize(25);
        tx2.setTextSize(25);
        tx1.setText(buylist);
        tx2.setText(selllist);
    }

    public String make_selllist_data(List<SellStockDBData> selllist) {
        String show_selllist="매도리스트\n";
        SellStockDBData unit_selllist;

        for(int i = 0;i<selllist.size();i++) {
            unit_selllist = selllist.get(i);
            show_selllist += unit_selllist.sell_date + " ";
            show_selllist += unit_selllist.stock_name + " ";
            show_selllist += Integer.toString(unit_selllist.sell_price) + " ";
            show_selllist += Integer.toString(unit_selllist.sell_quantity) + "\n";
        }
        return show_selllist;
    }

    public String make_buylist_data(List<BuyStockDBData> buylist) {
        String show_buylist="매수리스트\n";
        BuyStockDBData unit_buylist;

        for(int i = 0;i<buylist.size();i++) {
            unit_buylist = buylist.get(i);
            show_buylist += unit_buylist.buy_date + " ";
            show_buylist += unit_buylist.stock_name + " ";
            show_buylist += Integer.toString(unit_buylist.buy_price) + " ";
            show_buylist += Integer.toString(unit_buylist.buy_quantity) + "\n";
        }
        return show_buylist;
    }

    public void show_example_data(Intent intent) {
        String name = intent.getExtras().getString("name"); /*String형*/
        tx1.setText(name);

        int age = intent.getExtras().getInt("age"); /*int형*/
        tx2.setText(String.valueOf(age));

        String array[] = intent.getExtras().getStringArray("array"); /*배열*/
        String add_array="";
        for(int i=0;i<array.length;i++){
            add_array += array[i]+",";
        }
        tx3.setText(add_array);
    }
}