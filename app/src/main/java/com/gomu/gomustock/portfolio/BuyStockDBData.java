package com.gomu.gomustock.portfolio;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "buystock_table")
public class BuyStockDBData implements Serializable
{
    @PrimaryKey(autoGenerate = true)
    private int id;

    // 종목명, 평가손익, 평가금액, 현재가
    // 보유수량, 수익률, 매입금액, 평균단가

    @ColumnInfo(name = "종목번호")
    public String stock_code;

    @ColumnInfo(name = "종목명")
    public String stock_name;

    @ColumnInfo(name = "매수단가")
    public int buy_price;

    @ColumnInfo(name = "매수량")
    public int buy_quantity;

    @ColumnInfo(name = "매수일")
    public String buy_date;

    @ColumnInfo(name = "현재가")
    public int cur_price;

    public int getCurPrice()
    {
        return cur_price;
    }
    public void setCurPrice(int price)
    {
        cur_price = price;
    }

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
    public int getPrice()
    {
        return buy_price;
    }
    public void setPrice(int price)
    {
        buy_price = price;
    }
    public int getQuantity()
    {
        return buy_quantity;
    }
    public void setQuantity(int quantity)
    {
        buy_quantity = quantity;
    }

    public String getDate()
    {
        return buy_date;
    }
    public void setDate(String date)
    {
        buy_date = date;
    }
    public String getStockNo()
    {
        return stock_code;
    }
    public void setStockNo(String no)
    {
        stock_code = no;
    }
    //public void setName(String text) { this.stock_name = text; }

}
