package com.andrewlevada.carephone.logic;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class WhitelistAccesser {
    public static List<PhoneNumber> whitelist;

    // TODO: Remove filler
    static {
        whitelist = new ArrayList<>();
        whitelist.add(new PhoneNumber("+7 984 567 24 59", "Человек"));
        whitelist.add(new PhoneNumber("+7 970 687 36 85", "Котттичичечичек"));
        whitelist.add(new PhoneNumber("+7 863 234 23 74", "Hello world"));
    }

    public static boolean isInList(@NonNull String number) {
        number = number.trim().toLowerCase();

        for (PhoneNumber phoneNumber : whitelist) {
            String tempPhoneNumber = phoneNumber.number;
            if (number.equals(tempPhoneNumber)) return true;

            while (number.length() != tempPhoneNumber.length()) {
                if (number.length() > tempPhoneNumber.length()) number = number.substring(1);
                else tempPhoneNumber = tempPhoneNumber.substring(1);
            }

            if (number.equals(tempPhoneNumber)) return true;
        }

        return false;
    }
}
