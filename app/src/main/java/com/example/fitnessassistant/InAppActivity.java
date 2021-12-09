package com.example.fitnessassistant;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.fitnessassistant.diary.DiaryPageFragment;
import com.example.fitnessassistant.home.HomePageFragment;
import com.example.fitnessassistant.map.MapPageFragment;
import com.example.fitnessassistant.profile.LinkAccountsFragment;
import com.example.fitnessassistant.profile.ProfilePageFragment;
import com.example.fitnessassistant.profile.SettingsFragment;
import com.example.fitnessassistant.workout.WorkoutPageFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class InAppActivity extends AppCompatActivity {
    // setting up all fragments
    private final Fragment mapFragment = new MapPageFragment();
    private final Fragment diaryFragment = new DiaryPageFragment();
    private final Fragment homeFragment = new HomePageFragment();
    private final Fragment workoutFragment = new WorkoutPageFragment();
    private final Fragment profileFragment = new ProfilePageFragment();
    public static final Fragment settingsFragment = new SettingsFragment();
    public static final Fragment linkAccountsFragment = new LinkAccountsFragment();
    // and fragment manager
    private final FragmentManager fm = getSupportFragmentManager();
    // and setting the currently active fragment as home
    private Fragment active = homeFragment;

    // return to previous fragment (if it exists)
    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 0)
            super.onBackPressed();
        else
            getSupportFragmentManager().popBackStack();
    }

    private void setNavigationListener(){
        // navigation listener hides the active fragment, shows the selected one and sets the selected as the new active
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // popping all previous fragments pushed to the stack
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
        fm.beginTransaction().add(R.id.in_app_container, mapFragment).hide(mapFragment).commit();
        fm.beginTransaction().add(R.id.in_app_container, diaryFragment).hide(diaryFragment).commit();
        fm.beginTransaction().add(R.id.in_app_container, homeFragment).commit();
        fm.beginTransaction().add(R.id.in_app_container, workoutFragment).hide(workoutFragment).commit();
        fm.beginTransaction().add(R.id.in_app_container, profileFragment).hide(profileFragment).commit();
        setNavigationListener();
    }
}