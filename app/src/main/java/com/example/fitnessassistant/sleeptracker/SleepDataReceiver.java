package com.example.fitnessassistant.sleeptracker;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;

import com.example.fitnessassistant.database.mdbh.MDBHSleepTracker;
import com.google.android.gms.location.SleepSegmentEvent;

import java.util.Calendar;
import java.util.List;

public class SleepDataReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("Sleep update received!");
        if(SleepSegmentEvent.hasEvents(intent)){ // These are whole sleep segments
            List<SleepSegmentEvent> sleepEvents = SleepSegmentEvent.extractEvents(intent);

            for(int i = 0; i < sleepEvents.size(); i++){
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(sleepEvents.get(i).getEndTimeMillis());
                String date = (String) DateFormat.format("yyyyMMdd", cal);
                MDBHSleepTracker.getInstance(context).addNewSleepSegment(context, sleepEvents.get(i).getStartTimeMillis(), sleepEvents.get(i).getEndTimeMillis(), date, null, null, false);
                System.out.println(sleepEvents.get(i) + " SLEEP_DATA");
            }
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    public static PendingIntent createPendingIntent(Context context){
        Intent intent = new Intent(context, SleepDataReceiver.class);

        return PendingIntent.getBroadcast(context, SleepTracker.SLEEP_TRACKER_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
