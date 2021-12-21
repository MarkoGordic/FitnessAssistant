package com.example.fitnessassistant.pedometer;

import static java.lang.Math.abs;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.text.format.DateFormat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;

import java.util.Date;

public class Pedometer extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private String currentDate;
    private String lastKnownDate;

    float currentSteps = -1.0f;
    float lastKnownSteps = 0.0f;
    float lastNotificationSteps = 0.0f;

    // Required difference in steps before app pushes another notification to user
    int requiredDifferenceInSteps = 1;

    private SharedPreferences sharedPreferences;

    public Pedometer(){ }

    @Override
    public void onCreate(){
        Notification notification = pushPedometerNotification(this, "Starting Pedometer service...", "Please wait...");

        startForeground(25, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedPreferences = getApplicationContext().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        reRegisterSensor();

        currentDate = getCurrentDateFormatted();
        lastKnownDate = getCurrentDateFormatted();

        // in case we already have current date saved, pull saved data
        if(sharedPreferences.contains(currentDate)){
            lastKnownSteps = sharedPreferences.getFloat(currentDate, 0);
            currentSteps = -2;

            lastNotificationSteps = sharedPreferences.getFloat(currentDate, 0);
            pushPedometerNotification(getApplicationContext(), ((int) sharedPreferences.getFloat(currentDate, 0)) + " steps", "Your today's goal is 10000");
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

        // saving newest data from pedometer for later usage
        // In case we detect date change, we will reset counter to 0
        if(!currentDate.equals(lastKnownDate)){
            lastKnownDate = currentDate;
            lastKnownSteps = sensorEvent.values[0];
            newSteps = Math.round(currentSteps) - Math.round(lastKnownSteps);
        }

        SharedPreferences.Editor editor =  sharedPreferences.edit();
        editor.putFloat(currentDate, newSteps);
        editor.apply();

        // pushing new notification for current steps
        if(abs(newSteps - lastNotificationSteps) >= requiredDifferenceInSteps) {
            pushPedometerNotification(this, ((int) sharedPreferences.getFloat(currentDate, 0)) + " steps", "Your today's goal is 10000");
            lastNotificationSteps = newSteps;
        }
    }

    // TODO : Put app icon when done
    public static Notification pushPedometerNotification(Context context, String textTitle, String textContent){
        Intent intent = new Intent(context, InAppActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Pedometer")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle((CharSequence) textTitle)
                .setContentText((CharSequence) textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setShowWhen(false);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(25, builder.build());

        return builder.build();
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