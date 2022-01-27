package com.example.fitnessassistant.database.data;

public class Activity {
    float distance;
    float averageSpeed;
    float caloriesBurnt;
    long date;
    int activityType;
    String duration;
    int id;

    public int getId() {
        return id;
    }

    public String getDuration() {
        return duration;
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

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setId(int id) {
        this.id = id;
    }
}