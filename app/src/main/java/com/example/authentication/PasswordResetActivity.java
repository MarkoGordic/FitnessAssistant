package com.example.authentication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity {

    // used when email has been sent successfully
    private void successAnimation(TextView tv){
        // textView fading in and finishing the activity
        Animation fadeIn  = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(2000);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                finish();
            }
        });

        // textView fading out, setting new text and calling fadeIn animation
        Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(1000);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                tv.setText(R.string.link_has_been_sent);
                tv.startAnimation(fadeIn);
            }
        });

        tv.startAnimation(fadeOut);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // resetPassword listener
        findViewById(R.id.resetPasswordButton).setOnClickListener(view1 -> {
            EditText emailEdit = findViewById(R.id.emailEditForReset);
            if (TextUtils.isEmpty(emailEdit.getText().toString()))
                AuthFunctional.myError(getApplicationContext(),findViewById(R.id.emailEditForReset), getString(R.string.empty_email));
            else {
                AuthFunctional.startLoading(findViewById(R.id.resetPasswordButton), findViewById(R.id.resetPasswordBar));
                String email = emailEdit.getText().toString();
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AuthFunctional.finishLoading(findViewById(R.id.resetPasswordButton), findViewById(R.id.resetPasswordBar));
                        // set email to be filled out on sign in screen
                        AuthFunctional.emailLinked = email;
                        successAnimation((findViewById(R.id.smallResetPasswordTextView)));
                    } else {
                        AuthFunctional.finishLoading(findViewById(R.id.resetPasswordButton), findViewById(R.id.resetPasswordBar));
                        // set errors
                        AuthFunctional.setError(this, email, emailEdit, null);
                    }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_reset_screen);
        findViewById(R.id.backToSignInButton).setOnClickListener(view -> onBackPressed());
    }
}
