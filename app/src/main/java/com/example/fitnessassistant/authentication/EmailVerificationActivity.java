package com.example.fitnessassistant.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.home.HomePageActivity;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.util.AuthFunctional;
import com.facebook.login.LoginManager;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {
    private FirebaseAuth.AuthStateListener authListener;
    private NetworkManager networkManager;
    private boolean changeCredentialsButtonPressed = false; // used for back button

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
            ((SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout)).setRefreshing(true);
            if(!AuthFunctional.currentlyOnline){
                // if there is no internet, we can't reload, just load previously saved user's info and quickly flash the notification about connectivity
                ((TextView) findViewById(R.id.userEmailNotVerifiedTextView)).setText(String.format("%s (%s)", user.getEmail(), getString(R.string.verified_false)));
                findViewById(R.id.userEmailNotVerifiedTextView).setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.notification_layout_id));
                ((SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout)).setRefreshing(false);
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
                        ((TextView) findViewById(R.id.userEmailNotVerifiedTextView)).setText(String.format("%s (%s)", user.getEmail(), getString(R.string.verified_false)));
                        findViewById(R.id.userEmailNotVerifiedTextView).setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                        ((SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout)).setRefreshing(false);
                    }
                });
            }
        }
    }

    // sets up listeners for refreshing, signing out(changing credentials) and verifying email
    private void setUpOnClickListeners(){
        // swipeRefreshLayout refresh listener - refreshes for 1.5s while updating UI
        ((SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout)).setOnRefreshListener(this::updateUI);

        findViewById(R.id.changeCredentialsButton).setOnClickListener(view -> {
            if(!AuthFunctional.currentlyOnline) // if there is no internet, the animated notification quick flashes
                AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.notification_layout_id));
            else {
                if (changeCredentialsButtonPressed) {
                    FirebaseAuth.getInstance().signOut();
                    LoginManager.getInstance().logOut();
                    // signing out from facebook because they save it separately
                } else {
                    changeCredentialsButtonPressed = true;
                    Toast.makeText(getApplicationContext(), R.string.press_again_to_sign_out, Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(() -> changeCredentialsButtonPressed = false, 2000);
                }
            }
        });

        // verifyEmailButton listener
        findViewById(R.id.sendEmailVerificationButton).setOnClickListener(view -> {
            AuthFunctional.startLoading(view, findViewById(R.id.sendEmailVerificationBar));
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null) // probably redundant, reload(update) user
                if(!AuthFunctional.currentlyOnline) { // if there is no internet, the animated notification quick flashes
                    AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.notification_layout_id));
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
                                        AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.notification_layout_id));
                                    } catch (Exception e2){ // notify the user about the other error
                                        ((TextView)findViewById(R.id.resendEmailMessageTextView)).setText(getString(R.string.unsuccessful_verification_sent));
                                        findViewById(R.id.resendEmailMessageTextView).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                                    }
                                } else // if it's successful, give the user feedback that the email was sent
                                    AuthFunctional.emailVerificationSentAnimation(getApplicationContext(), findViewById(R.id.resendEmailMessageTextView));
                            });
                    });
                }
        });
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