package com.example.fitnessassistant.database.data;

import java.util.ArrayList;
import java.util.List;

public class SleepData {
    public List<String> data = new ArrayList<>();

    public SleepData(){}

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
