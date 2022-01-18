package com.example.fitnessassistant.sleeptracker;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.SleepSegmentRequest;

import java.util.Calendar;

public class SleepTracker extends Service {
    public static final int SLEEP_TRACKER_ID = 28;
    PendingIntent sleepReceiver;

    public void subscribeToSleepEvents(){
        sleepReceiver = SleepDataReceiver.createPendingIntent(this);

        // TODO: Before this, check for ActivityRecognition permission
        ActivityRecognition.getClient(this)
                .requestSleepSegmentUpdates(sleepReceiver, SleepSegmentRequest.getDefaultSleepSegmentRequest());
    }

    public void unsubscribeFromSleepEvents(){
        ActivityRecognition.getClient(this)
                .removeSleepSegmentUpdates(sleepReceiver);
    }

    @Override
    public void onCreate(){ }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Pushing base notification
        subscribeToSleepEvents();
        Notification notification = pushSleepTrackerNotification();
        startForeground(SLEEP_TRACKER_ID, notification);

        return START_STICKY;
    }

    private Notification pushSleepTrackerNotification(){
        Intent intent = new Intent(this, InAppActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, SLEEP_TRACKER_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "SleepTracker")
                .setSmallIcon(R.drawable.ic_sleep)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentTitle(String.valueOf(R.string.sleep_tracking))
                .setContentText(String.valueOf(R.string.sleep_tracking_on))
                .setContentIntent(pendingIntent)
                .setShowWhen(false);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(SLEEP_TRACKER_ID, notificationBuilder.build());

        return notificationBuilder.build();
    }

    public void pushSleepDetectedNotification(long startTime, long endTime){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        String startString = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
        calendar.setTimeInMillis(endTime);
        String endString = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + " ?";

        Intent intent = new Intent(this, InAppActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, SLEEP_TRACKER_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "SleepTracker")
                .setSmallIcon(R.drawable.ic_sleep)
                .setAutoCancel(false)
                .setOngoing(false)
                .setContentTitle(String.valueOf(R.string.sleep_tracking))
                .setContentText(R.string.sleep_detected_1 + startString + R.string.sleep_detected_2 + endString)
                .setContentIntent(pendingIntent)
                .setShowWhen(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(SLEEP_TRACKER_ID, notificationBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unsubscribeFromSleepEvents();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
