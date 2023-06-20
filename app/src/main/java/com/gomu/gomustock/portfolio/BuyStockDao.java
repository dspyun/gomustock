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

    @Query("UPDATE buystock_table SET 종목명 = :sText WHERE ID = :sID")
    void update(int sID, String sText);

    @Query("UPDATE buystock_table SET 매수단가 = :buy_price WHERE ID = :sID")
    void update_price(int sID, int buy_price);

    @Query("UPDATE buystock_table SET 매수량 = :buy_quantity WHERE ID = :sID")
    void update_quantity(int sID, int buy_quantity);

    @Query("SELECT * FROM buystock_table")
    List<BuyStockDBData> getAll();
}