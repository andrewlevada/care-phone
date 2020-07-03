package com.andrewlevada.carephone.logic;

import java.util.List;

public class StatisticsPack {
    private List<Integer> periodsMinutes;
    private List<String> phonesLabels;
    private List<Integer> phonesMinutes;

    public StatisticsPack(List<Integer> periodsMinutes, List<String> phonesLabels, List<Integer> phonesMinutes) {
        this.periodsMinutes = periodsMinutes;
        this.phonesLabels = phonesLabels;
        this.phonesMinutes = phonesMinutes;
    }

    public List<Integer> getPeriodsMinutes() {
        return periodsMinutes;
    }

    public List<String> getPhonesLabels() {
        return phonesLabels;
    }

    public List<Integer> getPhonesMinutes() {
        return phonesMinutes;
    }
}
