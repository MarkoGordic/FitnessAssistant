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

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private NetworkManager networkManager;

    // sets up listeners for signing in, resetting password, creating an account(registering)
    private void setUpOnClickListeners(){
        // signUpButton listener - checks basic errors, checks sign in errors
        findViewById(R.id.signInButton).setOnClickListener((View v)->{
            if(AuthFunctional.currentlyOnline) {
                EditText emailEdit = findViewById(R.id.edtTxtEmail);
                EditText passEdit = findViewById(R.id.edtTxtPassword);
                if (TextUtils.isEmpty(emailEdit.getText().toString()))
                    AuthFunctional.myError(getApplicationContext(), emailEdit, getString(R.string.empty_email));
                else if (TextUtils.isEmpty(passEdit.getText().toString()))
                    AuthFunctional.myError(getApplicationContext(), passEdit, getString(R.string.empty_password));
                else if (passEdit.getText().length() <= 5)
                    AuthFunctional.myError(getApplicationContext(), passEdit, getString(R.string.password_not_enough_characters));
                else {
                    AuthFunctional.startLoading(findViewById(R.id.signInButton), findViewById(R.id.signInProgressBar));
                    auth.signInWithEmailAndPassword(emailEdit.getText().toString(), passEdit.getText().toString()).addOnCompleteListener(task -> {
                        AuthFunctional.finishLoading(findViewById(R.id.signInButton), findViewById(R.id.signInProgressBar));
                        if(!task.isSuccessful())
                            AuthFunctional.setError(this, emailEdit.getText().toString(), emailEdit, passEdit);
                    });
                }
            } else // if there is no internet, the animated notification quick flashes
                AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.signInButton), findViewById(R.id.notification_layout_id));
        });

        // forgotPassword listener - going to the PasswordResetActivity
        findViewById(R.id.forgotPassword).setOnClickListener(view -> startActivity(new Intent(this, PasswordResetActivity.class)));

        // createAccount listener - going to the CreateAccountActivity
        findViewById(R.id.createAccount).setOnClickListener(view -> startActivity(new Intent(this, CreateAccountActivity.class)));
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

    // if user exists, emailVerification is checked and he is redirected to a new UI, otherwise he stays here
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            if (user.isEmailVerified())
                goToHomePageUI();
            else {
                user.sendEmailVerification();
                startActivity(new Intent(getApplicationContext(), EmailVerificationActivity.class));
                finish();
            }
        }
    }

    // declares firebase instance, sets up networkManager and creates the listener
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // setting up SignIn UI
        setContentView(R.layout.sign_in_screen);
        AuthFunctional.setUpPassword(findViewById(R.id.edtTxtPassword));
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
        networkManager.registerConnectionObserver(this,findViewById(R.id.signInScreen));
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
