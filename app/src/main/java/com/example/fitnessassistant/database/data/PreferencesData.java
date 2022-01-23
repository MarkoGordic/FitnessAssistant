package com.example.fitnessassistant.database.data;

import android.content.Context;

import com.example.fitnessassistant.database.mdbh.MDBHWeight;
import com.example.fitnessassistant.questions.BirthdayFragment;
import com.example.fitnessassistant.questions.GenderFragment;
import com.example.fitnessassistant.questions.HeightFragment;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;

import java.util.ArrayList;
import java.util.List;

public class PreferencesData {
    private float height;
    private String gender;
    private int birthday;
    private List<String> units = new ArrayList<>();
    private List<String> weights = new ArrayList<>();

    public PreferencesData(){}

    public void setData(Context context){
        this.height = HeightFragment.getHeight(context);
        this.gender = GenderFragment.getGender(context);
        this.birthday = BirthdayFragment.getBirthday(context);

        units.add(UnitPreferenceFragment.getHeightUnit(context));
        units.add(UnitPreferenceFragment.getWeightUnit(context));
        units.add(UnitPreferenceFragment.getDistanceUnit(context));
        units.add(UnitPreferenceFragment.getFluidUnit(context));
        units.add(UnitPreferenceFragment.getEnergyUnit(context));

        weights = MDBHWeight.getInstance(context).readWeightDB();
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

    public int getBirthday() {
        return birthday;
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

    public void setBirthday(int birthday) {
        this.birthday = birthday;
    }
}
