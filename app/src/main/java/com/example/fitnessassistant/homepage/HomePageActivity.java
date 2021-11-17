package com.example.fitnessassistant.homepage;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.authentication.SignInActivity;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.util.AuthFunctional;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

// TODO After adding in-app password change, update current user and make sure he doesn't get redirected to sign in
//  also for in-app email change

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
    }

    private void displayCurrentUser(){
        // setting up current user - this is in onResume in case anything gets changed after onPause()
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null) {
            // try to reload the user (update) to get the latest data
            currentUser.reload().addOnCompleteListener(task -> {
                if (!task.isSuccessful())
                    try{
                        if(task.getException() != null){
                            throw task.getException();
                        }
                    } catch (FirebaseNetworkException e1){ // if it fails and it's a network error, the animated notification quickly flashes
                        AuthFunctional.quickFlash(getApplicationContext(), null, findViewById(R.id.notification_layout_id));
                        // we're displaying the user's profile (last saved)
                        ((TextView) findViewById(R.id.userNameTextView)).setText(String.format("%s: %s", getString(R.string.user_name), currentUser.getDisplayName()));
                        ((TextView) findViewById(R.id.userEmailTextView)).setText(String.format("%s: %s", getString(R.string.user_email), currentUser.getEmail()));
                    } catch(Exception e2){ // if it fails and we're online(user deleted, disabled or credentials no longer valid) -> return to sign in
                        startActivity(new Intent(this, SignInActivity.class));
                        finish();
                    }
                else{
                    // we're displaying the user's profile (reloaded)
                    ((TextView) findViewById(R.id.userNameTextView)).setText(String.format("%s: %s", getString(R.string.user_name), currentUser.getDisplayName()));
                    ((TextView) findViewById(R.id.userEmailTextView)).setText(String.format("%s: %s", getString(R.string.user_email), currentUser.getEmail()));
                }
            });
        }
    }

    // sets up listeners for signing out
    private void setUpOnClickListeners(){
        // signOutButton listener - click
        findViewById(R.id.signOutButton).setOnClickListener(view -> {
            if(AuthFunctional.currentlyOnline)
                Toast.makeText(getApplicationContext(),getString(R.string.hold_for_signing_out), Toast.LENGTH_LONG).show(); // it's better to sign out on hold, user can click this on accident
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getApplicationContext(), ((Button) view), findViewById(R.id.notification_layout_id));
        });

        // signOutButton listener - hold
        findViewById(R.id.signOutButton).setOnLongClickListener(view -> {
            if(AuthFunctional.currentlyOnline)
                FirebaseAuth.getInstance().signOut();
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getApplicationContext(), ((Button) view), findViewById(R.id.notification_layout_id));
            return true; // returns true -> onClick doesn't get triggered
        });
    }

    // if user is somehow signed out, go to sign in
    private void updateUI() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        }
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account == null){
            startActivity(new Intent(this, SignInActivity.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        greetUser();
        setUpOnClickListeners();

        networkManager = new NetworkManager(getApplication());

        // setting up listener for firebase
        authListener = firebaseAuth -> updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // registering this activity when user comes first time or returns
        networkManager.registerConnectionObserver(this,findViewById(R.id.homeScreen));
        // adding the listener for firebase to change the UI
        FirebaseAuth.getInstance().addAuthStateListener(authListener);

        displayCurrentUser();
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