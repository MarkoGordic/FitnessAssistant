package com.example.fitnessassistant.database;

import java.util.ArrayList;
import java.util.List;

public class PedometerData {
    private List<String> data = new ArrayList<>();

    public PedometerData(){}

    public void setData(List<String> date, List<Integer> steps){
        for(int i = 0; i < date.size(); i++)
            data.add(date.get(i) + steps.get(i));
    }

    public List<String> getData(){
        return data;
    }
}
