package com.example.fitnessassistant;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;

public class PasswordResetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_reset_screen);
        findViewById(R.id.backToSignInButton).setOnClickListener(view -> onBackPressed());
    }

    private void startPasswordResetLoading(){
        findViewById(R.id.resetPasswordButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.resetPasswordBar).setVisibility(View.VISIBLE);
    }

    private void finishPasswordResetLoading(){
        findViewById(R.id.resetPasswordButton).setVisibility(View.VISIBLE);
        findViewById(R.id.resetPasswordBar).setVisibility(View.GONE);
    }

    // sets errors on based on user's entered email
    private void setPasswordResetError(String email){
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SignInMethodQueryResult result = task.getResult();
                if(result != null) {
                    List<String> signInMethods = result.getSignInMethods();
                    if (signInMethods != null)
                        if (!signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD))
                            myError(findViewById(R.id.emailEditForReset), getString(R.string.email_not_registered));
                }
            } else
                myError(findViewById(R.id.emailEditForReset), getString(R.string.invalid_email));
        });
    }

    // setting the outline of place to red and undoing when text is changed, also sets an error message
    private void myError(EditText place, String message){
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

    @Override
    protected void onStart() {
        super.onStart();
        findViewById(R.id.resetPasswordButton).setOnClickListener(view1 -> {
            EditText emailEdit = findViewById(R.id.emailEditForReset);
            if (TextUtils.isEmpty(emailEdit.getText().toString()))
                myError(findViewById(R.id.emailEditForReset), getString(R.string.empty_email));
            else {
                startPasswordResetLoading();
                // sending resetPassword email
                FirebaseAuth.getInstance().sendPasswordResetEmail(emailEdit.getText().toString()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        finishPasswordResetLoading();
                        ((TextView) findViewById(R.id.smallResetPasswordTextView)).setText(getString(R.string.link_has_been_sent));
                        // TODO SUCCESS - Bring email back to the SignInActivity
                        //  also clean up two group of functions above and clean up the code
                        //   also read about spamming with reset password (handle that in app)
                    } else {
                        finishPasswordResetLoading();
                        setPasswordResetError(emailEdit.getText().toString());
                    }
                });
            }
        });
    }
}
