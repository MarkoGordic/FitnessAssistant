package com.example.fitnessassistant.activitydetector;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.notifications.NotificationController;
import com.example.fitnessassistant.uiprefs.LocaleExt;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class ActivityDetector extends Service {
    private Context updatedContext;
    private static final List<ActivityTransition> transitions = new ArrayList<>();

    public static void startTransitionRecognition(Context context) {
        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);

        Intent intent = new Intent(context, ActivityDetectorReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Task<Void> task = ActivityRecognition.getClient(context)
                .requestActivityTransitionUpdates(request, pendingIntent);

        task.addOnSuccessListener(unused -> {
            // Connected!
        });

        task.addOnFailureListener(e -> {
            // Failed!
        });

    }

    public static void stopActivityRecognition(Context context){
        Intent intent = new Intent(context, ActivityDetectorReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Transitions unsuccessfully registered
        ActivityRecognition.getClient(context).removeActivityTransitionUpdates(pendingIntent)
                .addOnSuccessListener(unused -> {
                    // Transitions unregistered
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private void updateLang(){
        updatedContext = LocaleExt.toLangIfDiff(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(this).getString("langPref", "sys"), true, false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        createTransitions();
        updateLang();
        Notification notification = pushActivityUpdateNotification(this, updatedContext.getString(R.string.background_activity_detection), updatedContext.getString(R.string.background_activity_detection_started));
        startTransitionRecognition(this);

        startForeground(26, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopActivityRecognition(this);
        stopForeground(true);
        stopSelf();
    }

    public static Notification pushActivityUpdateNotification(Context context, String textTitle, String textContent){
        Intent intent = new Intent(context, InAppActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = NotificationController.createNotification(context, "ActivityDetection", textTitle, textContent, pendingIntent, false,true, false);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(26, notification);

        return notification;
    }

    private void createTransitions(){
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());
        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
