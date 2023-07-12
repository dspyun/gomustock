package com.gomu.gomustock.stockengin;

import com.gomu.gomustock.MyExcel;

import java.util.ArrayList;
import java.util.List;

public class Balance {

    String STOCK_CODE;
    MyExcel myexcel = new MyExcel();
    TestBox TESTBOX;
    List<Float> BUYLINE = new ArrayList<>();
    List<Float> SELLLINE = new ArrayList<>();
    float BALANCE=0;
    float TOTAL_BUY_PRICE=0, TOTAL_SELL_PRICE=0;
    float TOTAL_BUY_QUANTITY=0, TOTAL_SELL_QUANTITY=0;
    float HOLD_MONEY;
    float LAST_HOLD_ESTIM;

    public Balance(String stock_code, float cur_price) {
        STOCK_CODE = stock_code;
        // 엑셀에서 test data를 읽고 box에 정리한다
        TESTBOX = new TestBox(stock_code);
        makeBuyhistory();
        makeSellhistory();
        makeTestResult();
    }

    public List<Float> getBuyline() {
        return BUYLINE;
    }
    public List<Float> getSellline() {
        return SELLLINE;
    }

    void makeBuyhistory() {
        int size = TESTBOX.getBuy().size();
        float buyprice;
        for(int i =0;i<size;i++) {
            buyprice =   TESTBOX.getBuy().get(i) * TESTBOX.getClose().get(i);
            BUYLINE.add(buyprice);
        }
    }

    void makeSellhistory() {
        int size = TESTBOX.getSell().size();
        float sellprice;
        for(int i =0;i<size;i++) {
            sellprice =   TESTBOX.getSell().get(i) * TESTBOX.getClose().get(i);
            SELLLINE.add(sellprice);
        }
    }

    void makeTestResult() {
        int size = TESTBOX.getSell().size();

        for(int i =0;i<size;i++) {
            TOTAL_BUY_PRICE = TOTAL_BUY_PRICE + BUYLINE.get(i);
            TOTAL_SELL_PRICE = TOTAL_SELL_PRICE + SELLLINE.get(i);
            TOTAL_BUY_QUANTITY = TOTAL_BUY_QUANTITY + TESTBOX.getBuy().get(i);
            TOTAL_SELL_QUANTITY = TOTAL_SELL_QUANTITY + TESTBOX.getSell().get(i);
        }

        // 2. 잔량주식가격 : 잔량*최신close
        LAST_HOLD_ESTIM = (TOTAL_BUY_QUANTITY-TOTAL_SELL_QUANTITY)*TESTBOX.getClose().get(size-1);
        // 최종손익 = 현금손익 + 잔량가격
        BALANCE = HOLD_MONEY + LAST_HOLD_ESTIM;
        // 매수액, 평가액, 수익률, 평단가
        // 수익액 : BALANCE = 현금손익 + 잔량가격
        // 수익률 : 수익액/총매수액 = BALANCE/TOTAL_BUY_PRICE
        // 평단가 : 총매수액/총매수량 = TOTAL_BUY_PRICE/TOTAL_BUY_QUANTITY
        // 매수액 : TOTAL_BUY_PRICE
    }
    public float getProfitRate() { return (BALANCE/TOTAL_BUY_PRICE); }
    public int getProfit() { return (int)BALANCE; }
    public int getAVERPrice() { return (int)(TOTAL_BUY_PRICE/TOTAL_BUY_QUANTITY); } // 평단가
    public int getTotalBuyCost() { return (int)TOTAL_BUY_PRICE; } // 총매수액

    public int getSellPrice() { return(int)TOTAL_SELL_PRICE;} // 회수액

    public int getEstimPrice() { return (int)LAST_HOLD_ESTIM;} // 평가액

    public int getHoldQuantity() { return (int)(TOTAL_BUY_QUANTITY-TOTAL_SELL_QUANTITY);} // 잔량

}
