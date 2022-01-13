package com.example.fitnessassistant.database.data;

import android.content.Context;

import com.example.fitnessassistant.database.mdbh.MDBHActivityTracker;

import java.util.List;

public class ActivityData {
    public List<Activity> activities;
    public List<Integer> ids;

    public ActivityData(){}

    public ActivityData(Context context){
        activities = MDBHActivityTracker.getInstance(context).readActivitiesDataDB();
        ids = MDBHActivityTracker.getInstance(context).readActivitiesIDs();
    }

    public List<Integer> getIds() {
        return ids;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }
}
