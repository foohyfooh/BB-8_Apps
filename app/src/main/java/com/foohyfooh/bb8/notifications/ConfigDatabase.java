package com.foohyfooh.bb8.notifications;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Config.class}, version = 1)
public abstract class ConfigDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "bb8";

    private static volatile ConfigDatabase instance;

    public abstract ConfigDao dao();

    public static ConfigDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (ConfigDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            ConfigDatabase.class, DATABASE_NAME)
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return instance;
    }



}
