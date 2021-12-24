package com.example.fitnessassistant.util;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.example.fitnessassistant.pedometer.Pedometer;
import com.example.fitnessassistant.pedometer.PedometerWidget;

public class ServiceFunctional {

    public static void stopPedometerService(Context context){
        // stopping the service
        context.stopService(new Intent(context, Pedometer.class));

        // update widgets
        for (int id : AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, PedometerWidget.class))) {
            PedometerWidget.updateAppWidget(context, AppWidgetManager.getInstance(context), id, AppWidgetManager.getInstance(context).getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT), true);
        }
    }

    public static void startPedometerService(Context context){
        // starting the service
        context.startService(new Intent(context, Pedometer.class));

        // update widgets
        for (int id : AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, PedometerWidget.class))) {
            PedometerWidget.updateAppWidget(context, AppWidgetManager.getInstance(context), id, AppWidgetManager.getInstance(context).getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT), false);
        }
    }
}
