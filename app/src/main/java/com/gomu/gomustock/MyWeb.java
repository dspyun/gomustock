package com.gomu.gomustock;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyWeb {


    String target_stock ="";
    public String result ="";

    public MyWeb() {

    }
    public MyWeb(String stock_no) {
        target_stock = stock_no;
        target_stock = "005930";
    }

    public FormatStockInfo getStockinfo(String stock_code) {
        FormatStockInfo result = new FormatStockInfo();
        try {

            String URL = "https://comp.fnguide.com/SVO2/ASP/SVD_Main.asp?pGB=1&gicode=A"+stock_code+"&cID=&MenuYn=Y&ReportGB=&NewMenuID=101&stkGb=701";
            Document doc;
            doc = Jsoup.connect(URL).get();
            Elements classinfo = doc.select("#corp_group2");
            Elements dd_list = classinfo.select("dd");

            result.per = dd_list.get(1).text();
            result.per12 = dd_list.get(3).text();
            result.area_per = dd_list.get(5).text();
            result.pbr = dd_list.get(7).text();
            result.div_rate = dd_list.get(9).text();

            Elements classinfo1 = doc.select("#svdMainGrid1");
            Elements tbody_list = classinfo1.select("tbody");
            Elements tr_list = tbody_list.select("tr");;
            Elements td_list = tr_list.get(2).select("td");;
            Element th1 = td_list.get(1);;
            result.fogn_rate = th1.text();;

            Elements td2_list = tr_list.get(3).select("td");;
            Element th2 = td2_list.get(1);;
            result.beta = th2.text();

            Elements classinfo2 = doc.select("#svdMainGrid2");
            Elements tbody1_list = classinfo2.select("tbody");
            Elements td1_list = tbody1_list.select("td");;
            result.op_profit = td1_list.get(3).text();;

/*
            System.out.println("per = " + result.per+"\n");
            System.out.println("per12 = " + result.per12+"\n");
            System.out.println("area_per = " + result.area_per+"\n");
            System.out.println("pbr = " + result.pbr+"\n");
            System.out.println("div_rate = " +result.div_rate+"\n");
            System.out.println("fogn_rate = " + result.fogn_rate+"\n");
            System.out.println("beta = " + result.beta+"\n");
            System.out.println("op_profit = " + result.op_profit+"\n");
*/
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public List<List<String>> getAgencyFogn(String stock_code, String pageno) {

        List<String> agent = new ArrayList<>();
        List<String> fogn = new ArrayList<>();
        try {
            String pagenumber;
            if(pageno.equals("0")) pagenumber="";
            else pagenumber = "&page="+pageno;

            String URL = "https://finance.naver.com/item/frgn.naver?code="+stock_code+pagenumber;
            Document doc;
            doc = Jsoup.connect(URL).get();
            Elements classinfo = doc.select(".inner_sub");
            Element table1 = classinfo.select("table").get(1);
            Elements trlist = table1.select("tr");

            for(int i = 3;i<8;i++) {
                Element tr3 = trlist.get(i);
                Elements tdlist = tr3.select("td");
                agent.add(tdlist.get(5).text());
                fogn.add(tdlist.get(6).text());
            }
            for(int i = 11;i<16;i++) {
                Element tr3 = trlist.get(i);
                Elements tdlist = tr3.select("td");
                agent.add(tdlist.get(5).text());
                fogn.add(tdlist.get(6).text());
            }
            for(int i = 19;i<24;i++) {
                Element tr3 = trlist.get(i);
                Elements tdlist = tr3.select("td");
                agent.add(tdlist.get(5).text());
                fogn.add(tdlist.get(6).text());
            }
            for(int i = 27;i<32;i++) {
                Element tr3 = trlist.get(i);
                Elements tdlist = tr3.select("td");
                agent.add(tdlist.get(5).text());
                fogn.add(tdlist.get(6).text());
            }
            int j = 0;
;/*
            System.out.println("per = " + result.per+"\n");
            System.out.println("per12 = " + result.per12+"\n");
            System.out.println("area_per = " + result.area_per+"\n");
            System.out.println("pbr = " + result.pbr+"\n");
            System.out.println("div_rate = " +result.div_rate+"\n");
            System.out.println("fogn_rate = " + result.fogn_rate+"\n");
            System.out.println("beta = " + result.beta+"\n");
            System.out.println("op_profit = " + result.op_profit+"\n");
*/
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<List<String>> result = new ArrayList<List<String>>();
        result.add(agent);
        result.add(fogn);
        return result;
    }

    public String getCurrentStockPrice(String stockcode) {

        //https://jul-liet.tistory.com/209
        String stockprice="";
        //https://finance.naver.com/sise/sise_index.naver?code=KPI200
        String URL = "https://finance.naver.com/item/main.nhn?code=" + stockcode;
        Document doc;

        try {
            doc = Jsoup.connect(URL).get();
            Elements elem = doc.select(".date");
            String[] str = elem.text().split(" ");

            Elements todaylist =doc.select(".new_totalinfo dl>dd");

            String juga = todaylist.get(3).text().split(" ")[1];

            String DungRakrate = todaylist.get(3).text().split(" ")[6];
            String siga =  todaylist.get(5).text().split(" ")[1];
            String goga = todaylist.get(6).text().split(" ")[1];
            String zeoga = todaylist.get(8).text().split(" ")[1];
            String georaeryang = todaylist.get(10).text().split(" ")[1];

            String stype = todaylist.get(3).text().split(" ")[3]; //상한가,상승,보합,하한가,하락 구분

            String vsyesterday = todaylist.get(3).text().split(" ")[4];

            stockprice = juga;
            System.out.println(stockcode + " 주가 : "+juga);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return stockprice;
    }

    public String getCurrentKosp200() {

        //https://jul-liet.tistory.com/209
        String stockprice="";
        String URL = "https://finance.naver.com/sise/sise_index.naver?code=KPI200";
        Document doc;

        try {
            doc = Jsoup.connect(URL).get();

            Elements todaylist =doc.select(".subtop_sise_detail");

            Element tdlist = todaylist.select("tr").get(0);
            Element kospi200 = tdlist.select("td").get(0);
            return kospi200.text();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("반환값 "+stockprice);

        return stockprice;
    }

    public void dl_fogninfo(List<String> buylist) {

        for(int i =0;i<buylist.size();i++) {
            MyExcel myexcel = new MyExcel();
            List<List<String>> value = new ArrayList<List<String>>();
            List<String> agency = new ArrayList<>();
            List<String> fogn = new ArrayList<>();

            String stock_code = buylist.get(i);
            value = getAgencyFogn(stock_code,"0");
            agency.addAll(value.get(0));
            fogn.addAll(value.get(1));

            value = getAgencyFogn(stock_code,"2");
            agency.addAll(value.get(0));
            fogn.addAll(value.get(1));

            value = getAgencyFogn(stock_code,"3");
            agency.addAll(value.get(0));
            fogn.addAll(value.get(1));

            value = getAgencyFogn(stock_code,"4");
            agency.addAll(value.get(0));
            fogn.addAll(value.get(1));

            value = getAgencyFogn(stock_code,"5");
            agency.addAll(value.get(0));
            fogn.addAll(value.get(1));

            agency.add(0,"AGENCY");
            fogn.add(0,"FOREIgN");
            myexcel.writefogninfo(stock_code, fogn, agency);
        }
    }

    public void getNaverprice30(String stock_code) {
        List<FormatOHLCV> naverpricelist = new ArrayList<>();
        String page="";
        for(int i =0;i<3;i++) {
            page = Integer.toString(i);
            naverpricelist.addAll(getPrice10(stock_code,page));
        }
        FormatOHLCV naverheader = new FormatOHLCV();
        naverheader.date = "date";
        naverheader.close = "close";
        naverheader.open = "open";
        naverheader.high = "high";
        naverheader.low = "low";
        naverheader.volume = "volume";
        naverpricelist.add(0,naverheader);
        MyExcel myexcel = new MyExcel();
        myexcel.writeprice(stock_code,naverpricelist);
    }

    public List<FormatOHLCV> getPrice10(String stock_code, String pageno) {

        List<String> agent = new ArrayList<>();
        List<String> fogn = new ArrayList<>();
        List<FormatOHLCV> naverpricelist = new ArrayList<>();

        try {
            String pagenumber;
            if(pageno.equals("0")) pagenumber="";
            else pagenumber = "&page="+pageno;
            String URL = "https://finance.naver.com/item/sise_day.naver?code="+stock_code+pagenumber;
            Document doc;
            doc = Jsoup.connect(URL).get();
            Elements trlist = doc.select("tr");
            for(int i =2;i<7;i++ ){
                FormatOHLCV naverprice = new FormatOHLCV();
                Elements tdlist = trlist.get(i).select("td");
                naverprice.date = tdlist.get(0).text().replaceAll("\\.","");
                naverprice.close = tdlist.get(1).text().replaceAll(",", "");
                naverprice.open = tdlist.get(3).text().replaceAll(",", "");
                naverprice.high = tdlist.get(4).text().replaceAll(",", "");
                naverprice.low = tdlist.get(5).text().replaceAll(",", "");
                naverprice.volume = tdlist.get(6).text().replaceAll(",", "");
                naverpricelist.add(naverprice);
            }
            for(int i =10;i<15;i++ ){
                FormatOHLCV naverprice = new FormatOHLCV();
                Elements tdlist = trlist.get(i).select("td");
                naverprice.date = tdlist.get(0).text().replaceAll("\\.","");
                naverprice.close = tdlist.get(1).text().replaceAll(",", "");
                naverprice.open = tdlist.get(3).text().replaceAll(",", "");
                naverprice.high = tdlist.get(4).text().replaceAll(",", "");
                naverprice.low = tdlist.get(5).text().replaceAll(",", "");
                naverprice.volume = tdlist.get(6).text().replaceAll(",", "");
                naverpricelist.add(naverprice);
            }

            int j = 0;
            ;/*
            System.out.println("per = " + result.per+"\n");
            System.out.println("per12 = " + result.per12+"\n");
            System.out.println("area_per = " + result.area_per+"\n");
            System.out.println("pbr = " + result.pbr+"\n");
            System.out.println("div_rate = " +result.div_rate+"\n");
            System.out.println("fogn_rate = " + result.fogn_rate+"\n");
            System.out.println("beta = " + result.beta+"\n");
            System.out.println("op_profit = " + result.op_profit+"\n");
*/
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        };
        return naverpricelist;
    }


}
