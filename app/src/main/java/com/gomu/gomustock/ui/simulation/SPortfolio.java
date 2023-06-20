package com.gomu.gomustock.ui.simulation;

import android.app.Activity;

import com.gomu.gomustock.FormatTestData;
import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.portfolio.BuyStockDBData;
import com.gomu.gomustock.portfolio.PortfolioData;
import com.gomu.gomustock.portfolio.SellStockDBData;

import java.util.ArrayList;
import java.util.List;

public class SPortfolio {
    SBuyStockDB buystock_db;
    SSellStockDB sellstock_db;

    List<PortfolioData> portfolioList = new ArrayList<>();
    List<BuyStockDBData> buystockList = new ArrayList<>();
    List<SellStockDBData> sellstockList = new ArrayList<>();
    Activity context;
    List<BuyStockDBData> last_buylist = new ArrayList<BuyStockDBData>();
    SBuyStock buystock = new SBuyStock();
    SSellStock sellstock = new SSellStock();
    public SPortfolio(Activity mycontext) {
        this.context = mycontext;
        buystock_db = SBuyStockDB.getInstance(context);
        buystockList = buystock_db.buystockDao().getAll();
        sellstock_db = SSellStockDB.getInstance(context);
        sellstockList = sellstock_db.sellstockDao().getAll();
    }

    public SPortfolio() {
        buystock_db = SBuyStockDB.getInstance(context);
        buystockList = buystock_db.buystockDao().getAll();
        sellstock_db = SSellStockDB.getInstance(context);
        sellstockList = sellstock_db.sellstockDao().getAll();
    }

    public BuyStockDBData getPortfolio_dummy() {
        BuyStockDBData onebuystock = new BuyStockDBData();
        onebuystock.buy_date = "20230115";
        onebuystock.stock_name = "삼성전자";
        onebuystock.stock_code = "005930";
        onebuystock.buy_price = 0 ;
        onebuystock.buy_quantity = 0;

        return onebuystock;
    }
    public BuyStockDBData getPortfolio() {
        return last_buylist.get(0);
    }
    public List<BuyStockDBData> getBuyList() { return buystockList; }
    public List<SellStockDBData> getSellList() { return sellstockList; }

