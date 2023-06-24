package com.gomu.gomustock.ui.simulation;

import android.app.Activity;

import com.gomu.gomustock.ui.format.FormatTestData;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.stockdb.BuyStockDBData;
import com.gomu.gomustock.stockdb.SellStockDBData;

import java.util.ArrayList;
import java.util.List;

public class SBSManager {

    SBuyStockDB buystock_db;
    SSellStockDB sellstock_db;

    List<BuyStockDBData> buystockList = new ArrayList<>();
    List<SellStockDBData> sellstockList = new ArrayList<>();
    Activity context;
    List<BuyStockDBData> last_buylist = new ArrayList<BuyStockDBData>();
    SBuyStock buystock = new SBuyStock();
    SSellStock sellstock = new SSellStock();

    MyExcel myexcel = new MyExcel();
    public SBSManager(Activity mycontext) {
        this.context = mycontext;
        buystock_db = SBuyStockDB.getInstance(context);
        buystockList = buystock_db.buystockDao().getAll();
        sellstock_db = SSellStockDB.getInstance(context);
        sellstockList = sellstock_db.sellstockDao().getAll();
    }

    public SBSManager() {
        buystock_db = SBuyStockDB.getInstance(context);
        buystockList = buystock_db.buystockDao().getAll();
        sellstock_db = SSellStockDB.getInstance(context);
        sellstockList = sellstock_db.sellstockDao().getAll();
    }

    public BuyStockDBData getPortfolio_dummy() {
        BuyStockDBData onebuystock = new BuyStockDBData();
        onebuystock.buy_date = "20230115";
        onebuystock.stock_name = "코덱스 200";
        onebuystock.stock_code = "069500";
        onebuystock.buy_price = 0 ;
        onebuystock.buy_quantity = 0;

        return onebuystock;
    }
    public BuyStockDBData getLastBuy() {
        return last_buylist.get(0);
    }
    public List<BuyStockDBData> getBuyList() { return buystockList; }
    public List<SellStockDBData> getSellList() { return sellstockList; }

    // 추후 BuyDB + SellDB로 포트폴리오 정보를 만들어야 한다
    public List<BuyStockDBData>  makeLastBuyList() {

        // 과거>현재 순으로 저장된 최종 보유리스트를 불러온다.
        last_buylist = assemble_lastbuylist();
        int now_price=0;
        /*
        int size =  last_buylist.size();
        for(int i =0; i<size;i++) {

            // 매수한 주식을 현재가로 재평가한다
            PortfolioData estim_info = new PortfolioData();
            estim_info = estim_buystock(last_buylist.get(i), now_price);
            // 재평가한 주식정보를 화면에 보여줄 포트폴리오 리스트에 넣는다
            portfolioList.add(i, estim_info);
        }
        */
        //-----------------------------------------------------
        return last_buylist;
    }

    public ArrayList<String> extract_buystock_name() {

        ArrayList<String> oldLi = new ArrayList<String>();;
        ArrayList<String> newLi = new ArrayList<String>();

        int size =  buystockList.size();
        // buylist에서 주식명만 뽑아서 주식명 리스트를 만든다
        for(int i=0;i<size;i++) {
            oldLi.add(buystockList.get(i).stock_name);
        }

        // 주식명리스트에서 중복된 주식명을 제외한 주식명들은
        // 새로운 ArrayList에 요소를 추가
        for(String strValue : oldLi) {
            // 중복 요소가 없는 경우 요소를 추가
            if(!newLi.contains(strValue)) {
                newLi.add(strValue);
            }
        }
        return newLi;
    }

    // buylist를 읽어서 누정매수량, 누정매수액으로 구성된 buylist를 만들어준다. > 차트 그릴 때 써야 한다.
    public List<BuyStockDBData> make_buy_portfolio() {
        ArrayList<String> stocknamelist = new ArrayList<String>();
        List<BuyStockDBData> buystockList_arranged = new ArrayList<>();
        BuyStockDBData buystock_arranged;
        // 일단 종목명만 뽑아서 중복값 제거한다.
        // 시실 이래 추출 메소드는 필요없다.
        // 현재 한번에 한종목 데이터만 처리하기 때문아다.
        // 그냥 stock_code로 저리해도 된다.
        stocknamelist = extract_buystock_name();
        // 중복된 종목 액수는 모두 합쳐서 하나의 종목으로 만든다
        // 중복되지 않은 종목명으로 총수량, 총매수액을 계산해서
        // 재정렬된 buystocklist에 넣고 리턴한다

        String mystock="";
        String dbstock="";
        int size =  stocknamelist.size();
        // 엑셀2DB를 종료하고 DB를 읽어서 60일간의 buystockList를 채워놓았다.
        // buystockList는 과거>핸재 순으로 데이터가 정렬되어 있다.
        int size1 =  buystockList.size();
        for(int i=0; i< size ;i++) {
            // 아래에 선언을 하는 이유는 한 번 턴을 돌고 난다음
            // buystock_arranged 를 비워주기 위해서이다
            buystock_arranged = new BuyStockDBData();
            int quantity=0, total_buy_price=0;

            for(int j=0; j< size1;j++ ) {
                mystock = stocknamelist.get(i);
                dbstock = buystockList.get(j).stock_name;
                if(mystock.equals(dbstock)) {
                    quantity = quantity + buystockList.get(j).buy_quantity;
                    total_buy_price = total_buy_price + buystockList.get(j).buy_quantity * buystockList.get(j).buy_price;

                    buystock_arranged.buy_quantity = quantity;
                    buystock_arranged.stock_name = buystockList.get(j).stock_name;
                    buystock_arranged.stock_code = buystockList.get(j).stock_code;
                }
            }
            // 매수 평단가로 계산해야 함. 평균단가 = 총액/총수량
            if(quantity ==0) buystock_arranged.buy_price = 0;
            else buystock_arranged.buy_price = total_buy_price/quantity;
            buystockList_arranged.add(buystock_arranged);
        }
        // for 루프를 마치면
        // 60일간 누정매수량, 누적매수액 일인 데이터가
        // buystockList_arranged에 저장된다.
        return buystockList_arranged;
    }

