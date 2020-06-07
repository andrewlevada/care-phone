package com.andrewlevada.carephone.logic;

import java.util.List;

public class StatisticsPack {
    private List<Integer> periodsHours;
    private List<String> phonesLabels;
    private List<Integer> phonesHours;

    public StatisticsPack(List<Integer> periodsHours, List<String> phonesLabels, List<Integer> phonesHours) {
        this.periodsHours = periodsHours;
        this.phonesLabels = phonesLabels;
        this.phonesHours = phonesHours;
    }

    public List<Integer> getPeriodsHours() {
        return periodsHours;
    }

    public List<String> getPhonesLabels() {
        return phonesLabels;
    }

    public List<Integer> getPhonesHours() {
        return phonesHours;
    }
}
