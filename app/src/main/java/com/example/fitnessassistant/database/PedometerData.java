package com.example.fitnessassistant.database;

import java.util.ArrayList;
import java.util.List;

public class PedometerData {
    private List<String> pedometer = new ArrayList<>();

    public PedometerData(){}

    public void setData(List<String> date, List<Float> steps){
        for(int i = 0; i < date.size(); i++)
            pedometer.add(date.get(i) + "#" + steps.get(i));
    }

    public void setData(List<String> data){
        pedometer = data;
    }

    public List<String> getData(){
        return pedometer;
    }
}
