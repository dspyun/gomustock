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
    List<Float> srcstddata = new ArrayList<>();
    public List<FormatScore> scorebox = new ArrayList<>();
    List<String> buylist = new ArrayList<String>();
    Context context;


    public MySignal(Context inputcontext) {
        this.context = inputcontext;
        loadBuyList();
        makeScoreBodx();
        loadHistory2Scorebox();
    }

    public void loadBuyList() {
        BuyStockDB buystock_db;
        List<BuyStockDBData> buystockList = new ArrayList<>();
        BuyStockDBData onebuy = new BuyStockDBData();

        buystock_db = BuyStockDB.getInstance(context);
        buystockList = buystock_db.buystockDao().getAll();

        // 코덱스 200은 buylist에 무조건 넣어준다
        // 다른 종목들의 평가 기준이 되는 종목이기 때문에
        onebuy.stock_code = "069500";
        onebuy.stock_name = "코덱스 200";
        buystockList.add(onebuy);

        // buy stock list에서 중복된 종목을 제거한다
        Set<String> set = new HashSet<String>();
        int size = buystockList.size();
        for(int i =0;i < size ;i++ ) {
            set.add(buystockList.get(i).stock_code);
        }
        Iterator<String> iter = set.iterator();

        // 중복 종목명이 제거된 결과를 buylist에 저장한다
        while(iter.hasNext()) { //iter에 다음 읽을 데이터가 있다면
            buylist.add(iter.next());
        }
    }

    public void calcScore() {
        int size = scorebox.size();
        for(int i =0;i< size;i++) {
            //if(mixlist.get(i).stock_name.equals("코스피 200")) continue;
            String stock_code = scorebox.get(i).stock_code;
            int score = scoring(stock_code);
            scorebox.get(i).score = score;
            System.out.println(scorebox.get(i).stock_code + " = " + Integer.toString(score));
        }
    }

    public String getScore(String stock_code) {
        String result="";
        int size = scorebox.size();
        for(int i =0;i<size;i++) {
            if(scorebox.get(i).stock_code.equals(stock_code)) {
                result = String.valueOf(scorebox.get(i).score);
                break;
            }
        }
        return result;
    }

    public int getScorelistsize() {
        return scorebox.size();
    }
    public void loadHistory2Scorebox() {
        MyExcel myexcel = new MyExcel();
        int size = scorebox.size();
        for (int i = 0; i < size; i++) {
            FormatScore onemix = new FormatScore();
            onemix = scorebox.get(i);
            srcdata = myexcel.oa_readItem(onemix.stock_code+".xls", "CLOSE", false);
            srcdata.add(onemix.cur_price);
            scorebox.get(i).period_price = srcdata;
        }
        int i = 0;
    }

    public int scoring(String stock_code) {
        List<String> itemdata = new ArrayList<>();
        List<String> kodex200 = new ArrayList<>();
        int score=0;

        MyStat mystat = new MyStat();

        // 스코어링할 종목가격을 불러온다
        int index = find_index(stock_code);
        if(index == -1) { return score = 0; }
        itemdata = scorebox.get(index).period_price;
        //itemdata.add(0,scorebox.get(index).cur_price);
        int size = itemdata.size();
        srcstddata = mystat.oa_standardization(itemdata);

        // 기준종목 가격을 불러온다.
        if(basestddata.size() == 0 ){
            index = find_index("069500");
            kodex200 = scorebox.get(index).period_price;
            //kodex200.add(0,scorebox.get(index).cur_price);
            // kodex200은 한번만 읽어주면 된다
            int size1 = kodex200.size();
            basestddata = mystat.oa_standardization(kodex200);
        }

        // 기준종목과 스코어링종목을 비교하여 스코어링을 한다
        float diff=0;
        int i = srcstddata.size()-1;
        diff = srcstddata.get(i) - basestddata.get(i);

        // -는 sell : 기준보다 가격이 높으면 판다
        if(diff >= 0.5 && diff < 1) score = -1;
        else if(diff >= 1) score = -2;

        // +는 buy : 기준보다 가격이 낮으면 산다
        if(diff <= -0.5 && diff > -1) score = 1;
        else if(diff <= -1) score = 3;

        //bbands_test(stock_code);

        return score;
    }

    public void makeScoreBodx() {
        scorebox.clear();
        int size = buylist.size();
        for(int i =0;i<size;i++) {
            FormatScore temp = new FormatScore();
            temp.stock_code = buylist.get(i);
            temp.cur_price = "0";
            temp.score = 0;
            scorebox.add(temp);
        }
    }

    public int find_index(String stock_code) {
        int size = scorebox.size();
        for (int i = 0; i < size ;i++) {
            if(stock_code.equals(scorebox.get(i).stock_code))
            {
                return i;
            }
        }
        return -1;
    }

    // 아래 메소드는 activity에서 thread로 감싸서 사용해야 한다.
    public void addCurprice2Scorebox() {
        // TODO Auto-generated method stub
        MyWeb myweb = new MyWeb();
        int size = scorebox.size();
        for (int i = 0; i < size; i++) {
            try {
                String cur_price = myweb.getCurrentStockPrice(scorebox.get(i).stock_code);
                scorebox.get(i).cur_price  = cur_price.replaceAll(",", "");
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        /*
        FormatScore onemix1 = new FormatScore();
        String temp  = myweb.getCurrentKosp200();
        onemix1.cur_price = temp.replaceAll(",", "");
        onemix1.stock_code = "069500";
        int index = find_index("코덱스 200");
        if(index == -1) scorebox.add(onemix1);
        else scorebox.set(index,onemix1);
        */
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
        int size =  closePrice.length;
        for (int i = 0; i < size; i++) {
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
        size = mystddev.size();
        for(int i = 0 ;i<size;i++) {
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
