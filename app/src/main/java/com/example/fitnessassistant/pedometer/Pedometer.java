package com.example.fitnessassistant.pedometer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

public class Pedometer implements SensorEventListener {
    private final SensorManager sensorManager;

    Context context;
    TextView stepsText;

    float currentSteps = -1f;
    float lastKnownSteps = 0f;

    public Pedometer(Context context, TextView textView){
        this.context = context;
        this.stepsText = textView;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

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

        int newSteps = (int)(Math.round(currentSteps)) - (int)(Math.round(lastKnownSteps));

        stepsText.setText(String.valueOf(newSteps));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}