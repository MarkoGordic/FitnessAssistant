package com.example.fitnessassistant;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;

// TODO Handle wrong email input and password input on creating an account (email doesn't need handling, only send the verification email)

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    // sets sign in error based on user input
    private void setSignInError(String email){
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SignInMethodQueryResult result = task.getResult();
                if(result != null) {
                    List<String> signInMethods = result.getSignInMethods();
                    if (signInMethods != null) {
                        if (signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD))
                            SignInFunctional.myError(getApplicationContext(), findViewById(R.id.edtTxtPassword), getString(R.string.incorrect_password));
                        else
                            SignInFunctional.myError(getApplicationContext(), findViewById(R.id.edtTxtEmail), getString(R.string.email_not_registered));
                    }
                }
            } else
                SignInFunctional.myError(getApplicationContext(), findViewById(R.id.edtTxtEmail), getString(R.string.invalid_email));
        });
    }

    // sets up listeners for signing in, creating an account, resetting password
    private void setUpOnClickListeners(){
        // signUpButton listener
        findViewById(R.id.signInButton).setOnClickListener((View v)->{
            EditText emailEdit = findViewById(R.id.edtTxtEmail);
            EditText passEdit = findViewById(R.id.edtTxtPassword);
            if(TextUtils.isEmpty(emailEdit.getText().toString()))
                SignInFunctional.myError(getApplicationContext(), findViewById(R.id.edtTxtEmail), getString(R.string.empty_email));
            else if(TextUtils.isEmpty(passEdit.getText().toString()))
                SignInFunctional.myError(getApplicationContext(), findViewById(R.id.edtTxtPassword), getString(R.string.empty_password));
            else if(passEdit.getText().length() <= 5)
                SignInFunctional.myError(getApplicationContext(), findViewById(R.id.edtTxtPassword), getString(R.string.invalid_password));
            else {
                SignInFunctional.startLoading(findViewById(R.id.signInButton), findViewById(R.id.signInProgressBar));
                auth.signInWithEmailAndPassword(emailEdit.getText().toString(), passEdit.getText().toString()).addOnFailureListener(e -> {
                    setSignInError(emailEdit.getText().toString());
                    SignInFunctional.finishLoading(findViewById(R.id.signInButton), findViewById(R.id.signInProgressBar));
                });
            }
        });

        // forgotPassword listener
        findViewById(R.id.forgotPassword).setOnClickListener(view -> startActivity(new Intent(this, PasswordResetActivity.class)));
    }

    // used at the start of the app
    private void openingAnimation(){
        Animation openAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fitnessassistant_opening);
        openAnim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                setContentView(R.layout.sign_in_screen);

                // it won't be null after successful resetPassword email sent or
                // TODO after creating an account
                if(SignInFunctional.emailLinked != null){
                    ((EditText) findViewById(R.id.edtTxtEmail)).setText(SignInFunctional.emailLinked);
                    SignInFunctional.emailLinked = null;
                }

                SignInFunctional.setUpPassword(((EditText) findViewById(R.id.edtTxtPassword)));

                setUpOnClickListeners();
            }
        });
        findViewById(R.id.FitnessAssistant).startAnimation(openAnim);
    }

    // used when user has to wait
    private void loadingAnimation(){
        Animation loadAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fitnessassistant_loading);
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
            loadingAnimation();
        else
            openingAnimation();
    }

    // declares firebase instance and creates the listener
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        authListener = firebaseAuth -> updateUI(firebaseAuth.getCurrentUser());
    }

    // sets the listener
    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    // removes the listener
    @Override
    protected void onStop() {
        super.onStop();
        if(authListener != null)
            auth.removeAuthStateListener(authListener);
    }
}
