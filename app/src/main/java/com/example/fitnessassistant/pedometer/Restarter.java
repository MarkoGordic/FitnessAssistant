package com.example.fitnessassistant.pedometer;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class Restarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case "restartservice":
            case Intent.ACTION_BOOT_COMPLETED:
                context.startForegroundService(new Intent(context, Pedometer.class));
                // update available widgets
                for (int id : AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, PedometerWidget.class))) {
                    PedometerWidget.updateAppWidget(context, AppWidgetManager.getInstance(context), id, AppWidgetManager.getInstance(context).getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT), PedometerWidget.defaultBehavior);
                }
                break;
        }

    }
}