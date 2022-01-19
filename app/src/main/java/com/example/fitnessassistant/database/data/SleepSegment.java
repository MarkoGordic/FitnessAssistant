package com.example.fitnessassistant.database.data;

public class SleepSegment {
    long startTime;
    long endTime;
    int quality;
    int confirmationStatus;

    public int getConfirmationStatus() {
        return confirmationStatus;
    }

    public int getQuality() {
        return quality;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setConfirmationStatus(int confirmationStatus) {
        this.confirmationStatus = confirmationStatus;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
