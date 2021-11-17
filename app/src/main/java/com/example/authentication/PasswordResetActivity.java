package com.example.authentication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.network.NetworkManager;
import com.example.util.authentication.AuthFunctional;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity {
    private NetworkManager networkManager;

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

    // sets up listeners for back button and resetPassword button
    private void setUpOnClickListeners(){
        // backButton listener - goes back to SignInActivity
        findViewById(R.id.backToSignInButton).setOnClickListener(view -> onBackPressed());

        // resetPassword listener - checking for errors(email empty/not registered) -> sending reset password email
        findViewById(R.id.resetPasswordButton).setOnClickListener(view -> {
            EditText emailEdit = findViewById(R.id.emailEditForPassReset);
            if (TextUtils.isEmpty(emailEdit.getText().toString()))
                AuthFunctional.myError(getApplicationContext(), emailEdit, getString(R.string.empty_email));
            else {
                String email = emailEdit.getText().toString();
                AuthFunctional.startLoading(view, findViewById(R.id.resetPasswordBar));
                // try sending the mail
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                    AuthFunctional.finishLoading(view, findViewById(R.id.resetPasswordBar));
                    if (task.isSuccessful())
                        successAnimation((findViewById(R.id.smallResetPasswordTextView)));
                    else // if it fails set errors
                        try{
                            if(task.getException() != null)
                                throw task.getException();
                        } catch (FirebaseNetworkException e1){ // if there is no internet, the animated notification quick flashes
                            AuthFunctional.quickFlash(getApplicationContext(), ((Button) view), findViewById(R.id.notification_layout_id));
                        } catch (Exception e2){ // if there is internet, set authentication errors
                            AuthFunctional.setAuthenticationError(getApplicationContext(), email, emailEdit, null, null, null);
                        }
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_reset_screen);
        setUpOnClickListeners();

        networkManager = new NetworkManager(getApplication());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // registering this activity when user comes first time or returns
        networkManager.registerConnectionObserver(this,findViewById(R.id.passwordResetScreen));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregistering this activity when another activity comes into the foreground or else
        networkManager.unregisterConnectionObserver(this);
    }
}