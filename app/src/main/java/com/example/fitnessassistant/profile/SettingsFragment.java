package com.example.fitnessassistant.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.util.AuthFunctional;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

// TODO idea: Save Notification and Dark Mode Settings in SharedPreferences

public class SettingsFragment extends Fragment {
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
                AuthFunctional.quickFlash(getActivity(), requireActivity().findViewById(R.id.no_network_notification));
        });

        // changeEmailTextView listener - alert dialog
        view.findViewById(R.id.changeEmailTextView).setOnClickListener(view1 -> {
            if (AuthFunctional.currentlyOnline)
                AuthFunctional.setUpEmailChange(getActivity());
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getActivity(), requireActivity().findViewById(R.id.no_network_notification));
        });

        // changePasswordButton listener - alert dialog
        view.findViewById(R.id.changePasswordTextView).setOnClickListener(view1 -> {
            if(AuthFunctional.currentlyOnline)
                AuthFunctional.setUpPasswordChange(getActivity());
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getActivity(), requireActivity().findViewById(R.id.no_network_notification));
        });

        // privacyPolicy listener - opens up privacy policy in web browser
        view.findViewById(R.id.ppTextView).setOnClickListener(view1 -> startActivity(new Intent("android.intent.action.VIEW", Uri.parse(getString(R.string.privacy_policy_link)))));

        // signOutButton listener - click
        view.findViewById(R.id.signOutAccountTextView).setOnClickListener(view1 -> {
            if(AuthFunctional.currentlyOnline)
                Toast.makeText(getContext(),getString(R.string.hold_for_signing_out), Toast.LENGTH_LONG).show(); // it's better to sign out on hold, user can click this on accident
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getActivity(), requireActivity().findViewById(R.id.no_network_notification));
        });

        // signOutButton listener - hold
        view.findViewById(R.id.signOutAccountTextView).setOnLongClickListener(view1 -> {
            if(AuthFunctional.currentlyOnline) {
                FirebaseAuth.getInstance().signOut();
                // signing out from facebook because they save it separately
                LoginManager.getInstance().logOut();
            }else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getActivity(), requireActivity().findViewById(R.id.no_network_notification));
            return true; // returns true -> onClick doesn't get triggered
        });

        // deleteAccountButton listener - pops up alert dialog for deletion
        view.findViewById(R.id.deleteAccountTextView).setOnClickListener(view1 -> {
            if(AuthFunctional.currentlyOnline) {
                // setting up a custom dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(R.layout.custom_two_button_alert_dialog);
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                ((TextView) dialog.findViewById(R.id.dialog_header)).setText(R.string.delete_your_account);
                ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.account_deletion_message);

                dialog.findViewById(R.id.dialog_input).setVisibility(View.GONE);
                dialog.findViewById(R.id.dialog_negative_button).setOnClickListener(view2 -> dialog.dismiss());

                ((Button) dialog.findViewById(R.id.dialog_positive_button)).setText(R.string.delete_account);
                dialog.findViewById(R.id.dialog_positive_button).setOnClickListener(view2 -> {
                    dialog.dismiss();
                    if(AuthFunctional.currentlyOnline) // signing in silently (because google id tokens expire really quick)
                        googleLinkingClient.silentSignIn().addOnCompleteListener(task -> AuthFunctional.setUpDeletion(getActivity()));
                    else // no network notification flashes
                        AuthFunctional.quickFlash(getActivity(), requireActivity().findViewById(R.id.no_network_notification));
                });
            }
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getActivity(), requireActivity().findViewById(R.id.no_network_notification));
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
        // setting up Google client
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.server_client_id)).requestEmail().build();
        googleLinkingClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions);
    }

}
