package com.example.fitnessassistant.database.data;

public class Activity {
    float distance;
    float averageSpeed;
    int caloriesBurnt;
    long date;

    public float getAverageSpeed() {
        return averageSpeed;
    }

    public float getDistance() {
        return distance;
    }

    public int getCaloriesBurnt() {
        return caloriesBurnt;
    }

    public long getDate() {
        return date;
    }

    public void setAverageSpeed(float averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public void setCaloriesBurnt(int caloriesBurnt) {
        this.caloriesBurnt = caloriesBurnt;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
