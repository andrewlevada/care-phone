package com.andrewlevada.carephone.logic;

import java.util.Date;

public class LogRecord {
    public String phoneNumber;
    public Date startTimestamp;
    public int secondsDuration;
    public int type;

    public LogRecord(String phoneNumber, Date startTimestamp, int secondsDuration, int type) {
        this.phoneNumber = phoneNumber;
        this.startTimestamp = startTimestamp;
        this.secondsDuration = secondsDuration;
        this.type = type;
    }
}
