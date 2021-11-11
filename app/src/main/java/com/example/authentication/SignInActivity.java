package com.example.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.homepage.HomePageActivity;
import com.example.network.NetworkManager;
import com.example.util.authentication.AuthFunctional;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// TODO Handle wrong email input and password input on creating an account (email doesn't need handling, only send the verification email)

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private NetworkManager networkManager;

    // sets up listeners for signing in, resetting password
    private void setUpOnClickListeners(){
        // signUpButton listener - checks basic errors, checks sign in errors
        findViewById(R.id.signInButton).setOnClickListener((View v)->{
            if(AuthFunctional.currentlyOnline) {
                EditText emailEdit = findViewById(R.id.edtTxtEmail);
                EditText passEdit = findViewById(R.id.edtTxtPassword);
                if (TextUtils.isEmpty(emailEdit.getText().toString()))
                    AuthFunctional.myError(getApplicationContext(), findViewById(R.id.edtTxtEmail), getString(R.string.empty_email));
                else if (TextUtils.isEmpty(passEdit.getText().toString()))
                    AuthFunctional.myError(getApplicationContext(), findViewById(R.id.edtTxtPassword), getString(R.string.empty_password));
                else if (passEdit.getText().length() <= 5)
                    AuthFunctional.myError(getApplicationContext(), findViewById(R.id.edtTxtPassword), getString(R.string.invalid_password));
                else {
                    AuthFunctional.startLoading(findViewById(R.id.signInButton), findViewById(R.id.signInProgressBar));
                    auth.signInWithEmailAndPassword(emailEdit.getText().toString(), passEdit.getText().toString()).addOnFailureListener(e -> {
                        AuthFunctional.setError(this, emailEdit.getText().toString(), emailEdit, passEdit);
                        AuthFunctional.finishLoading(findViewById(R.id.signInButton), findViewById(R.id.signInProgressBar));
                    });
                }
            } else // if there is no internet, the animated notification quick flashes
                AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.signInButton), findViewById(R.id.notification_layout_id));
        });

        // forgotPassword listener - going to the PasswordResetActivity
        findViewById(R.id.forgotPassword).setOnClickListener(view -> startActivity(new Intent(this, PasswordResetActivity.class)));
    }

    // sets everything up for signing in
    private void setUpSignInUI(){
        setContentView(R.layout.sign_in_screen);
        AuthFunctional.setUpPassword(findViewById(R.id.edtTxtPassword));
        setUpOnClickListeners();
        // registering this activity when user comes first time or returns
        networkManager.registerConnectionObserver(this,findViewById(R.id.signInScreen));
    }

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

    // if user exists loadingAnimation appears prior to HomePage, otherwise openingAnimation appears prior to SignInPage
    private void updateUI(FirebaseUser user) {
        if (user != null)
            goToHomePageUI();
        else
            setUpSignInUI();
    }

    // declares firebase instance and creates the listener
    //  sets up networkManager and performs openingAnimation
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_screen);
        networkManager = new NetworkManager(getApplication());

        // setting up for firebase
        auth = FirebaseAuth.getInstance();
        authListener = firebaseAuth -> updateUI(firebaseAuth.getCurrentUser());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // adding the listener for firebase to change the UI furthermore
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
