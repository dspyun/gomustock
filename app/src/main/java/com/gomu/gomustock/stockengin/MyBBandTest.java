package com.gomu.gomustock.stockengin;

import static com.tictactec.ta.lib.MAType.Sma;

import com.gomu.gomustock.MyExcel;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.util.ArrayList;
import java.util.List;

public class MyBBandTest {

    String srcfile;
    String basefile;
    List<String> srcdata = new ArrayList<>();
    List<String> basedata = new ArrayList<>();

    TAlib mytalib = new TAlib();

    MyExcel myexcel = new MyExcel();
    String stock_code;

    List<Double>  CLOSEDATA = new ArrayList<>();

    RSITest rsitest;
    public MyBBandTest (String stockcode) {
        this.srcfile = stockcode+".xls";
        this.stock_code = stockcode;
        loadPrice();
        //rsitest = new RSITest(stockcode);
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
        //rsi_buyscore = rsitest.rsi_testtest(stock_code);

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
        close = myexcel.arrangeRev_string(close);
        List<String> date = myexcel.oa_readItem(srcfile, "DATE", false);
        date = myexcel.arrangeRev_string(date);
        myexcel.write_testdata(stock_code,date,close, buy_score, sell_score);
    }

    public void ScoringNTestfile() {

        List<Float> bband_buyscore = new ArrayList<>();
        List<Float> rsi_buyscore = new ArrayList<>();
        List<Integer> buy_score = new ArrayList<>();
        List<Integer> sell_score = new ArrayList<>();

        int days = 60;
        bband_buyscore = bband_test_loop(days);
        //rsi_buyscore = rsitest.rsi_test_loop(days);
        int size = bband_buyscore.size();
        for(int i = 0; i< size; i++) {
            buy_score.add(0);
            sell_score.add(0);
        }

        for(int i=0;i<size;i++){
            if(bband_buyscore.get(i)<=30 && bband_buyscore.get(i) > 20
                    && rsi_buyscore.get(i)<=50 && rsi_buyscore.get(i) > 40) {
                buy_score.set(i,1);
            } else if( bband_buyscore.get(i)<=20 && bband_buyscore.get(i) >10
                    && rsi_buyscore.get(i)<=40 && rsi_buyscore.get(i) > 30){
                buy_score.set(i,2);
            }else if( bband_buyscore.get(i)<=10
                    && rsi_buyscore.get(i)<=30 ) {
                buy_score.set(i, 3);
            } else {
                buy_score.set(i, 0);
            }
        }

        List<String> close = myexcel.read_ohlcv(srcfile, "CLOSE", days,false);
        close = myexcel.arrangeRev_string(close);
        List<String> date = myexcel.read_ohlcv(srcfile, "DATE", days,false);
        date = myexcel.arrangeRev_string(date);
        myexcel.write_testdata(stock_code,date,close, buy_score, sell_score);
    }

    public List<Float> bband_test_loop(int days) {
        List<Float> todaylist = new ArrayList<>();

        // The total number of periods to generate data for.
        final int TOTAL_PERIODS = days;

        // The number of periods to average together.
        final int PERIODS_AVERAGE = 5;

        double[] closePrice = new double[TOTAL_PERIODS];
        double[] outRealUpperBand = new double[TOTAL_PERIODS];
        double[] outRealMiddleBand = new double[TOTAL_PERIODS];
        double[] outRealLowerBand = new double[TOTAL_PERIODS];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double optInNbDevUp = 2; // 상한선 = 표준편차*2
        double optInNbDevDn = 2; // 하한선 = 표준편차*2
        MAType optInMAType = Sma; // 단순이동평균

        Core c = new Core();
        for(int l = 0;l<days;l++) {
            // CLOSEDATA는 과거>현재순으로 정렬된 상태
            for (int i = 0; i < days; i++) {
                closePrice[i] = (double) CLOSEDATA.get(l+days * 2 + i);
            }

            RetCode retCode = c.bbands_oneday(0, closePrice.length - 1, closePrice, PERIODS_AVERAGE,
                    optInNbDevUp, optInNbDevDn, optInMAType,
                    begin, length, outRealUpperBand, outRealMiddleBand, outRealLowerBand);

            int start = begin.value;
            int end = (begin.value + length.value);
            int today = end - start - 1;
            float today_perb = (float) ((closePrice[start + today] - outRealLowerBand[today]) / (outRealUpperBand[today] - outRealLowerBand[today]));
            todaylist.add(today_perb*100);
        }
        return todaylist;
    }

    // 리턴값이 1~100 사이이지만 마이너스 또는 100을 초과할 때도 있음
    public Float TodayScore(int days) {
        List<Float> todaylist = new ArrayList<>();

        // The total number of periods to generate data for.
        final int TOTAL_PERIODS = days;

        // The number of periods to average together.
        final int PERIODS_AVERAGE = 5;

        double[] closePrice = new double[TOTAL_PERIODS];
        double[] outRealUpperBand = new double[TOTAL_PERIODS];
        double[] outRealMiddleBand = new double[TOTAL_PERIODS];
        double[] outRealLowerBand = new double[TOTAL_PERIODS];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();
        double optInNbDevUp = 2; // 상한선 = 표준편차*2
        double optInNbDevDn = 2; // 하한선 = 표준편차*2
        MAType optInMAType = Sma; // 단순이동평균

        Core c = new Core();

        // CLOSEDATA는 과거>현재순으로 정렬된 상태
        for (int i = 0; i < days; i++) {
            closePrice[i] = (double) CLOSEDATA.get(i);
        }

        RetCode retCode = c.bbands_oneday(0, closePrice.length - 1, closePrice, PERIODS_AVERAGE,
                optInNbDevUp, optInNbDevDn, optInMAType,
                begin, length, outRealUpperBand, outRealMiddleBand, outRealLowerBand);

        int start = begin.value;
        int end = (begin.value + length.value);
        int today = end - start - 1;

        float today_perb = (float) ((closePrice[start + today] - outRealLowerBand[today]) / (outRealUpperBand[today] - outRealLowerBand[today]));

        return today_perb*100;
    }

    public void loadPrice( ) {
        MyExcel myexcel = new MyExcel();
        List<String> close_str = new ArrayList<>();
        // days가 -1이면 max로 읽는다.
        close_str = myexcel.read_ohlcv(stock_code+".xls","CLOSE",-1,false);
        close_str = myexcel.arrangeRev_string(close_str);
        CLOSEDATA = myexcel.string2double(close_str,1);

    }

    public List<Float> bband_testtest(String stock_code) {
        List<Float> bband_score = new ArrayList<>();
        List<Float> bband_signal = new ArrayList<>();
        //mytalib.bbands_test(stock_code, 60);
        //bband_signal = mytalib.bband_test_signal();

        for(float ftemp: bband_signal) {
            bband_score.add(ftemp*100); // 보통 0~100이지만 마이너스와 100을 넘어갈때도 있다
        }
        return bband_score;
    }

    public void makeBackTestData() {
        readData();
        ScoringNTestfile();
        //genQuantity();
    }
}
