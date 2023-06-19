package com.gomu.gomustock.portfolio;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import com.gomu.gomustock.FormatTestData;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.ui.simulation.SBuyStockDB;

import java.util.ArrayList;
import java.util.List;

public class BuyStock {

    private BuyStockDB buystock_db;

    public List<BuyStockDBData> buystockList = new ArrayList<>();

    public BuyStock() {
        buystock_db = BuyStockDB.getInstance(context);
        buystockList = buystock_db.buystockDao().getAll();
    }


    public void insert_stock2buydb(String name, String stock_code, int quantity, int price, String date) {
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

        int withdrawal=0, remain_cache=0;

        String name="", code="", date="";
        int quan=0, price=0;
        List<FormatTestData> tdlist = new ArrayList<FormatTestData>();

        MyExcel myexcel = new MyExcel();
        tdlist.addAll(myexcel.readtestbuy(stock_code+"_testset.xls", false));
        code = stock_code;
        name = myexcel.find_stockname(code);
        for(int i=0;i<tdlist.size();i++) {

            if(tdlist.get(i).buy_quantity.equals("")) quan = 0;
            else quan = Integer.parseInt(tdlist.get(i).buy_quantity);
            price = Integer.parseInt(tdlist.get(i).price);
            date = tdlist.get(i).date;
            // db에 저장한다
            insert_stock2buydb(name, code, quan, price, date);

            withdrawal = price * quan;
            // 통장 잔고를 불러온다
            // 잔고에서 차감액을 뺀다
            remain_cache = remain_cache - withdrawal;
        }
        // 잔고 = 잔고 - 매수액 : 위에서 계산된 매수액이 (-)가 붙어서 오기 때문에 (+)로 처리
        Cache mycache = new Cache();
        mycache.update_cache(remain_cache);
        int test = 0;
    }

    public List<String> getBuyCodeList() {

        List<String> oldLi = new ArrayList<String>();;
        List<String> newLi = new ArrayList<String>();

        // selllist에서 주식코드만 뽑아서 주식코드 리스트를 만든다
        for(int i=0;i<buystockList.size();i++) {
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
    public void reset() {
        buystock_db = BuyStockDB.getInstance(context);
        buystockList = buystock_db.buystockDao().getAll();
        buystock_db.buystockDao().reset(buystockList);
    }
}
