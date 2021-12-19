package com.example.fitnessassistant.pedometer;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.widget.TextView;
import java.util.Date;

public class Pedometer implements SensorEventListener {
    private final SensorManager sensorManager;
    private final TextView stepsText;
    private String currentDate;

    float currentSteps = -1.0f;
    float lastKnownSteps = 0.0f;

    private SharedPreferences sharedPreferences;
    private SharedPreferences globalPreferences;

    public Pedometer(Context context, TextView textView){
        this.stepsText = textView;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        sharedPreferences = context.getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        globalPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        currentDate = getCurrentDateFormatted();
        if(sharedPreferences.contains(currentDate)){
            lastKnownSteps = sharedPreferences.getFloat(currentDate, 0);
            currentSteps = sharedPreferences.getFloat(currentDate, 0);
        }

        reRegisterSensor();
    }

    public void reRegisterSensor(){
        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if(stepSensor != null){
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(currentSteps == -1){
            currentSteps = sensorEvent.values[0];
            lastKnownSteps = sensorEvent.values[0];
        }
        else{
            currentSteps = sensorEvent.values[0];
        }

        int newSteps = Math.round(currentSteps) - Math.round(lastKnownSteps);

        currentDate = getCurrentDateFormatted();

        // TODO fix - this can result in a NullPointerException if user is not at homePage
        stepsText.setText(String.valueOf(newSteps));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    public static String getCurrentDateFormatted(){
        return (String) DateFormat.format("yyyyMMdd", new Date());
    }
}