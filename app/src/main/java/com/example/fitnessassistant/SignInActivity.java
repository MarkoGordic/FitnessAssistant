package com.example.fitnessassistant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;

// TODO Handle wrong email input and password input on creating an account (email doesn't need handling, only send the verification email)

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    // when password is hidden, its characters are transformed by this function into '●'
    private static class MyPasswordTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(super.getTransformation(source, view));
        }
        private static class PasswordCharSequence implements CharSequence {
            private final CharSequence mSource;
            public PasswordCharSequence(CharSequence source) { mSource = source; }
            public char charAt(int index) {
                if(mSource.charAt(index) == '\u2022') // '\u2022' are dots used by default
                    return '●'; // they are replaced by bigger ones
                else
                    return mSource.charAt(index);
            }
            public int length() { return mSource.length(); }
            @NonNull
            public CharSequence subSequence(int start, int end) { return mSource.subSequence(start, end); }
        }
    }

    // adding the toggle view option for lock drawable in password editText
    @SuppressLint("ClickableViewAccessibility")
    private void addPasswordViewToggle() {
        EditText pass = findViewById(R.id.edtTxtPassword);
        Typeface defaultTypeface = pass.getTypeface(); // regular font converted to typeface
        TransformationMethod defaultTransformationMethod = pass.getTransformationMethod(); // used for regular text
        pass.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2; // index of right drawables
            if (event.getAction() == MotionEvent.ACTION_UP) {
                // ACTION_DOWN = finger on screen, ACTION_UP = finger on -> off screen
                // getRawX() is where touch is registered, anything on x axis greater than eTRightPosition - 2 * drawableWidth is registered
                if (event.getRawX() >= (pass.getRight() - 2 * pass.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    if (pass.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        pass.setTransformationMethod(new MyPasswordTransformationMethod());
                        pass.setTypeface(defaultTypeface); // used because it changes the font
                        pass.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.custom_lock, 0); // changes the icon
                    } else {
                        pass.setTransformationMethod(defaultTransformationMethod);
                        pass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        pass.setTypeface(defaultTypeface); // used because it changes the font
                        pass.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.custom_unlock, 0); // changes the icon
                    }
                    return true;
                }
            }
            return false;
        });
    }

    // setting the outline of place to red and undoing when text is changed, also sets an error message
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

    // sets sign in error based on user input
    private void setSignInError(String email){
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
    }

    // making the circular progress indicator visible while hiding the button
    private void startSignInLoading(){
        findViewById(R.id.signInButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.signInProgressBar).setVisibility(View.VISIBLE);
    }

    // removing the circular progress indicator, making the button visible again
    private void finishSignInLoading(){
        findViewById(R.id.signInButton).setVisibility(View.VISIBLE);
        findViewById(R.id.signInProgressBar).setVisibility(View.GONE);
    }

    // sets a listener for signInButton that's showing errors in input and signing in
    private void signInUserFromInput(){
        findViewById(R.id.signInButton).setOnClickListener((View v)->{
            EditText emailEdit = findViewById(R.id.edtTxtEmail);
            EditText passEdit = findViewById(R.id.edtTxtPassword);
            if(TextUtils.isEmpty(emailEdit.getText().toString()))
                error(findViewById(R.id.edtTxtEmail), getString(R.string.empty_email));
            else if(TextUtils.isEmpty(passEdit.getText().toString()))
                error(findViewById(R.id.edtTxtPassword), getString(R.string.empty_password));
            else if(passEdit.getText().length() <= 5)
                error(findViewById(R.id.edtTxtPassword), getString(R.string.invalid_password));
            else {
                startSignInLoading();
                auth.signInWithEmailAndPassword(emailEdit.getText().toString(), passEdit.getText().toString()).addOnFailureListener(e -> {
                    setSignInError(emailEdit.getText().toString());
                    finishSignInLoading();
                });
            }
        });
    }

    // adds toggle to drawable on the right of password editText and transformation method
    private void setUpPassword(){
        addPasswordViewToggle();
        ((EditText) findViewById(R.id.edtTxtPassword)).setTransformationMethod(new MyPasswordTransformationMethod());
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
                setContentView(R.layout.signin_screen);
                setUpPassword();
                signInUserFromInput();
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
