package com.gomu.gomustock.ui.simulation;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import com.gomu.gomustock.stockdb.StockDic;
import com.gomu.gomustock.ui.format.FormatTestData;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.stockdb.BuyStockDBData;

import java.util.ArrayList;
import java.util.List;

public class SBuyStock {
    SBuyStockDB buystock_db;

    List<BuyStockDBData> buystockList;
    MyExcel myexcel = new MyExcel();
    StockDic stockdic = new StockDic();
    public SBuyStock() {
        buystock_db = SBuyStockDB.getInstance(context);
        buystockList = buystock_db.buystockDao().getAll();
    }
    public void insert2db(String name, String stock_code, int quantity, int price, String date) {
        //buystock_db = BuyStockDB.getInstance(context);
        BuyStockDBData first_buystock_data= new BuyStockDBData();

        first_buystock_data.stock_code = stock_code;
        first_buystock_data.buy_date = date;
        first_buystock_data.stock_name = name;
        first_buystock_data.buy_quantity = quantity;
        first_buystock_data.buy_price = price;
        buystock_db.buystockDao().insert(first_buystock_data);
        buystockList = buystock_db.buystockDao().getAll();
        //buystock_db.close();
    }
    public void insert2db(BuyStockDBData onebuy) {
        //buystock_db = BuyStockDB.getInstance(context);
        buystock_db.buystockDao().insert(onebuy);
        buystockList = buystock_db.buystockDao().getAll();
        //buystock_db.close();
    }

    public void add(String stock_code) {

        int withdrawal, remain_cache=0;

        String name, code, date;
        int quan, price;
        List<FormatTestData> simBuylist = new ArrayList<FormatTestData>();


        simBuylist.addAll(myexcel.readtestbuy(stock_code+"_testset.xls", false));
        code = stock_code;
        name = stockdic.getStockname(code);
        int size = simBuylist.size();
        for(int i=0;i<size;i++) {

            if(simBuylist.get(i).buy_quantity.equals("")) quan = 0;
            else quan = Integer.parseInt(simBuylist.get(i).buy_quantity);
            price = Integer.parseInt(simBuylist.get(i).price);
            date = simBuylist.get(i).date;
            // db에 저장한다
            insert2db(name, code, quan, price, date);
            withdrawal = price * quan;
            remain_cache = remain_cache - withdrawal;
        }
        // 잔고 = 잔고 - 매수액 : 위에서 계산된 매수액이 (-)가 붙어서 오기 때문에 (+)로 처리
        SCache mycache = new SCache();
        mycache.update_cache(remain_cache);
        buystockList = buystock_db.buystockDao().getAll();
        int test = 0;
    }

    public void reset() {
        buystock_db = SBuyStockDB.getInstance(context);
        buystockList = buystock_db.buystockDao().getAll();
        buystock_db.buystockDao().reset(buystockList);
    }

    public List<String> getBuyCodeList() {

        List<String> oldLi = new ArrayList<String>();;
        List<String> newLi = new ArrayList<String>();

        // selllist에서 주식코드만 뽑아서 주식코드 리스트를 만든다
        int size = buystockList.size();
        for(int i=0;i<size;i++) {
            oldLi.add(buystockList.get(i).stock_code);
        }

        // 주식명리스트에서 중복된 주식명을 제외한 주시명들은
        // 새로운 ArrayList에 요소를 추가
        for(String strValue : oldLi) {
            // 중복 요소가 없는 경우 요소를 추가
            if(!newLi.contains(strValue)) {
                newLi.add(strValue);
            }
        }
        return newLi;
    }
    public List<BuyStockDBData> getBuyStockDataList() {
        return buystockList;
    }

    public boolean checksamestock(String stock_code) {
        List<String> codelist = new ArrayList<>();
        codelist = getBuyCodeList();
        boolean result = codelist.contains(stock_code);
        return result;
    }
}
