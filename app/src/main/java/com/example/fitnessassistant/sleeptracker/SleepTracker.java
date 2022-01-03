package com.example.fitnessassistant.sleeptracker;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.SleepSegmentRequest;

public class SleepTracker extends Service {
    PendingIntent sleepReceiver;

    public void subscribeToSleepEvents(Context context){
        sleepReceiver = SleepDataReceiver.createPendingIntent(context);

        // TODO: Before this, check for ActivityRecognition permission
        ActivityRecognition.getClient(context)
                .requestSleepSegmentUpdates(sleepReceiver, SleepSegmentRequest.getDefaultSleepSegmentRequest());
    }

    public void unsubscribeFromSleepEvents(Context context){
        ActivityRecognition.getClient(context)
                .removeSleepSegmentUpdates(sleepReceiver);
    }

    @Override
    public void onCreate(){ }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
