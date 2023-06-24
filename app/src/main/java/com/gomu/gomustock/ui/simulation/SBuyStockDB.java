package com.gomu.gomustock.ui.simulation;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.gomu.gomustock.stockdb.BuyStockDBData;
import com.gomu.gomustock.stockdb.BuyStockDao;

@Database(entities = {BuyStockDBData.class}, version = 1, exportSchema = false)
public abstract class SBuyStockDB extends RoomDatabase {

    private static SBuyStockDB sbuystockdb;

    private static String DATABASE_NAME = "sbuystock_db";

    public synchronized static SBuyStockDB getInstance(Context context)
    {
        if (sbuystockdb == null)
        {
            sbuystockdb = Room.databaseBuilder(context.getApplicationContext(), SBuyStockDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return sbuystockdb;
    }

    public abstract BuyStockDao buystockDao();
}
