package com.example.fitnessassistant;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.fitnessassistant.diary.DiaryPageFragment;
import com.example.fitnessassistant.home.HomePageFragment;
import com.example.fitnessassistant.map.MapPageFragment;
import com.example.fitnessassistant.profile.ProfilePageFragment;
import com.example.fitnessassistant.workout.WorkoutPageFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class InAppActivity extends AppCompatActivity {
    // setting up all fragments
    final Fragment mapFragment = new MapPageFragment();
    final Fragment diaryFragment = new DiaryPageFragment();
    final Fragment homeFragment = new HomePageFragment();
    final Fragment workoutFragment = new WorkoutPageFragment();
    final Fragment profileFragment = new ProfilePageFragment();
    // and fragment manager
    final FragmentManager fm = getSupportFragmentManager();
    // and setting the currently active fragment as home
    Fragment active = homeFragment;

    private void setNavigationListener(){
        // navigation listener hides the active fragment, shows the selected one and sets the selected as the new active
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if(item.getItemId() == R.id.map){
                fm.beginTransaction().hide(active).show(mapFragment).commit();
                active = mapFragment;
                return true;
            } else if(item.getItemId() == R.id.diary){
                fm.beginTransaction().hide(active).show(diaryFragment).commit();
                active = diaryFragment;
                return true;
            } else if(item.getItemId() == R.id.home){
                fm.beginTransaction().hide(active).show(homeFragment).commit();
                active = homeFragment;
                return true;
            } else if(item.getItemId() == R.id.workout){
                fm.beginTransaction().hide(active).show(workoutFragment).commit();
                active = workoutFragment;
                return true;
            } else if(item.getItemId() == R.id.profile) {
                fm.beginTransaction().hide(active).show(profileFragment).commit();
                active = profileFragment;
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_app_screen);
        // managing all fragments
        fm.beginTransaction().add(R.id.in_app_container, mapFragment, "map").hide(mapFragment).commit();
        fm.beginTransaction().add(R.id.in_app_container, diaryFragment, "diary").hide(diaryFragment).commit();
        fm.beginTransaction().add(R.id.in_app_container, homeFragment, "home").commit();
        fm.beginTransaction().add(R.id.in_app_container, workoutFragment, "workout").hide(workoutFragment).commit();
        fm.beginTransaction().add(R.id.in_app_container, profileFragment, "profile").hide(profileFragment).commit();
        setNavigationListener();
    }
}