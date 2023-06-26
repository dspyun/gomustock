package com.gomu.gomustock.stockdb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


@Database(entities = {StockDicDBData.class}, version = 1, exportSchema = false)
public abstract class StockDicDB extends RoomDatabase {
    private static StockDicDB stockdicdb;

    private static String DATABASE_NAME = "stockdic_db";

    public synchronized static StockDicDB getInstance(Context context)
    {
        if (stockdicdb == null)
        {
            stockdicdb = Room.databaseBuilder(context.getApplicationContext(), StockDicDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return stockdicdb;
    }

    public abstract StockDicDao dicDao();
}
