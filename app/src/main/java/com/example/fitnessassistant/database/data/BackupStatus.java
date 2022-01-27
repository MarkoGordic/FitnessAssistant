package com.example.fitnessassistant.database.data;

public class BackupStatus {
    String pedometer;
    String userPreferences;
    String userGoals;
    String sleepTracker;
    String activitiesTracker;
    String products;
    String meals;

    public String getMeals() {
        return meals;
    }

    public String getProducts() {
        return products;
    }

    public String getActivitiesTracker() {
        return activitiesTracker;
    }

    public String getPedometer() {
        return pedometer;
    }

    public String getSleepTracker() {
        return sleepTracker;
    }

    public String getUserGoals() {
        return userGoals;
    }

    public String getUserPreferences() {
        return userPreferences;
    }

    public void setActivitiesTracker(String activitiesTracker) {
        this.activitiesTracker = activitiesTracker;
    }

    public void setPedometer(String pedometer) {
        this.pedometer = pedometer;
    }

    public void setSleepTracker(String sleepTracker) {
        this.sleepTracker = sleepTracker;
    }

    public void setUserGoals(String userGoals) {
        this.userGoals = userGoals;
    }

    public void setUserPreferences(String userPreferences) {
        this.userPreferences = userPreferences;
    }

    public void setMeals(String meals) {
        this.meals = meals;
    }

    public void setProducts(String products) {
        this.products = products;
    }
}
