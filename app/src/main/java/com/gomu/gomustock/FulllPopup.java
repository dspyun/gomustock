package com.gomu.gomustock;

import static android.view.View.TEXT_ALIGNMENT_VIEW_START;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gomu.gomustock.ui.format.FormatStockInfo;

public class FulllPopup extends AppCompatActivity {
    FullPopup_Option popupoption;
    FormatStockInfo basic_info = new FormatStockInfo();

    TextView mytext, stock_news, company_info, stock_stat;
    String SHORT_NEWS;
    String COMPANY_INFO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_popup);

        Intent intent = getIntent(); /*데이터 수신*/
        popupoption = (FullPopup_Option) intent.getSerializableExtra("class"); /*클래스*/

        mytext = findViewById(R.id.fullpopup_list);

        String contents = popupoption.getContents();
        String type = popupoption.getContentsType();
        switch(type) {
            case "news":
                mytext.setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
                mytext.setTextSize(14);
                mytext.setText(contents);
                break;
            default:
                mytext.setText(contents);
                break;
        }
    }
}