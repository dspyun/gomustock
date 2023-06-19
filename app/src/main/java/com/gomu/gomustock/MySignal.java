package com.gomu.gomustock;

import static com.tictactec.ta.lib.MAType.Sma;

import android.content.Context;

import com.gomu.gomustock.portfolio.BuyStockDB;
import com.gomu.gomustock.portfolio.BuyStockDBData;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MAType;
import com.tictactec.ta.lib.MInteger;
import com.tictactec.ta.lib.RetCode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MySignal {

    List<String> srcdata = new ArrayList<>();
    List<String> basedata = new ArrayList<>();
    List<Float> basestddata = new ArrayList<>();
    List<Float>srcstddata = new ArrayList<>();
    public List<FormatScore> scorebox = new ArrayList<>();
    List<String> buylist = new ArrayList<String>();
    Context context;


    public MySignal(Context inputcontext) {
        this.context = inputcontext;
        ReadBuyList();
    }

    public void ReadBuyList() {
        BuyStockDB buystock_db;
        List<BuyStockDBData> buystockList = new ArrayList<>();
        buystock_db = BuyStockDB.getInstance(context);
        buystockList = buystock_db.buystockDao().getAll();
        // buy stock list에서 중복된 종목을 제거한다
        Set<String> set = new HashSet<String>();
        for(int i =0;i < buystockList.size();i++ ) {
            set.add(buystockList.get(i).stock_code);
        }
        Iterator<String> iter = set.iterator();

        // 중복 종목명이 제거된 결과를 buylist에 저장한다
        while(iter.hasNext()) { //iter에 다음 읽을 데이터가 있다면
            buylist.add(iter.next());
        }
    }

    public void calcScore() {
        addPerioddata();
        for(int i =0;i<scorebox.size();i++) {
            //if(mixlist.get(i).stock_name.equals("코스피 200")) continue;
            String temp = scorebox.get(i).stock_code;
            int score = scoring(temp);
            scorebox.get(i).score = score;
            System.out.println(scorebox.get(i).stock_code + " = " + Integer.toString(score));
        }
    }

    public String getSignal(String stock_code) {
        String result="";
        for(int i =0;i<scorebox.size();i++) {
            if(scorebox.get(i).stock_code.equals(stock_code)) {
                result = String.valueOf(scorebox.get(i).score);
                break;
            }
        }
        return result;
    }

    public int getSocrelistsize() {
        return scorebox.size();
    }
    public void addPerioddata() {
        MyExcel myexcel = new MyExcel();
        for (int i = 0; i < scorebox.size(); i++) {
            FormatScore onemix = new FormatScore();
            onemix = scorebox.get(i);
            srcdata = myexcel.oa_readItem(onemix.stock_code+".xls", "CLOSE", false);
            scorebox.get(i).period_price = srcdata;
            srcdata.add(onemix.cur_price);
        }
    }

    public int scoring(String stock_code) {
        List<String> itemdata = new ArrayList<>();
        List<String> kospi200 = new ArrayList<>();
        int score=0;

        MyStat mystat = new MyStat();
        MyExcel myexcel = new MyExcel();

        int index = find_index(stock_code);
        if(index == -1) { return score = 0; }

        itemdata = scorebox.get(index).period_price;
        index = find_index("코스피 200");
        kospi200 = scorebox.get(index).period_price;

        srcstddata = mystat.oa_standardization(itemdata);
        basestddata = mystat.oa_standardization(kospi200);

        float diff=0;
        int i = srcstddata.size()-1;
        diff = srcstddata.get(i) - basestddata.get(i);

        // -는 sell : 기준보다 가격이 높으면 판다
        if(diff >= 0.5 && diff < 1) score = -1;
        else if(diff >= 1) score = -2;

        // +는 buy : 기준보다 가격이 낮으면 산다
        if(diff <= -0.5 && diff > -1) score = 1;
        else if(diff <= -1) score = 3;

        bbands_test(stock_code);

        return score;
    }

    public int find_index(String stock_code) {
        for (int i = 0; i < scorebox.size();i++) {
            if(stock_code.equals(scorebox.get(i).stock_code))
            {
                return i;
            }
        }
        return -1;
    }
    // 여기에 두기는 좀 애매하지만...
    public void addCurprice() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                MyWeb myweb = new MyWeb();
                scorebox.clear();
                for(int i =0;i<buylist.size();i++) {
                    FormatScore temp = new FormatScore();
                    temp.stock_code = buylist.get(i);
                    scorebox.add(temp);
                }
                for (int i = 0; i < buylist.size(); i++) {
                    try {
                        FormatScore onemix = new FormatScore();
                        String temp = myweb.getCurrentStockPrice(buylist.get(i));
                        onemix.cur_price = temp.replaceAll(",", "");
                        onemix.stock_code = buylist.get(i);
                        int index = find_index(buylist.get(i));
                        if(index == -1) scorebox.add(onemix);
                        else scorebox.set(index,onemix);
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
                FormatScore onemix1 = new FormatScore();
                String temp  = myweb.getCurrentKosp200();
                onemix1.cur_price = temp.replaceAll(",", "");
                onemix1.stock_code = "코스피 200";
                int index = find_index("코스피 200");
                if(index == -1) scorebox.add(onemix1);
                else scorebox.set(index,onemix1);
            }
        }).start();
    }

    public int bbands_test(String stock_code) {

        List<List<Float>> threechart = new ArrayList<List<Float>>();

        // The total number of periods to generate data for.
        final int TOTAL_PERIODS = 60;

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
        close_str = myexcel.oa_readItem(stock_code + ".xls","CLOSE", false);
        List<Double> closedata = myexcel.string2double(close_str, 1);

        for (int i = 0; i < closePrice.length; i++) {
            closePrice[i] = (double) closedata.get(i);
        }

        Core c = new Core();
        //RetCode retCode = c.sma(0, closePrice.length - 1, closePrice, PERIODS_AVERAGE, begin, length, out);
        RetCode retCode = c.bbands(0, closePrice.length - 1, closePrice, PERIODS_AVERAGE,
                optInNbDevUp, optInNbDevDn, optInMAType,
                begin, length, outRealUpperBand, outRealMiddleBand, outRealLowerBand);

        List<Double> mystddev = new ArrayList<>();
        mystddev = c.get_bband_stddev();

        int score=0;
        double diff=0, average=0;
        double sum = 0;
        for(int i = 0 ;i<mystddev.size();i++) {
            sum += mystddev.get(i);
        }
        average = sum/mystddev.size();
        diff = average - mystddev.get(mystddev.size()-1);

        // 표준편차값이 표준편차의 평균보다 작으면(diff가 양수) 매수(오목한 지점이다)
        // 볼록한 지점은? 매도지점은 따로 해석법을 찾아보자
        if(diff >= 0) score = 1;
        else if(diff < 0) score = 0;

        return score;
    }
}
