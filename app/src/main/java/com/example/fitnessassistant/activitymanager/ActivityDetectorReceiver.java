package com.example.fitnessassistant.activitymanager;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;

public class ActivityDetectorReceiver extends android.content.BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(ActivityTransitionResult.hasResult(intent)){
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            if (result != null) {
                for(ActivityTransitionEvent event : result.getTransitionEvents()){
                    // Push notification for now,
                    // implement complex detection system
                    ActivityDetector.pushActivityUpdateNotification(context, "Activity detected!", String.valueOf(event.getActivityType()));
                }
            }
        }
    }
}
