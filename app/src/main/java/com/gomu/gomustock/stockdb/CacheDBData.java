package com.gomu.gomustock.stockdb;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cache_table")
public class CacheDBData {
    // db id는 1부터 시작한다
    @PrimaryKey(autoGenerate = true)
    private int id;

    // 종목명, 평가손익, 평가금액, 현재가
    // 보유수량, 수익률, 매입금액, 평균단가

    @ColumnInfo(name = "잔금")
    public int remain_cache;

    @ColumnInfo(name = "원금")
    public int first_cache;

    public int getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }

    public int getRemain()
    {
        return remain_cache;
    }
    public void setRemain(int price)
    {
        remain_cache = price;
    }

    public int getFirstcache()
    {
        return first_cache;
    }
    public void setFirstcache(int cache) {
        first_cache = cache;
    }
}
