package com.example.fitnessassistant.database.data;

import java.util.ArrayList;
import java.util.List;

public class PedometerData {
    private List<String> pedometer = new ArrayList<>();

    public PedometerData(){}

    public void setData(List<String> data){
        pedometer = data;
    }

    public List<String> getData(){
        return pedometer;
    }
}
