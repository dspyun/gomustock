package com.gomu.gomustock.stockdb;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "sellstock_table")
public class SellStockDBData implements Serializable
{
    @PrimaryKey(autoGenerate = true)
    private int id;

    // 종목명, 평가손익, 평가금액, 현재가
    // 보유수량, 수익률, 매입금액, 평균단가

    @ColumnInfo(name = "종목코드")
    public String stock_code;

    @ColumnInfo(name = "종목명")
    public String stock_name;

    @ColumnInfo(name = "매도단가")
    public int sell_price;

    @ColumnInfo(name = "매도량")
    public int sell_quantity;

    @ColumnInfo(name = "매도일")
    public String sell_date;

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
    public int getPirce()
    {
        return sell_price;
    }
    public int getQuantity()
    {
        return sell_quantity;
    }

    //public void setName(String text) { this.stock_name = text; }
    public void clear() {
        sell_quantity=0;
        sell_price=0;
        stock_name="";
    }

}

