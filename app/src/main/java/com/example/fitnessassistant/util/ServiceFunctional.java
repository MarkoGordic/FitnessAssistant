package com.example.fitnessassistant.util;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.pedometer.Pedometer;
import com.example.fitnessassistant.pedometer.PedometerWidget;
import com.example.fitnessassistant.sleeptracker.SleepTracker;

public class ServiceFunctional {

    // setting pedometer state in shared preferences
    public synchronized static void setPedometerShouldRun(Context context, boolean pedometerShouldRun){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("pedometer_should_run", pedometerShouldRun).apply();
    }

    // get pedometer state from shared preferences
    public synchronized static boolean getPedometerShouldRun(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pedometer_should_run", false);
    }

    // setting pedometer state in shared preferences
    public synchronized static void setSleepTrackerShouldRun(Context context, boolean sleepTrackerShouldRun){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("sleepTracker_should_run", sleepTrackerShouldRun).apply();
    }

    // get pedometer state from shared preferences
    public synchronized static boolean getSleepTrackerShouldRun(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("sleepTracker_should_run", false);
    }

    public synchronized static void stopPedometerService(Context context){
        // stopping services
        context.stopService(new Intent(context, Pedometer.class));

        if(context instanceof InAppActivity)
            ((InAppActivity) context).setUpPedometerFragmentUI(false);
    }

    public synchronized static void startPedometerService(Context context){
        // starting services
        context.startService(new Intent(context, Pedometer.class));

        if(context instanceof InAppActivity)
            ((InAppActivity) context).setUpPedometerFragmentUI(true);

        // setting here too because we're updating widgets (they need to have sync information)
        setPedometerShouldRun(context, true);

        // update widgets
        for (int id : AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, PedometerWidget.class))) {
            PedometerWidget.updateAppWidget(context, AppWidgetManager.getInstance(context), id, AppWidgetManager.getInstance(context).getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT));
        }
    }

    public synchronized static void stopSleepTrackerService(Context context){
        // stopping services
        context.stopService(new Intent(context, SleepTracker.class));

        if(context instanceof InAppActivity)
            ((InAppActivity) context).setUpSleepFragmentUI(false);
    }

    public synchronized static void startSleepTrackerService(Context context){
        // starting services
        context.startService(new Intent(context, SleepTracker.class));

        if(context instanceof InAppActivity)
            ((InAppActivity) context).setUpSleepFragmentUI(true);

        setSleepTrackerShouldRun(context, true);
    }
}
