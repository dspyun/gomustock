package com.gomu.gomustock.ui.simulation;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.gomu.gomustock.stockdb.CacheDBData;
import com.gomu.gomustock.stockdb.CacheDao;
@Database(entities = {CacheDBData.class}, version = 1, exportSchema = false)
public abstract class SCacheDB extends RoomDatabase{


        private static SCacheDB scachedb;

        private static String DATABASE_NAME = "scache_db";

        public synchronized static SCacheDB getInstance(Context context)
        {
            if (scachedb == null)
            {
                scachedb = Room.databaseBuilder(context.getApplicationContext(),SCacheDB.class, DATABASE_NAME)
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build();
            }
            return scachedb;
        }

        public abstract CacheDao cacheDao();
}
