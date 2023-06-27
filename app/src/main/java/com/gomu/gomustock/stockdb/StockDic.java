package com.gomu.gomustock.stockdb;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import com.gomu.gomustock.MyExcel;

import java.util.ArrayList;
import java.util.List;

public class StockDic {

    private StockDicDB stockdic_db;

    public List<StockDicDBData> diclist = new ArrayList<>();

    public StockDic() {
        stockdic_db = StockDicDB.getInstance(context);
        diclist = stockdic_db.dicDao().getAll();
        if(diclist.size() == 0) {
            MyExcel myexcel = new MyExcel();
            List<List<String>> dicdata = new ArrayList<List<String>>();
            dicdata = myexcel.readStockDic();
            makeDic(dicdata.get(0), dicdata.get(1));
        }
    }

    public void makeDic(List<String> stock_code, List<String> stock_name) {
        int size = stock_code.size();
        for(int i =0;i<size;i++) {
            StockDicDBData onedic = new StockDicDBData();
            onedic.stock_code = stock_code.get(i);
            onedic.stock_name = stock_name.get(i);
            diclist.add(onedic);
        }
        stockdic_db.dicDao().insertAll(diclist);
    }

    public String getStockname(String stock_code) {
        StockDicDBData onedic = new StockDicDBData();
        onedic = stockdic_db.dicDao().getStockname(stock_code);
        if(onedic==null) return "";
        return onedic.stock_name;
    }

    public String getStockcode(String stock_name) {
        StockDicDBData onedic = new StockDicDBData();
        onedic = stockdic_db.dicDao().getStockcode(stock_name);
        if(onedic==null) return "";
        return onedic.stock_code;
    }
}
