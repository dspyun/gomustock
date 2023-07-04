package com.gomu.gomustock.ui.simulation;

import android.content.Context;

import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.stockdb.BuyStockDBData;
import com.gomu.gomustock.stockdb.SellStockDBData;
import com.gomu.gomustock.stockdb.StockDic;
import com.gomu.gomustock.ui.format.FormatTestData;

import java.util.ArrayList;
import java.util.List;

public class BSManager {
    SBuyStockDB buystock_db;
    SSellStockDB sellstock_db;
    StockDic stockdic = new StockDic();
    MyExcel myexcel = new MyExcel();
    SBuyStock buystock = new SBuyStock();
    SSellStock sellstock = new SSellStock();
    List<BuyStockDBData> buystockList = new ArrayList<>();
    List<SellStockDBData> sellstockList = new ArrayList<>();

    List<BuyStockDBData> lastbuylist = new ArrayList<BuyStockDBData>();
    String STOCK_CODE;
    Context context;

    public BSManager(Context inputontext, String stock_code) {
        context = inputontext;
        STOCK_CODE = stock_code;
        buystock_db = SBuyStockDB.getInstance(context);
        sellstock_db = SSellStockDB.getInstance(context);
        buystock.reset(); // buydb를 비운다
        sellstock.reset(); // selldb를 비운다
        loadExcel2DB(); // simul file을 db에 넣고
        makeBuySelllist(); //
    }

    public List<BuyStockDBData> getBuystockList()
    {
        // 매수이력이기 때문에 차트에
        return buystockList;
    }
    public List<SellStockDBData> getSellstockList()
    {
        return sellstockList;
    }
    public List<BuyStockDBData> getLastBuystockList()
    {
        return lastbuylist;
    }
    public List<Integer> getBuyQuantityList() {
        List<Integer> quantity = new ArrayList<>();
        int size =buystockList.size();
        for(int i =0;i<size;i++) {
            quantity.add(buystockList.get(i).buy_quantity);
        }
        return quantity;
    }
    public List<Integer> getSellQuantityList() {
        List<Integer> quantity = new ArrayList<>();
        int size =sellstockList.size();
        for(int i =0;i<size;i++) {
            quantity.add(sellstockList.get(i).sell_quantity);
        }
        return quantity;
    }


