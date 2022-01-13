package com.example.fitnessassistant.database.data;

import android.content.Context;

import com.example.fitnessassistant.database.mdbh.MDBHActivityTracker;

import java.util.List;

public class ActivityData {
    public List<Activity> activities;

    public ActivityData(){}

    public ActivityData(Context context){
        activities = MDBHActivityTracker.getInstance(context).readActivitiesDataDB();
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }
}
