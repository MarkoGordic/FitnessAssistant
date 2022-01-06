package com.example.fitnessassistant.pedometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.uiprefs.LocaleExt;

public class DailyRestarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Context updatedContext = LocaleExt.toLangIfDiff(context, PreferenceManager.getDefaultSharedPreferences(context).getString("langPref", "sys"), true, true);

        Pedometer.pushPedometerNotification(updatedContext, "0 " + updatedContext.getString(R.string.steps_small), updatedContext.getString(R.string.your_today_goal) + " " + StepGoalFragment.getStepGoalForToday(updatedContext) + ".");
        Pedometer.updatePedometerWidgetData(updatedContext, 0, null);
    }
}
