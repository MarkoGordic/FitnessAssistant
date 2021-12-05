package com.example.fitnessassistant.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.diary.DiaryPageActivity;
import com.example.fitnessassistant.map.MapPageActivity;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.profile.ProfilePageActivity;
import com.example.fitnessassistant.util.AuthFunctional;
import com.example.fitnessassistant.workout.WorkoutPageActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class HomePageActivity extends AppCompatActivity {
    private FirebaseAuth.AuthStateListener authListener;
    private NetworkManager networkManager;

    // gives welcome message based on time
    private void greetUser(){
        TextView welcomeTextView = findViewById(R.id.welcomeMessageTextView); // TextView in top right corner for welcome message

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());

        int systemHours = calendar.get(Calendar.HOUR_OF_DAY);

        if (systemHours >= 6 && systemHours < 12)
            welcomeTextView.setText(getString(R.string.good_morning));
        else if(systemHours >= 12 && systemHours < 18)
            welcomeTextView.setText(getString(R.string.good_afternoon));
        else if(systemHours >= 18 && systemHours < 22)
            welcomeTextView.setText(getString(R.string.good_evening));
        else
            welcomeTextView.setText(getString(R.string.good_night));
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
            welcomeTextView.setText(String.format("%s, %s", welcomeTextView.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
    }

    // sets up listeners
    private void setUpOnClickListeners(){
        // setting up bottom navigation listeners
        ((BottomNavigationView) findViewById(R.id.bottomNavigation)).setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.map)
                startActivity(new Intent(this, MapPageActivity.class));
            else if(item.getItemId() == R.id.diary)
                startActivity(new Intent(this, DiaryPageActivity.class));
            else if(item.getItemId() == R.id.workout)
                startActivity(new Intent(this, WorkoutPageActivity.class));
            else if(item.getItemId() == R.id.profile)
                startActivity(new Intent(this, ProfilePageActivity.class));
            return true;
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        greetUser();
        setUpOnClickListeners();

        networkManager = new NetworkManager(getApplication());

        // setting up listener for firebase
        authListener = firebaseAuth -> AuthFunctional.refreshUser(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // setting current selected item of bottom navigation to current activity
        ((BottomNavigationView) findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.home);
        // registering this activity when user comes first time or returns
        networkManager.registerConnectionObserver(this,findViewById(R.id.homeScreen));
        // adding the listener for firebase to change the UI
        FirebaseAuth.getInstance().addAuthStateListener(authListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregistering this activity when another activity comes into the foreground or else
        networkManager.unregisterConnectionObserver(this);
        // removing the listener when activity pauses
        if(authListener != null)
            FirebaseAuth.getInstance().removeAuthStateListener(authListener);
    }
}