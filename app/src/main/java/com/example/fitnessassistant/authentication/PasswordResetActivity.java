package com.example.fitnessassistant.authentication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.util.AuthFunctional;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;

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
                // send the mail (if email is connected with an email/password sign in method) or set the errors
                FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        SignInMethodQueryResult result = task.getResult();
                        if(result != null) {
                            List<String> signInMethods = result.getSignInMethods();
                            if (signInMethods != null) { // checking sign in methods available for entered email
                                if (signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) // sending password reset email only if there is this method available
                                    FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(task1 -> {
                                        AuthFunctional.finishLoading(view, findViewById(R.id.resetPasswordBar));
                                        if (task1.isSuccessful())
                                            successAnimation((findViewById(R.id.smallResetPasswordTextView)));
                                        else
                                            Toast.makeText(getApplicationContext(), R.string.password_reset_unsuccessful, Toast.LENGTH_LONG).show();
                                    }); // TODO add in Twitter and Apple
                                else if(signInMethods.contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD))
                                    AuthFunctional.myError(getApplicationContext(), emailEdit, getString(R.string.email_connected_via_google));
                                else if(signInMethods.contains(FacebookAuthProvider.FACEBOOK_SIGN_IN_METHOD))
                                    AuthFunctional.myError(getApplicationContext(), emailEdit, getString(R.string.email_connected_via_facebook));
                                AuthFunctional.finishLoading(view, findViewById(R.id.resetPasswordBar));
                            }
                        }
                    } else{
                        AuthFunctional.finishLoading(view, findViewById(R.id.resetPasswordBar));
                        try{
                            if(task.getException() != null){
                                throw task.getException();
                            }
                        } catch (FirebaseNetworkException e1){ // in case it's a network error - notification quickly flashes
                            AuthFunctional.quickFlash(getApplicationContext(), ((Button) view), findViewById(R.id.notification_layout_id));
                        } catch (Exception e2){ // in other cases, it's an invalid email input
                            AuthFunctional.myError(getApplicationContext(), emailEdit, getString(R.string.invalid_email));
                        }
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