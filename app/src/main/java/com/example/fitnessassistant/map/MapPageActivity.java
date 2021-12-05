package com.example.fitnessassistant.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.diary.DiaryPageActivity;
import com.example.fitnessassistant.home.HomePageActivity;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.pedometer.Pedometer;
import com.example.fitnessassistant.profile.ProfilePageActivity;
import com.example.fitnessassistant.util.AuthFunctional;
import com.example.fitnessassistant.util.PermissionFunctional;
import com.example.fitnessassistant.workout.WorkoutPageActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MapPageActivity extends AppCompatActivity {
    private final int RC_PEDOMETER_PERMISSION = 101;
    private NetworkManager networkManager;
    private FirebaseAuth.AuthStateListener authListener;
    private Pedometer pedometer;

    // setting up onClickListeners for items in nav bar
    private void setOnClickListeners(){
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

        ((BottomNavigationView) findViewById(R.id.bottomNavigation)).setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.diary)
                startActivity(new Intent(this, DiaryPageActivity.class));
            else if(item.getItemId() == R.id.home)
                startActivity(new Intent(this, HomePageActivity.class));
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_screen);
        setOnClickListeners();

        networkManager = new NetworkManager(getApplication());
        pedometer = new Pedometer(this, findViewById(R.id.stepCountTextView));

        // setting up listener for firebase
        authListener = firebaseAuth -> AuthFunctional.refreshUser(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // setting currently selected item in nav bar
        ((BottomNavigationView) findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.map);
        // registering this activity when user comes first time or returns
        networkManager.registerConnectionObserver(this,findViewById(R.id.mapScreen));
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
