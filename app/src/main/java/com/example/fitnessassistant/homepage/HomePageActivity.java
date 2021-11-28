package com.example.fitnessassistant.homepage;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.authentication.CreateAccountActivity;
import com.example.fitnessassistant.authentication.SignInActivity;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.pedometer.Pedometer;
import com.example.fitnessassistant.util.ActivityResultFunctional;
import com.example.fitnessassistant.util.AuthFunctional;
import com.example.fitnessassistant.util.PermissionFunctional;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import org.json.JSONException;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

// TODO After adding in-app password change, update current user and make sure he doesn't get redirected to sign in
//  also for in-app email change

public class HomePageActivity extends AppCompatActivity {
    private final ActivityResultFunctional<Intent, ActivityResult> activityLauncher = ActivityResultFunctional.registerActivityForResult(this);
    private final int RC_PEDOMETER_PERMISSION = 101;
    private FirebaseAuth.AuthStateListener authListener;
    private GoogleSignInClient googleLinkingClient;
    private CallbackManager facebookCallbackManager;
    private NetworkManager networkManager;
    private Pedometer pedometer;

    // gives welcome message based on time
    private void greetUser(){
        TextView welcomeTextView = findViewById(R.id.welcomeMessageTextView); // TextView in top right corner for welcome message

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());

        int systemHours = calendar.get(Calendar.HOUR_OF_DAY);

