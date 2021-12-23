package com.example.fitnessassistant.pedometer;

import static java.lang.Math.abs;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.notifications.NotificationController;
import com.example.fitnessassistant.uiprefs.LocaleExt;

import java.util.Date;

public class Pedometer extends Service implements SensorEventListener {
    private Context updatedContext;
    private SensorManager sensorManager;
    private String currentDate;
    private String lastKnownDate;

    float currentSteps = -1.0f;
    float lastKnownSteps = 0.0f;
    float lastNotificationSteps = 0.0f;

    // Required difference in steps before app pushes another notification to user
    int requiredDifferenceInSteps = 1;

    // Array for step count in last 6 days (for stats on widget)
    private final int[] pedometerHistory = new int[6];

    // Current sum of steps in last 6 days
    private int currentHistorySum;

    private SharedPreferences sharedPreferences;
    private SharedPreferences prefs;

    public Pedometer(){ }

    private void updateLang(){
        updatedContext = LocaleExt.toLangIfDiff(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(this).getString("langPref", "sys"), true, false);
    }

    @Override
    public void onCreate(){
        updateLang();
        Notification notification = pushPedometerNotification(this, updatedContext.getString(R.string.starting_pedometer_service), updatedContext.getString(R.string.registering_steps));

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences = getApplicationContext().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        currentDate = getCurrentDateFormatted();

        for(int i = 0; i < 6; i++)
            pedometerHistory[i] = (int)sharedPreferences.getFloat(String.valueOf(Integer.parseInt(currentDate) - i - 1), 0);

        currentHistorySum = 0;
        for(int i = 0; i < 6; i++)
            currentHistorySum += pedometerHistory[i];


        startForeground(25, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        reRegisterSensor();

        currentDate = getCurrentDateFormatted();
        lastKnownDate = getCurrentDateFormatted();

        // in case we already have current date saved, pull saved data
        if(sharedPreferences.contains(currentDate)){
            lastKnownSteps = sharedPreferences.getFloat(currentDate, 0);
            currentSteps = -2;

            lastNotificationSteps = sharedPreferences.getFloat(currentDate, 0);
            updateLang();
            pushPedometerNotification(updatedContext, ((int) sharedPreferences.getFloat(currentDate, 0)) + " " + updatedContext.getString(R.string.steps_small), updatedContext.getString(R.string.your_today_goal));
        }

        return START_STICKY;
    }

    public void reRegisterSensor(){
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if(stepSensor != null){
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        currentDate = getCurrentDateFormatted();

        if(currentSteps == -2){
            lastKnownSteps = sensorEvent.values[0] - sharedPreferences.getFloat(currentDate, 0);
            currentSteps = sensorEvent.values[0];
        }
        else if(currentSteps == -1){
            currentSteps = sensorEvent.values[0];
            lastKnownSteps = sensorEvent.values[0];
        }
        else{
            currentSteps = sensorEvent.values[0];
        }

        int newSteps = Math.round(currentSteps) - Math.round(lastKnownSteps);

        // In case we detect date change, we will reset counter to 0
        if(!currentDate.equals(lastKnownDate)){
            lastKnownDate = currentDate;
            lastKnownSteps = sensorEvent.values[0];
            newSteps = Math.round(currentSteps) - Math.round(lastKnownSteps);

            // we also need to recalculate sum of step history
            for(int i = 0; i < 6; i++)
                pedometerHistory[i] = (int)sharedPreferences.getFloat(String.valueOf(Integer.parseInt(currentDate) - i - 1), 0);

            currentHistorySum = 0;
            for(int i = 0; i < 6; i++)
                currentHistorySum += pedometerHistory[i];
        }

        // saving newest data from pedometer for later usage
        SharedPreferences.Editor editor =  sharedPreferences.edit();
        editor.putFloat(currentDate, newSteps);
        editor.apply();

        // updating widget if widget exists on home screen
        if(prefs.getBoolean("PedometerWidget", false)) {
            Context context = this;
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.pedometer_widget);
            ComponentName thisWidget = new ComponentName(context, PedometerWidget.class);
            remoteViews.setTextViewText(R.id.stepCountTextView, String.valueOf(newSteps));
            remoteViews.setTextViewText(R.id.averageStepCountTextView, String.valueOf((currentHistorySum + newSteps) / 7));

            remoteViews.setProgressBar(R.id.pedometerProgressBar, 100, (int) Math.round((newSteps / 10000.0) * 69), false);

            appWidgetManager.updateAppWidget(thisWidget, remoteViews);
        }

        // pushing new notification for current steps
        if(abs(newSteps - lastNotificationSteps) >= requiredDifferenceInSteps) {
            updateLang();
            pushPedometerNotification(this, ((int) sharedPreferences.getFloat(currentDate, 0)) + " " + updatedContext.getString(R.string.steps_small), updatedContext.getString(R.string.your_today_goal));
            lastNotificationSteps = newSteps;
        }
    }

    // TODO : Put app icon when done
    public static Notification pushPedometerNotification(Context context, String textTitle, String textContent){
        Intent intent = new Intent(context, InAppActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = NotificationController.createNotification(context, "Pedometer", textTitle, textContent, pendingIntent, false,true, false);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(25, notification);

        return notification;
    }

    public static String getCurrentDateFormatted(){
        return (String) DateFormat.format("yyyyMMdd", new Date());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);

        sendBroadcast(broadcastIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}