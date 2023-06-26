package com.gomu.gomustock.stockengin;

import static com.tictactec.ta.lib.MAType.Sma;

import com.gomu.gomustock.MyExcel;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TAlib {

    public TAlib() {

    }

    public List<List<Float>> macd_test(String stock_code, int total_period ) {

        List<List<Float>> result = new ArrayList<List<Float>>();
        final int TOTAL_PERIODS = total_period;

        double[] closePrice = new double[TOTAL_PERIODS];
        double[] outMACD = new double[TOTAL_PERIODS];
        double[] outMACDSignal = new double[TOTAL_PERIODS];
        double[] outMACDHist = new double[TOTAL_PERIODS];
        double[] outSlowD = new double[TOTAL_PERIODS];
        MInteger outBegIdx = new MInteger();;
        MInteger outNBElement = new MInteger();
        MInteger begin = new MInteger();;
        MInteger length = new MInteger();
        int optInTimePeriod = 14;
        MAType optInMAType = Sma; // 단순이동평균
        int optInFastPeriod = 12;
        int optInSlowPeriod = 26;
        int optInSignalPeriod = 9;

        MyExcel myexcel = new MyExcel();
        List<String> close_str = new ArrayList<>();
        close_str = myexcel.oa_readItem(stock_code+".xls","CLOSE",false);
        close_str = myexcel.arrangeRev_string(close_str);
        List<Double> closedata = myexcel.string2double(close_str,1);
        for (int i =  0; i < closePrice.length; i++) {
            closePrice[i] = (double) closedata.get(i);
        }

        Core c = new Core();
        RetCode retCode = c.macd(0, closePrice.length - 1,closePrice,
                optInFastPeriod, optInSlowPeriod, optInSignalPeriod,
                begin, length, outMACD, outMACDSignal,outMACDHist);

        if (retCode == RetCode.Success) {
            System.out.println("Output Start Period: " + begin.value);
            System.out.println("Output End Period: " + (begin.value + length.value - 1));
            int start = begin.value;
            int end = (begin.value + length.value);
            // 결과를 float 리스트로 패킹해서 전달한다;
            // 시작이 4부터임. 그래서 0~3까지 초기값으로 채워넣음
            List<Float> value = new ArrayList<Float>();
            for (int i = 0; i < start; i++) {
                value.add((float) outMACD[0]);
            }
            for (int i = 0; i < end - start; i++) {
                value.add((float) outMACD[i]);
            }
            result.add(value);

            List<Float> value1 = new ArrayList<Float>();
            for (int i = 0; i < start; i++) {
                value1.add((float) outMACDSignal[0]);
            }
            for (int i = 0; i < end - start; i++) {
                value1.add((float) outMACDSignal[i]);
            }
            result.add(value1);

            List<Float> value2 = new ArrayList<Float>();
            for (int i = 0; i < start; i++) {
                value2.add((float) outMACDHist[0]);
            }
            for (int i = 0; i < end - start; i++) {
                value2.add((float) outMACDHist[i]);
            }
            result.add(value2);
        }
        return result;
    }

    public List<Float> rsi_test(String stock_code, int total_period) {


        // The total number of periods to generate data for.
        final int TOTAL_PERIODS = total_period;

        // The number of periods to average together.
        final int optInTimePeriod = 5;

        double[] closePrice = new double[TOTAL_PERIODS];
        double[] outReal = new double[TOTAL_PERIODS];
        MInteger begin = new MInteger();
        MInteger length = new MInteger();

        MyExcel myexcel = new MyExcel();
        List<String> close_str = new ArrayList<>();
        close_str = myexcel.oa_readItem(stock_code+".xls","CLOSE",false);
        close_str = myexcel.arrangeRev_string(close_str);
        List<Double> closedata = myexcel.string2double(close_str,1);
        for (int i =  0; i < closePrice.length; i++) {
            closePrice[i] = (double) closedata.get(i);
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

            for (int i = 0; i < start; i++) {
                result.add((float) outReal[0]);
            }
            for(int i = 0;i < end-start;i++ ) {
                result.add((float)outReal[i]);
            }
        }
        return result;
    }



    public List<List<Float>> bbands_test(String stock_code, int total_period) {

        List<List<Float>> threechart = new ArrayList<List<Float>>();

        // The total number of periods to generate data for.
        final int TOTAL_PERIODS = total_period;

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

        MyExcel myexcel = new MyExcel();
        List<String> close_str = new ArrayList<>();
        close_str = myexcel.oa_readItem(stock_code+".xls","CLOSE",false);
        close_str = myexcel.arrangeRev_string(close_str);
        List<Double> closedata = myexcel.string2double(close_str,1);
        for (int i =  0; i < closePrice.length; i++) {
            closePrice[i] = (double) closedata.get(i);
        }

        Core c = new Core();
        //RetCode retCode = c.sma(0, closePrice.length - 1, closePrice, PERIODS_AVERAGE, begin, length, out);
        RetCode retCode = c.bbands(0, closePrice.length - 1, closePrice, PERIODS_AVERAGE,
                optInNbDevUp, optInNbDevDn, optInMAType,
                begin, length, outRealUpperBand, outRealMiddleBand, outRealLowerBand);

        if (retCode == RetCode.Success) {
            System.out.println("Output Start Period: " + begin.value);
            System.out.println("Output End Period: " + (begin.value + length.value - 1));
            int start = begin.value;
            int end = (begin.value + length.value);
            // 결과를 float 리스트로 패킹해서 전달한다
            int length2 = outRealUpperBand.length;
            // 시작이 4부터임. 그래서 0~3까지 초기값으로 채워넣음
            List<Float> value = new ArrayList<Float>();
            for(int i = 0;i<start;i++) {
                value.add((float)outRealUpperBand[0]);
            }
            for(int i = 0;i < end-start;i++ ) {
                value.add((float)outRealUpperBand[i]);
            }
            threechart.add(value);

            List<Float> value1 = new ArrayList<Float>();
            for(int i = 0;i<start;i++) {
                value1.add((float)outRealMiddleBand[0]);
            }
            for(int i = 0; i <end-start;i++ ) {
                value1.add((float)outRealMiddleBand[i]);
            }
            threechart.add(value1);

            List<Float> value2 = new ArrayList<Float>();
            for(int i = 0;i<start;i++) {
                value2.add((float)outRealLowerBand[0]);
            }
            for(int i = 0; i <end-start;i++ ) {
                value2.add((float)outRealLowerBand[i]);
            }
            threechart.add(value2);
            /* 커스텀 매도 타임 시그널
            double pricemax = Collections.max(closedata);
            List<Float> value3 = new ArrayList<Float>();
            for(int i = 0;i<start;i++) {
                value3.add((float)pricemax);
            }
            for(int i = 0; i <end-start;i++ ) {
                double temp1 = closePrice[start+i]-outRealMiddleBand[i];
                double temp2 = closePrice[start+i]-outRealLowerBand[i];
                double temp3 = 0.2*(outRealMiddleBand[i]-outRealLowerBand[i]);
                if((temp1 <= 0) && (temp2 <= temp3)) {
                    value3.add((float)(pricemax+pricemax*0.02));
                }
                else {
                    value3.add((float)pricemax);
                }
            }
            */
            double pricemax = Collections.max(closedata);
            List<Float> value3 = new ArrayList<Float>();
            for(int i = 0;i<start;i++) {
                value3.add((float)pricemax);
            }
            for(int i = 0; i <end-start;i++ ) {
                double temp = (closePrice[start+i]-outRealLowerBand[i])/(outRealUpperBand[i]-outRealLowerBand[i]);
                //%b 곡선에서 하단 0,2 아래 지점들에 표시를 해준다. 즉 %b값으로 저점매수 시기를 알려주는 표시.
                //if(temp>0.8) value3.add((float)(pricemax + pricemax*0.02));
                if(temp <= 0.2 && temp > 0.1) value3.add((float)(pricemax - pricemax*0.02));
                else if(temp <= 0.1) value3.add((float)(pricemax - pricemax*0.03));
                else value3.add((float)pricemax);
            }

            threechart.add(value3);
        }
        else {
            System.out.println("Error");
        }

        return threechart;
    }

    public List<Float> adx_test(String stock_code, int total_period) {


        // The total number of periods to generate data for.
        final int TOTAL_PERIODS = total_period;

        double[] closePrice = new double[TOTAL_PERIODS];
        double[] highPrice = new double[TOTAL_PERIODS];
        double[] lowPrice = new double[TOTAL_PERIODS];
        double[] outReal = new double[TOTAL_PERIODS];
        MInteger outBegIdx = new MInteger();;
        MInteger outNBElement = new MInteger();
        MInteger begin = new MInteger();;
        MInteger length = new MInteger();
        int optInTimePeriod = 14;

        List<Float> value = new ArrayList<Float>();

        MyExcel myexcel = new MyExcel();
        List<String> close_str = new ArrayList<>();
        close_str = myexcel.oa_readItem(stock_code+".xls","CLOSE",false);
        close_str = myexcel.arrangeRev_string(close_str);
        List<Double> closedata = myexcel.string2double(close_str,1);
        for (int i =  0; i < closePrice.length; i++) {
            closePrice[i] = (double) closedata.get(i);
        }
        List<String> high_str = new ArrayList<>();
        high_str = myexcel.oa_readItem(stock_code+".xls","HIGH",false);
        high_str = myexcel.arrangeRev_string(high_str);
        List<Double> highdata = myexcel.string2double(high_str,1);
        for (int i =  0; i < highPrice.length; i++) {
            highPrice[i] = (double) highdata.get(i);
        }
        List<String> low_str = new ArrayList<>();
        low_str = myexcel.oa_readItem(stock_code+".xls","LOW",false);
        low_str = myexcel.arrangeRev_string(low_str);
        List<Double> lowdata = myexcel.string2double(low_str,1);
        for (int i =  0; i < lowPrice.length; i++) {
            lowPrice[i] = (double) lowdata.get(i);
        }

        Core c = new Core();
        //RetCode retCode = c.sma(0, closePrice.length - 1, closePrice, PERIODS_AVERAGE, begin, length, out);
        RetCode retCode = c.adx( 0, closePrice.length -1,  highPrice, lowPrice, closePrice,
                optInTimePeriod, begin, length,  outReal);

        if (retCode == RetCode.Success) {
            System.out.println("Output Start Period: " + begin.value);
            System.out.println("Output Period length : " +length.value);
            int start = begin.value;
            int end = (begin.value + length.value);
            // 결과를 float 리스트로 패킹해서 전달한다
            int length2 = outReal.length;
            // 시작이 4부터임. 그래서 0~3까지 초기값으로 채워넣음

            for(int i = 0;i<start;i++) {
                value.add((float)outReal[0]);
            }
            for(int i = 0;i < end-start;i++ ) {
                value.add((float)outReal[i]);
            }

        }
        else {
            System.out.println("Error");
        }

        return value;
    }


    public List<List<Float>> stoch_test(String stock_code, int total_period ) {

        // The total number of periods to generate data for.
        int TOTAL_PERIODS = total_period;

        double[] closePrice = new double[TOTAL_PERIODS];
        double[] highPrice = new double[TOTAL_PERIODS];
        double[] lowPrice = new double[TOTAL_PERIODS];
        double[] outSlowK = new double[TOTAL_PERIODS];
        double[] outSlowD = new double[TOTAL_PERIODS];
        MInteger outBegIdx = new MInteger();;
        MInteger outNBElement = new MInteger();
        MInteger begin = new MInteger();;
        MInteger length = new MInteger();
        int optInTimePeriod = 14;
        MAType optInMAType = Sma; // 단순이동평균
        int optInFastK_Period = 5;
        int optInSlowK_Period = 3;
        int optInSlowD_Period = 3;
        MAType optInSlowK_MAType = Sma;
        MAType optInSlowD_MAType = Sma;

        MyExcel myexcel = new MyExcel();
        List<String> close_str = new ArrayList<>();
        close_str = myexcel.oa_readItem(stock_code+".xls","CLOSE",false);
        close_str = myexcel.arrangeRev_string(close_str);
        List<Double> closedata = myexcel.string2double(close_str,1);
        for (int i =  0; i < closePrice.length; i++) {
            closePrice[i] = (double) closedata.get(i);
        }
        List<String> high_str = new ArrayList<>();
        high_str = myexcel.oa_readItem(stock_code+".xls","HIGH",false);
        high_str = myexcel.arrangeRev_string(high_str);
        List<Double> highdata = myexcel.string2double(high_str,1);
        for (int i =  0; i < highPrice.length; i++) {
            highPrice[i] = (double) highdata.get(i);
        }
        List<String> low_str = new ArrayList<>();
        low_str = myexcel.oa_readItem(stock_code+".xls","LOW",false);
        low_str = myexcel.arrangeRev_string(low_str);
        List<Double> lowdata = myexcel.string2double(low_str,1);
        for (int i =  0; i < lowPrice.length; i++) {
            lowPrice[i] = (double) lowdata.get(i);
        }

        // outNBElement : 생성된 자료의 배술 수. = output 길이
        // 결과가 배열로 반환되기 때문에 길이정보도 같이 반환되어야 함
        Core c = new Core();
        RetCode retCode  = c.stoch( 0, closePrice.length -1, highPrice, lowPrice, closePrice,
                optInFastK_Period, optInSlowK_Period, optInSlowK_MAType, optInSlowD_Period, optInSlowD_MAType,
                outBegIdx, outNBElement, outSlowK, outSlowD );

        List<List<Float>> threechart = new ArrayList<List<Float>>();

        List<Float> outSlowK_list = new ArrayList<Float>();
        int start = outBegIdx.value;
        int end = (outBegIdx.value + outNBElement.value);

        for(int i = 0;i<start;i++) {
            outSlowK_list.add((float)outSlowK[0]);
        }
        for(int i = 0;i < end-start;i++ ) {
            outSlowK_list.add((float)outSlowK[i]);
        }
        threechart.add(outSlowK_list);

        List<Float> outSlowD_list = new ArrayList<Float>();
        start = outBegIdx.value;
        end = (outBegIdx.value + outNBElement.value);

        for(int i = 0;i<start;i++) {
            outSlowD_list.add((float)outSlowD[0]);
        }
        for(int i = 0;i < end-start;i++ ) {
            outSlowD_list.add((float)outSlowD[i]);
        }
        threechart.add(outSlowD_list);

        return threechart;
    }
}
