package com.example.pedometer;

import android.app.AlarmManager;
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

import androidx.annotation.Nullable;

import java.util.Calendar;

public class Pedometer extends Service implements SensorEventListener {
    private int newSteps = 0;

    private int lastKnownSteps = 0;
    private long lastKnownTime = 0;

    private final static long SAVE_DELAY_TIME = AlarmManager.INTERVAL_FIFTEEN_MINUTES; // Time gap between saves
    private final static int SAVE_DELAY_STEPS = 0; // Gap between saves in steps

    private final SharedPreferences prefs = getSharedPreferences("pedometer", Context.MODE_PRIVATE);

    //private final BroadcastReceiver shutdownReceiver = new ShutdownReceiver();

    @Override
    public void onSensorChanged(SensorEvent event) {
        // If sensor detect number larger than max value of int, its probably not a real value.
        if(event.values[0] > Integer.MAX_VALUE){
            newSteps = lastKnownSteps;
        }
        else{
            newSteps = (int) event.values[0];
            checkForUpdates();
        }
    }

    private boolean checkForUpdates(){
        if ((newSteps > lastKnownSteps + SAVE_DELAY_STEPS) || (newSteps > 0 && System.currentTimeMillis() > lastKnownTime + SAVE_DELAY_TIME)){
            // In this case, we need to update our database

            // We will save our most recent params
            lastKnownSteps = newSteps;
            lastKnownTime = System.currentTimeMillis();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("steps", lastKnownSteps);
            editor.apply();

            // TODO : When you add widget and notification, update them here
            return true;
        }
        else
            return false;
    }

    private void registerSensor(){
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        try{
            sensorManager.unregisterListener(this);
        } catch (Exception e){
            e.printStackTrace();
        }

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), SensorManager.SENSOR_DELAY_NORMAL, (int) (3 * 60000000));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        registerSensor();

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 1);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.DATE, 1);

        long nextCall = Math.min(c.getTimeInMillis(), System.currentTimeMillis() + AlarmManager.INTERVAL_HALF_HOUR);
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 2, new Intent(this, Pedometer.class), PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC, nextCall, pendingIntent);

        return START_STICKY;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
