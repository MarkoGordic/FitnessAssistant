package com.example.fitnessassistant.pedometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.example.fitnessassistant.uiprefs.LocaleExt;

public class DailyRestarter extends BroadcastReceiver {

    // used to update notifications and widgets at midnight
    @Override
    public void onReceive(Context context, Intent intent) {
        LocaleExt.toLangIfDiff(context, PreferenceManager.getDefaultSharedPreferences(context).getString("langPref", "sys"), true, true);
    }
}
