package com.andrewlevada.carephone.logic.localdb;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.andrewlevada.carephone.logic.LogRecord;

import java.util.List;

@Dao
public interface LogRecordsDao {

    @Insert
    void insert(LogRecord logRecords);

    @Query("SELECT * FROM logrecord ORDER BY startTimestamp DESC LIMIT :limit OFFSET :offset")
    List<LogRecord> getAllPeopleWithFavoriteColor(int limit, int offset);
}
