package com.foohyfooh.bb8.notifications;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

@Dao
public interface ConfigDao {

    @Insert
    void insert(Config config);

    @Query("SELECT * FROM " + Config.TABLE_NAME + " WHERE " + Config.COLUMN_PACKAGE  + " = :packageName")
    Config get(String packageName);

    @Update
    void update(Config config);

    @Query("DELETE FROM " + Config.TABLE_NAME + " WHERE " + Config.COLUMN_PACKAGE + " = :packageName")
    void delete(String packageName);

}
