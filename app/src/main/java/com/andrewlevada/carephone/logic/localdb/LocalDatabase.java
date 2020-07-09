package com.andrewlevada.carephone.logic.localdb;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.andrewlevada.carephone.logic.LogRecord;

@Database(entities = { LogRecord.class }, version = 1)
public abstract class LocalDatabase extends RoomDatabase {
    public abstract LogRecordsDao getLogRecordsDao();
}