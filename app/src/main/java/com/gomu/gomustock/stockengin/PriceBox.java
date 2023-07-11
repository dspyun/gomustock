package com.gomu.gomustock.stockengin;


import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.MyStat;

import java.util.ArrayList;
import java.util.List;

public class PriceBox {

    String STOCK_CODE;
    int ONEYEAR = -1;
    List<Float> CLOSEPRICE = new ArrayList<>();
    List<Float> HIGHPRICE = new ArrayList<>();
    List<Float> LOWPRICE = new ArrayList<>();

    List<String> DATE = new ArrayList<>();

    public PriceBox(String stock_code) {
        STOCK_CODE = stock_code;
        loadClose();
        loadHigh();
        loadLow();
        loadDate();
    }
    public List<Float> getHigh() {
        return HIGHPRICE;
    }
    public List<Float> getLow() {
        return LOWPRICE;
    }
    public List<Float> getClose() {
        return CLOSEPRICE;
    }
    public List<String> getDate() {
        return DATE;
    }

    void loadClose() {
        List<String> closeprice = new ArrayList<>();
        MyExcel myexcel = new MyExcel();
        MyStat mystat = new MyStat();
        closeprice = myexcel.read_ohlcv(STOCK_CODE,"CLOSE",ONEYEAR,false);
        CLOSEPRICE = mystat.string2float(closeprice,1);
    }

    void loadDate() {
        MyExcel myexcel = new MyExcel();
        DATE = myexcel.read_ohlcv(STOCK_CODE,"DATE",ONEYEAR,false);
    }

    void loadHigh() {
        List<String> closeprice = new ArrayList<>();
        MyExcel myexcel = new MyExcel();
        MyStat mystat = new MyStat();
        closeprice = myexcel.read_ohlcv(STOCK_CODE,"HIGH",ONEYEAR,false);
        HIGHPRICE = mystat.string2float(closeprice,1);
    }

    void loadLow() {
        List<String> closeprice = new ArrayList<>();
        MyExcel myexcel = new MyExcel();
        MyStat mystat = new MyStat();
        closeprice = myexcel.read_ohlcv(STOCK_CODE,"LOW",ONEYEAR,false);
        LOWPRICE = mystat.string2float(closeprice,1);
    }
}
