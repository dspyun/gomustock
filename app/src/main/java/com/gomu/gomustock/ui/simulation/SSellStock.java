package com.gomu.gomustock.ui.simulation;

import static com.gun0912.tedpermission.provider.TedPermissionProvider.context;

import com.gomu.gomustock.FormatTestData;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.portfolio.SellStockDBData;

import java.util.ArrayList;
import java.util.List;

public class SSellStock {

    private final SSellStockDB sellstock_db;

    List<SellStockDBData> sellstockList;

    public SSellStock () {
        sellstock_db = SSellStockDB.getInstance(context);
        sellstockList = sellstock_db.sellstockDao().getAll();
    }
    public void sell_insert2db(String name, String stock_code, int quantity, int price, String date) {

        SellStockDBData first_sellstock_data= new SellStockDBData();

        first_sellstock_data.stock_code = stock_code;
        first_sellstock_data.sell_date = date;
        first_sellstock_data.stock_name = name;
        first_sellstock_data.sell_quantity = quantity;
        first_sellstock_data.sell_price = price;
        sellstock_db.sellstockDao().insert(first_sellstock_data);
    }

    public void add(String stock_code) {

        // 매수정보를 활용해서 DB에 매수정보를 저장한다
        // 이 부분은 임시로 여기에 만든 것임
        // 저장하는 인터페이스는 별도로 개발 필요함
        // 만약 DB에 저장된 데이터가 있으면 시험용 데이터를 추가 저장하지 않는다
        // 매도했으므로 매도액은 통장에 집어 넣는다
        int receipt=0, remain_cache=0;

        String name="", code="", date="";
        int quan=0, price=0;
        List<FormatTestData> simSellist = new ArrayList<FormatTestData>();
        MyExcel myexcel = new MyExcel();
        simSellist.addAll(myexcel.readtestsell(stock_code+"_testset.xls", false));
        code = stock_code;
        name = myexcel.find_stockname(code);
        for(int i=0;i<simSellist.size();i++) {

            if(simSellist.get(i).sell_quantity.equals("")) quan = 0;
            else quan = Integer.parseInt(simSellist.get(i).sell_quantity);
            price = Integer.parseInt(simSellist.get(i).price);
            date = simSellist.get(i).date;
            sell_insert2db(name, code, quan, price, date);

            // 위에서 매도한 애도액이 통장 입금액 receipt
            receipt = quan*price;
            // 잔고에 입금액을 더해준다
            remain_cache = remain_cache + receipt;
            // 잔고를 저장한다
        }
        // 잔고 = 잔고 + 매도액
        SCache mycache = new SCache();
        mycache.update_cache(remain_cache);
        int test = 0;
    }

    public List<String> getSellStockList() {

        List<String> oldLi = new ArrayList<String>();;
        List<String> newLi = new ArrayList<String>();

        // selllist에서 주식명만 뽑아서 주식명 리스트를 만든다
        for(int i=0;i<sellstockList.size();i++) {
            oldLi.add(sellstockList.get(i).stock_name);
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
}
