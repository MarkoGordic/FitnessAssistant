package com.example.fitnessassistant.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.homepage.HomePageActivity;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.util.AuthFunctional;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// TODO later on, add real refresh to the whole layout instead of a button (not a big job)
//  also adding refresh more throughout the app could be pretty helpful

public class EmailVerificationActivity extends AppCompatActivity {
    private FirebaseAuth.AuthStateListener authListener;
    private NetworkManager networkManager;

    private void goToHomePageUI(){
        setContentView(R.layout.loading_screen);
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
        findViewById(R.id.appLogo).startAnimation(loadAnim);
    }

    private void updateUI(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { // if user is null -> user is signed out -> go to sign in screen
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        } else if (user.isEmailVerified()) // if saved user is email verified, go to home page
            goToHomePageUI();
        else{ // if saved user's email is not verified, try to reload if possible
            AuthFunctional.startLoading(findViewById(R.id.refreshButton), findViewById(R.id.refreshBar));
            if(!AuthFunctional.currentlyOnline){
                // if there is no internet, we can't reload, just load previously saved user's info and quickly flash the notification about connectivity
                ((TextView) findViewById(R.id.userEmailNotVerifiedTextView)).setText(String.format("%s: %s (%s)", getString(R.string.user_email), user.getEmail(), getString(R.string.verified_false)));
                findViewById(R.id.userEmailNotVerifiedTextView).setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.refreshButton), findViewById(R.id.notification_layout_id));
                AuthFunctional.finishLoading(findViewById(R.id.refreshButton), findViewById(R.id.refreshBar));
            } else { // if there is internet, we try to reload the user
                user.reload().addOnCompleteListener(task -> {
                    if (!task.isSuccessful())
                        try {
                            if (task.getException() != null)
                                throw task.getException();
                        } catch (Exception e) { // if unsuccessful (user deleted, disabled or credentials no longer valid) -> return to sign in
                            startActivity(new Intent(this, SignInActivity.class));
                            finish();
                        }
                    else if (user.isEmailVerified()) // if reloaded user has emailVerified, go to home page (this may get triggered often)
                        goToHomePageUI();
                    else { // if not, just load reloaded user's info
                        ((TextView) findViewById(R.id.userEmailNotVerifiedTextView)).setText(String.format("%s: %s (%s)", getString(R.string.user_email), user.getEmail(), getString(R.string.verified_false)));
                        findViewById(R.id.userEmailNotVerifiedTextView).setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                        AuthFunctional.finishLoading(findViewById(R.id.refreshButton), findViewById(R.id.refreshBar));
                    }
                });
            }
        }
    }

    // sets up listeners for signing out(changing credentials), verifying email and refreshing
    private void setUpOnClickListeners(){
        // signOutButton listener - click - tells user to hold to sign out(it's better, click can happen on accident)
        findViewById(R.id.changeCredentialsButton).setOnClickListener(view -> {
            if(AuthFunctional.currentlyOnline)
                Toast.makeText(getApplicationContext(),getString(R.string.hold_for_signing_out), Toast.LENGTH_LONG).show();
            else
                AuthFunctional.quickFlash(getApplicationContext(), ((Button) view), findViewById(R.id.notification_layout_id));
        });

        // signOutButton listener - hold - signing out user
        findViewById(R.id.changeCredentialsButton).setOnLongClickListener(view -> {
            if(AuthFunctional.currentlyOnline)
                FirebaseAuth.getInstance().signOut();
            else // if there is no internet, the animated notification quick flashes
                AuthFunctional.quickFlash(getApplicationContext(), ((Button) view), findViewById(R.id.notification_layout_id));
            return true; // returns true -> onClick doesn't get triggered
        });

        // verifyEmailButton listener
        findViewById(R.id.sendEmailVerificationButton).setOnClickListener(view -> {
            AuthFunctional.startLoading(view, findViewById(R.id.sendEmailVerificationBar));
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null) // probably redundant, reload(update) user
                if(!AuthFunctional.currentlyOnline) { // if there is no internet, the animated notification quick flashes
                    AuthFunctional.quickFlash(getApplicationContext(), ((Button) view), findViewById(R.id.notification_layout_id));
                    AuthFunctional.finishLoading(view, findViewById(R.id.sendEmailVerificationBar));
                } else { // if there is internet, we try to reload the user
                    user.reload().addOnCompleteListener(task -> {
                        if (!task.isSuccessful())
                            try {
                                if (task.getException() != null)
                                    throw task.getException();
                            } catch (Exception e) { // if unsuccessful (user deleted, disabled or credentials no longer valid) -> return to sign in
                                startActivity(new Intent(this, SignInActivity.class));
                                finish();
                            }
                        else if (user.isEmailVerified()) // if reloaded user has emailVerified, go to home page (this may get triggered often)
                            goToHomePageUI();
                        else
                            user.sendEmailVerification().addOnCompleteListener(task1 -> {
                                AuthFunctional.finishLoading(view, findViewById(R.id.sendEmailVerificationBar));
                                // display results based on if email was sent successfully
                                if(!task1.isSuccessful()){ // if it's not successful, notify the user about the error with sending the email verification
                                    try{
                                        if(task1.getException() != null)
                                            throw task1.getException();
                                    } catch (FirebaseNetworkException e1){ // if it's a network error, the no connectivity notification quickly flashes
                                        AuthFunctional.quickFlash(getApplicationContext(), ((Button) view), findViewById(R.id.notification_layout_id));
                                    } catch (Exception e2){ // notify the user about the other error
                                        ((TextView)findViewById(R.id.smallVerifyEmailTextView)).setText(getString(R.string.unsuccessful_verification_sent));
                                        findViewById(R.id.smallVerifyEmailTextView).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                                    }
                                } else // if it's successful, give the user feedback that the email was sent
                                    AuthFunctional.emailVerificationSentAnimation(getApplicationContext(), findViewById(R.id.smallVerifyEmailTextView));
                            });
                    });
                }
        });

        // refreshButton listener - updates UI(refreshes)
        findViewById(R.id.refreshButton).setOnClickListener(view -> updateUI());

    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_verification_screen);
        setUpOnClickListeners();

        networkManager = new NetworkManager(getApplication());

        // setting up listener for firebase
        authListener = firebaseAuth -> updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // registering this activity when user comes first time or returns
        networkManager.registerConnectionObserver(this,findViewById(R.id.emailVerificationScreen));
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