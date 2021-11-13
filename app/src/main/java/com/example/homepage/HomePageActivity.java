package com.example.homepage;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.authentication.R;
import com.example.authentication.SignInActivity;
import com.example.network.NetworkManager;
import com.example.util.authentication.AuthFunctional;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class HomePageActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private NetworkManager networkManager;

    // gives welcome message based on time
    private void greetUser(){
        TextView welcomeTextView = findViewById(R.id.welcomeMessage); // TextView in top right corner for welcome message

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

    private void setUpOnClickListeners(){
        // signOutButton listener - click
        findViewById(R.id.signOutHPButton).setOnClickListener(view -> {
            if(AuthFunctional.currentlyOnline)
                Toast.makeText(this,getString(R.string.hold_for_signing_out), Toast.LENGTH_LONG).show();
            else
                AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.signOutHPButton), findViewById(R.id.notification_layout_id));
        });

        // signOutButton listener - hold
        findViewById(R.id.signOutHPButton).setOnLongClickListener(view -> {
            if(AuthFunctional.currentlyOnline)
                auth.signOut();
            else // if there is no internet, the animated notification quick flashes
                AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.signOutHPButton), findViewById(R.id.notification_layout_id));
            return true; // returns true -> onClick doesn't get triggered
        });
    }

    private void updateUI(FirebaseUser user) {
        if(user == null) {
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        greetUser();

        networkManager = new NetworkManager(getApplication());

        // setting up for firebase
        auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){ // redundant
            String name = currentUser.getDisplayName();
            ((TextView) findViewById(R.id.userName)).setText(String.format("%s: %s", getString(R.string.user_name), name));

            String email = currentUser.getEmail();
            boolean isEmailVerified = currentUser.isEmailVerified();
            String emailVerified = (isEmailVerified ? getString(R.string.verified_true) : getString(R.string.verified_false));
            ((TextView) findViewById(R.id.userEmail)).setText(String.format("%s: %s (%s)", getString(R.string.user_email), email, emailVerified));

            Uri photoUri = currentUser.getPhotoUrl();
            if(photoUri == null)
                System.out.println("PhotoUri is null");
            setUpOnClickListeners();
        }

        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if(user != null){
                user.reload(); // used to update data from firebase
                user = firebaseAuth.getCurrentUser(); // necessary
            }
            updateUI(user);
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // registering this activity when user comes first time or returns
        networkManager.registerConnectionObserver(this,findViewById(R.id.homeScreen));
        // adding the listener for firebase to change the UI
        auth.addAuthStateListener(authListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregistering this activity when another activity comes into the foreground or else
        networkManager.unregisterConnectionObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // removing the listener when activity finishes
        if(authListener != null)
            auth.removeAuthStateListener(authListener);
    }
}
