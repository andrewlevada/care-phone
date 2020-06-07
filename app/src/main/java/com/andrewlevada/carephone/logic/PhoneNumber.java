package com.andrewlevada.carephone.logic;

public class PhoneNumber {
    private String phone;
    private String label;

    public PhoneNumber(String phone, String label) {
        this.phone = phone;
        this.label = label;
    }

    public String getPhone() {
        return phone;
    }

    public String getLabel() {
        return label;
    }
}
