package com.example.fitnessassistant.pedometer;

import static com.example.fitnessassistant.util.TimeFunctional.getCurrentDateFormatted;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.fitnessassistant.database.mdbh.MDBHPedometer;

public class Restarter extends BroadcastReceiver {

    // this handles the case when user restarts their phone (Service will be called again when phone boots)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            context.startForegroundService(new Intent(context, Pedometer.class));

            // update widgets
            Pedometer.updatePedometerWidgetData(context, (int) MDBHPedometer.getInstance(context).readPedometerSteps(getCurrentDateFormatted()), null);
        }
    }
}