package com.example.fitnessassistant;

import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.fitnessassistant.diary.DiaryPageFragment;
import com.example.fitnessassistant.home.HomePageFragment;
import com.example.fitnessassistant.map.MapPageFragment;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.profile.LinkAccountsFragment;
import com.example.fitnessassistant.profile.ProfilePageFragment;
import com.example.fitnessassistant.profile.SettingsFragment;
import com.example.fitnessassistant.util.AuthFunctional;
import com.example.fitnessassistant.util.PermissionFunctional;
import com.example.fitnessassistant.workout.WorkoutPageFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class InAppActivity extends AppCompatActivity {
    // network manager for network connectivity checking
    private NetworkManager networkManager;
    // auth listener for refreshing user and UI
    private FirebaseAuth.AuthStateListener authListener;
    // setting up all fragments
    private MapPageFragment mapFragment;
    private DiaryPageFragment diaryFragment;
    private HomePageFragment homeFragment;
    private WorkoutPageFragment workoutFragment;
    private ProfilePageFragment profileFragment;
    public static SettingsFragment settingsFragment;
    public static LinkAccountsFragment linkAccountsFragment;
    // and fragment manager
    private final FragmentManager fm = getSupportFragmentManager();
    // and setting the currently active fragment as home
    private Fragment active;

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
                // user can access this fragment only if he granted the activity recognition permission
                new PermissionFunctional(mapFragment, mapFragment.permissionLauncher).checkActivityRecognitionPermission();
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

        // setting up network manager
        networkManager = new NetworkManager(getApplication());

        mapFragment = new MapPageFragment();
        diaryFragment = new DiaryPageFragment();
        homeFragment = new HomePageFragment();
        workoutFragment = new WorkoutPageFragment();
        profileFragment = new ProfilePageFragment();
        settingsFragment = new SettingsFragment();
        linkAccountsFragment = new LinkAccountsFragment();

        active = homeFragment;

        // setting up listener for firebase
        authListener = firebaseAuth -> AuthFunctional.refreshUser(this);

        // setting up the flashing animation for the no network notification
        Animation flash = new AlphaAnimation(0.0f, 1.0f);
        flash.setDuration(800); // flash duration
        flash.setStartOffset(1600); // staying visible duration
        flash.setRepeatMode(Animation.REVERSE);
        flash.setRepeatCount(Animation.INFINITE);
        findViewById(R.id.no_network_notification).startAnimation(flash);

        // managing all fragments
        fm.beginTransaction().add(R.id.in_app_container, mapFragment).hide(mapFragment).commit();
        fm.beginTransaction().add(R.id.in_app_container, diaryFragment).hide(diaryFragment).commit();
        fm.beginTransaction().add(R.id.in_app_container, homeFragment).commit();
        fm.beginTransaction().add(R.id.in_app_container, workoutFragment).hide(workoutFragment).commit();
        fm.beginTransaction().add(R.id.in_app_container, profileFragment).hide(profileFragment).commit();
        setNavigationListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        // registering this when user comes first time or returns
        networkManager.registerConnectionObserver(this, findViewById(R.id.no_network_text_view));
        // adding the listener for firebase to change the UI
        FirebaseAuth.getInstance().addAuthStateListener(authListener);

        networkManager = new NetworkManager(getApplication());

        // setting up listener for firebase
        authListener = firebaseAuth -> AuthFunctional.refreshUser(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregistering this when something comes into the foreground or else
        networkManager.unregisterConnectionObserver(this);
        // removing the listener
        if(authListener != null)
            FirebaseAuth.getInstance().removeAuthStateListener(authListener);
    }
}