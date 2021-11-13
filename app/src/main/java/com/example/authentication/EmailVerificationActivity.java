package com.example.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.homepage.HomePageActivity;
import com.example.network.NetworkManager;
import com.example.util.authentication.AuthFunctional;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private NetworkManager networkManager;

    // when everything is done, go to homePage
    private void goToHomePageUI(){
        setContentView(R.layout.opening_screen);
        Animation loadAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.authentication_loading);
        loadAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                // after loading goes to HomePageActivity
                startActivity(new Intent(getApplicationContext(), HomePageActivity.class));
                finish();
            }
        });
        findViewById(R.id.FitnessAssistant).startAnimation(loadAnim);
    }

    // firebase listener
    private void updateUI(FirebaseUser user){
        if(user != null) {
            if (user.isEmailVerified())
                goToHomePageUI();
            else{
                // user-specific textView set up and animation - notifying that email is not verified
                ((TextView) findViewById(R.id.userEmailTextView)).setText(String.format("%s: %s (%s)", getString(R.string.user_email), user.getEmail(), getString(R.string.verified_false)));
                findViewById(R.id.userEmailTextView).setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
            }
        } else { // if no user is signed in -> user is signed out -> go to sign in screen
            startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            finish();
        }
    }

    // setting up listeners for signing out and verifying email
    private void setUpOnClickListeners(){
        // signOutButton listener - click
        findViewById(R.id.signOutButton).setOnClickListener(view -> {
            if(AuthFunctional.currentlyOnline)
                Toast.makeText(this,getString(R.string.hold_for_signing_out), Toast.LENGTH_LONG).show();
            else
                AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.signOutButton), findViewById(R.id.notification_layout_id));
        });

        // signOutButton listener - hold
        findViewById(R.id.signOutButton).setOnLongClickListener(view -> {
            if(AuthFunctional.currentlyOnline)
                auth.signOut();
            else // if there is no internet, the animated notification quick flashes
                AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.signOutButton), findViewById(R.id.notification_layout_id));
            return true; // returns true -> onClick doesn't get triggered
        });

        // verifyEmailButton listener
        findViewById(R.id.verifyEmailButton).setOnClickListener(view -> {
            if(AuthFunctional.currentlyOnline){
                AuthFunctional.startLoading(view, findViewById(R.id.verifyEmailBar));
                FirebaseUser user = auth.getCurrentUser();
                if(user != null) // send email verification
                    user.sendEmailVerification().addOnCompleteListener(task -> {
                        AuthFunctional.finishLoading(view, findViewById(R.id.verifyEmailBar));
                        // display results based on if email was sent successfully
                        AuthFunctional.displayEmailVerificationResults(getApplicationContext(), findViewById(R.id.smallVerifyEmailTextView), task.isSuccessful());
                    });
            } else{ // if there is no internet, the animated notification quick flashes
                AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.verifyEmailButton), findViewById(R.id.notification_layout_id));
            }
        });
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting up UI
        setContentView(R.layout.email_verification_screen);
        setUpOnClickListeners();

        networkManager = new NetworkManager(getApplication());

        // setting up for firebase
        auth = FirebaseAuth.getInstance();
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
        networkManager.registerConnectionObserver(this,findViewById(R.id.emailVerificationScreen));
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