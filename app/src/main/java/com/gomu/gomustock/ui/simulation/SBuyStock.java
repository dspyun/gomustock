package com.gomu.gomustock.ui.simulation;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import com.gomu.gomustock.FormatTestData;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.portfolio.BuyStockDBData;

import java.util.ArrayList;
import java.util.List;

public class SBuyStock {
    SBuyStockDB buystock_db;

    List<BuyStockDBData> buystockList;
    MyExcel myexcel = new MyExcel();
    public SBuyStock() {
        buystock_db = SBuyStockDB.getInstance(context);
        buystockList = buystock_db.buystockDao().getAll();
    }
    public void buy_insert2db(String name, String stock_code, int quantity, int price, String date) {
        //buystock_db = BuyStockDB.getInstance(context);
        BuyStockDBData first_buystock_data= new BuyStockDBData();

        first_buystock_data.stock_code = stock_code;
        first_buystock_data.buy_date = date;
        first_buystock_data.stock_name = name;
        first_buystock_data.buy_quantity = quantity;
        first_buystock_data.buy_price = price;
        buystock_db.buystockDao().insert(first_buystock_data);
        //buystock_db.close();
    }

    public void add(String stock_code) {

        int withdrawal, remain_cache=0;

        String name, code, date;
        int quan, price;
        List<FormatTestData> simBuylist = new ArrayList<FormatTestData>();


        simBuylist.addAll(myexcel.readtestbuy(stock_code+"_testset.xls", false));
        code = stock_code;
        name = myexcel.find_stockname(code);
        int size = simBuylist.size();
        for(int i=0;i<size;i++) {

            if(simBuylist.get(i).buy_quantity.equals("")) quan = 0;
            else quan = Integer.parseInt(simBuylist.get(i).buy_quantity);
            price = Integer.parseInt(simBuylist.get(i).price);
            date = simBuylist.get(i).date;
            // db에 저장한다
            buy_insert2db(name, code, quan, price, date);
            withdrawal = price * quan;
            remain_cache = remain_cache - withdrawal;
        }
        // 잔고 = 잔고 - 매수액 : 위에서 계산된 매수액이 (-)가 붙어서 오기 때문에 (+)로 처리
        SCache mycache = new SCache();
        mycache.update_cache(remain_cache);
        int test = 0;
    }

    public void reset() {
        buystock_db = SBuyStockDB.getInstance(context);
        buystockList = buystock_db.buystockDao().getAll();
        buystock_db.buystockDao().reset(buystockList);
    }
}
