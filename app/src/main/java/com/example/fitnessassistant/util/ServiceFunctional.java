package com.example.fitnessassistant.util;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.pedometer.Pedometer;
import com.example.fitnessassistant.pedometer.PedometerWidget;

public class ServiceFunctional {

    // setting pedometer state in shared preferences
    public synchronized static void setPedometerShouldRun(Context context, boolean pedometerShouldRun){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("pedometer_should_run", pedometerShouldRun).apply();
    }

    // get pedometer state from shared preferences
    public synchronized static boolean getPedometerShouldRun(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pedometer_should_run", false);
    }

    public synchronized static void stopPedometerService(Context context){
        // stopping services
        context.stopService(new Intent(context, Pedometer.class));

        if(context instanceof InAppActivity)
            ((InAppActivity) context).setUpHomePageFragmentUI(false);
    }

    public synchronized static void startPedometerService(Context context){
        // starting services
        context.startService(new Intent(context, Pedometer.class));

        ((InAppActivity) context).setUpHomePageFragmentUI(true);

        // update widgets
        for (int id : AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, PedometerWidget.class))) {
            PedometerWidget.updateAppWidget(context, AppWidgetManager.getInstance(context), id, AppWidgetManager.getInstance(context).getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT));
        }
    }
}
