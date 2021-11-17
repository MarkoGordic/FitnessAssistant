package com.example.fitnessassistant.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.homepage.HomePageActivity;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.util.AuthFunctional;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {
    private FirebaseAuth.AuthStateListener authListener;
    private NetworkManager networkManager;
    private GoogleSignInClient googleSignInClient;

    // sets up listeners for signing in, resetting password, creating an account(registering)
    private void setUpOnClickListeners(){
        // signUpButton listener - checks basic errors, checks sign in errors
        findViewById(R.id.signInButton).setOnClickListener(v -> {
            EditText emailEdit = findViewById(R.id.edtTxtEmail);
            EditText passEdit = findViewById(R.id.edtTxtPassword);
            if (TextUtils.isEmpty(emailEdit.getText().toString()))
                AuthFunctional.myError(getApplicationContext(), emailEdit, getString(R.string.empty_email));
            else if (TextUtils.isEmpty(passEdit.getText().toString()))
                AuthFunctional.myError(getApplicationContext(), passEdit, getString(R.string.empty_password));
            else if (passEdit.getText().length() <= 5)
                AuthFunctional.myError(getApplicationContext(), passEdit, getString(R.string.password_not_enough_characters));
            else { // in case everything is fine, try to signIn
                AuthFunctional.startLoading(v, findViewById(R.id.signInProgressBar));
                FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEdit.getText().toString(), passEdit.getText().toString()).addOnFailureListener(e -> {
                    AuthFunctional.finishLoading(v, findViewById(R.id.signInProgressBar));
                    try{ // if we fail, throw the exception
                        throw e;
                    } catch(FirebaseNetworkException e1){ // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                        AuthFunctional.quickFlash(this, ((Button) v), findViewById(R.id.notification_layout_id));
                    } catch(Exception e2){ // if it's any other we set the authentication error
                        AuthFunctional.setAuthenticationError(getApplicationContext(), emailEdit.getText().toString(), emailEdit, passEdit, findViewById(R.id.forgotPasswordTextView), findViewById(R.id.createAccountTextView));
                    }
                });
            }
        });

        // forgotPassword listener - going to the PasswordResetActivity
        findViewById(R.id.forgotPasswordTextView).setOnClickListener(view -> startActivity(new Intent(this, PasswordResetActivity.class)));

        // createAccount listener - going to the CreateAccountActivity
        findViewById(R.id.createAccountTextView).setOnClickListener(view -> startActivity(new Intent(this, CreateAccountActivity.class)));

        findViewById(R.id.googleSignInButton).setOnClickListener(view -> startActivityForResult(googleSignInClient.getSignInIntent(), 1));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Log.d("GSI", "handleSignInResult:" + task.isSuccessful());
            if (task.isSuccessful()) {
                // Signed in successfully, show authenticated UI.
                GoogleSignInAccount acct = task.getResult();

                Log.e("GSI", "display name: " + acct.getDisplayName());

                String personName = acct.getDisplayName();
                String personPhotoUrl = "";
                if(acct.getPhotoUrl() != null)
                    personPhotoUrl = acct.getPhotoUrl().toString();
                String email = acct.getEmail();

                Log.e("GSI", "Name: " + personName + ", email: " + email
                        + ", Image: " + personPhotoUrl);
            }
            updateUI();
        }
    }

    private void goToHomePageUI(){
        setContentView(R.layout.loading_screen);
        Animation loadAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.authentication_loading);
        loadAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationRepeat(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                // after loading goes to HomePageActivity
                startActivity(new Intent(getApplicationContext(), HomePageActivity.class));
                finish();
            }
        });
        findViewById(R.id.appLogo).startAnimation(loadAnim);
    }

    // if user exists, emailVerification is checked and he is redirected to a new UI, otherwise he stays here
    private void updateUI() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (user.isEmailVerified())
                goToHomePageUI();
            else {
                user.sendEmailVerification();
                startActivity(new Intent(this, EmailVerificationActivity.class));
                finish();
            }
        }
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null){
            goToHomePageUI();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_screen);
        AuthFunctional.setUpPassword(findViewById(R.id.edtTxtPassword));
        setUpOnClickListeners();

        networkManager = new NetworkManager(getApplication());

        // setting up listener for firebase
        authListener = firebaseAuth -> updateUI();

        // setting up client for google
        googleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // registering this activity when user comes first time or returns
        networkManager.registerConnectionObserver(this,findViewById(R.id.signInScreen));
        // adding the listener for firebase to change the UI
        FirebaseAuth.getInstance().addAuthStateListener(authListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregistering this activity when another activity comes into the foreground or else
        networkManager.unregisterConnectionObserver(this);
        // removing the listener when activity pauses
        if(authListener != null)
            FirebaseAuth.getInstance().removeAuthStateListener(authListener);
    }
}