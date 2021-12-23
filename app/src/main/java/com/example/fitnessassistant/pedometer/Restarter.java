package com.example.fitnessassistant.pedometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Restarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case "restartservice":
            case Intent.ACTION_BOOT_COMPLETED:
                context.startForegroundService(new Intent(context, Pedometer.class));
                break;
        }

    }
}