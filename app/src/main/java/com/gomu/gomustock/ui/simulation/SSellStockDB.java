package com.gomu.gomustock.ui.simulation;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.gomu.gomustock.portfolio.SellStockDBData;
import com.gomu.gomustock.portfolio.SellStockDao;
@Database(entities = {SellStockDBData.class}, version = 1, exportSchema = false)
public abstract class SSellStockDB extends RoomDatabase{

        private static SSellStockDB ssellstockdb;

        private static String DATABASE_NAME = "ssellstock_db";

        public synchronized static SSellStockDB getInstance(Context context)
        {
            if (ssellstockdb == null)
            {
                ssellstockdb = Room.databaseBuilder(context.getApplicationContext(), SSellStockDB.class, DATABASE_NAME)
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build();
            }
            return ssellstockdb;
        }

        public abstract SellStockDao sellstockDao();

}
