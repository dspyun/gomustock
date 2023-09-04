package com.gomu.gomustock.network;


import com.gomu.gomustock.MyExcel;
import com.gomu.gomustock.stockengin.StockDic;
import com.gomu.gomustock.ui.format.FormatETFInfo;
import com.gomu.gomustock.ui.format.FormatStockInfo;

import java.util.ArrayList;
import java.util.List;

public class InfoDownload {

    MyExcel myexcel = new MyExcel();
    MyWeb myweb = new MyWeb();
    StockDic stockdic = new StockDic();
    private String msg;

    public InfoDownload() {

    }

    public interface IFCallback {
        public void callback(String str);
    }
    // 콜백인터페이스를 구현한 클래스 인스턴스
    private IFCallback _cb;
    public void setCallback(IFCallback cb) {
        this._cb = cb;
    }


    public void downloadStockInfoCustom(String filename) {

        List<String> stock_list = new ArrayList<>();
        List<String> name_list = new ArrayList<>();
        List<FormatStockInfo> web_stockinfo = new ArrayList<FormatStockInfo>();
        web_stockinfo = myexcel.readStockinfoCustom(filename);
        int size = web_stockinfo.size();
        for(int i =0;i<size;i++) {
            stock_list.add(web_stockinfo.get(i).stock_code);
            name_list.add(web_stockinfo.get(i).stock_name);
        }
        web_stockinfo.clear();
        size = stock_list.size();
        for(int i =0;i<size;i++) {
            String stock_code = stock_list.get(i);
            FormatStockInfo stockinfo = new FormatStockInfo();
            FormatETFInfo etfinfo = new FormatETFInfo();
            fnGuide myfnguide = new fnGuide();
            String news;

            if(stock_code.equals("") || stockdic.getMarket(stock_code)=="KONEX") continue;
            if(!stockdic.checkKRStock(stock_code)) {
                stockinfo.stock_code = stock_list.get(i);
                stockinfo.stock_name = name_list.get(i);
                stockinfo.stock_type = "GLOBAL";
                web_stockinfo.add(stockinfo);
            }
            else if(stockdic.checkKRStock(stock_code) && (stockdic.getMarket(stock_code)!="")) {
                // stock_cdoe정보를 포함하고 있는
                // 네이버 정보를 가장 먼저 가져오고 그 다음에 다른 정보를 추가해야 한다
                _cb.callback("stock info" + "\n"+"download" + "\n" + stock_code);

                stockinfo = myweb.getNaverStockinfo(stock_code);
                stockinfo.stock_code = stock_code;
                stockinfo.stock_type="KSTOCK";
                // 네이버 뉴스를 가져온다
                news = myweb.getNaverStockNews(stock_code);
                stockinfo.news = news;

                // fnguide정보를 가져온다
                stockinfo.fninfo = myfnguide.getFnguideInfo(stock_code);

                web_stockinfo.add(stockinfo);
            } else {
                _cb.callback("etf info" + "\n"+"download" + "\n" + stock_code);

                etfinfo = myweb.getNaverETFinfo(stock_code);
                stockinfo.stock_type="KETF";
                stockinfo.etfinfo = etfinfo.toString();
                stockinfo.stock_code = etfinfo.stock_code;
                stockinfo.stock_name = etfinfo.stock_name;
                stockinfo.desc = etfinfo.desc;
                stockinfo.nav = etfinfo.nav;
                stockinfo.etfcompanies = etfinfo.companies;

                news = myweb.getNaverStockNews(stock_code);
                stockinfo.news = news;
                // fnguide정보를 가져온다
                stockinfo.fninfo = myfnguide.getFnguideETFInfo(stock_code);
                web_stockinfo.add(stockinfo);
            }
        }
        myexcel.writestockinfoCustom(filename,web_stockinfo);
    }

    public FormatStockInfo downloadStockInfoOne(String stock_code) {

            FormatStockInfo stockinfo = new FormatStockInfo();
            FormatETFInfo etfinfo = new FormatETFInfo();
            fnGuide myfnguide = new fnGuide();
            String news;

            if(stockdic.checkKRStock(stock_code) && (stockdic.getMarket(stock_code)!="")) {
                // stock_cdoe정보를 포함하고 있는
                // 네이버 정보를 가장 먼저 가져오고 그 다음에 다른 정보를 추가해야 한다

                stockinfo = myweb.getNaverStockinfo(stock_code);
                stockinfo.stock_code = stock_code;
                stockinfo.stock_type="KSTOCK";
                // 네이버 뉴스를 가져온다
                news = myweb.getNaverStockNews(stock_code);
                stockinfo.news = news;

                // fnguide정보를 가져온다
                stockinfo.fninfo = myfnguide.getFnguideInfo(stock_code);

            } else {

                etfinfo = myweb.getNaverETFinfo(stock_code);
                stockinfo.stock_type = "KETF";
                stockinfo.etfinfo = etfinfo.toString();
                stockinfo.stock_code = etfinfo.stock_code;
                stockinfo.stock_name = etfinfo.stock_name;
                stockinfo.desc = etfinfo.desc;

                news = myweb.getNaverStockNews(stock_code);
                stockinfo.news = news;
                // fnguide정보를 가져온다
                stockinfo.fninfo = myfnguide.getFnguideETFInfo(stock_code);

            }
        return stockinfo;

    }

    public void downloadNowPrice(List<String> stock_list, int hour) {
        int size = stock_list.size();
        String sizestr = Integer.toString(size);
        for(int i =0;i<size;i++) {
            String stock_code = stock_list.get(i);
            _cb.callback("오늘가격" + "\n"+"다운로드" + "\n"+ Integer.toString(i)+ "/"+ sizestr+"\n"+ stock_code);
            myweb.getNaverpriceByToday(stock_code, 6 * hour); // 1시간을 읽어서 저장한다
        }
    }

    public void downloadYFPrice(List<String> stock_list) {
        int size = stock_list.size();
        String sizestr = Integer.toString(size);
        for(int i =0;i<size;i++) {
            String stock_code = stock_list.get(i);
            if(stock_code.equals("")) continue;
            _cb.callback("일년가격" + "\n"+"다운로드" + "\n" +Integer.toString(i)+ "/"+ sizestr+"\n"+ stock_code);
            new YFDownload(stock_code);
        }
    }

    public void downloadTotalInformation(String filename, List<String> stock_list) {
        // 통계재무정보 다운로드
        downloadStockInfoCustom(filename);
        // 하루 가격 다운로드
        downloadNowPrice(stock_list, 3);
        // 년간 가격 다운로드
        downloadYFPrice(stock_list);
    }
}
