package com.gomu.gomustock.stockdb;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SellStockDao {

    @Insert(onConflict = REPLACE)
    void insert(SellStockDBData sellstockdbData);
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    List<Long> insertAll(List<SellStockDBData> sellstockdbData);

    @Delete
    void delete(SellStockDBData sellstockdbData);

    @Delete
    void reset(List<SellStockDBData> sellstockdbData);

    @Query("SELECT * FROM sellstock_table WHERE" + " 매도일 IN (:selldate) AND 종목코드 IN (:stockcode)")
    SellStockDBData getDataByDate(String selldate, String stockcode);

    // 추가해야 할 것 : 매수일, 한꺼번에 update하는 메소드

    @Query("SELECT * FROM sellstock_table")
    List<SellStockDBData> getAll();
}
