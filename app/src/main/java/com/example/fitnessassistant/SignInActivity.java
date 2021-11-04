package com.example.fitnessassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {
    private boolean isOnSignInScreen = false;
    private SharedPreferences prefs;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // TODO Handle wrong input(null, pass <5 chars,...)
    // getting user input and calling signInUser()
    private void signInUserFromInput(){
        findViewById(R.id.signInButton).setOnClickListener((View v)->{
            String email = ((EditText) findViewById(R.id.edtTxtEmail)).getText().toString();
            String password = ((EditText) findViewById(R.id.edtTxtPassword)).getText().toString(); // added so user can't spam click button
            signInUser(email,password);
        });
    }

    private void setViewSignInScreen(){
        setContentView(R.layout.signin_screen);
        isOnSignInScreen = true;
    }

    // used at the start of the app
    private void openingAnimation(){
        TextView appName = findViewById(R.id.FitnessAssistant);
        Animation openAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fitnessassistant_opening);

        openAnim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                setViewSignInScreen();
                signInUserFromInput();
            }
        });
        appName.startAnimation(openAnim);
    }

    // used when user has to wait
    private void loadingAnimation(){
        Animation loadAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fitnessassistant_loading);
        findViewById(R.id.FitnessAssistant).startAnimation(loadAnim);
    }

    // saving credentials if user has successfully signed in
    private void storeCredentials(String email, String password){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.email_key), email);
        editor.putString(getString(R.string.password_key), password);
        editor.apply();
    }

    private void shortToast(String message){
        Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    // signs user in based on prefs/input
    private void signInUser(String email, String password){
        Task<AuthResult> task = auth.signInWithEmailAndPassword(email, password);
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            storeCredentials(email, password);
            startActivity(new Intent(getApplicationContext(), HomePageActivity.class));
            finish();
        });
        task.addOnFailureListener(e -> {
            if(isOnSignInScreen)
                shortToast(getString(R.string.sign_in_failed)); // TODO Make credentials red (don't clear them completely)
            else // this can happen if password gets changed w/o the use of our app while user is logged in
                setViewSignInScreen();
            signInUserFromInput();
        });
    }

    private boolean credentialsExist(){
        return prefs.contains(getString(R.string.email_key)) && prefs.contains(getString(R.string.password_key));
    }

    // called on app opening, if user is logged in loadingAnimation appears, otherwise openingAnimation appears and user can log in
    private void signIn(){
        prefs = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);
        if(credentialsExist()){
            loadingAnimation();
            signInUser(prefs.getString(getString(R.string.email_key), ""), prefs.getString(getString(R.string.password_key), ""));
        } else{
            openingAnimation();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opening_screen);
        signIn();
    }
}
