package com.gomu.gomustock.ui.dashboard;

import java.io.Serializable;

public class BoardSubOption implements Serializable {
    private static final long serialVersionUID = 1L;

    String layout;
    String stock_code;
    String stock_name;
    String region;
    String basic_info;

    public BoardSubOption(){

    }
    public BoardSubOption(String layout, String stockcode, String stockname, String basicinfo){

        this.layout = layout;
        this.stock_code = stockcode;
        this.stock_name = stockname;
        this.basic_info = basicinfo;
    }
    public BoardSubOption(String layout, String stockcode, String stockname){
        this.layout = layout;
        this.stock_code = stockcode;
        this.stock_name = stockname;
    }
    public BoardSubOption(String layout, String region){
        this.layout = layout;
        this.region = region;
    }

    public String getStockname() {
        return stock_name;
    }
    public String getLayout() { return layout; }
    public String getStockcode() { return stock_code; }
    public String getRegion() { return region; }
    public String getStockinfo() { return basic_info; }

}
