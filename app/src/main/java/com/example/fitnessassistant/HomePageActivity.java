package com.example.fitnessassistant;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class HomePageActivity extends AppCompatActivity {

    // gives welcome message based on time
    private void greetUser(){
        TextView welcomeTV = findViewById(R.id.welcomeMessage); // TextView in top right corner for welcome message

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());

        int systemHours = calendar.get(Calendar.HOUR_OF_DAY);

        if (systemHours >= 6 && systemHours < 12)
            welcomeTV.setText(getString(R.string.good_morning));
        else if(systemHours >= 12 && systemHours < 18)
            welcomeTV.setText(getString(R.string.good_afternoon));
        else if(systemHours >= 18 && systemHours < 22)
            welcomeTV.setText(getString(R.string.good_evening));
        else
            welcomeTV.setText(getString(R.string.good_night));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        greetUser();
    }

}