    // 추후 BuyDB + SellDB로 포트폴리오 정보를 만들어야 한다
    public List<PortfolioData> loadDB2Portfolio() {

        last_buylist = assemble_lastbuylist();
        int now_price=0;

        int size =  last_buylist.size();
        for(int i =0; i<size;i++) {
            /*
            String stock_name = last_buylist.get(i).stock_name;
            if(stock_name.equals("삼성전자")) now_price = 65000;
            else if(stock_name.equals("SK하이닉스")) now_price = 95000;
            else now_price = 65000;
             */
            // 매수한 주식을 현재가로 재평가한다
            PortfolioData estim_info = new PortfolioData();
            estim_info = estim_buystock(last_buylist.get(i), now_price);
            // 재평가한 주식정보를 화면에 보여줄 포트폴리오 리스트에 넣는다
            portfolioList.add(i, estim_info);
        }
        //-----------------------------------------------------
        return portfolioList;
    }
    public PortfolioData estim_buystock(BuyStockDBData buystock, int cur_price) {
        String stock_name; // 종목명
        int estim_profit, estim_price; // 평가손익
        int hold_quantity,unit_price,ave_price;
        double profit_rate;
        PortfolioData screen_info = new PortfolioData();

        unit_price = buystock.getPrice();
        hold_quantity = buystock.getQuantity();

        estim_profit = (cur_price - unit_price) * hold_quantity;
        estim_price = cur_price * hold_quantity;
        profit_rate = ((cur_price*1.0)/(unit_price*1.0)-1)*100;
        ave_price = unit_price;

        screen_info.transaction_type = "buy";
        screen_info.stock_name = buystock.stock_name;
        screen_info.estim_profit = estim_profit;
        screen_info.estim_price = estim_price;
        screen_info.cur_price = cur_price;
        screen_info.hold_quantity = hold_quantity;
        screen_info.profit_rate = profit_rate;
        screen_info.buy_price = unit_price*hold_quantity;
        screen_info.ave_price = ave_price;
        //Toast.makeText(context, Double.toString(profit_rate), Toast.LENGTH_SHORT).show();
        return screen_info;
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

    // buylist는 삼성+하이닉스+삼성+삼성+하이닉스 이런 순으로 저장되어 있다
    // 이것을 삼성+하이닉스로 재정렬하고 액수, 수량도 재조합한다
    public List<BuyStockDBData> make_buy_portfolio() {
        ArrayList<String> stocknamelist = new ArrayList<String>();
        List<BuyStockDBData> buystockList_arranged = new ArrayList<>();

        // 일단 종목명만 뽑아서 중복값 제거한다.
        stocknamelist = extract_buystock_name();
        // 중복된 종목 액수는 모두 합쳐서 하나의 종목으로 만든다
        // 중복되지 않은 종목명으로 총수량, 총매수액을 계산해서
        // 재정렬된 buystocklist에 넣고 리턴한다

        String mystock="";
        String dbstock="";
        int size =  stocknamelist.size();
        int size1 =  buystockList.size();
        for(int i=0; i< size ;i++) {
            // 아래에 선언을 하는 이유는 한 번 턴을 돌고 난다음
            // buystock_arranged 를 비워주기 위해서이다
            BuyStockDBData buystock_arranged = new BuyStockDBData();
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

        List<BuyStockDBData> buylist = new ArrayList<BuyStockDBData>();
        buylist = make_buy_portfolio();

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
/*
        List<BuyStockDBData> templist = new ArrayList<BuyStockDBData>();
        templist = buylist;
        for(int k = buylist.size();k>=0;k--) {
            for(int j =0;j<last_buylist.size();j++) {
                if (buylist.get(k).stock_name.equals(selllist.get(j).stock_name)) {
                    templist.remove(k);
                }
            }
        }

        for(int i = 0;i<templist.size();i++) {
            last_buylist.add(templist.get(i));
        }
 */
        // 최종 남은 매수종목 및 가격, 수량이 현재 포트폴리오 임
        return last_buylist;
    }


    public void loadExcel2DB2(String stock_code) {

        // 엑셀파일에서 시험용 매수주식 이력을 읽어들인다
        buystockList = buystock_db.buystockDao().getAll();
        // 엑셀파일에서 시험용 매도주식 이력을 읽어들인다
        sellstockList = sellstock_db.sellstockDao().getAll();

        if(buystockList.size() == 0) {

            int withdrawal=0, remain_cache=0;

            String name="", code="", date="";
            int buyquan=0, sellquan=0, price=0,receipt=0;
            int buy_total_quan=0, sell_total_quan=0;
            List<FormatTestData> signallist = new ArrayList<FormatTestData>();

            MyExcel myexcel = new MyExcel();
            signallist.addAll(myexcel.readtestset(stock_code+"_testset.xls", false));

            code = stock_code;
            name = myexcel.find_stockname(code);
            int size = signallist.size();
            for(int i=0;i<size;i++) {

                if(signallist.get(i).buy_quantity.equals("")) buyquan = 0;
                else buyquan = Integer.parseInt(signallist.get(i).buy_quantity);
                price = Integer.parseInt(signallist.get(i).price);
                date = signallist.get(i).date;
                // 그날의 매수현황을 db에 저장한다(매수시그널이 없으면 0으로, 있으면 시그널 만큼)
                buystock.buy_insert2db(name, code, buyquan, price, date);
                // 매수 후 매수액을 원금애써 빼줌
                withdrawal = price * buyquan;
                remain_cache = remain_cache - withdrawal;
                buy_total_quan = buy_total_quan + buyquan;


                // 매도시그널을 정수로 변환하고
                if (signallist.get(i).sell_quantity.equals("")) sellquan = 0;
                else sellquan = Integer.parseInt(signallist.get(i).sell_quantity);
                // 매도하기 전에 매도할 수량만큼 보유했는지 검사. 모자라면 매수량은 0으로
                if(buy_total_quan < sellquan ) sellquan = 0;
                if(sellquan > 0) {
                    // 매도할 수 있으면, 매수량에서 매도량 뺌
                    buy_total_quan = buy_total_quan - sellquan;
                }
                // 그날의 매도현황을 db에 저장한다(매도시그널이 없으면 0으로, 있으면 시그널 만큼)
                sellstock.sell_insert2db(name, code, sellquan, price, date);

                // 매도 후 매도액을 원금에 추가해줌
                receipt = sellquan * price;
                remain_cache = remain_cache + receipt;
                sell_total_quan = sell_total_quan + sellquan;
            }
            // 잔고 = 잔고 - 매수액 : 위에서 계산된 매수액이 (-)가 붙어서 오기 때문에 (+)로 처리
            SCache mycache = new SCache();
            mycache.update_cache(remain_cache);
            int test = 0;
        }
        // 이렇게 하면 모든날짜의 매수/매도 히스토리 db(엑셀)가 만들어진다
        // 매수 매도 히스토리 db(엑셀)는 차트를 그리는데 사용된다.
        // buystockList는 매수시스널 테이블과 동일해야 한다
        // sellsignalList도 매도시그널 테이블과 동일해야 하지만 과매도가 나오면 안된다
        // 과매도는 매도할 시점에 매수량이 없는데 매도를 하는 현상
        // 위에서 모두 예외처리를 했지만 버그가 있을 수도 있다
        buystockList = buystock_db.buystockDao().getAll();
        //buystockList = buystock_db.buystockDao().getAll();
        sellstockList = sellstock_db.sellstockDao().getAll();
        //sellstockList = sellstock_db.sellstockDao().getAll();
    }
}
