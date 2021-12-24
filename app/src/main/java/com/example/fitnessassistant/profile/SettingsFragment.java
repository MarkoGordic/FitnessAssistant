package com.example.fitnessassistant.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.fitnessassistant.uiprefs.LanguageAdapter;
import com.example.fitnessassistant.util.AuthFunctional;
import com.example.fitnessassistant.util.ServiceFunctional;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class SettingsFragment extends Fragment {
    private GoogleSignInClient googleLinkingClient;
    // TODO let user crop the image selected
    ActivityResultLauncher<String> imageGetter = registerForActivityResult(new ActivityResultContracts.GetContent(), URI -> {
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
            FirebaseAuth.getInstance().getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(URI).build()).addOnCompleteListener(task -> onResume());
    });

    // used to get emoji for given locale
    private String localeToEmoji(Locale locale) {
        String countryCode = locale.getCountry();
        int firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6;
        int secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6;
        return new String(Character.toChars(firstLetter)) + new String(Character.toChars(secondLetter));
    }

    // used to restart the application (0.5 sec upon calling for any unfinished tasks)
    public static void restartApp(Context context, long delayMillis){
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        new Handler().postDelayed(() -> {
            context.startActivity(mainIntent);
            Runtime.getRuntime().exit(0);
        }, delayMillis);
    }

    private void setUpOnClickListeners(View view){
        // backButton listener - calls activity's onBackPressed()
        view.findViewById(R.id.backButton).setOnClickListener(view1 -> requireActivity().onBackPressed());

        // linkAccountsTextView listener - shows LinkAccountsFragment, hides current fragment
        view.findViewById(R.id.linkAccountsTextView).setOnClickListener(view1 -> requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, InAppActivity.linkAccountsFragment).addToBackStack(null).commit());

        view.findViewById(R.id.changeProfilePicture).setOnClickListener(view1 -> imageGetter.launch("image/*"));

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
                // stopping Pedometer service
                ServiceFunctional.stopPedometerService(requireActivity());
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
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit();
            editor.putBoolean("theme_changed", true);
            editor.apply();
        });

        // selectLanguageTextView listener - gives alert dialogs for choosing the language
        view.findViewById(R.id.selectLanguageTextView).setOnClickListener(view1 -> {

            AlertDialog.Builder builder1 = new AlertDialog.Builder(requireContext());
            builder1.setView(R.layout.custom_select_choice_dialog);
            Dialog dialog = builder1.create();
            dialog.show();
            ((AppCompatImageView) dialog.findViewById(R.id.dialog_drawable)).setImageResource(R.drawable.world);
            ((TextView) dialog.findViewById(R.id.dialog_header)).setText(R.string.select_a_language);

            ((Button) dialog.findViewById(R.id.dialog_negative_button)).setText(R.string.cancel);
            dialog.findViewById(R.id.dialog_negative_button).setOnClickListener(view22 -> dialog.dismiss());

            // sets text with emojis
            String serbian = String.format("%s  %s", localeToEmoji(new Locale("sr", "RS")), getString(R.string.serbian));
            String english = String.format("%s  %s", localeToEmoji(new Locale("en", "GB")), getString(R.string.english));

            ListView listView = dialog.findViewById(R.id.languagesList);
            ArrayList<String> languageList = new ArrayList<>();

            languageList.add(serbian);
            languageList.add(english);

            LanguageAdapter languageAdapter = new LanguageAdapter(requireContext(), languageList);

            listView.setAdapter(languageAdapter);

            listView.setOnItemClickListener((parent, view23, position, id) -> {
                dialog.dismiss();
                AlertDialog.Builder builder2 = new AlertDialog.Builder(requireContext());
                builder2.setView(R.layout.custom_two_button_alert_dialog);
                Dialog dialog1 = builder2.create();
                dialog1.show();

                dialog1.findViewById(R.id.dialog_drawable).setVisibility(View.GONE);
                dialog1.findViewById(R.id.dialog_input).setVisibility(View.GONE);

                ((TextView) dialog1.findViewById(R.id.dialog_header)).setText(R.string.your_selected_language_is);

                ((Button) dialog1.findViewById(R.id.dialog_negative_button)).setText(R.string.cancel);
                dialog1.findViewById(R.id.dialog_negative_button).setOnClickListener(view24 -> {
                    dialog1.dismiss();
                    dialog.show();
                });

                ((Button) dialog1.findViewById(R.id.dialog_positive_button)).setText(R.string.continue_ad);

                // get language selected
                String languageSelected = languageAdapter.getItem(position);

//                android:padding="20dp"

                // prepares dialogs based on country/language chosen
                if(languageSelected != null) {
                    if (languageSelected.equals(serbian)) {
                        // setting up language UI
                        ((TextView) dialog1.findViewById(R.id.dialog_message)).setText(serbian);
                        ((TextView) dialog1.findViewById(R.id.dialog_message)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                        dialog1.findViewById(R.id.dialog_message).setPadding(0,30,0,10);
                        ((TextView) dialog1.findViewById(R.id.dialog_message)).setTextColor(requireContext().getColor(R.color.SpaceCadet));

                        dialog1.findViewById(R.id.dialog_positive_button).setOnClickListener(view24 -> {
                            dialog1.dismiss();
                            PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext()).edit().putString("langPref", "sr").apply();
                            restartApp(requireContext(), 500);
                        });
                    } else if (languageSelected.equals(english)) {
                        // setting up language UI
                        ((TextView) dialog1.findViewById(R.id.dialog_message)).setText(english);
                        ((TextView) dialog1.findViewById(R.id.dialog_message)).setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                        dialog1.findViewById(R.id.dialog_message).setPadding(0,30,0,10);
                        ((TextView) dialog1.findViewById(R.id.dialog_message)).setTextColor(requireContext().getColor(R.color.SpaceCadet));

                        dialog1.findViewById(R.id.dialog_positive_button).setOnClickListener(view24 -> {
                            dialog1.dismiss();
                            PreferenceManager.getDefaultSharedPreferences(requireActivity().getApplicationContext()).edit().putString("langPref", "en").apply();
                            restartApp(requireContext(), 500);
                        });
                    }
                }
            });
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
