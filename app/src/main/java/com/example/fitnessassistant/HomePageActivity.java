package com.example.fitnessassistant;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class HomePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);

        TextView welcome = findViewById(R.id.welcomeMessage); // TextView in top right corner for welcome message

        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);

        int systemHours = calendar.get(Calendar.HOUR_OF_DAY);

        if (systemHours >= 6 && systemHours < 12)
            welcome.setText("Good Morning, ");
        else if(systemHours >= 12 && systemHours < 18)
            welcome.setText("Good Afternoon, ");
        else if(systemHours >= 18 && systemHours < 23)
            welcome.setText("Good Evening, ");
        else if((systemHours >= 23 && systemHours <= 24) || (systemHours >= 0 && systemHours < 6))
            welcome.setText("Good Night, ");

    }
}
