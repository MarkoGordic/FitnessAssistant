package com.example.fitnessassistant.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.authentication.CreateAccountActivity;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.util.ActivityResultFunctional;
import com.example.fitnessassistant.util.AuthFunctional;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
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
import java.util.List;

public class SettingsFragment extends Fragment {
    private final ActivityResultFunctional<Intent, ActivityResult> activityLauncher = ActivityResultFunctional.registerActivityForResult(this);
    private NetworkManager networkManager;
    private FirebaseAuth.AuthStateListener authListener;
    private GoogleSignInClient googleLinkingClient;
    private CallbackManager facebookCallbackManager;

    // setting up onClickListener for unlinking Google
    private void setUpGoogleUnlinkingSystem(Button googleLinkButton){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            googleLinkButton.setOnClickListener(view -> {
                if(AuthFunctional.currentlyOnline) { // if we're online silently sign in to google(to re-authenticate) and then unlink
                    AuthFunctional.startLoading(view, requireView().findViewById(R.id.googleLinkingProgressBar));
                    googleLinkingClient.silentSignIn().addOnCompleteListener(task1 -> user.unlink(GoogleAuthProvider.PROVIDER_ID).addOnCompleteListener(task -> {
                        AuthFunctional.finishLoading(view, requireView().findViewById(R.id.googleLinkingProgressBar));
                        if (task.isSuccessful())
                            setUpLinkingSystem(requireView());
                        else {
                            try { // if we fail, throw the exception
                                if (task.getException() != null)
                                    throw task.getException();
                            } catch (FirebaseNetworkException e1) { // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                                AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                            } catch (Exception e2) { // if it's any other we notify the user the unlinking process was unsuccessful
                                view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.quick_flash));
                                Toast.makeText(getContext(), getString(R.string.google_unlinking_unsuccessful), Toast.LENGTH_LONG).show();
                            }
                        }
                    }));
                } else
                    AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
            });
        }
    }

    // setting up onClickListener for linking Google
    private void setUpGoogleLinkingSystem(Button googleLinkButton, boolean hasOurAccount){
        googleLinkButton.setOnClickListener(view -> {
            AuthFunctional.startLoading(view, requireView().findViewById(R.id.googleLinkingProgressBar));
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
                                    if (currentUser != null && currentUser.getEmail() != null) {
                                        if(!hasOurAccount){ // if user doesn't have our account, re-authenticate using facebook
                                            if(AccessToken.getCurrentAccessToken() != null)
                                                currentUser.reauthenticate(FacebookAuthProvider.getCredential(AccessToken.getCurrentAccessToken().getToken())).addOnCompleteListener(task1 -> {
                                                    if (!task1.isSuccessful()) {
                                                        AuthFunctional.finishLoading(view, requireView().findViewById(R.id.googleLinkingProgressBar));
                                                        try { // throw the exception to check errors
                                                            if (task1.getException() != null)
                                                                throw task1.getException();
                                                        } catch (FirebaseNetworkException e1) { // if it's a network error, the animated notification quickly flashes
                                                            AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                                                        } catch (Exception e2) { // else notify user
                                                            Toast.makeText(getContext(), getString(R.string.re_authentication_unsuccessful), Toast.LENGTH_LONG).show();
                                                        }
                                                    } else
                                                        currentUser.linkWithCredential(credential).addOnCompleteListener(task2 -> {
                                                            AuthFunctional.finishLoading(view, requireView().findViewById(R.id.googleLinkingProgressBar));
                                                            if (task2.isSuccessful())
                                                                setUpLinkingSystem(requireView());
                                                            else {
                                                                try { // if we fail, throw the exception
                                                                    if (task2.getException() != null)
                                                                        throw task2.getException();
                                                                } catch (FirebaseNetworkException e1) { // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                                                                    AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                                                                } catch (Exception e2) { // if it's any other we notify the user the linking process was unsuccessful
                                                                    view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.quick_flash));
                                                                    Toast.makeText(getContext(), getString(R.string.google_linking_unsuccessful), Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                                });
                                        } else { // if user has our account, ask him to re-authenticate with his password
                                            EditText passwordInput = new EditText(getActivity());
                                            passwordInput.setHint(R.string.re_enter_your_password);
                                            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                            passwordInput.setTransformationMethod(new AuthFunctional.MyPasswordTransformationMethod());

                                            FrameLayout layout = new FrameLayout(getActivity());
                                            layout.setPaddingRelative(45, 0, 45, 0);
                                            layout.addView(passwordInput);

                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setTitle(R.string.re_authenticate_your_account)
                                                    .setMessage(R.string.re_authentication_message)
                                                    .setView(layout)
                                                    .setNegativeButton(R.string.cancel, (dialog, i) -> dialog.dismiss())
                                                    .setPositiveButton(R.string.link, (dialog, i) -> {
                                                        if (!TextUtils.isEmpty(passwordInput.getText().toString())){
                                                            dialog.dismiss(); // we create credential based on user's email and given password
                                                            if(AuthFunctional.currentlyOnline)
                                                                currentUser.reauthenticate(EmailAuthProvider.getCredential(currentUser.getEmail(), passwordInput.getText().toString())).addOnCompleteListener(task12 -> {
                                                                    if (!task12.isSuccessful()) {
                                                                        try { // throw the exception to check errors
                                                                            if (task12.getException() != null)
                                                                                throw task12.getException();
                                                                        } catch (FirebaseNetworkException e1) { // if it's a network error, the animated notification quickly flashes
                                                                            AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                                                                        } catch (Exception e2) { // else notify user
                                                                            Toast.makeText(getContext(), getString(R.string.re_authentication_unsuccessful), Toast.LENGTH_LONG).show();
                                                                        }
                                                                    } else
                                                                        currentUser.linkWithCredential(credential).addOnCompleteListener(task2 -> {
                                                                            if (task2.isSuccessful())
                                                                                setUpLinkingSystem(requireView());
                                                                            else {
                                                                                try { // if we fail, throw the exception
                                                                                    if (task2.getException() != null)
                                                                                        throw task2.getException();
                                                                                } catch (FirebaseNetworkException e1) { // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                                                                                    AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                                                                                } catch (Exception e2) { // if it's any other we notify the user the linking process was unsuccessful
                                                                                    view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.quick_flash));
                                                                                    Toast.makeText(getContext(), getString(R.string.google_linking_unsuccessful), Toast.LENGTH_LONG).show();
                                                                                }
                                                                            }
                                                                        });
                                                                });
                                                            else  // if we're offline, the animated notification quickly flashes
                                                                AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                                                        }
                                                    })
                                                    .setOnDismissListener(dialogInterface -> AuthFunctional.finishLoading(view, requireView().findViewById(R.id.googleLinkingProgressBar)));
                                            builder.create().show();
                                        }
                                    } else // in case somehow user got here, but there is no user available, we get him back to sign in
                                        AuthFunctional.refreshUser(getActivity());
                                } else {
                                    AuthFunctional.finishLoading(view, requireView().findViewById(R.id.googleLinkingProgressBar));
                                    view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.quick_flash));
                                    Toast.makeText(getContext(), getString(R.string.account_already_linked), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                AuthFunctional.finishLoading(view, requireView().findViewById(R.id.googleLinkingProgressBar));
                                try { // if we fail, throw the exception
                                    if (task.getException() != null)
                                        throw task.getException();
                                } catch (FirebaseNetworkException e1) { // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                                    AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                                } catch (Exception e2) { // if it's any other we notify the user the sign in process was unsuccessful
                                    view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.quick_flash));
                                    Toast.makeText(getContext(), getString(R.string.google_linking_unsuccessful), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                } catch (ApiException e){ // if there is an error, check if we're currently offline
                    AuthFunctional.finishLoading(view, requireView().findViewById(R.id.googleLinkingProgressBar));
                    if(!AuthFunctional.currentlyOnline) // if so, quick flash the notification
                        AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                    else{ // else quick flash the button and tell the user the linking was unsuccessful by toasting
                        view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.quick_flash));
                        Toast.makeText(getContext(), getString(R.string.google_linking_unsuccessful), Toast.LENGTH_LONG).show();
                    }
                }
            });
        });
    }

    // setting up onClickListener for unlinking Facebook
    private void setUpFacebookUnlinkingSystem(Button fbLinkButton){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null && AccessToken.getCurrentAccessToken() != null) {
            fbLinkButton.setOnClickListener(view -> {
                AuthFunctional.startLoading(view, requireView().findViewById(R.id.facebookLinkingProgressBar));
                // re-authenticate using facebook
                user.reauthenticate(FacebookAuthProvider.getCredential(AccessToken.getCurrentAccessToken().getToken())).addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        AuthFunctional.finishLoading(view, requireView().findViewById(R.id.facebookLinkingProgressBar));
                        try { // throw the exception to check errors
                            if (task.getException() != null)
                                throw task.getException();
                        } catch (FirebaseNetworkException e1) { // if it's a network error, the animated notification quickly flashes
                            AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                        } catch (Exception e2) { // else notify user
                            Toast.makeText(getContext(), getString(R.string.re_authentication_unsuccessful), Toast.LENGTH_LONG).show();
                        }
                    } else
                        user.unlink(FacebookAuthProvider.PROVIDER_ID).addOnCompleteListener(task1 -> {
                            AuthFunctional.finishLoading(view, requireView().findViewById(R.id.facebookLinkingProgressBar));
                            if (task1.isSuccessful())
                                setUpLinkingSystem(requireView());
                            else {
                                try { // if we fail, throw the exception
                                    if (task1.getException() != null)
                                        throw task1.getException();
                                } catch (FirebaseNetworkException e1) { // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                                    AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                                } catch (Exception e2) { // if it's any other we notify the user the unlinking process was unsuccessful
                                    view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.quick_flash));
                                    Toast.makeText(getContext(), getString(R.string.facebook_unlinking_unsuccessful), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                });
            });
        } else
            AuthFunctional.refreshUser(getActivity());
    }

    // setting up onClickListener for linking Facebook
    private void setUpFacebookLinkingSystem(Button fbLinkButton, boolean hasOurAccount){
        fbLinkButton.setOnClickListener(view -> {
            LoginManager.getInstance().logOut();
            LoginManager.getInstance().registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    // this is used to retrieve signed in user's data (email)
                    AuthFunctional.startLoading(view, requireView().findViewById(R.id.facebookLinkingProgressBar));
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
                                            if(currentUser != null && currentUser.getEmail() != null) {
                                                if(!hasOurAccount){ // if user doesn't have our account, re-authenticate using google
                                                    googleLinkingClient.silentSignIn().addOnCompleteListener(task12 -> {
                                                        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
                                                        if(account != null) {
                                                            currentUser.reauthenticate(GoogleAuthProvider.getCredential(account.getIdToken(), null)).addOnCompleteListener(task1 -> {
                                                                if (!task1.isSuccessful()) {
                                                                    AuthFunctional.finishLoading(view, requireView().findViewById(R.id.facebookLinkingProgressBar));
                                                                    try { // throw the exception to check errors
                                                                        if (task1.getException() != null)
                                                                            throw task1.getException();
                                                                    } catch (FirebaseNetworkException e1) { // if it's a network error, the animated notification quickly flashes
                                                                        AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                                                                    } catch (Exception e2) { // else notify user
                                                                        Toast.makeText(getContext(), getString(R.string.re_authentication_unsuccessful), Toast.LENGTH_LONG).show();
                                                                    }
                                                                } else
                                                                    currentUser.linkWithCredential(credential).addOnCompleteListener(task2 -> {
                                                                        AuthFunctional.finishLoading(view, requireView().findViewById(R.id.facebookLinkingProgressBar));
                                                                        if (task2.isSuccessful())
                                                                            setUpLinkingSystem(requireView());
                                                                        else {
                                                                            LoginManager.getInstance().logOut();
                                                                            try { // if we fail, throw the exception
                                                                                if (task2.getException() != null)
                                                                                    throw task2.getException();
                                                                            } catch (FirebaseNetworkException e1) { // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                                                                                AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                                                                            } catch (Exception e2) { // if it's any other we notify the user the linking process was unsuccessful
                                                                                view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.quick_flash));
                                                                                Toast.makeText(getContext(), getString(R.string.facebook_linking_unsuccessful), Toast.LENGTH_LONG).show();
                                                                            }
                                                                        }
                                                                    });
                                                            });
                                                        }
                                                    });
                                                } else { // if user has our account, re-authenticate the user with the given password
                                                    EditText passwordInput = new EditText(getActivity());
                                                    passwordInput.setHint(R.string.re_enter_your_password);
                                                    passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                                    passwordInput.setTransformationMethod(new AuthFunctional.MyPasswordTransformationMethod());

                                                    FrameLayout layout = new FrameLayout(getActivity());
                                                    layout.setPaddingRelative(45, 0, 45, 0);
                                                    layout.addView(passwordInput);

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    builder.setTitle(R.string.re_authenticate_your_account)
                                                            .setMessage(R.string.re_authentication_message)
                                                            .setView(layout)
                                                            .setNegativeButton(R.string.cancel, (dialog, i) -> dialog.dismiss())
                                                            .setPositiveButton(R.string.link, (dialog, i) -> {
                                                                if (TextUtils.isEmpty(passwordInput.getText().toString()))
                                                                    passwordInput.setError(getString(R.string.empty_password));
                                                                else {
                                                                    dialog.dismiss(); // we create credential based on user's email and given password
                                                                    if(AuthFunctional.currentlyOnline)
                                                                        currentUser.reauthenticate(EmailAuthProvider.getCredential(currentUser.getEmail(), passwordInput.getText().toString())).addOnCompleteListener(task12 -> {
                                                                            if (!task12.isSuccessful())
                                                                                try { // throw the exception to check errors
                                                                                    if (task12.getException() != null)
                                                                                        throw task12.getException();
                                                                                } catch (FirebaseNetworkException e1) { // if it's a network error, the animated notification quickly flashes
                                                                                    AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                                                                                } catch (Exception e2) { // else notify user
                                                                                    Toast.makeText(getContext(), getString(R.string.re_authentication_unsuccessful), Toast.LENGTH_LONG).show();
                                                                                }
                                                                            else
                                                                                currentUser.linkWithCredential(credential).addOnCompleteListener(task2 -> {
                                                                                    if (task2.isSuccessful())
                                                                                        setUpLinkingSystem(requireView());
                                                                                    else {
                                                                                        LoginManager.getInstance().logOut();
                                                                                        try { // if we fail, throw the exception
                                                                                            if (task2.getException() != null)
                                                                                                throw task2.getException();
                                                                                        } catch (FirebaseNetworkException e1) { // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                                                                                            AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                                                                                        } catch (Exception e2) { // if it's any other we notify the user the linking process was unsuccessful
                                                                                            view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.quick_flash));
                                                                                            Toast.makeText(getContext(), getString(R.string.facebook_linking_unsuccessful), Toast.LENGTH_LONG).show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                        });
                                                                    else // if we're offline, the animated notification quickly flashes
                                                                        AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                                                                }
                                                            })
                                                            .setOnDismissListener(dialogInterface -> AuthFunctional.finishLoading(view, requireView().findViewById(R.id.facebookLinkingProgressBar)));
                                                    builder.create().show();
                                                }
                                            } else // in case somehow user got here, but there is no user available, we get him back to sign in
                                                AuthFunctional.refreshUser(SettingsFragment.this.getActivity());
                                        } else{ // else, some account is already linked, we notify the user
                                            AuthFunctional.finishLoading(view, requireView().findViewById(R.id.facebookLinkingProgressBar));
                                            LoginManager.getInstance().logOut();
                                            view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.quick_flash));
                                            Toast.makeText(getContext(), getString(R.string.account_already_linked), Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        AuthFunctional.finishLoading(view, requireView().findViewById(R.id.facebookLinkingProgressBar));
                                        LoginManager.getInstance().logOut();
                                        try { // if we fail, throw the exception
                                            if (task.getException() != null)
                                                throw task.getException();
                                        } catch (FirebaseNetworkException e1) { // if it's this one, it's network problems, so we quick flash the notification of no connectivity
                                            AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                                        } catch (Exception e2) { // if it's any other we notify the user the sign in process was unsuccessful
                                            view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.quick_flash));
                                            Toast.makeText(getContext(), getString(R.string.facebook_linking_unsuccessful), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            } catch (JSONException e){ // notify about failing
                                AuthFunctional.finishLoading(view, requireView().findViewById(R.id.facebookLinkingProgressBar));
                                LoginManager.getInstance().logOut();
                                view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.quick_flash));
                                Toast.makeText(getContext(), getString(R.string.facebook_linking_unsuccessful), Toast.LENGTH_LONG).show();
                            }
                        } else{ // notify about failing
                            AuthFunctional.finishLoading(view, requireView().findViewById(R.id.facebookLinkingProgressBar));
                            LoginManager.getInstance().logOut();
                            view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.quick_flash));
                            Toast.makeText(getContext(), getString(R.string.facebook_linking_unsuccessful), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getContext(), R.string.facebook_linking_unsuccessful , Toast.LENGTH_LONG).show();
                    if(!AuthFunctional.currentlyOnline)
                        AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                }
                @Override
                public void onError(@NonNull FacebookException e) {
                    Toast.makeText(getContext(), R.string.facebook_linking_unsuccessful , Toast.LENGTH_LONG).show();
                    if(!AuthFunctional.currentlyOnline)
                        AuthFunctional.quickFlash(getContext(), requireView().findViewById(R.id.notification_layout_id));
                }
            });
            LoginManager.getInstance().logInWithReadPermissions(requireActivity(), facebookCallbackManager , Arrays.asList("email", "public_profile")); // asking for the use of email and public profile on sign in
        });
    }

    // setting up oCListeners for all auth providers
    private void setUpLinkingSystem(View view){
        Button googleLinkButton = view.findViewById(R.id.googleLinkingButton);
        boolean signedInWithGoogle = false;

        Button fbLinkButton = view.findViewById(R.id.facebookLinkingButton);
        boolean signedInWithFacebook = false;

        Button ourLinkButton = view.findViewById(R.id.FAlinkingButton);
        boolean hasOurAccount  = false;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            for(UserInfo info : user.getProviderData()){
                if(info.getProviderId().contains(GoogleAuthProvider.PROVIDER_ID))
                    signedInWithGoogle = true;
                if(info.getProviderId().contains(FacebookAuthProvider.PROVIDER_ID))
                    signedInWithFacebook = true;
                if(info.getProviderId().contains(EmailAuthProvider.PROVIDER_ID)){
                    hasOurAccount = true;
                }
            }
        } else // if somehow this happens we make sure it doesn't happen :)
            AuthFunctional.refreshUser(getActivity());

        // setting up onClickListener and text for Google Linking
        if(signedInWithGoogle){
            googleLinkButton.setText(R.string.unlink);
            if(!signedInWithFacebook && !hasOurAccount)
                googleLinkButton.setOnClickListener(view1 -> {
                    view1.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.quick_flash));
                    Toast.makeText(getContext(), R.string.error_last_auth_provider, Toast.LENGTH_LONG).show();
                });
            else
                setUpGoogleUnlinkingSystem(googleLinkButton);
        } else{
            googleLinkButton.setText(R.string.link);
            setUpGoogleLinkingSystem(googleLinkButton, hasOurAccount);
        }

        // setting up onClickListener and text for Facebook Linking
        if(signedInWithFacebook){
            fbLinkButton.setText(R.string.unlink);
            if(!signedInWithGoogle && !hasOurAccount)
                fbLinkButton.setOnClickListener(view1 -> {
                    view1.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.quick_flash));
                    Toast.makeText(getContext(), R.string.error_last_auth_provider, Toast.LENGTH_LONG).show();
                });
            else
                setUpFacebookUnlinkingSystem(fbLinkButton);
        } else {
            fbLinkButton.setText(R.string.link);
            setUpFacebookLinkingSystem(fbLinkButton, hasOurAccount);
        }

        if(!hasOurAccount)
            ourLinkButton.setOnClickListener(view1 -> {
                if(AuthFunctional.currentlyOnline) // google tokens expire early (thus this is handled)
                    googleLinkingClient.silentSignIn().addOnCompleteListener(task -> activityLauncher.launch(new Intent(getActivity(), CreateAccountActivity.class), result -> setUpLinkingSystem(view)));
                else // no network notification flashes
                    AuthFunctional.quickFlash(getContext(), view.findViewById(R.id.notification_layout_id));
            });
        else
            ourLinkButton.setVisibility(View.GONE);
    }



    private void setUpOnClickListeners(View view){
        // backButton listener - calls activity's onBackPressed()
        view.findViewById(R.id.backButton).setOnClickListener(view1 -> requireActivity().onBackPressed());

        // signOutButton listener - click
        view.findViewById(R.id.signOutButton).setOnClickListener(view1 -> {
            if(AuthFunctional.currentlyOnline)
                Toast.makeText(getContext(),getString(R.string.hold_for_signing_out), Toast.LENGTH_LONG).show(); // it's better to sign out on hold, user can click this on accident
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getContext(), view.findViewById(R.id.notification_layout_id));
        });

        // signOutButton listener - hold
        view.findViewById(R.id.signOutButton).setOnLongClickListener(view1 -> {
            if(AuthFunctional.currentlyOnline) {
                FirebaseAuth.getInstance().signOut();
                // signing out from facebook because they save it separately
                LoginManager.getInstance().logOut();
            }else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getContext(), view.findViewById(R.id.notification_layout_id));
            return true; // returns true -> onClick doesn't get triggered
        });

        // deleteAccountButton listener - pops up alert dialog for deletion
        view.findViewById(R.id.deleteAccountButton).setOnClickListener(view1 -> {
            if(AuthFunctional.currentlyOnline) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.delete_your_account)
                        .setMessage(R.string.account_deletion_message)
                        .setNegativeButton(R.string.cancel, (dialog, i) -> dialog.dismiss())
                        .setPositiveButton(R.string.delete_account, (dialog, i) -> {
                            if(AuthFunctional.currentlyOnline) // signing in silently (because google id tokens expire really quick)
                                googleLinkingClient.silentSignIn().addOnCompleteListener(task -> AuthFunctional.setUpDeletion(getActivity()));
                            else // no network notification flashes
                                AuthFunctional.quickFlash(getContext(), view.findViewById(R.id.notification_layout_id));
                        });
                builder.create().show();
            }
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getContext(), view.findViewById(R.id.notification_layout_id));
        });

        // changePasswordButton listener - alert dialog for confirmation
        view.findViewById(R.id.changePasswordButton).setOnClickListener(view1 -> {
            if(AuthFunctional.currentlyOnline)
                AuthFunctional.setUpPasswordChange(getActivity());
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getContext(), view.findViewById(R.id.notification_layout_id));
        });

        // privacyPolicy listener - opens up privacy policy in web browser
        view.findViewById(R.id.ppTextView).setOnClickListener(view1 -> startActivity(new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.privacy_policy_link)))));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.settings_screen, container, false);
        // setting up the view
        setUpOnClickListeners(view);
        setUpLinkingSystem(view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        networkManager = new NetworkManager(requireActivity().getApplication());

        // setting up listener for firebase
        authListener = firebaseAuth -> AuthFunctional.refreshUser(getActivity());

        // setting up Google client
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.server_client_id)).requestEmail().build();
        googleLinkingClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions);

        // setting up the Facebook callback manager
        facebookCallbackManager = CallbackManager.Factory.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        // registering this activity when user comes first time or returns
        networkManager.registerConnectionObserver(getActivity(),requireView().findViewById(R.id.settingsScreen));
        // adding the listener for firebase to change the UI
        FirebaseAuth.getInstance().addAuthStateListener(authListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregistering this activity when another activity comes into the foreground or else
        networkManager.unregisterConnectionObserver(getActivity());
        // removing the listener when activity pauses
        if(authListener != null)
            FirebaseAuth.getInstance().removeAuthStateListener(authListener);
    }
}
