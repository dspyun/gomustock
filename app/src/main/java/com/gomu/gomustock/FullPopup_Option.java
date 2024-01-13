
package com.gomu.gomustock;

import java.io.Serializable;

public class FullPopup_Option implements Serializable {
    private static final long serialVersionUID = 1L;

    String layout;
    String stock_code;
    String stock_name;
    String region;
    String basic_info;
    String stat_info;
    String filelist;

    public FullPopup_Option(){

    }
    public FullPopup_Option(String layout, String stockcode, String stockname, String basicinfo, String statinfo){

        this.layout = layout;
        this.stock_code = stockcode;
        this.stock_name = stockname;
        this.basic_info = basicinfo;
        this.stat_info = statinfo;
    }

    public FullPopup_Option(String layout, String stockcode, String stockname, String basicinfo){

        this.layout = layout;
        this.stock_code = stockcode;
        this.stock_name = stockname;
        this.basic_info = basicinfo;
    }
    public FullPopup_Option(String layout, String stockcode, String stockname){
        this.layout = layout;
        this.stock_code = stockcode;
        this.stock_name = stockname;
    }
    public FullPopup_Option(String layout, String region){
        this.layout = layout;
        this.region = region;
    }

    public FullPopup_Option( String filelist){

        this.filelist = filelist;
    }

    public String getStockname() {
        return stock_name;
    }
    public String getLayout() { return layout; }
    public String getStockcode() { return stock_code; }
    public String getRegion() { return region; }
    public String getStockinfo() { return basic_info; }
    public String getStatinfo() { return stat_info; }
    public String getFilelist() { return filelist; }
}