package com.andrewlevada.carephone.logic;

public class LogRecord {
    private String phoneNumber;
    private long startTimestamp;
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
}
