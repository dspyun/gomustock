package com.gomu.gomustock.stockengin;

import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.ui.format.FormatTestData;

import java.util.ArrayList;
import java.util.List;

public class TestBox {

    String STOCK_CODE;
    int ONEYEAR = -1;
    List<String> DATE = new ArrayList<>();
    List<Float> CLOSE = new ArrayList<>();
    List<Integer> BUY = new ArrayList<>();
    List<Integer> SELL = new ArrayList<>();


    public TestBox(String stock_code) {
        STOCK_CODE = stock_code;
        MyExcel myexcel = new MyExcel();
        FormatTestData preonetest = new FormatTestData();
        List<FormatTestData> TESTDATA = new ArrayList<>();
        TESTDATA = myexcel.readall_testdata(STOCK_CODE,false);
        int size = TESTDATA.size();
        for(int i=0;i<size;i++) {
            FormatTestData onetest = TESTDATA.get(i);
            if(nullcheck(onetest)) onetest = preonetest;

            DATE.add(TESTDATA.get(i).date);
            CLOSE.add(Float.parseFloat(TESTDATA.get(i).price));
            BUY.add(Integer.parseInt(TESTDATA.get(i).buy_quantity));
            SELL.add(Integer.parseInt(TESTDATA.get(i).sell_quantity));
            preonetest = onetest;
        }
    }

    public List<Float> getClose() {
        return CLOSE;
    }
    public List<String> getDate() {
        return DATE;
    }
    public List<Integer> getBuy() {
        return BUY;
    }
    public List<Integer> getSell() {
        return SELL;
    }

    boolean nullcheck(FormatTestData data) {
        if(data.price.equals("null")) return true;
        return false;
    }
}
