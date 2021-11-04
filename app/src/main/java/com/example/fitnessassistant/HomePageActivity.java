package com.example.fitnessassistant;

import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class HomePageActivity extends AppCompatActivity {
    private double MagnitudePrevious = 1;
    private int stepCount;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        prefs = getSharedPreferences("StepCounter", MODE_PRIVATE);
        stepCount = 0;

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        SensorEventListener stepDetector = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event != null){
                    float x_acceleration = event.values[0];
                    float y_acceleration = event.values[1];
                    float z_acceleration = event.values[2];

                    double Magnitude = Math.sqrt(x_acceleration*x_acceleration + y_acceleration*y_acceleration + z_acceleration*z_acceleration);
                    double MagnitudeDelta = Magnitude - MagnitudePrevious;
                    MagnitudePrevious = Magnitude;

                    if (MagnitudeDelta > 4)
                        stepCount++;

                    ((TextView) findViewById(R.id.stepCount)).setText(String.valueOf(stepCount));
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        sensorManager.registerListener(stepDetector, sensor, SensorManager.SENSOR_DELAY_UI);

        TextView welcome = findViewById(R.id.welcomeMessage); // TextView in top right corner for welcome message

        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);

        int systemHours = calendar.get(Calendar.HOUR_OF_DAY);

        if (systemHours >= 6 && systemHours < 12)
            welcome.setText(getString(R.string.good_morning));
        else if(systemHours >= 12 && systemHours < 18)
            welcome.setText(getString(R.string.good_afternoon));
        else if(systemHours >= 18 && systemHours < 23)
            welcome.setText(getString(R.string.good_evening));
        else
            welcome.setText(getString(R.string.good_night));
    }

    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("stepCount", stepCount);
        editor.apply();
    }

    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("stepCount", stepCount);
        editor.apply();
    }

    protected void onResume() {
        super.onResume();
        stepCount = prefs.getInt("stepCount", 0);
    }
}
