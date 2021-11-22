package com.example.fitnessassistant.authentication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.homepage.HomePageActivity;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.util.AuthFunctional;
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
import com.google.firebase.auth.SignInMethodQueryResult;

import org.json.JSONException;

import java.util.Arrays;
import java.util.List;

public class SignInActivity extends AppCompatActivity {
    private final int RC_GOOGLE_SIGN_IN = 100;
    private FirebaseAuth.AuthStateListener authListener;
    private NetworkManager networkManager;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager facebookCallbackManager;

    // sets up listeners for signing in, resetting password, creating an account(registering) and other sign in methods
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

        // googleSignInButton listener - gives google sign in pop-up
        Button googleButton =  findViewById(R.id.googleSignInButton);
        googleButton.setOnClickListener(view -> {
            AuthFunctional.startLoading(findViewById(R.id.googleSignInButton), findViewById(R.id.googleSignInProgressBar));
            googleSignInClient.signOut(); // signing out, just in case there is a previously saved user
            // TODO fix because it's deprecated
            startActivityForResult(googleSignInClient.getSignInIntent(), RC_GOOGLE_SIGN_IN); // starts the SignInIntent -> results in onActivityResult with called request code
        });

        Button customFBButton = findViewById(R.id.facebookSignInButton);
        customFBButton.setOnClickListener(view -> {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile")); // asking for the use of email and public profile on sign in
            LoginManager.getInstance().registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    // this is used to retrieve signed in user's data (email)
                    AuthFunctional.startLoading(view, findViewById(R.id.facebookSignInProgressBar));
                    GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), (jsonObject, graphResponse) -> {
                        if (jsonObject != null) {
                            try {
                                String email = jsonObject.getString("email");
                                // after getting the email, fetch to check if it is already connected with another type of authorization
                                FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        SignInMethodQueryResult result = task.getResult();
                                        if (result != null) {
                                            List<String> signInMethods = result.getSignInMethods();
                                            if (signInMethods != null) { // checking signInMethods
                                                if (signInMethods.isEmpty() || signInMethods.contains(FacebookAuthProvider.FACEBOOK_SIGN_IN_METHOD)) { // if it's a new account or it's an account authorized with facebook, simply sign in
                                                    AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                                                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnFailureListener(e -> {
                                                        AuthFunctional.finishLoading(view, findViewById(R.id.facebookSignInProgressBar));
                                                        LoginManager.getInstance().logOut();
                                                        try { // if we fail, throw the exception and sign out of fb
                                                            throw e;
                                                        } catch (FirebaseNetworkException e1) { // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                                                            AuthFunctional.quickFlash(getApplicationContext(), null, findViewById(R.id.notification_layout_id));
                                                        } catch (Exception e2) { // if it's any other we notify the user the sign in process was unsuccessful
                                                            findViewById(R.id.facebookSignInButton).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                                                            Toast.makeText(getApplicationContext(), getString(R.string.facebook_sign_in_unsuccessful), Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                                } else{
                                                    AuthFunctional.finishLoading(view, findViewById(R.id.facebookSignInProgressBar));
                                                    LoginManager.getInstance().logOut();
                                                    // set errors for other authorizations
                                                    if (signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD))
                                                        AuthFunctional.myError(getApplicationContext(), findViewById(R.id.edtTxtEmail), getString(R.string.facebook_error_password));
                                                    else if (signInMethods.contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD)) {
                                                        AuthFunctional.myError(getApplicationContext(), findViewById(R.id.edtTxtEmail), getString(R.string.facebook_error_google));
                                                        findViewById(R.id.googleSignInButton).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        AuthFunctional.finishLoading(view, findViewById(R.id.facebookSignInProgressBar));
                                        LoginManager.getInstance().logOut();
                                        try { // if we fail, throw the exception
                                            if (task.getException() != null)
                                                throw task.getException();
                                        } catch (FirebaseNetworkException e1) { // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                                            AuthFunctional.quickFlash(getApplicationContext(), null, findViewById(R.id.notification_layout_id));
                                        } catch (Exception e2) { // if it's any other we notify the user the sign in process was unsuccessful
                                            findViewById(R.id.facebookSignInButton).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                                            Toast.makeText(getApplicationContext(), getString(R.string.facebook_sign_in_unsuccessful), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } catch (JSONException e){ // notify about failing
                                AuthFunctional.finishLoading(view, findViewById(R.id.facebookSignInProgressBar));
                                LoginManager.getInstance().logOut();
                                findViewById(R.id.facebookSignInButton).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                                Toast.makeText(getApplicationContext(), getString(R.string.facebook_sign_in_unsuccessful), Toast.LENGTH_LONG).show();
                            }
                        } else{ // notify about failing
                            AuthFunctional.finishLoading(view, findViewById(R.id.facebookSignInProgressBar));
                            LoginManager.getInstance().logOut();
                            findViewById(R.id.facebookSignInButton).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                            Toast.makeText(getApplicationContext(), getString(R.string.facebook_sign_in_unsuccessful), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), R.string.facebook_sign_in_unsuccessful , Toast.LENGTH_LONG).show();
                    if(!AuthFunctional.currentlyOnline)
                        AuthFunctional.quickFlash(getApplicationContext(), null, findViewById(R.id.notification_layout_id));
                }
                @Override
                public void onError(@NonNull FacebookException e) {
                    Toast.makeText(getApplicationContext(), R.string.facebook_sign_in_unsuccessful , Toast.LENGTH_LONG).show();
                    if(!AuthFunctional.currentlyOnline)
                        AuthFunctional.quickFlash(getApplicationContext(), null, findViewById(R.id.notification_layout_id));
                }
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_GOOGLE_SIGN_IN){
            try { // we get the account's credential from the SignInIntent (account's ID token)
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                // and pass the credential to sign in with firebase
                FirebaseAuth.getInstance().signInWithCredential(credential).addOnFailureListener(e -> {
                    AuthFunctional.finishLoading(findViewById(R.id.googleSignInButton), findViewById(R.id.googleSignInProgressBar));
                    try{ // if we fail, throw the exception
                        throw e;
                    } catch(FirebaseNetworkException e1){ // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                        AuthFunctional.quickFlash(getApplicationContext(), null, findViewById(R.id.notification_layout_id));
                    } catch(Exception e2){ // if it's any other we notify the user the sign in process was unsuccessful
                        findViewById(R.id.googleSignInButton).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                        Toast.makeText(getApplicationContext(), getString(R.string.google_sign_in_unsuccessful), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (ApiException e){ // if there is an error, check if we're currently not online
                AuthFunctional.finishLoading(findViewById(R.id.googleSignInButton), findViewById(R.id.googleSignInProgressBar));
                if(!AuthFunctional.currentlyOnline) // if so, quick flash the notification
                    AuthFunctional.quickFlash(getApplicationContext(), null, findViewById(R.id.notification_layout_id));
                else{ // else quick flash the button and tell the user the sign in was unsuccessful by toasting
                    findViewById(R.id.googleSignInButton).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.quick_flash));
                    Toast.makeText(getApplicationContext(), getString(R.string.google_sign_in_unsuccessful), Toast.LENGTH_LONG).show();
                }
                e.printStackTrace();
            }
        } else if(FacebookSdk.isFacebookRequestCode(requestCode)) // pass the activity result back to the Facebook SDK
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
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

    private void updateUI() {
        // checking firebase user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) { // if user exists, emailVerification is checked and he is redirected to a new UI, otherwise he stays here
            if (user.isEmailVerified())
                goToHomePageUI();
            else {
                user.sendEmailVerification();
                startActivity(new Intent(this, EmailVerificationActivity.class));
                finish();
            }
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

        // setting up Google client
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.server_client_id)).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        // setting up the Facebook callback manager
        facebookCallbackManager = CallbackManager.Factory.create();
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