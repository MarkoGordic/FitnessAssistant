package com.example.fitnessassistant.sleeptracker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.uiprefs.LocaleExt;
import com.example.fitnessassistant.util.ServiceFunctional;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.SleepSegmentRequest;

import java.util.Calendar;

public class SleepTracker extends Service {
    public static final int SLEEP_TRACKER_ID = 28;
    public static final int SLEEP_TRACKER_ALERT_ID = 29;
    private Context updatedContext;
    private PendingIntent sleepReceiver;

    public void subscribeToSleepEvents(){
        sleepReceiver = SleepDataReceiver.createPendingIntent(this);

        ActivityRecognition.getClient(this)
                .requestSleepSegmentUpdates(sleepReceiver, SleepSegmentRequest.getDefaultSleepSegmentRequest());
    }

    public void unsubscribeFromSleepEvents(){
        ActivityRecognition.getClient(this)
                .removeSleepSegmentUpdates(sleepReceiver);
    }

    private synchronized void updateLang(){
        updatedContext = LocaleExt.toLangIfDiff(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(this).getString("langPref", "sys"), true, true);
    }

    @Override
    public void onCreate(){ updateLang(); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Pushing base notification
        subscribeToSleepEvents();
        Notification notification = pushSleepTrackerNotification(updatedContext);
        startForeground(SLEEP_TRACKER_ID, notification);

        return START_STICKY;
    }

    public static Notification pushSleepTrackerNotification(Context context){
        Intent intent = new Intent(context, InAppActivity.class);
        intent.putExtra("desiredFragment", "SleepFragment");
        PendingIntent pendingIntent = PendingIntent.getActivity(context, SLEEP_TRACKER_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "SleepTracker")
                .setSmallIcon(R.drawable.ic_sleep)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentTitle(context.getString(R.string.sleep_tracking))
                .setContentText(context.getString(R.string.sleep_tracking_on))
                .setContentIntent(pendingIntent)
                .setShowWhen(false);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(SLEEP_TRACKER_ID, notificationBuilder.build());

        return notificationBuilder.build();
    }

    @SuppressLint("DefaultLocale")
    public static void pushSleepDetectedNotification(Context context, long startTime, long endTime){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);
        String startString = String.format(" %02d:%02d ", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        calendar.setTimeInMillis(endTime);
        String endString = String.format(" %02d:%02d ?", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));

        Intent intent = new Intent(context, InAppActivity.class);
        intent.putExtra("desiredFragment", "SleepDateFragment");
        intent.putExtra("date", DateFormat.format("yyyyMMdd", calendar));
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, SLEEP_TRACKER_ALERT_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "SleepTrackerAlerts")
                .setSmallIcon(R.drawable.ic_sleep)
                .setAutoCancel(true)
                .setOngoing(false)
                .setContentTitle(context.getString(R.string.sleep_tracking))
                .setContentText(context.getString(R.string.sleep_detected_1) + startString + context.getString(R.string.sleep_detected_2) + endString)
                .setContentIntent(pendingIntent)
                .setShowWhen(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(SLEEP_TRACKER_ALERT_ID, notificationBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!ServiceFunctional.getSleepTrackerShouldRun(updatedContext))
            unsubscribeFromSleepEvents();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
