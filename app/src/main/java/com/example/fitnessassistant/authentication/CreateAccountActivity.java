package com.example.fitnessassistant.authentication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.util.AuthFunctional;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class CreateAccountActivity extends AppCompatActivity {
    private NetworkManager networkManager;

    private void updateNewUser(FirebaseUser newUser, String name){
        if (newUser != null) // probably redundant
            newUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(name).build()).addOnCompleteListener(task1 -> {
                // update user's username
                if (!task1.isSuccessful()) // if unsuccessful, check errors
                    try {
                        AuthFunctional.finishLoading(findViewById(R.id.registerButton), findViewById(R.id.registerBar));
                        if (task1.getException() != null)
                            throw task1.getException();
                    } catch (FirebaseNetworkException e1) { // if it's a network error, the animated notification quickly flashes
                        AuthFunctional.quickFlash(this, findViewById(R.id.notification));
                    } catch (Exception e2) { // else notify user
                        Toast.makeText(getApplicationContext(), getString(R.string.account_created_username_update_unsuccessful), Toast.LENGTH_LONG).show();
                    }
                else {
                    if (AuthFunctional.currentlyOnline) {
                        newUser.reload().addOnFailureListener(e -> Toast.makeText(getApplicationContext(), getString(R.string.register_unsuccessful), Toast.LENGTH_LONG).show());
                    }
                    AuthFunctional.finishLoading(findViewById(R.id.registerButton), findViewById(R.id.registerBar));
                    finish(); // finish after (not) calling reload -> get back to sign in, trigger the listener that will get the user to the home page
                }
            });
        else // finishing loading here too, just in case (probably redundant)
            AuthFunctional.finishLoading(findViewById(R.id.registerButton), findViewById(R.id.registerBar));
    }

    // sets up listeners for back button and register button
    @SuppressLint("ClickableViewAccessibility")
    private void setUpOnClickListeners(){
        // backButton listener - goes back to SignInActivity
        findViewById(R.id.backToSignInButton).setOnClickListener(view -> onBackPressed());

        // registerButton Listener - validates input and checkbox checked and if online creates a new user
        findViewById(R.id.registerButton).setOnClickListener(view -> {
            EditText nameEdit = findViewById(R.id.usernameEditTextForRegister);
            EditText emailEdit = findViewById(R.id.emailEditTextForRegister);
            EditText passEdit = findViewById(R.id.passwordEditTextForRegister);
            EditText cPassEdit = findViewById(R.id.confirmPasswordEditTextForRegister);
            String name = nameEdit.getText().toString();
            String email = emailEdit.getText().toString();
            String password = passEdit.getText().toString();
            String cPassword = cPassEdit.getText().toString();
            if(AuthFunctional.validUsername(getApplicationContext(), nameEdit) && AuthFunctional.validEmail(getApplicationContext(), emailEdit) && AuthFunctional.validPassword(getApplicationContext(), passEdit)){
                if(!password.equals(cPassword)) // check if password and confirmPassword are equal
                    AuthFunctional.bothPasswordsError(getApplicationContext(), passEdit, cPassEdit, getString(R.string.passwords_not_equal));
                else if(!((CheckBox) findViewById(R.id.privacyPolicyCheckbox)).isChecked())
                    AuthFunctional.checkboxFlash(getApplicationContext(), findViewById(R.id.privacyPolicyCheckbox));
                else{ // if everything is set, create the user
                    AuthFunctional.startLoading(view, findViewById(R.id.registerBar));
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if(currentUser == null) { // if currentUser is null, we're creating a new account
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                            if(task.isSuccessful())// user is created
                                updateNewUser(FirebaseAuth.getInstance().getCurrentUser(), name);
                            else{
                                AuthFunctional.finishLoading(view, findViewById(R.id.registerBar));
                                try {
                                    if (task.getException() != null)
                                        throw task.getException();
                                } catch (FirebaseNetworkException e1) { // if it's a network error, the animated notification quickly flashes
                                    AuthFunctional.quickFlash(this, findViewById(R.id.notification));
                                } catch (Exception e2) { // else errors are checked
                                    AuthFunctional.emailAlreadyRegistered(getApplicationContext(), emailEdit, email);
                                    Toast.makeText(getApplicationContext(), getString(R.string.register_unsuccessful), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else { // if currentUser is not null, we're linking the created account (using created credential) with the current user
                        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                        currentUser.linkWithCredential(credential).addOnCompleteListener(task -> {
                            AuthFunctional.finishLoading(view, findViewById(R.id.registerBar));
                            if (task.isSuccessful())
                                finish();
                            else
                                try { // if we fail, throw the exception
                                    if (task.getException() != null)
                                        throw task.getException();
                                } catch (FirebaseNetworkException e1) { // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                                    AuthFunctional.quickFlash(this, findViewById(R.id.notification));
                                } catch (Exception e2) { // if it's any other we notify the user the linking process was unsuccessful
                                    view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                                    Toast.makeText(getApplicationContext(), getString(R.string.email_linking_unsuccessful), Toast.LENGTH_LONG).show();
                                }
                        });
                    }
                }
            }
        });

        // privacyPolicyTextView listener - redirects to our Privacy Policy(with Uri)
        findViewById(R.id.privacyPolicyTextView).setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP) // ACTION_DOWN = finger on screen, ACTION_UP = finger on -> off screen
                    if (event.getRawX() >= (v.getRight() - v.getWidth() / 3) && event.getRawX() <= (v.getRight() - v.getWidth() / 15)) { // getRawX() is where touch is registered, anything on x axis in the last quarter
                        startActivity(new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.privacy_policy_link))));
                        return true;
                    }
                return false;
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);
        AuthFunctional.setUpPassword(findViewById(R.id.passwordEditTextForRegister));
        AuthFunctional.setUpPassword(findViewById(R.id.confirmPasswordEditTextForRegister));
        setUpOnClickListeners();

        networkManager = new NetworkManager(getApplication());

        // setting up the flashing animation for the no network notification
        Animation flash = new AlphaAnimation(0.0f, 1.0f);
        flash.setDuration(800); // flash duration
        flash.setStartOffset(1600); // staying visible duration
        flash.setRepeatMode(Animation.REVERSE);
        flash.setRepeatCount(Animation.INFINITE);
        findViewById(R.id.notification).startAnimation(flash);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // registering this activity when user comes first time or returns
        networkManager.registerConnectionObserver(this,findViewById(R.id.no_network_text_view));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregistering this activity when another activity comes into the foreground or else
        networkManager.unregisterConnectionObserver(this);
    }
}