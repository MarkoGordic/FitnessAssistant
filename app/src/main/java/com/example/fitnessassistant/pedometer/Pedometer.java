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
import com.example.fitnessassistant.util.ServiceFunctional;

import java.util.Date;

public class Pedometer extends Service implements SensorEventListener {
    private Context updatedContext;
    private SensorManager sensorManager;
    private String currentDate;
    private String lastKnownDate;

    private float currentSteps = -1.0f;
    private float lastKnownSteps = 0.0f;
    private float lastNotificationSteps = 0.0f;

    // Required difference in steps before app pushes another notification to user
    int requiredDifferenceInSteps = 1;

    private SharedPreferences sharedPreferences;

    public Pedometer(){ }

    private synchronized void updateLang(){
        updatedContext = LocaleExt.toLangIfDiff(getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(this).getString("langPref", "sys"), true, true);
    }

    private static int calculateWeeklyAverage(Context context){
        int currentHistorySum = 0;

        for(int i = 0; i < 7; i++)
            currentHistorySum += (int) context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).getFloat(String.valueOf(Integer.parseInt(getCurrentDateFormatted()) - i), 0);

        return currentHistorySum / 7;
    }

    public static void updatePedometerWidgetData(Context updatedContext, int newSteps){
        for (int id : AppWidgetManager.getInstance(updatedContext).getAppWidgetIds(new ComponentName(updatedContext, PedometerWidget.class))) {
            RemoteViews rView = PedometerWidget.getRemoteViews(updatedContext, AppWidgetManager.getInstance(updatedContext).getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT));
            if(ServiceFunctional.getPedometerShouldRun(updatedContext)) {
                rView.setTextViewText(R.id.stepCountTextView, String.valueOf(newSteps));
                rView.setTextViewText(R.id.averageStepCountTextView, String.valueOf(calculateWeeklyAverage(updatedContext)));
                rView.setProgressBar(R.id.pedometerProgressBar, 100, (int) Math.round((newSteps / 10000.0) * 69), false);
            }
            AppWidgetManager.getInstance(updatedContext).updateAppWidget(id, rView);
        }
    }

    @Override
    public void onCreate(){
        updateLang();
        Notification notification = pushPedometerNotification(this, updatedContext.getString(R.string.starting_pedometer_service), updatedContext.getString(R.string.registering_steps));

        ServiceFunctional.setPedometerShouldRun(updatedContext, true);

        sharedPreferences = getApplicationContext().getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        currentDate = getCurrentDateFormatted();

        startForeground(25, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ServiceFunctional.setPedometerShouldRun(updatedContext, true);

        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        reRegisterSensor();

        currentDate = getCurrentDateFormatted();
        lastKnownDate = getCurrentDateFormatted();

        // in case we already have current date saved, pull saved data
        if(sharedPreferences.contains(currentDate)){
            lastKnownSteps = sharedPreferences.getFloat(currentDate, 0);
            currentSteps = -2;

            lastNotificationSteps = lastKnownSteps;
            pushPedometerNotification(updatedContext, (int) lastNotificationSteps + " " + updatedContext.getString(R.string.steps_small), updatedContext.getString(R.string.your_today_goal));
            updatePedometerWidgetData(updatedContext, (int) lastNotificationSteps);
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
        }

        // saving newest data from pedometer for later usage
        SharedPreferences.Editor editor =  sharedPreferences.edit();
        editor.putFloat(currentDate, newSteps);
        editor.apply();

        // pushing new notification for current steps
        if(abs(newSteps - lastNotificationSteps) >= requiredDifferenceInSteps) {
            pushPedometerNotification(this, newSteps + " " + updatedContext.getString(R.string.steps_small), updatedContext.getString(R.string.your_today_goal));
            updatePedometerWidgetData(updatedContext, newSteps);
            lastNotificationSteps = newSteps;
        }
    }

    // TODO Put logo
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
        if(!ServiceFunctional.getPedometerShouldRun(updatedContext)){
            stopForeground(true);
            stopSelf();
            sensorManager.unregisterListener(this);

            // update widgets
            for (int id : AppWidgetManager.getInstance(getApplicationContext()).getAppWidgetIds(new ComponentName(getApplicationContext(), PedometerWidget.class))) {
                PedometerWidget.updateAppWidget(getApplicationContext(), AppWidgetManager.getInstance(getApplicationContext()), id, AppWidgetManager.getInstance(getApplicationContext()).getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT) );
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}