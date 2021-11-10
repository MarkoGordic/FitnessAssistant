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

    // sets up listeners for signing in, creating an account, resetting password
    private void setUpOnClickListeners(){
        // signUpButton listener
        findViewById(R.id.signInButton).setOnClickListener((View v)->{
            EditText emailEdit = findViewById(R.id.edtTxtEmail);
            EditText passEdit = findViewById(R.id.edtTxtPassword);
            if(TextUtils.isEmpty(emailEdit.getText().toString()))
                AuthFunctional.myError(getApplicationContext(), findViewById(R.id.edtTxtEmail), getString(R.string.empty_email));
            else if(TextUtils.isEmpty(passEdit.getText().toString()))
                AuthFunctional.myError(getApplicationContext(), findViewById(R.id.edtTxtPassword), getString(R.string.empty_password));
            else if(passEdit.getText().length() <= 5)
                AuthFunctional.myError(getApplicationContext(), findViewById(R.id.edtTxtPassword), getString(R.string.invalid_password));
            else {
                AuthFunctional.startLoading(findViewById(R.id.signInButton), findViewById(R.id.signInProgressBar));
                auth.signInWithEmailAndPassword(emailEdit.getText().toString(), passEdit.getText().toString()).addOnFailureListener(e -> {
                    AuthFunctional.setError(this, emailEdit.getText().toString(), emailEdit, passEdit);
                    AuthFunctional.finishLoading(findViewById(R.id.signInButton), findViewById(R.id.signInProgressBar));
                });
            }
        });

        // forgotPassword listener
        findViewById(R.id.forgotPassword).setOnClickListener(view -> startActivity(new Intent(this, PasswordResetActivity.class)));
    }

    // sets everything up for signing in
    private void setUpSignInUI(){
        setContentView(R.layout.sign_in_screen);
        AuthFunctional.setUpPassword(findViewById(R.id.edtTxtPassword));
        setUpOnClickListeners();
    }

    // used at the start of the app
    private void openingAnimation(){
        setContentView(R.layout.opening_screen);
        Animation openAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.authentication_opening);
        openAnim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                // after opening animation ends adding the listener that will change UI furthermore
                auth.addAuthStateListener(authListener);
            }
        });
        findViewById(R.id.FitnessAssistant).startAnimation(openAnim);
    }

    private void goToHomePageUI(){
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
        setContentView(R.layout.opening_screen);
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
        auth = FirebaseAuth.getInstance();
        networkManager = new NetworkManager(getApplication());
        authListener = firebaseAuth -> updateUI(firebaseAuth.getCurrentUser());
        openingAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // registering this activity when user comes first time or returns
        networkManager.registerConnectionObserver(this);
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
