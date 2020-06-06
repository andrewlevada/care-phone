package com.andrewlevada.carephone.logic;

public class LogRecord {
    public String phoneNumber;
    public long startTimestamp;
    public int secondsDuration;
    public int type;

    public LogRecord(String phoneNumber, long startTimestamp, int secondsDuration, int type) {
        this.phoneNumber = phoneNumber;
        this.startTimestamp = startTimestamp;
        this.secondsDuration = secondsDuration;
        this.type = type;
    }
}
