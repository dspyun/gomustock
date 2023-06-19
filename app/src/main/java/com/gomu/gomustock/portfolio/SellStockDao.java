package com.gomu.gomustock.portfolio;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SellStockDao {

    @Insert(onConflict = REPLACE)
    void insert(SellStockDBData sellstockdbData);

    @Delete
    void delete(SellStockDBData sellstockdbData);

    @Delete
    void reset(List<SellStockDBData> sellstockdbData);

    @Query("UPDATE sellstock_table SET 종목명 = :stock_name WHERE ID = :sID")
    void update_name(int sID, String stock_name);

    @Query("UPDATE sellstock_table SET 매도단가 = :sell_price WHERE ID = :sID")
    void update_price(int sID, int sell_price);

    @Query("UPDATE sellstock_table SET 매도량 = :sell_quantity WHERE ID = :sID")
    void update_quantity(int sID, int sell_quantity);

    // 추가해야 할 것 : 매수일, 한꺼번에 update하는 메소드

    @Query("SELECT * FROM sellstock_table")
    List<SellStockDBData> getAll();
}