    public void loadExcel2DB() {

        // 엑셀파일에서 시험용 매수주식 시그널을 읽는다
        // 이 시그널은 60개로 구성된 것이 아니고 매수,,매도 횟수만큼 데이터가 있다
        // 이것을 60개의 기간데이터로 만든다
        // 그리고 buylist와 selllist로 분리해서 각각 DB에 저장한다

        int withdrawal=0, remain_cache=0;

        String name="",code="", date="";
        int buyquan=0, sellquan=0, price=0,receipt=0;
        int buy_total_quan=0, sell_total_quan=0;
        List<FormatTestData> signallist = new ArrayList<FormatTestData>();
        // 과거>현재 순으로 매수, 매도 시그널데이터를 읽어온다.
        signallist.addAll(myexcel.readtestset(STOCK_CODE+"_testset.xls", false));

        code = STOCK_CODE;
        name = stockdic.getStockname(code); // 이부분은 시간이 걸리는 함수라서 db로 개선필요? array로 넣고 array find를 활용?
        int size = signallist.size();
        for(int i=0;i<size;i++) {
            // 시그널파일의 0번째가 가장 과거데이터이기 때문에 과거데이터부터 매수데이터를 DB에 넣는다
            // 수량, 매수가, 매수일을 읽는다.
            if(signallist.get(i).buy_quantity.equals("")) buyquan = 0;
            else buyquan = Integer.parseInt(signallist.get(i).buy_quantity);
            price = Integer.parseInt(signallist.get(i).price);
            date = signallist.get(i).date;
            // 그날의 매수현황을 db에 저장한다(수량, 가격, 매수일을 저장한다.)
            buystock.insert2db(name, code, buyquan, price, date);
            // 누적 매수애을 계산한다. 매수 후 매수액을 원금애써 빼줌
            withdrawal = price * buyquan;
            remain_cache = remain_cache - withdrawal;
            buy_total_quan = buy_total_quan + buyquan;

            // 과거>현재순으로 매도시그널 데이터를 DB에 넣는다.
            // 매도량이 0이면 그냥 넣어주고 매도량이 1주라도 있으면
            // 넣기 전에 매수량을 검사해서 매수량이 매도량보다 클때만 DB에 넣는다.
            // 가지고 있는 것이 있으야지 팔수도 있으니까.
            if (signallist.get(i).sell_quantity.equals("")) sellquan = 0;
            else sellquan = Integer.parseInt(signallist.get(i).sell_quantity);
            // 매도하기 전에 매도할 수량만큼 보유했는지 검사. 모자라면 매수량은 0으로
            if(buy_total_quan < sellquan ) sellquan = 0; // 매도시그널의 매도량이 보유량보다 적으면 매도량을 0으로.
            if(sellquan > 0) {
                // 매도할 수 있으면, 일단 매수량에서 매도량 빼주고
                buy_total_quan = buy_total_quan - sellquan;
            }
            // 그날의 매도현황을 db에 저장한다(매도시그널이 없으면 0으로, 있으면 시그널만큼 저장됨(보유량이 있을 시에만))
            sellstock.insert2db(name, code, sellquan, price, date);
            // 매도 후 매도액을 원금에 추가해줌
            receipt = sellquan * price;
            remain_cache = remain_cache + receipt;
            sell_total_quan = sell_total_quan + sellquan;
        }
        // for루프를 마치면 Excel data가 DB에 모두 덜어간 상태이다
        // DB에 들어간 순서는 과거>현재 순으로 들어갔다. 히자만 나중에 읽을때도 동일한 순서인지는 모르겠다.
        // 잔고 = 잔고 - 매수액 : 위에서 계산된 매수액이 (-)가 붙어서 오기 때문에 (+)로 처리
        SCache mycache = new SCache();
        mycache.update_cache(remain_cache);
        int test = 0;

        // 이렇게 하면 모든날짜의 매수/매도 히스토리 db(엑셀)가 만들어진다
        // 매수 매도 히스토리 db(엑셀)는 차트를 그리는데 사용된다.
        // buystockList는 매수시스널 테이블과 동일해야 한고
        // 과매도 수량은 모두 0으로 처리되어야 한다.
        // 아래 리스트에서 엑셀파일의 데이터와 비교해야 검증할 수 있다.
        buystockList = buystock_db.buystockDao().getAll();
        //buystockList = buystock_db.buystockDao().getAll();
        sellstockList = sellstock_db.sellstockDao().getAll();
        //sellstockList = sellstock_db.sellstockDao().getAll();
    }


    // buylist를 읽어서 누정매수량, 누정매수액으로 구성된 buylist를 만들어준다. > 차트 그릴 때 써야 한다.
    public List<BuyStockDBData> make_buystocklist_accumulated() {
        BuyStockDBData buystock_accumulated = new BuyStockDBData();;
        List<BuyStockDBData> buystocklist_accumulated = new ArrayList<>();

        // 엑셀2DB를 종료하고 DB를 읽어서 60일간의 buystockList를 채워놓았다.
        // buystockList는 과거>핸재 순으로 데이터가 정렬되어 있다.
        int size =  buystockList.size();

        int quantity=0, total_buy_price=0;
        for(int j=0; j< size;j++ ) {
            quantity = quantity + buystockList.get(j).buy_quantity;
            total_buy_price = total_buy_price + buystockList.get(j).buy_quantity * buystockList.get(j).buy_price;

            buystock_accumulated.buy_quantity = quantity;
            buystock_accumulated.stock_name = buystockList.get(j).stock_name;
            buystock_accumulated.stock_code = buystockList.get(j).stock_code;
            if(quantity ==0) buystock_accumulated.buy_price = 0;
            else buystock_accumulated.buy_price = total_buy_price/quantity;
            buystocklist_accumulated.add(buystock_accumulated);
        }

        return buystocklist_accumulated;
    }

