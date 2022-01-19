package com.example.fitnessassistant.pedometer;

import static com.example.fitnessassistant.util.TimeFunctional.getCurrentDateFormatted;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.database.mdbh.MDBHPedometer;
import com.example.fitnessassistant.sleeptracker.SleepTracker;
import com.example.fitnessassistant.uiprefs.LocaleExt;
import com.example.fitnessassistant.util.ServiceFunctional;
import com.google.firebase.auth.FirebaseAuth;

public class Restarter extends BroadcastReceiver {

    // this handles the case when user restarts their phone (Service will be called again when phone boots)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if(FirebaseAuth.getInstance().getCurrentUser() != null) {
                // getting updated context
                Context updatedContext = LocaleExt.toLangIfDiff(context, PreferenceManager.getDefaultSharedPreferences(context).getString("langPref", "sys"), true, true);

                // putting step goal for previous day
                StepGoalFragment.putUnsavedStepGoals(updatedContext);

                // updating widget and pedometer with today's data
                Pedometer.updatePedometerWidgetData(updatedContext, ((int) MDBHPedometer.getInstance(updatedContext).readPedometerSteps(getCurrentDateFormatted())), null);

                // starting pedometer foreground service
                if (ServiceFunctional.getPedometerShouldRun(updatedContext)) {
                    context.startForegroundService(new Intent(updatedContext, Pedometer.class));
                    Pedometer.pushPedometerNotification(updatedContext, ((int) MDBHPedometer.getInstance(updatedContext).readPedometerSteps(getCurrentDateFormatted())) + " " + updatedContext.getString(R.string.steps_small), updatedContext.getString(R.string.your_today_goal) + " " + StepGoalFragment.getStepGoalForToday(updatedContext) + ".");
                }
                if (ServiceFunctional.getSleepTrackerShouldRun(updatedContext)) {
                    context.startForegroundService(new Intent(updatedContext, SleepTracker.class));
//                    TODO push correct notification
//                    SleepTracker.pushSleepDetectedNotification();
                }
            }
        }
    }
}