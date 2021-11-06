package com.example.fitnessassistant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;

public class SignInActivity extends AppCompatActivity {
    private boolean isOnSignInScreen = false;
    private SharedPreferences prefs;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private void error(EditText place, String message){
        place.setBackground(AppCompatResources.getDrawable(this, R.drawable.custom_input_error));
        place.setError(message);
        place.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            { place.setBackground(AppCompatResources.getDrawable(getApplicationContext(), R.drawable.custom_input));}
            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private boolean empty(EditText et){
        return et.getText().toString().equals("") && et.getText().length() <= 0;
    }

    private void signInLoading(){
        findViewById(R.id.signInButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.signInProgressBar).setVisibility(View.VISIBLE);
    }

    private void finishSignInLoading(){
        findViewById(R.id.signInButton).setVisibility(View.VISIBLE);
        findViewById(R.id.signInProgressBar).setVisibility(View.GONE);
    }

    // TODO Handle wrong email input and password input on creating an account (email doesn't need handling, only send the verification email)
    // getting user input and calling signInUser()
    private void signInUserFromInput(){
        findViewById(R.id.signInButton).setOnClickListener((View v)->{
            EditText emailEdit = findViewById(R.id.edtTxtEmail);
            EditText passEdit = findViewById(R.id.edtTxtPassword);
            if(empty(emailEdit))
                error(findViewById(R.id.edtTxtEmail), getString(R.string.empty_email));
            else if(empty(passEdit))
                error(findViewById(R.id.edtTxtPassword), getString(R.string.empty_password));
            else if(passEdit.getText().length() <= 5)
                error(findViewById(R.id.edtTxtPassword), getString(R.string.invalid_password));
            else {
                signInLoading();
                signInUser(emailEdit.getText().toString(),passEdit.getText().toString());
            }
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

    // signs user in based on prefs/input
    private void signInUser(String email, String password){
        Task<AuthResult> task = auth.signInWithEmailAndPassword(email, password);
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            storeCredentials(email, password);
            startActivity(new Intent(getApplicationContext(), HomePageActivity.class));
            finish();
        });
        task.addOnFailureListener(e -> {
            if(isOnSignInScreen){
                auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        SignInMethodQueryResult result = task1.getResult();
                        if(result != null) {
                            List<String> signInMethods = result.getSignInMethods();
                            if (signInMethods != null) {
                                if (signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD))
                                    error(findViewById(R.id.edtTxtPassword), getString(R.string.incorrect_password));
                                else
                                    error(findViewById(R.id.edtTxtEmail), getString(R.string.email_not_registered));
                            }
                        }
                    } else
                        error(findViewById(R.id.edtTxtEmail), getString(R.string.invalid_email));
                });
            }else // this can happen if password gets changed w/o the use of our app while user is logged in
                setViewSignInScreen();
            finishSignInLoading();
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
