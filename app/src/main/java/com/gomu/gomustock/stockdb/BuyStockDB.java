package com.gomu.gomustock.stockdb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {BuyStockDBData.class}, version = 1, exportSchema = false)
public abstract class BuyStockDB extends RoomDatabase
{
    private static BuyStockDB buystockdb;

    private static String DATABASE_NAME = "buystock_db";

    public synchronized static BuyStockDB getInstance(Context context)
    {
        if (buystockdb == null)
        {
            buystockdb = Room.databaseBuilder(context.getApplicationContext(), BuyStockDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return buystockdb;
    }

    public abstract BuyStockDao buystockDao();
}