package com.gomu.gomustock;

public class FormatOHLCV {
    // open api 주식정보 포맹
    // 아래 정보를 엑셀에 저장한다
    public String date; // basDt 날짜
    public String open; // mkp 시가
    public String high; // hipr 고가
    public String low; // lopr 저가
    public String close; // clpr 종가
    public String volume; // trqu 거래량

    public FormatOHLCV() {
        date="";
        open="";
        high = "";
        low="";
        close="";
        volume="";
    }
    public void set(String date1, String open1, String high1, String low1, String close1, String volume1) {
        date = date1;
        open = open1;
        high = high1;
        low = low1;
        close =close1;
        volume = volume1;
    }
    public void setheader() {
        date="DATE";
        open="OPEN";
        high="HIGH";
        low="LOW";
        close="CLOSE";
        volume="VOLUME";
    }
}
