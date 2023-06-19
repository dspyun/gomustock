package com.gomu.gomustock.portfolio;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CacheDBData.class}, version = 1, exportSchema = false)
public abstract class CacheDB extends RoomDatabase {
    private static CacheDB cachedb;

    private static String DATABASE_NAME = "cache_db";

    public synchronized static CacheDB getInstance(Context context)
    {
        if (cachedb == null)
        {
            cachedb = Room.databaseBuilder(context.getApplicationContext(), CacheDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return cachedb;
    }

    public abstract CacheDao cacheDao();
}
