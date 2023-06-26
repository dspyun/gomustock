package com.gomu.gomustock.stockdb;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stockdic_table")
public class StockDicDBData {


    @PrimaryKey(autoGenerate = true)
    private int id;

    // 종목명, 평가손익, 평가금액, 현재가
    // 보유수량, 수익률, 매입금액, 평균단가

    @ColumnInfo(name = "종목코드")
    public String stock_code;

    @ColumnInfo(name = "종목명")
    public String stock_name;

    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return stock_name;
    }
    public void setName(String name)
    {
        this.stock_name = name;
    }

    //public void setName(String text) { this.stock_name = text; }
    public void clear() {
        stock_code="";
        stock_name="";
    }
}
