package com.example.fitnessassistant.sleeptracker;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.SleepClassifyEvent;
import com.google.android.gms.location.SleepSegmentEvent;

import java.util.List;

public class SleepDataReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Sleep update received!");
        if(SleepSegmentEvent.hasEvents(intent)){ // These are whole sleep segments
            List<SleepSegmentEvent> sleepEvents = SleepSegmentEvent.extractEvents(intent);

            for(int i = 0; i < sleepEvents.size(); i++){
                System.out.println(sleepEvents.get(i) + " SLEEP_DATA");
            }
        } else if (SleepClassifyEvent.hasEvents(intent)){ // These are regular sleep updates
            List<SleepClassifyEvent> sleepEvents = SleepClassifyEvent.extractEvents(intent);

            for(int i = 0; i < sleepEvents.size(); i++){
                System.out.println(sleepEvents.get(i) + " SLEEP_DATA");
            }
        }
    }

    public static PendingIntent createPendingIntent(Context context){
        Intent intent = new Intent(context, SleepDataReceiver.class);

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
