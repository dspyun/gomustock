package com.gomu.gomustock.stockengin;

import com.gomu.gomustock.MyExcel;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.util.ArrayList;
import java.util.List;

public class RSITest {
    TAlib mytalib = new TAlib();
    String srcfile;

    String stock_code;
    List<Double>  CLOSEDATA = new ArrayList<>();

    public RSITest(String stockcode) {
        this.srcfile = stockcode+".xls";
        this.stock_code = stockcode;
        loadPrice();
    }


    public List<Float> rsi_testtest(String stock_code) {

        List<List<Float>> rsi_score = new ArrayList<>();
        rsi_score = mytalib.rsi_test(stock_code, 60);
        return rsi_score.get(0); // rsi는 0~100 사이의 값을 가진다
    }


    public List<Float> rsi_test_loop(int days) {
        List<Float> todaylist = new ArrayList<>();

        // The total number of periods to generate data for.
        final int TOTAL_PERIODS = days;

        // The number of periods to average together.
        final int optInTimePeriod = 5;

        double[] closePrice = new double[TOTAL_PERIODS];
        double[] outReal = new double[TOTAL_PERIODS];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();

        for(int l = 0;l<days;l++) {
            // CLOSEDATA는 과거>현재순으로 정렬된 상태
            for (int i = 0; i < days; i++) {
                closePrice[i] = (double) CLOSEDATA.get(l+days * 2 + i);
            }

            Core c = new Core();
            RetCode retCode = c.rsi(0, closePrice.length - 1, closePrice, optInTimePeriod, begin, length, outReal);

            List<Float> result = new ArrayList<Float>();
            if (retCode == RetCode.Success) {
                System.out.println("Output Start Period: " + begin.value);
                System.out.println("Output End Period: " + (begin.value + length.value - 1));
                int start = begin.value;
                int end = (begin.value + length.value);
                // 결과를 float 리스트로 패킹해서 전달한다
                int length2 = outReal.length;
                // 시작이 4부터임. 그래서 0~3까지 초기값으로 채워넣음

                int today = end - start - 1;
                for (int i = 0; i < start; i++) {
                    result.add((float) outReal[0]);
                }
                for(int i = 0;i < end-start;i++ ) {
                    result.add((float)outReal[i]);
                }
                float today_perb = (float)outReal[today] ;
                todaylist.add(today_perb);
            }

        }
        return todaylist;
    }

    // 리턴값이 1~100 사이임
    public Float TodayScore(int days) {
        List<Float> todaylist = new ArrayList<>();

        // The total number of periods to generate data for.
        final int TOTAL_PERIODS = days;

        // The number of periods to average together.
        final int optInTimePeriod = 5;

        double[] closePrice = new double[TOTAL_PERIODS];
        double[] outReal = new double[TOTAL_PERIODS];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();

        float today_perb=0;

        // CLOSEDATA는 과거>현재순으로 정렬된 상태
        for (int i = 0; i < days; i++) {
            closePrice[i] = (double) CLOSEDATA.get(i);
        }

        Core c = new Core();
        RetCode retCode = c.rsi(0, closePrice.length - 1, closePrice, optInTimePeriod, begin, length, outReal);

        List<Float> result = new ArrayList<Float>();
        if (retCode == RetCode.Success) {
            System.out.println("Output Start Period: " + begin.value);
            System.out.println("Output End Period: " + (begin.value + length.value - 1));
            int start = begin.value;
            int end = (begin.value + length.value);
            // 결과를 float 리스트로 패킹해서 전달한다
            int length2 = outReal.length;
            // 시작이 4부터임. 그래서 0~3까지 초기값으로 채워넣음

            int today = end - start - 1;
            for (int i = 0; i < start; i++) {
                result.add((float) outReal[0]);
            }
            for(int i = 0;i < end-start;i++ ) {
                result.add((float)outReal[i]);
            }
            today_perb = (float)outReal[today] ;
        }
        return today_perb;
    }

    public void loadPrice( ) {
        MyExcel myexcel = new MyExcel();
        List<String> close_str = new ArrayList<>();
        // days가 -1이면 max로 읽는다.
        close_str = myexcel.readhistory(stock_code+".xls","CLOSE",-1,false);
        close_str = myexcel.arrangeRev_string(close_str);
        CLOSEDATA = myexcel.string2double(close_str,1);

    }
}