    public List<SellStockDBData> make_sellstocklist_accumulated() {
        SellStockDBData sellstock_accumulated = new SellStockDBData();;
        List<SellStockDBData> sellstocklist_accumulated = new ArrayList<>();

        // 엑셀2DB를 종료하고 DB를 읽어서 60일간의 buystockList를 채워놓았다.
        // buystockList는 과거>핸재 순으로 데이터가 정렬되어 있다.
        int size =  sellstockList.size();

        int quantity=0, total_sell_price=0;
        for(int j=0; j< size;j++ ) {
            quantity = quantity + sellstockList.get(j).sell_quantity;
            total_sell_price = total_sell_price + sellstockList.get(j).sell_quantity * sellstockList.get(j).sell_price;

            sellstock_accumulated.sell_quantity = quantity;
            sellstock_accumulated.stock_name = buystockList.get(j).stock_name;
            sellstock_accumulated.stock_code = buystockList.get(j).stock_code;
            if(quantity ==0) sellstock_accumulated.sell_price = 0;
            else sellstock_accumulated.sell_price = total_sell_price/quantity;
            sellstocklist_accumulated.add(sellstock_accumulated);
        }

        return sellstocklist_accumulated;
    }


    public List<BuyStockDBData> makeBuySelllist() {



        // 누적매수량, 누정매수액이 저장된 리스트를 읽어온다.
        // 과거>핸재 순으로 저장되어 있다.
        List<BuyStockDBData> buylist = new ArrayList<BuyStockDBData>();
        buylist = make_buystocklist_accumulated();

        // 누정매도량, 누정매도액이 저장된 리스트를 읽어온다
        // 과거>현재 순으로 저정되어 있다.
        List<SellStockDBData> selllist = new ArrayList<SellStockDBData>();
        selllist = make_sellstocklist_accumulated();

        int temp=0;
        int buyprice=0, buyquantity=0, sellprice=0, sellquantity=0, diff_quantity=0;
        BuyStockDBData arraybuff;

        int size = buylist.size();
        int size1 = selllist.size();
        for(int i=0; i< size; i++ ) {

            String buystock = buylist.get(i).stock_name;
            buyquantity = buylist.get(i).buy_quantity;
            buyprice = buylist.get(i).buy_price;
            for(int j=0;j<size1;j++ ) {
                arraybuff = new BuyStockDBData();
                String sellstock = selllist.get(j).stock_name;
                sellquantity = selllist.get(j).sell_quantity;
                sellprice = selllist.get(j).sell_price;

                if (buystock.equals(sellstock)) {
                    // 잔량 및 평단가(총매수액/수량)
                    diff_quantity = buyquantity - sellquantity;
                    // buyprice는 평균단가를 구해야 한다
                    // 평균단가 = (매수총액-매도총액)/잔량
                    if (diff_quantity <= 0) {
                        buyprice = 0;
                        buyquantity = 0;
                    } else {
                        buyprice = (buyprice * buyquantity - sellprice * sellquantity) / diff_quantity;
                        buyquantity = diff_quantity;
                    }
                    arraybuff.setStockNo(buylist.get(i).stock_code);
                    arraybuff.setName(buystock);
                    arraybuff.setPrice(buyprice);
                    arraybuff.setQuantity(buyquantity);
                    //buylist.set(i, arraybuff);
                    lastbuylist.add(arraybuff);
                }
            }
        }
        // 최종 남은 매수종목 및 가격, 수량이 현재 포트폴리오 임
        // 과거>현재 순으로 저장되어 있다
        return lastbuylist;
    }
}
