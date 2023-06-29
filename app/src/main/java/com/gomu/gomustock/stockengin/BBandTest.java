package com.gomu.gomustock.stockengin;

import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.MyStat;

import java.util.ArrayList;
import java.util.List;

public class BBandTest {


    String srcfile;
    String basefile;
    List<String> srcdata = new ArrayList<>();
    List<String> basedata = new ArrayList<>();
    List<Float> basestddata = new ArrayList<>();
    List<Float>srcstddata = new ArrayList<>();
    TAlib mytalib = new TAlib();
    MyStat mystat = new MyStat();
    MyExcel myexcel = new MyExcel();
    String stock_code, base_code;
    public BBandTest (String stockcode) {
        this.srcfile = stockcode+".xls";
        this.stock_code = stockcode;
    }

    public void readData() {
        srcdata = myexcel.oa_readItem(srcfile, "CLOSE", false);
        basedata = myexcel.oa_readItem(basefile, "CLOSE", false);
    }

    public void genQuantity() {

        List<Float> bband_buyscore = new ArrayList<>();
        List<Float> rsi_buyscore = new ArrayList<>();
        List<Integer> buy_score = new ArrayList<>();
        List<Integer> sell_score = new ArrayList<>();
        int size = srcdata.size();
        for(int i = 0; i< size; i++) {
            buy_score.add(0);
            sell_score.add(0);
        }
        bband_buyscore = bband_testtest(stock_code);
        rsi_buyscore = rsi_testtest(stock_code);
        size = bband_buyscore.size();
        for(int i=0;i<size;i++){
            if(bband_buyscore.get(i)>=70 && bband_buyscore.get(i) <80
            && rsi_buyscore.get(i)>=70 && rsi_buyscore.get(i) < 80) {
                buy_score.set(i,1);
            } else if( bband_buyscore.get(i)>=80 && bband_buyscore.get(i) <80
                    && rsi_buyscore.get(i)>=80 && rsi_buyscore.get(i) < 80){
                buy_score.set(i,2);
            }else if( bband_buyscore.get(i)>=90
                    && rsi_buyscore.get(i)>=90 ) {
                buy_score.set(i, 3);
            } else {
                buy_score.set(i, 0);
            }
        }

        List<String> close = myexcel.oa_readItem(srcfile, "CLoSE", false);
        List<String> date = myexcel.oa_readItem(srcfile, "DATE", false);
        myexcel.write_testdata(stock_code,date,close, buy_score, sell_score);
    }

    public List<Float> bband_testtest(String stock_code) {
        List<Float> bband_score = new ArrayList<>();
        List<Float> bband_signal = new ArrayList<>();
        mytalib.bbands_test(stock_code, 60);
        bband_signal = mytalib.bband_test_signal();

        for(float ftemp: bband_signal) {
            bband_score.add(ftemp*100); // 보통 0~100이지만 마이너스와 100을 넘어갈때도 있다
        }
        return bband_score;
    }

    public List<Float> rsi_testtest(String stock_code) {

        List<Float> rsi_score = new ArrayList<>();
        rsi_score = mytalib.rsi_test(stock_code, 60);
        return rsi_score; // rsi는 0~100 사이의 값을 가진다
    }

    public void makeBackdata() {
        readData();
        genQuantity();
    }

}
