package com.gomu.gomustock.portfolio;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {SellStockDBData.class}, version = 1, exportSchema = false)
public abstract class SellStockDB extends RoomDatabase {

    private static SellStockDB sellstockdb;

    private static String DATABASE_NAME = "sellstock_db";

    public synchronized static SellStockDB getInstance(Context context)
    {
        if (sellstockdb == null)
        {
            sellstockdb = Room.databaseBuilder(context.getApplicationContext(), SellStockDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return sellstockdb;
    }

    public abstract SellStockDao sellstockDao();
}
