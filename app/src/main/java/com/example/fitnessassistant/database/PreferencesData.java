package com.example.fitnessassistant.database;

import android.content.Context;
import android.util.Pair;

import com.example.fitnessassistant.questions.GenderFragment;
import com.example.fitnessassistant.questions.HeightFragment;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;
import com.example.fitnessassistant.questions.WeightFragment;

import java.util.ArrayList;
import java.util.List;

public class PreferencesData {
    private float height;
    private String gender;
    private List<String> units = new ArrayList<>();
    private List<String> weights = new ArrayList<>();

    public PreferencesData(){}

    public void setData(Context context){
        this.height = HeightFragment.getHeight(context);
        this.gender = GenderFragment.getGender(context);

        units.add(UnitPreferenceFragment.getHeightUnit(context));
        units.add(UnitPreferenceFragment.getWeightUnit(context));
        units.add(UnitPreferenceFragment.getDistanceUnit(context));
        units.add(UnitPreferenceFragment.getFluidUnit(context));
        units.add(UnitPreferenceFragment.getEnergyUnit(context));

        weights = WeightFragment.getWeightsForDB(context);
    }

    public float getHeight() {
        return height;
    }

    public String getGender() {
        return gender;
    }

    public List<String> getUnits() {
        return units;
    }

    public List<String> getWeights() {
        return weights;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setUnits(List<String> units) {
        this.units = units;
    }

    public void setWeights(List<String> weights) {
        this.weights = weights;
    }
}
