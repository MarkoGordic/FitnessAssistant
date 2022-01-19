package com.example.fitnessassistant.pedometer;

import static com.example.fitnessassistant.util.TimeFunctional.getCurrentDateFormatted;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.database.mdbh.MDBHPedometer;
import com.example.fitnessassistant.uiprefs.LocaleExt;
import com.example.fitnessassistant.util.ServiceFunctional;

public class DailyRestarter extends BroadcastReceiver {

    // used to update notifications and widgets at midnight
    @Override
    public void onReceive(Context context, Intent intent) {
        // getting updated context
        Context updatedContext = LocaleExt.toLangIfDiff(context, PreferenceManager.getDefaultSharedPreferences(context).getString("langPref", "sys"), true, true);

        // putting step goal for previous day
        StepGoalFragment.putUnsavedStepGoals(updatedContext);

        // updating widget and pedometer with today's data
        Pedometer.updatePedometerWidgetData(updatedContext ,((int) MDBHPedometer.getInstance(updatedContext).readPedometerSteps(getCurrentDateFormatted())), null);
        if(ServiceFunctional.getPedometerShouldRun(updatedContext))
            Pedometer.pushPedometerNotification(updatedContext, ((int) MDBHPedometer.getInstance(updatedContext).readPedometerSteps(getCurrentDateFormatted())) + " " + updatedContext.getString(R.string.steps_small),updatedContext.getString(R.string.your_today_goal) + " " + StepGoalFragment.getStepGoalForToday(updatedContext) + ".");
    }
}
