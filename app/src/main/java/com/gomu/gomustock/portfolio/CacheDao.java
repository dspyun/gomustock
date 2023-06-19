package com.gomu.gomustock.portfolio;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CacheDao {

    @Insert(onConflict = REPLACE)
    void insert(CacheDBData cachedbData);

    @Delete
    void delete(CacheDBData cachedbData);

    @Update
    void update(CacheDBData cachedbData);

    @Query("DELETE FROM cache_table")
    void reset();

    @Query("UPDATE cache_table SET 잔금 = :remain_cache WHERE ID = :sID")
    void update_cache(int sID, int remain_cache);

    @Query("SELECT * FROM cache_table")
    List<CacheDBData> getAll();
}
