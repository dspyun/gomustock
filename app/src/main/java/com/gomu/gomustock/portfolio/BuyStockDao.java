package com.gomu.gomustock.portfolio;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// 쿼리문을 method와 연결시켜주는 곳
// 쿼리문을 알아야 한다

@Dao
public interface BuyStockDao {
    @Insert(onConflict = REPLACE)
    void insert(BuyStockDBData buystockdbData);
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    List<Long> insertAll(List<BuyStockDBData> buystockdbData);

    @Delete
    void delete(BuyStockDBData buystockdbData);

    @Delete
    void reset(List<BuyStockDBData> buystockdbData);

    @Update
    void update(BuyStockDBData buystockdbData);

    @Query("SELECT * FROM buystock_table WHERE" + " 매수일 IN (:buydate) AND 종목코드 IN (:stockcode)")
    BuyStockDBData getDataByDate(String buydate, String stockcode);

    @Query("SELECT * FROM buystock_table")
    List<BuyStockDBData> getAll();
}