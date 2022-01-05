package com.example.fitnessassistant.pedometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.fitnessassistant.R;

public class DailyRestarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Pedometer.pushPedometerNotification(context, "0 " + context.getString(R.string.steps_small), context.getString(R.string.your_today_goal) + " " + context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).getInt("dailyStepGoal", 10000) + ".");
        Pedometer.updatePedometerWidgetData(context, 0, null);
    }
}
