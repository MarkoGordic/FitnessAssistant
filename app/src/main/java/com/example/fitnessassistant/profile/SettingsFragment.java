package com.example.fitnessassistant.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.util.AuthFunctional;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends Fragment {
    private NetworkManager networkManager;
    private FirebaseAuth.AuthStateListener authListener;
    private GoogleSignInClient googleLinkingClient;

    private void setUpOnClickListeners(View view){
        // backButton listener - calls activity's onBackPressed()
        view.findViewById(R.id.backButton).setOnClickListener(view1 -> requireActivity().onBackPressed());

        // linkAccountsTextView listener - shows LinkAccountsFragment, hides current fragment
        view.findViewById(R.id.linkAccountsTextView).setOnClickListener(view1 -> requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, InAppActivity.linkAccountsFragment).addToBackStack(null).commit());

        // changeUserNameTextView listener - alert dialog
        view.findViewById(R.id.changeUserNameTextView).setOnClickListener(view1 -> {
            if (AuthFunctional.currentlyOnline)
                AuthFunctional.setUpUserNameChange(getActivity());
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getContext(), view.findViewById(R.id.notification_layout_id));
        });

        // changeEmailTextView listener - alert dialog
        view.findViewById(R.id.changeEmailTextView).setOnClickListener(view1 -> {
            if (AuthFunctional.currentlyOnline)
                AuthFunctional.setUpEmailChange(getActivity());
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getContext(), view.findViewById(R.id.notification_layout_id));
        });

        // changePasswordButton listener - alert dialog
        view.findViewById(R.id.changePasswordTextView).setOnClickListener(view1 -> {
            if(AuthFunctional.currentlyOnline)
                AuthFunctional.setUpPasswordChange(getActivity());
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getContext(), view.findViewById(R.id.notification_layout_id));
        });

        // privacyPolicy listener - opens up privacy policy in web browser
        view.findViewById(R.id.ppTextView).setOnClickListener(view1 -> startActivity(new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.privacy_policy_link)))));

        // signOutButton listener - click
        view.findViewById(R.id.signOutAccountTextView).setOnClickListener(view1 -> {
            if(AuthFunctional.currentlyOnline)
                Toast.makeText(getContext(),getString(R.string.hold_for_signing_out), Toast.LENGTH_LONG).show(); // it's better to sign out on hold, user can click this on accident
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getContext(), view.findViewById(R.id.notification_layout_id));
        });

        // signOutButton listener - hold
        view.findViewById(R.id.signOutAccountTextView).setOnLongClickListener(view1 -> {
            if(AuthFunctional.currentlyOnline) {
                FirebaseAuth.getInstance().signOut();
                // signing out from facebook because they save it separately
                LoginManager.getInstance().logOut();
            }else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getContext(), view.findViewById(R.id.notification_layout_id));
            return true; // returns true -> onClick doesn't get triggered
        });

        // deleteAccountButton listener - pops up alert dialog for deletion
        view.findViewById(R.id.deleteAccountTextView).setOnClickListener(view1 -> {
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.settings_screen, container, false);
        // setting up the view
        setUpOnClickListeners(view);
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
