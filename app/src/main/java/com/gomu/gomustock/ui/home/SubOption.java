package com.gomu.gomustock.ui.home;

import com.gomu.gomustock.stockdb.BuyStockDBData;
import com.gomu.gomustock.stockdb.SellStockDBData;

import java.io.Serializable;
import java.util.List;

public class SubOption implements Serializable {
    // 직렬화 객체 신규생성시 uid는 변경해줘야 한다
    private static final long serialVersionUID = 1L;
    String phone;
    String addr;

    List<BuyStockDBData> buylist;
    List<SellStockDBData> selllist;

    public SubOption(){

    }
    public SubOption(String phone, String addr){
        this.phone = phone;
        this.addr = addr;
    }

    public SubOption(List<BuyStockDBData> mybuylist, List<SellStockDBData> myselllist){
        this.buylist = mybuylist;
        this.selllist = myselllist;
    }
    public List<BuyStockDBData> getBuyList() {
        return buylist;
    }
    public List<SellStockDBData> getSellList() {
        return selllist;
    }
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
}



