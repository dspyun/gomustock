package com.gomu.gomustock.ui.format;

import java.util.ArrayList;
import java.util.List;

public class FormatMyStock {
    // file format ------------------------
    public String stock_code;
    public String stock_name;
    public String quantity;
    public String buy_price;
    public String memo01;
    // ux buffer ------------------------

    public int cur_price;
    public List<Float> chartdata = new ArrayList<>();

    public List<FormatChart> chartlist1;
    public List<FormatChart> chartlist2;
    public String today_level;
    public String period_level;

    public String expect_profit;

    public void setheader() {
        stock_code="종목코드";
        stock_name="종목명";
        quantity="매수량";
        buy_price="매수가";
        memo01="메모";
    }
}
