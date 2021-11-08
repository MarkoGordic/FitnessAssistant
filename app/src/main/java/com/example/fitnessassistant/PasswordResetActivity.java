package com.example.fitnessassistant;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

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

    // sets errors on based on user's entered email
    private void setPasswordResetError(String email){
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                SignInMethodQueryResult result = task.getResult();
                if(result != null) {
                    List<String> signInMethods = result.getSignInMethods();
                    if (signInMethods != null)
                        if (!signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD))
                            SignInFunctional.myError(getApplicationContext(),findViewById(R.id.emailEditForReset), getString(R.string.email_not_registered));
                }
            } else
                SignInFunctional.myError(getApplicationContext(),findViewById(R.id.emailEditForReset), getString(R.string.invalid_email));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        findViewById(R.id.resetPasswordButton).setOnClickListener(view1 -> {
            EditText emailEdit = findViewById(R.id.emailEditForReset);
            if (TextUtils.isEmpty(emailEdit.getText().toString()))
                SignInFunctional.myError(getApplicationContext(),findViewById(R.id.emailEditForReset), getString(R.string.empty_email));
            else {
                SignInFunctional.startLoading(findViewById(R.id.resetPasswordButton), findViewById(R.id.resetPasswordBar));
                // sending resetPassword email
                String email = emailEdit.getText().toString();
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SignInFunctional.finishLoading(findViewById(R.id.resetPasswordButton), findViewById(R.id.resetPasswordBar));
                        // TODO - show success (in some AnimationOnEnd finish()) - ((TextView) findViewById(R.id.smallResetPasswordTextView)).setText(getString(R.string.link_has_been_sent));
                        SignInFunctional.emailLinked = email;
                        finish();
                        //  TODO  read about spamming with reset password (maybe handle that in app)
                    } else {
                        SignInFunctional.finishLoading(findViewById(R.id.resetPasswordButton), findViewById(R.id.resetPasswordBar));
                        setPasswordResetError(email);
                    }
                });
            }
        });
    }
}
