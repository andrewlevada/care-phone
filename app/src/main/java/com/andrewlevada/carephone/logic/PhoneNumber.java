package com.andrewlevada.carephone.logic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PhoneNumber {
    @NonNull
    public String phone;
    public String label;

    public PhoneNumber(@NonNull String phone, @Nullable String label) {
        this.phone = phone;
        this.label = label;
    }
}
