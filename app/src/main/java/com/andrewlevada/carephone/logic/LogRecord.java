package com.andrewlevada.carephone.logic;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LogRecord {
    private String phoneNumber;
    @PrimaryKey private long startTimestamp;
    private int secondsDuration;
    private int type;

    public LogRecord(String phoneNumber, long startTimestamp, int secondsDuration, int type) {
        this.phoneNumber = phoneNumber;
        this.startTimestamp = startTimestamp;
        this.secondsDuration = secondsDuration;
        this.type = type;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public int getSecondsDuration() {
        return secondsDuration;
    }

    public int getType() {
        return type;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public void setSecondsDuration(int secondsDuration) {
        this.secondsDuration = secondsDuration;
    }

    public void setType(int type) {
        this.type = type;
    }
}
