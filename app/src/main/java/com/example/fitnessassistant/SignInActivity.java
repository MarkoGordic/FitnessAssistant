package com.example.fitnessassistant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;

// TODO Handle wrong email input and password input on creating an account (email doesn't need handling, only send the verification email)

public class SignInActivity extends AppCompatActivity {
    private SharedPreferences prefs;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // when password is hidden, its characters are transformed by this function into '●'
    public static class MyPasswordTransformationMethod extends PasswordTransformationMethod {
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

    private boolean emptyET(EditText et){
        return et.getText().toString().equals("") && et.getText().length() <= 0;
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

    // getting user input, showing errors if needed and calling signInUser()
    private void signInUserFromInput(){
        findViewById(R.id.signInButton).setOnClickListener((View v)->{
            EditText emailEdit = findViewById(R.id.edtTxtEmail);
            EditText passEdit = findViewById(R.id.edtTxtPassword);
            if(emptyET(emailEdit))
                error(findViewById(R.id.edtTxtEmail), getString(R.string.empty_email));
            else if(emptyET(passEdit))
                error(findViewById(R.id.edtTxtPassword), getString(R.string.empty_password));
            else if(passEdit.getText().length() <= 5)
                error(findViewById(R.id.edtTxtPassword), getString(R.string.invalid_password));
            else {
                startSignInLoading();
                signInUser(emailEdit.getText().toString(),passEdit.getText().toString());
            }
        });
    }

    // adds toggle to drawable on the right of password editText and transformation method
    private void setUpPassword(){
        addPasswordViewToggle();
        ((EditText) findViewById(R.id.edtTxtPassword)).setTransformationMethod(new MyPasswordTransformationMethod());
    }

    private void setViewSignInScreen(){
        setContentView(R.layout.signin_screen);
        setUpPassword();
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

    private boolean notOnSignInScreen(){
        return findViewById(R.id.signInButton) == null;
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

    // signs user in based on prefs/input
    private void signInUser(String email, String password){
        Task<AuthResult> task = auth.signInWithEmailAndPassword(email, password);
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener(authResult -> {
            storeCredentials(email, password);
            startActivity(new Intent(getApplicationContext(), HomePageActivity.class));
            finish();
        });
        task.addOnFailureListener(e -> {
            if(notOnSignInScreen()) // this can happen if password gets changed w/o the use of our app while user is logged in
                setViewSignInScreen();
            else{
                setSignInError(email);
            }
            finishSignInLoading();
            signInUserFromInput();
        });
    }

    private boolean credentialsExist(){
        return prefs.contains(getString(R.string.email_key)) && prefs.contains(getString(R.string.password_key));
    }

    // called on app opening, if user is logged in loadingAnimation appears, otherwise openingAnimation appears and user can log in
    private void signIn(){
        prefs = getSharedPreferences(getString(R.string.credentials_file_key), MODE_PRIVATE);
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
