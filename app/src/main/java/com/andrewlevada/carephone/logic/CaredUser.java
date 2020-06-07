package com.andrewlevada.carephone.logic;

public class CaredUser {
    private String uid;
    private String phone;

    public CaredUser(String uid, String phone) {
        this.uid = uid;
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public String getPhone() {
        return phone;
    }
}
