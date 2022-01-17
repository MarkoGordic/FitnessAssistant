package com.example.fitnessassistant.database.data;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

public class ActivityRecycler {
    public final static int ACTIVITY_RUNNING = 1;
    public final static int ACTIVITY_WALKING = 2;
    public final static int ACTIVITY_CYCLING = 3;

    private int id;
    private float distance;
    private float averageSpeed;
    private float caloriesBurnt;
    private long date;
    private int activityType;
    String duration;
    Bitmap image;

    public int getId() {
        return id;
    }

    public String getDuration() {
        return duration;
    }

    public Bitmap getImage() {
        return image;
    }

    public int getActivityType() {
        return activityType;
    }

    public float getAverageSpeed() {
        return averageSpeed;
    }

    public float getDistance() {
        return distance;
    }

    public float getCaloriesBurnt() {
        return caloriesBurnt;
    }

    public long getDate() {
        return date;
    }

    public void setAverageSpeed(float averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public void setCaloriesBurnt(float caloriesBurnt) {
        this.caloriesBurnt = caloriesBurnt;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public void setActivityType(int activityType) {
        this.activityType = activityType;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(!(obj instanceof ActivityRecycler))
            return false;
        ActivityRecycler other = (ActivityRecycler) obj;
        return id == other.id
                && distance == other.distance
                && averageSpeed == other.averageSpeed
                && caloriesBurnt == other.caloriesBurnt
                && date == other.date
                && activityType == other.activityType
                && duration.equals(other.duration)
                && image.equals(other.image);
    }
}
