package com.andrewlevada.carephone.logic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PhoneNumber {
    @NonNull
    public String number;
    public String label;

    public PhoneNumber(@NonNull String number, @Nullable String label) {
        this.number = number;
        this.label = label;
    }
}
