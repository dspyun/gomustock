package com.gomu.gomustock.network;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Investing {

    public String getNews() {

        String news_string="";

        try {
            String URL = "https://kr.investing.com/news/stock-market-news";
            Document doc;
            doc = Jsoup.connect(URL).get();
            Elements articlelist = doc.select(".largeTitle");
            Elements alist = articlelist.select("a");
            int size = alist.size();
            for(int i=0;i<size;i++)  {
                String title = alist.get(i).text();
                if(!title.equals("")) news_string += title + "\n";
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return news_string;
    }
}
