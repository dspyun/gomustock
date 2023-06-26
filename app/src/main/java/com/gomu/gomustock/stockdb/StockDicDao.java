package com.gomu.gomustock.stockdb;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StockDicDao {

    @Insert(onConflict = REPLACE)
    void insert(StockDicDBData dicdbData);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    List<Long> insertAll(List<StockDicDBData> dicdbData);

    @Delete
    void delete(StockDicDBData dicdbData);

    @Delete
    void reset(List<StockDicDBData> dicdbData);

    @Update
    void update(StockDicDBData dicdbData);

    @Query("SELECT * FROM stockdic_table WHERE 종목명 LIKE :name")
    StockDicDBData getStockcode(String name);

    @Query("SELECT * FROM stockdic_table WHERE 종목코드 LIKE :code")
    StockDicDBData getStockname(String code);

    @Query("SELECT * FROM stockdic_table")
    List<StockDicDBData> getAll();
}
