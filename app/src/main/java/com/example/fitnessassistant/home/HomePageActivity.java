package com.example.fitnessassistant.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.diary.DiaryPageActivity;
import com.example.fitnessassistant.map.MapPageActivity;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.pedometer.Pedometer;
import com.example.fitnessassistant.profile.ProfilePageActivity;
import com.example.fitnessassistant.util.AuthFunctional;
import com.example.fitnessassistant.util.PermissionFunctional;
import com.example.fitnessassistant.workout.WorkoutPageActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
// TODO After adding in-app password change, update current user and make sure he doesn't get redirected to sign in
//  also for in-app email change

public class HomePageActivity extends AppCompatActivity {
    private final int RC_PEDOMETER_PERMISSION = 101;
    private FirebaseAuth.AuthStateListener authListener;
    private NetworkManager networkManager;
    private Pedometer pedometer;

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
    }

    // sets up listeners for signing out
    private void setUpOnClickListeners(){
        // startPedometer listener - asks for permission for activity recognition (if permission is not already granted)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            findViewById(R.id.startPedometerButton).setVisibility(View.VISIBLE);
            findViewById(R.id.startPedometerButton).setOnClickListener(view -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    PermissionFunctional.askForPermission(this, getString(R.string.activity_recognition_permission), getString(R.string.activity_recognition_rationale), Manifest.permission.ACTIVITY_RECOGNITION, RC_PEDOMETER_PERMISSION);
                else // tell user he needs android (Q)10 for activity recognition
                    Toast.makeText(getApplicationContext(), getString(R.string.android_q_needed), Toast.LENGTH_LONG).show();
            });
        }

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_PEDOMETER_PERMISSION) // requestCode is used to retrieve the results of asking for permission
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                findViewById(R.id.startPedometerButton).setVisibility(View.GONE); // hide the button if it's granted
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        greetUser();
        setUpOnClickListeners();

        networkManager = new NetworkManager(getApplication());
        pedometer = new Pedometer(this, findViewById(R.id.stepCountTextView));

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
        // re-registering the pedometer sensor
        pedometer.reRegisterSensor();
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