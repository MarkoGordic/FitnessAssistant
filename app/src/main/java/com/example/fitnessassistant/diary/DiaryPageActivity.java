package com.example.fitnessassistant.diary;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.home.HomePageActivity;
import com.example.fitnessassistant.map.MapPageActivity;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.profile.ProfilePageActivity;
import com.example.fitnessassistant.util.AuthFunctional;
import com.example.fitnessassistant.workout.WorkoutPageActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class DiaryPageActivity extends AppCompatActivity {
    private NetworkManager networkManager;
    private FirebaseAuth.AuthStateListener authListener;

    // setting up onClickListeners for items in the navigation bar
    private void setOnClickListeners(){
        ((BottomNavigationView) findViewById(R.id.bottomNavigation)).setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.map)
                startActivity(new Intent(this, MapPageActivity.class));
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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.diary_screen);
        setOnClickListeners();

        networkManager = new NetworkManager(getApplication());

        // setting up listener for firebase
        authListener = firebaseAuth -> AuthFunctional.refreshUser(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // setting up currently selected item in the nav bar
        ((BottomNavigationView) findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.diary);
        // registering this activity when user comes first time or returns
        networkManager.registerConnectionObserver(this,findViewById(R.id.diaryScreen));
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