    public List<String> extract_sellstock_name() {

        ArrayList<String> oldLi = new ArrayList<String>();;
        ArrayList<String> newLi = new ArrayList<String>();

        int size =  sellstockList.size();
        // selllist에서 주식명만 뽑아서 주식명 리스트를 만든다
        for(int i=0;i<size;i++) {
            oldLi.add(sellstockList.get(i).stock_name);
        }

        // 주식명리스트에서 중복된 주식명을 제외한 주식명들은
        // 새로운 ArrayList에 요소를 추가
        for(String strValue : oldLi) {
            // 중복 요소가 없는 경우 요소를 추가
            if(!newLi.contains(strValue)) {
                newLi.add(strValue);
            }
        }
        return newLi;
    }
    // buylist는 삼성+하이닉스+삼성+삼성+하이닉스 이런 순으로 저장되어 있다
    // 이것을 삼성+하이닉스로 재정렬하고 액수, 수량도 재조합한다
    public List<SellStockDBData> make_sell_portfolio() {
        List<String> stocknamelist = new ArrayList<String>();

        List<SellStockDBData> sellstockList_arranged = new ArrayList<>();

        // 일단 종목명만 뽑아서 중복값 제거한다.
        stocknamelist = extract_sellstock_name();

        // 중복되지 않은 종목명으로 총수량, 총매수액을 계산해서
        // 재정렬된 buystocklist에 넣고 리턴한다
        String mystock="";
        String dbstock="";
        int size =  stocknamelist.size();
        int size1 = sellstockList.size();
        for(int i=0; i< size;i++) {
            SellStockDBData sellstock_arranged = new SellStockDBData();
            int quantity=0, total_sell_price=0;
            sellstock_arranged.stock_code = sellstockList.get(i).stock_code;
            for(int j=0;j < size1; j++ ) {
                mystock = stocknamelist.get(i);
                dbstock = sellstockList.get(j).stock_name;
                if(mystock.equals(dbstock) ) {
                    quantity = quantity + sellstockList.get(j).sell_quantity;
                    total_sell_price = total_sell_price + sellstockList.get(j).sell_quantity * sellstockList.get(j).sell_price;

                    sellstock_arranged.sell_price = total_sell_price;
                    sellstock_arranged.sell_quantity = quantity;
                    sellstock_arranged.stock_name = sellstockList.get(j).stock_name;
                }
            }
            if(quantity == 0 ) sellstock_arranged.sell_price = 0;
            else sellstock_arranged.sell_price = total_sell_price/quantity;
            sellstockList_arranged.add(sellstock_arranged);
        }
        return sellstockList_arranged;
    }

    // 보유최종 buylist를 만든다
    // 총buylist - 총selllist를 하면 보유 buy list가 나오고
    // 보유 buy list로 화면에 뿌려줄 포트폴리오 리스트를 만들면 된
    public List<BuyStockDBData> assemble_lastbuylist() {

        List<BuyStockDBData> last_buylist = new ArrayList<BuyStockDBData>();

        // 누적매수량, 누정매수액이 저장된 리스트를 읽어온다.
        // 과거>핸재 순으로 저장되어 있다.
        List<BuyStockDBData> buylist = new ArrayList<BuyStockDBData>();
        buylist = make_buy_portfolio();

        // 누정매도량, 누정매도액이 저장된 리스트를 읽어온다
        // 과거>현재 순으로 저정되어 있다.
        List<SellStockDBData> selllist = new ArrayList<SellStockDBData>();
        selllist = make_sell_portfolio();

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
                    last_buylist.add(arraybuff);
                }
            }
        }
        // 최종 남은 매수종목 및 가격, 수량이 현재 포트폴리오 임
        // 과거>현재 순으로 저장되어 있다
        return last_buylist;
    }


    public void loadExcel2DB(String stock_code) {

        // 엑셀파일에서 시험용 매수주식 이력을 읽어들인다.


        int withdrawal=0, remain_cache=0;

        String name="", code="", date="";
        int buyquan=0, sellquan=0, price=0,receipt=0;
        int buy_total_quan=0, sell_total_quan=0;
        List<FormatTestData> signallist = new ArrayList<FormatTestData>();
        //시뮬레이션용 매수, 매도 시그널을 읽는다.
        // 과거>현재 순으로 데이터가 정렬되었는데 DB로 넣으면 어떻게 정렬되는지 모르겠다. 일단 넣는다.
        signallist.addAll(myexcel.readtestset(stock_code+"_testset.xls", false));

        code = stock_code;
        name = myexcel.find_stockname(code); // 이부분은 시간이 걸리는 함수라서 db로 개선필요? array로 넣고 array find를 활용?
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
}
