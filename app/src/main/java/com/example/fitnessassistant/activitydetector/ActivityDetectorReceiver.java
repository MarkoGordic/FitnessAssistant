package com.example.fitnessassistant.activitydetector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.uiprefs.LocaleExt;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityDetectorReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(ActivityTransitionResult.hasResult(intent)){
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            if (result != null) {
                for(ActivityTransitionEvent event : result.getTransitionEvents()){

                    // TODO Push notification for now, implement complex detection system
                    // update context to newest locale detected
                    context = LocaleExt.toLangIfDiff(context, PreferenceManager.getDefaultSharedPreferences(context).getString("langPref", "sys"), true, true);

                    // get activity type
                    String activityType = "";
                    switch(event.getActivityType()){
                        case DetectedActivity.IN_VEHICLE:
                            activityType = context.getString(R.string.in_vehicle);
                            break;
                        case DetectedActivity.ON_BICYCLE:
                            activityType = context.getString(R.string.on_bicycle);
                            break;
                        case DetectedActivity.ON_FOOT:
                            activityType = context.getString(R.string.on_foot);
                            break;
                        case DetectedActivity.RUNNING:
                            activityType = context.getString(R.string.running);
                            break;
                        case DetectedActivity.STILL:
                            activityType = context.getString(R.string.still);
                            break;
                        case DetectedActivity.TILTING:
                            activityType = context.getString(R.string.tilting);
                            break;
                        case DetectedActivity.UNKNOWN:
                            activityType = context.getString(R.string.unknown);
                            break;
                        case DetectedActivity.WALKING:
                            activityType = context.getString(R.string.walking);
                            break;
                    }

                    ActivityDetector.pushActivityUpdateNotification(context, context.getString(R.string.activity_detected), activityType);
                }
            }
        }
    }
}