        if (systemHours >= 6 && systemHours < 12)
            welcomeTextView.setText(getString(R.string.good_morning));
        else if(systemHours >= 12 && systemHours < 18)
            welcomeTextView.setText(getString(R.string.good_afternoon));
        else if(systemHours >= 18 && systemHours < 22)
            welcomeTextView.setText(getString(R.string.good_evening));
        else
            welcomeTextView.setText(getString(R.string.good_night));
    }

    private void dispUser(FirebaseUser currentUser){
        ((TextView) findViewById(R.id.userNameTextView)).setText(String.format("%s: %s", getString(R.string.user_name), currentUser.getDisplayName()));
        ((TextView) findViewById(R.id.userEmailTextView)).setText(String.format("%s: %s", getString(R.string.user_email), currentUser.getEmail()));
    }

    private void displayCurrentUser(){
        // setting up current user - this is in onResume in case anything gets changed after onPause()
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null) {
            // try to reload the user (update) to get the latest data
            currentUser.reload().addOnCompleteListener(task -> {
                if(task.isSuccessful()) // we're displaying the user's profile (reloaded)
                    dispUser(currentUser);
                else
                    try{
                        if(task.getException() != null)
                            throw task.getException();
                    } catch (FirebaseNetworkException e1){ // if it fails and it's a network error, the animated notification quickly flashes
                        AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.notification_layout_id));
                        // we're displaying the user's profile (last saved)
                        dispUser(currentUser);
                    } catch(Exception e2){ // if it fails and we're online(user deleted, disabled or credentials no longer valid) -> return to sign in
                        startActivity(new Intent(this, SignInActivity.class));
                        finish();
                    }
            });
        }
    }

    private void setUpGoogleUnlinkingSystem(Button googleLinkButton){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            googleLinkButton.setOnClickListener(view -> {
                AuthFunctional.startLoading(view, findViewById(R.id.googleLinkingProgressBar));
                user.unlink(GoogleAuthProvider.PROVIDER_ID).addOnCompleteListener(task -> {
                    AuthFunctional.finishLoading(view, findViewById(R.id.googleLinkingProgressBar));
                    if (task.isSuccessful())
                        setUpLinkingSystem();
                    else {
                        try { // if we fail, throw the exception
                            if (task.getException() != null)
                                throw task.getException();
                        } catch (FirebaseNetworkException e1) { // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                            AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.notification_layout_id));
                        } catch (Exception e2) { // if it's any other we notify the user the unlinking process was unsuccessful
                            view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                            Toast.makeText(getApplicationContext(), getString(R.string.google_unlinking_unsuccessful), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            });
        }
    }

    private void setUpGoogleLinkingSystem(Button googleLinkButton){
        googleLinkButton.setOnClickListener(view -> {
            AuthFunctional.startLoading(view, findViewById(R.id.googleLinkingProgressBar));
            googleLinkingClient.signOut();
            activityLauncher.launch(googleLinkingClient.getSignInIntent(), result -> {
                try {
                    GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData()).getResult(ApiException.class);
                    String email = account.getEmail();
                    if(email != null)
                        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                            if (task.isSuccessful() && task.getResult() != null && task.getResult().getSignInMethods() != null) {
                                List<String> signInMethods = task.getResult().getSignInMethods();
                                if (!signInMethods.contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD)) { // check if that google account is already connected
                                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                    if (currentUser != null) {
                                        currentUser.linkWithCredential(credential).addOnCompleteListener(task1 -> {
                                            AuthFunctional.finishLoading(view, findViewById(R.id.googleLinkingProgressBar));
                                            if (task1.isSuccessful())
                                                setUpLinkingSystem();
                                            else {
                                                try { // if we fail, throw the exception
                                                    if (task1.getException() != null)
                                                        throw task1.getException();
                                                } catch (FirebaseNetworkException e1) { // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                                                    AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.notification_layout_id));
                                                } catch (Exception e2) { // if it's any other we notify the user the linking process was unsuccessful
                                                    view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                                                    Toast.makeText(getApplicationContext(), getString(R.string.google_linking_unsuccessful), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    } else // in case somehow user got here, but there is no user available, we get him back to sign in
                                        updateUI();
                                } else {
                                    AuthFunctional.finishLoading(view, findViewById(R.id.googleLinkingProgressBar));
                                    view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                                    Toast.makeText(getApplicationContext(), getString(R.string.account_already_linked), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                AuthFunctional.finishLoading(view, findViewById(R.id.googleLinkingProgressBar));
                                try { // if we fail, throw the exception
                                    if (task.getException() != null)
                                        throw task.getException();
                                } catch (FirebaseNetworkException e1) { // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                                    AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.notification_layout_id));
                                } catch (Exception e2) { // if it's any other we notify the user the sign in process was unsuccessful
                                    view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                                    Toast.makeText(getApplicationContext(), getString(R.string.google_linking_unsuccessful), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                } catch (ApiException e){ // if there is an error, check if we're currently offline
                    AuthFunctional.finishLoading(view, findViewById(R.id.googleLinkingProgressBar));
                    if(!AuthFunctional.currentlyOnline) // if so, quick flash the notification
                        AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.notification_layout_id));
                    else{ // else quick flash the button and tell the user the linking was unsuccessful by toasting
                        view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                        Toast.makeText(getApplicationContext(), getString(R.string.google_linking_unsuccessful), Toast.LENGTH_LONG).show();
                    }
                }
            });
        });
    }

    private void setUpFacebookUnlinkingSystem(Button fbLinkButton){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            fbLinkButton.setOnClickListener(view -> {
                AuthFunctional.startLoading(view, findViewById(R.id.facebookLinkingProgressBar));
                user.unlink(FacebookAuthProvider.PROVIDER_ID).addOnCompleteListener(task -> {
                    AuthFunctional.finishLoading(view, findViewById(R.id.facebookLinkingProgressBar));
                    if (task.isSuccessful())
                        setUpLinkingSystem();
                    else {
                        try { // if we fail, throw the exception
                            if (task.getException() != null)
                                throw task.getException();
                        } catch (FirebaseNetworkException e1) { // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                            AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.notification_layout_id));
                        } catch (Exception e2) { // if it's any other we notify the user the unlinking process was unsuccessful
                            view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                            Toast.makeText(getApplicationContext(), getString(R.string.facebook_unlinking_unsuccessful), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            });
        } else
            updateUI();
    }

    private void setUpFacebookLinkingSystem(Button fbLinkButton){
        fbLinkButton.setOnClickListener(view -> {
            LoginManager.getInstance().logOut();
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile")); // asking for the use of email and public profile on sign in
            LoginManager.getInstance().registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    // this is used to retrieve signed in user's data (email)
                    AuthFunctional.startLoading(view, findViewById(R.id.facebookLinkingProgressBar));
                    GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), (jsonObject, graphResponse) -> {
                        if (jsonObject != null) {
                            try {
                                String email = jsonObject.getString("email");
                                // after getting the email, fetch to check if it is already connected with another type of provider
                                FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getSignInMethods() != null) {
                                        List<String> signInMethods = task.getResult().getSignInMethods();
                                        if (!signInMethods.contains(FacebookAuthProvider.FACEBOOK_SIGN_IN_METHOD)) { // this can be done only if that fb account is not already connected
                                            AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                            if(currentUser != null) {
                                                currentUser.linkWithCredential(credential).addOnCompleteListener(task1 -> {
                                                    AuthFunctional.finishLoading(view, findViewById(R.id.facebookLinkingProgressBar));
                                                    if (task1.isSuccessful())
                                                        setUpLinkingSystem();
                                                    else {
                                                        LoginManager.getInstance().logOut();
                                                        try { // if we fail, throw the exception
                                                            if (task1.getException() != null)
                                                                throw task1.getException();
                                                        } catch (FirebaseNetworkException e1) { // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                                                            AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.notification_layout_id));
                                                        } catch (Exception e2) { // if it's any other we notify the user the linking process was unsuccessful
                                                            view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                                                            Toast.makeText(getApplicationContext(), getString(R.string.facebook_linking_unsuccessful), Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            } else // in case somehow user got here, but there is no user available, we get him back to sign in
                                                updateUI();
                                        } else{ // else, some account is already linked, we notify the user
                                            AuthFunctional.finishLoading(view, findViewById(R.id.facebookLinkingProgressBar));
                                            LoginManager.getInstance().logOut();
                                            view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                                            Toast.makeText(getApplicationContext(), getString(R.string.account_already_linked), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        AuthFunctional.finishLoading(view, findViewById(R.id.facebookLinkingProgressBar));
                                        LoginManager.getInstance().logOut();
                                        try { // if we fail, throw the exception
                                            if (task.getException() != null)
                                                throw task.getException();
                                        } catch (FirebaseNetworkException e1) { // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                                            AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.notification_layout_id));
                                        } catch (Exception e2) { // if it's any other we notify the user the sign in process was unsuccessful
                                            view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                                            Toast.makeText(getApplicationContext(), getString(R.string.facebook_linking_unsuccessful), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } catch (JSONException e){ // notify about failing
                                AuthFunctional.finishLoading(view, findViewById(R.id.facebookLinkingProgressBar));
                                LoginManager.getInstance().logOut();
                                view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                                Toast.makeText(getApplicationContext(), getString(R.string.facebook_linking_unsuccessful), Toast.LENGTH_LONG).show();
                            }
                        } else{ // notify about failing
                            AuthFunctional.finishLoading(view, findViewById(R.id.facebookLinkingProgressBar));
                            LoginManager.getInstance().logOut();
                            view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                            Toast.makeText(getApplicationContext(), getString(R.string.facebook_linking_unsuccessful), Toast.LENGTH_LONG).show();
                        }
                    });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "email");
                    request.setParameters(parameters);
                    // creating a bundle of needed data
                    request.executeAsync();
                }
                @Override
                public void onCancel() {
                    Toast.makeText(getApplicationContext(), R.string.facebook_linking_unsuccessful , Toast.LENGTH_LONG).show();
                    if(!AuthFunctional.currentlyOnline)
                        AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.notification_layout_id));
                }
                @Override
                public void onError(@NonNull FacebookException e) {
                    Toast.makeText(getApplicationContext(), R.string.facebook_linking_unsuccessful , Toast.LENGTH_LONG).show();
                    if(!AuthFunctional.currentlyOnline)
                        AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.notification_layout_id));
                }
            });
        });
    }

    private void setUpLinkingSystem(){
        Button googleLinkButton = findViewById(R.id.googleLinkingButton);
        boolean signedInWithGoogle = false;

        Button fbLinkButton = findViewById(R.id.facebookLinkingButton);
        boolean signedInWithFacebook = false;

        Button ourLinkButton = findViewById(R.id.FAlinkingButton);
        boolean signedInWithPassword = false;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            for(UserInfo info : user.getProviderData()){
                if(info.getProviderId().contains(GoogleAuthProvider.PROVIDER_ID))
                    signedInWithGoogle = true;
                if(info.getProviderId().contains(FacebookAuthProvider.PROVIDER_ID))
                    signedInWithFacebook = true;
                if(info.getProviderId().contains(EmailAuthProvider.PROVIDER_ID)){
                    signedInWithPassword = true;
                }
            }
        } else // if somehow this happens we make sure it doesn't happen :)
            updateUI();

        // setting up onClickListener and text for Google Linking
        if(signedInWithGoogle){
            googleLinkButton.setText(R.string.unlink);
            if(!signedInWithFacebook && !signedInWithPassword)
                googleLinkButton.setOnClickListener(view -> {
                    view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                    Toast.makeText(getApplicationContext(), R.string.error_last_auth_provider, Toast.LENGTH_LONG).show();
                });
            else
                setUpGoogleUnlinkingSystem(googleLinkButton);
        } else{
            googleLinkButton.setText(R.string.link);
            setUpGoogleLinkingSystem(googleLinkButton);
        }


        // setting up onClickListener and text for Facebook Linking
        if(signedInWithFacebook){
            fbLinkButton.setText(R.string.unlink);
            if(!signedInWithGoogle && !signedInWithPassword)
                fbLinkButton.setOnClickListener(view -> {
                    view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                    Toast.makeText(getApplicationContext(), R.string.error_last_auth_provider, Toast.LENGTH_LONG).show();
                });
            else
                setUpFacebookUnlinkingSystem(fbLinkButton);
        } else {
            fbLinkButton.setText(R.string.link);
            setUpFacebookLinkingSystem(fbLinkButton);
        }

        if(!signedInWithPassword)
            ourLinkButton.setOnClickListener(view -> activityLauncher.launch(new Intent(this, CreateAccountActivity.class), result -> view.setVisibility(View.GONE)));
        else
            ourLinkButton.setVisibility(View.GONE);
    }

    // sets up listeners for signing out
    private void setUpOnClickListeners(){
        // signOutButton listener - click
        findViewById(R.id.signOutButton).setOnClickListener(view -> {
            if(AuthFunctional.currentlyOnline)
                Toast.makeText(getApplicationContext(),getString(R.string.hold_for_signing_out), Toast.LENGTH_LONG).show(); // it's better to sign out on hold, user can click this on accident
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.notification_layout_id));
        });

        // signOutButton listener - hold
        findViewById(R.id.signOutButton).setOnLongClickListener(view -> {
            if(AuthFunctional.currentlyOnline) {
                FirebaseAuth.getInstance().signOut();
                // signing out from facebook because they save it separately
                LoginManager.getInstance().logOut();
            }else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getApplicationContext(), findViewById(R.id.notification_layout_id));
            return true; // returns true -> onClick doesn't get triggered
        });

        // startPedometer listener - asks for permission for activity recognition (if permission is not already granted)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            findViewById(R.id.startPedometerButton).setVisibility(View.VISIBLE);
            findViewById(R.id.startPedometerButton).setOnClickListener(view -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    PermissionFunctional.askForPermission(this, getString(R.string.activity_recognition_permission), getString(R.string.activity_recognition_rationale), Manifest.permission.ACTIVITY_RECOGNITION, RC_PEDOMETER_PERMISSION);
                else // tell user he needs android (Q)10 for activity recognition
                    Toast.makeText(getApplicationContext(), getString(R.string.android_q_needed), Toast.LENGTH_LONG).show();
            });
        }
    }

    // if user is signed out, go to sign in
    private void updateUI() {
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(FacebookSdk.isFacebookRequestCode(requestCode)) // pass the activity result back to the Facebook SDK
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_PEDOMETER_PERMISSION) // requestCode is used to retrieve the results of asking for permission
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                findViewById(R.id.startPedometerButton).setVisibility(View.GONE); // hide the button if it's granted
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);
        greetUser();
        setUpOnClickListeners();

        networkManager = new NetworkManager(getApplication());
        pedometer = new Pedometer(this, findViewById(R.id.stepCountTextView));

        // setting up listener for firebase
        authListener = firebaseAuth -> updateUI();

        // setting up Google client
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.server_client_id)).requestEmail().build();
        googleLinkingClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // setting up the Facebook callback manager
        facebookCallbackManager = CallbackManager.Factory.create();

        setUpLinkingSystem();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // registering this activity when user comes first time or returns
        networkManager.registerConnectionObserver(this,findViewById(R.id.homeScreen));
        // adding the listener for firebase to change the UI
        FirebaseAuth.getInstance().addAuthStateListener(authListener);
        // re-registering the pedometer sensor
        pedometer.reRegisterSensor();

        displayCurrentUser();
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