package com.gomu.gomustock.stockengin;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.stockdb.StockDicDB;
import com.gomu.gomustock.stockdb.StockDicDBData;

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
            makeDic(dicdata.get(0), dicdata.get(1), dicdata.get(2));
        }
    }

    public void makeDic(List<String> stock_code, List<String> stock_name, List<String> market) {
        int size = stock_code.size();
        for(int i =0;i<size;i++) {
            StockDicDBData onedic = new StockDicDBData();
            onedic.stock_code = stock_code.get(i);
            onedic.stock_name = stock_name.get(i);
            onedic.market = market.get(i);
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

    public String getMarket(String stock_code) {
        // stock code를 포함하고 있는 row를 찾아서
        // onedic으로 받은 다음, market값을 리턴한다
        StockDicDBData onedic = new StockDicDBData();
        onedic = stockdic_db.dicDao().getStockname(stock_code);
        if(onedic==null) return "";
        return onedic.market;
    }

    public void reMakeDic(List<String> stock_code, List<String> stock_name) {
        stockdic_db = StockDicDB.getInstance(context);
        diclist = stockdic_db.dicDao().getAll();
        diclist.clear();
        MyExcel myexcel = new MyExcel();
        List<List<String>> dicdata = new ArrayList<List<String>>();
        dicdata = myexcel.readStockDic();
        makeDic(dicdata.get(0), dicdata.get(1), dicdata.get(2));
    }

    public boolean checkKRStock(String stock_code) {
        // 숫자 스트링이면 true, 문자가 있으면 false를 반환한다.
        // 즉 한국주식이면 true, 외국주식이면 false 반환
        boolean isNumeric =  stock_code.matches("[+-]?\\d*(\\.\\d+)?");
        return isNumeric;
    }
}
