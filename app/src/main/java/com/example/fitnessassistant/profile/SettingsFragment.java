package com.example.fitnessassistant.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.uiprefs.ColorMode;
import com.example.fitnessassistant.util.AuthFunctional;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

// TODO idea: Save Notification and Dark Mode Settings in SharedPreferences

public class SettingsFragment extends Fragment {
    private GoogleSignInClient googleLinkingClient;

    private void setUpOnClickListeners(View view){
        // backButton listener - calls activity's onBackPressed()
        view.findViewById(R.id.backButton).setOnClickListener(view1 -> requireActivity().onBackPressed());

        // linkAccountsTextView listener - shows LinkAccountsFragment, hides current fragment
        view.findViewById(R.id.linkAccountsTextView).setOnClickListener(view1 -> requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, InAppActivity.linkAccountsFragment).addToBackStack(null).commit());

        // changeUserNameTextView listener - calling the alert dialog
        view.findViewById(R.id.changeUserNameTextView).setOnClickListener(view1 -> {
            if (AuthFunctional.currentlyOnline)
                AuthFunctional.setUpUserNameChange(getActivity());
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getActivity(), requireActivity().findViewById(R.id.no_network_notification));
        });

        // changeEmailTextView listener - calling the alert dialog
        view.findViewById(R.id.changeEmailTextView).setOnClickListener(view1 -> {
            if (AuthFunctional.currentlyOnline)
                AuthFunctional.setUpEmailChange(getActivity());
            else // if there is no internet, the animated notification quickly flashes
                AuthFunctional.quickFlash(getActivity(), requireActivity().findViewById(R.id.no_network_notification));
        });

        // changePasswordButton listener - calling the alert dialog
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

                Drawable trash = DrawableCompat.wrap(Objects.requireNonNull(AppCompatResources.getDrawable(requireContext(), R.drawable.trash)));
                DrawableCompat.setTint(trash, requireContext().getColor(R.color.SpaceCadet));
                ((AppCompatImageView) dialog.findViewById(R.id.dialog_drawable)).setImageDrawable(trash);

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


        SwitchCompat darkModeSwitch = view.findViewById(R.id.darkModeSwitch);

        // setChecked of darkModeSwitch dependant of what mode is active
        if(ColorMode.ACTIVE_MODE.equals(ColorMode.DARK_MODE)){
            darkModeSwitch.setChecked(true);
            darkModeSwitch.setText(R.string.dark_mode);
            darkModeSwitch.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(requireContext(), R.drawable.moon),null,null,null);
        } else{
            darkModeSwitch.setChecked(false);
            darkModeSwitch.setText(R.string.light_mode);
            darkModeSwitch.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(requireContext(), R.drawable.sun),null,null,null);
        }

        // set onCheckedListener for darkModeSwitch
        ((SwitchCompat) view.findViewById(R.id.darkModeSwitch)).setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if(isChecked){
                compoundButton.setText(R.string.dark_mode);
                compoundButton.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(requireContext(), R.drawable.moon),null,null,null);
                ColorMode.applyColorMode(requireActivity(), ColorMode.DARK_MODE);
            } else{
                compoundButton.setText(R.string.light_mode);
                compoundButton.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(requireContext(), R.drawable.sun),null,null,null);
                ColorMode.applyColorMode(requireActivity(), ColorMode.LIGHT_MODE);
            }
            // putting it into prefs, so that it can be used if user enters the app again
            SharedPreferences prefs = requireActivity().getSharedPreferences("ui_preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("ui_preferences", true);
            editor.apply();
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
