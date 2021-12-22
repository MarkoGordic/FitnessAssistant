package com.example.fitnessassistant;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessassistant.authentication.SignInActivity;
import com.example.fitnessassistant.notifications.NotificationController;
import com.example.fitnessassistant.uiprefs.ColorMode;
import com.example.fitnessassistant.uiprefs.LocaleExt;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;

public class LaunchActivity extends AppCompatActivity {

    // used at the start of the app
    private void openingAnimation(){
        setContentView(R.layout.loading_screen);
        Animation openAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.authentication_opening);
        openAnim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                finish();
            }
        });
        findViewById(R.id.appLogo).startAnimation(openAnim);
    }

    // providing only one context through contextWrapper
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleExt.toLangIfDiff(newBase, PreferenceManager.getDefaultSharedPreferences(newBase).getString("langPref", "sys"), true));
    }

    // applying config changes
    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        super.applyOverrideConfiguration(getBaseContext().getResources().getConfiguration());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initializing notification channels
        NotificationController.createNotificationChannel(this, "Pedometer", "Showing step counter data.", false);

        // initializing safety net
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(SafetyNetAppCheckProviderFactory.getInstance());

        // apply the color mode needed
        ColorMode.applyColorMode(this, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        openingAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // if activity gets paused, we remove any animations attached
        if(findViewById(R.id.appLogo) != null)
            findViewById(R.id.appLogo).setAnimation(null);
    }
}