package com.andrewlevada.carephone.logic;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return Objects.equals(phone, that.phone) &&
                Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phone, label);
    }
}
