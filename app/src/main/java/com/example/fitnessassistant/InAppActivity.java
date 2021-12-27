package com.example.fitnessassistant;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.fitnessassistant.activitytracker.ActivityTrackingFragment;
import com.example.fitnessassistant.diary.DiaryPageFragment;
import com.example.fitnessassistant.home.HomePageFragment;
import com.example.fitnessassistant.map.MapPageFragment;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.profile.LinkAccountsFragment;
import com.example.fitnessassistant.profile.ProfilePageFragment;
import com.example.fitnessassistant.profile.SettingsFragment;
import com.example.fitnessassistant.uiprefs.ColorMode;
import com.example.fitnessassistant.uiprefs.LocaleExt;
import com.example.fitnessassistant.util.AuthFunctional;
import com.example.fitnessassistant.util.ServiceFunctional;
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
    public static ActivityTrackingFragment activityTrackingFragment;
    public static SettingsFragment settingsFragment;
    public static LinkAccountsFragment linkAccountsFragment;
    // and fragment manager
    private final FragmentManager fm = getSupportFragmentManager();
    // and setting the currently active fragment as home
    private Fragment active;

    // launcher for the Activity Recognition Permission
    public final ActivityResultLauncher<String> activityRecognitionPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if(result)
            ServiceFunctional.startPedometerService(this);
        else {
            // creates an alert dialog with rationale shown
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(R.layout.custom_ok_alert_dialog);
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ((AppCompatImageView)dialog.findViewById(R.id.dialog_drawable)).setImageResource(R.drawable.exclamation);

            ((TextView) dialog.findViewById(R.id.dialog_header)).setText(R.string.activity_recognition_access_denied);
            dialog.findViewById(R.id.dialog_ok_button).setOnClickListener(view2 -> dialog.dismiss());

            // showing messages (one case if user selected don't ask again, other if user just selected deny)
            if(!shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION))
                ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.activity_recognition_access_message_denied_forever);
            else
                ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.activity_recognition_access_message_denied);
        }
    });

    // return to previous fragment (if it exists)
    @Override
    public void onBackPressed() {
        if(fm.getBackStackEntryCount() == 0)
            super.onBackPressed();
        else
            fm.popBackStack();
            if(fm.getBackStackEntryCount() == 1)
                active.onResume();
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
                active.onResume();
                return true;
            } else if(item.getItemId() == R.id.diary){
                fm.beginTransaction().hide(active).show(diaryFragment).commit();
                active = diaryFragment;
                active.onResume();
                return true;
            } else if(item.getItemId() == R.id.home){
                fm.beginTransaction().hide(active).show(homeFragment).commit();
                active = homeFragment;
                active.onResume();
                return true;
            } else if(item.getItemId() == R.id.workout){
                fm.beginTransaction().hide(active).show(workoutFragment).commit();
                active = workoutFragment;
                active.onResume();
                return true;
            } else if(item.getItemId() == R.id.profile) {
                fm.beginTransaction().hide(active).show(profileFragment).commit();
                active = profileFragment;
                active.onResume();
                return true;
            }
            return false;
        });
    }

    // providing one and only context available, throughout contextWrapper
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleExt.toLangIfDiff(newBase, PreferenceManager.getDefaultSharedPreferences(newBase).getString("langPref", "sys"), true, true));
    }

    // applying config changes
    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        super.applyOverrideConfiguration(getBaseContext().getResources().getConfiguration());
    }

    public void setUpMapPageFragmentUI(boolean pedometerRuns){
        mapFragment.setUpUI(pedometerRuns);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // determining whether device rebooted or not
        SharedPreferences savedKeys = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = savedKeys.edit();

        editor.putBoolean("reboot", ((System.currentTimeMillis() - SystemClock.elapsedRealtime()) - (savedKeys.getLong("key_oldDelta", 0))) > 100);

        editor.putLong("key_oldDelta", (System.currentTimeMillis() - SystemClock.elapsedRealtime()));
        editor.apply();

        // applying the color mode needed
        ColorMode.applyColorMode(this, null);

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
        activityTrackingFragment = new ActivityTrackingFragment();

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

        if(ServiceFunctional.getPedometerShouldRun(this))
            ServiceFunctional.startPedometerService(this);
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