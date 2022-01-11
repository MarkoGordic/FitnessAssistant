package com.example.fitnessassistant;

import static com.example.fitnessassistant.profile.AccountDataFragment.scaleBitmap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.fitnessassistant.activitytracker.ActivityTrackingFragment;
import com.example.fitnessassistant.diary.DiaryPageFragment;
import com.example.fitnessassistant.home.HomePageFragment;
import com.example.fitnessassistant.map.MapPageFragment;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.pedometer.PedometerFragment;
import com.example.fitnessassistant.profile.AccountDataFragment;
import com.example.fitnessassistant.profile.GoalsFragment;
import com.example.fitnessassistant.profile.LinkAccountsFragment;
import com.example.fitnessassistant.profile.PersonalDataFragment;
import com.example.fitnessassistant.profile.ProfilePageFragment;
import com.example.fitnessassistant.profile.SettingsFragment;
import com.example.fitnessassistant.questions.BirthdayFragment;
import com.example.fitnessassistant.questions.GenderFragment;
import com.example.fitnessassistant.questions.HeightFragment;
import com.example.fitnessassistant.questions.OpeningQuestionFragment;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;
import com.example.fitnessassistant.questions.WeightFragment;
import com.example.fitnessassistant.uiprefs.ColorMode;
import com.example.fitnessassistant.uiprefs.LocaleExt;
import com.example.fitnessassistant.util.AuthFunctional;
import com.example.fitnessassistant.util.ServiceFunctional;
import com.example.fitnessassistant.workout.WorkoutPageFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class InAppActivity extends AppCompatActivity {
    // network manager for network connectivity checking
    private NetworkManager networkManager;
    // auth listener for refreshing user and UI
    private FirebaseAuth.AuthStateListener authListener;
    // setting up all fragments
    private MapPageFragment mapFragment;
    private DiaryPageFragment diaryFragment;
    private HomePageFragment homeFragment;
    private WorkoutPageFragment workoutFragment;
    private ProfilePageFragment profileFragment;
    public static ActivityTrackingFragment activityTrackingFragment;
    public static SettingsFragment settingsFragment;
    public static LinkAccountsFragment linkAccountsFragment;
    public static GoalsFragment goalsFragment;
    public static AccountDataFragment accountDataFragment;
    public static PersonalDataFragment personalDataFragment;
    public static PedometerFragment pedometerFragment;
    // this atomic boolean is used for Personal Data Fragments (Height, Weight, Gender, UnitPreference)
    public static AtomicBoolean useNewPersonalDataFragments = new AtomicBoolean(false);
    // and fragment manager
    private final FragmentManager fm = getSupportFragmentManager();
    // and setting the currently active fragment as home
    private Fragment active;

    // launcher for the Activity Recognition Permission
    public final ActivityResultLauncher<String> activityRecognitionPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if(result)
            ServiceFunctional.startPedometerService(this);
        else {
            // creates an alert dialog with rationale shown
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(R.layout.custom_ok_alert_dialog);
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ((AppCompatImageView)dialog.findViewById(R.id.dialog_drawable)).setImageResource(R.drawable.exclamation);

            ((TextView) dialog.findViewById(R.id.dialog_header)).setText(R.string.activity_recognition_access_denied);
            dialog.findViewById(R.id.dialog_ok_button).setOnClickListener(view2 -> dialog.dismiss());

            // showing messages (one case if user selected don't ask again, other if user just selected deny)
            if(!shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION))
                ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.activity_recognition_access_message_denied_forever);
            else
                ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.activity_recognition_access_message_denied);
        }
    });

    // used for getting the image from gallery
    public final ActivityResultLauncher<Intent> imageGetter = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getData() != null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            findViewById(R.id.additional_progress_bar).setVisibility(View.VISIBLE);
            findViewById(R.id.additional_text_view).setVisibility(View.VISIBLE);
            Animation oldAnim = findViewById(R.id.notification).getAnimation();
            findViewById(R.id.notification).setAnimation(null);
            try {
                Uri uri = result.getData().getData();
                InputStream fileInputStream = getContentResolver().openInputStream(uri);
                if(fileInputStream.available() != -1 && fileInputStream.available() < 4 * 1024 * 1024) { // 4MB
                    Bitmap bmp = BitmapFactory.decodeStream(fileInputStream);
                    Bitmap scaledBmp = scaleBitmap(bmp, 1024 * 1024); // 1MB (+ additional compressing to jpeg below)

                    if(scaledBmp != bmp){
                        bmp.recycle();
                    }

                    scaledBmp.compress(Bitmap.CompressFormat.JPEG, 100, new ByteArrayOutputStream());
                    Uri newUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), scaledBmp, "ProfilePic", null));

                    StorageReference ref = FirebaseStorage.getInstance().getReference().child("users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/profile.jpg");

                    ref.putFile(newUri).addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(URI -> FirebaseAuth.getInstance().getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setPhotoUri(URI).build()).addOnSuccessListener(unused -> {
                        Toast.makeText(this, getString(R.string.profile_picture_successfully_uploaded), Toast.LENGTH_SHORT).show();
                        findViewById(R.id.additional_progress_bar).setVisibility(View.GONE);
                        findViewById(R.id.additional_text_view).setVisibility(View.GONE);
                        findViewById(R.id.notification).startAnimation(oldAnim);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, getString(R.string.profile_picture_not_successfully_uploaded), Toast.LENGTH_SHORT).show();
                        findViewById(R.id.additional_progress_bar).setVisibility(View.GONE);
                        findViewById(R.id.additional_text_view).setVisibility(View.GONE);
                        findViewById(R.id.notification).startAnimation(oldAnim);
                    })).addOnFailureListener(e -> {
                        Toast.makeText(this, getString(R.string.profile_picture_not_successfully_uploaded), Toast.LENGTH_SHORT).show();
                        findViewById(R.id.additional_progress_bar).setVisibility(View.GONE);
                        findViewById(R.id.additional_text_view).setVisibility(View.GONE);
                        findViewById(R.id.notification).startAnimation(oldAnim);
                    })).addOnFailureListener(e -> {
                        Toast.makeText(this, getString(R.string.profile_picture_not_successfully_uploaded), Toast.LENGTH_SHORT).show();
                        findViewById(R.id.additional_progress_bar).setVisibility(View.GONE);
                        findViewById(R.id.additional_text_view).setVisibility(View.GONE);
                        findViewById(R.id.notification).startAnimation(oldAnim);
                    });
                } else {
                    Toast.makeText(this, getString(R.string.image_size_too_large), Toast.LENGTH_SHORT);
                    findViewById(R.id.additional_progress_bar).setVisibility(View.GONE);
                    findViewById(R.id.additional_text_view).setVisibility(View.GONE);
                    findViewById(R.id.notification).startAnimation(oldAnim);
                }
            } catch (Exception e) {
                e.printStackTrace();
                findViewById(R.id.additional_progress_bar).setVisibility(View.GONE);
                findViewById(R.id.additional_text_view).setVisibility(View.GONE);
                findViewById(R.id.notification).startAnimation(oldAnim);
            }
        }
    });

    // return to previous fragment (if it exists)
    @Override
    public void onBackPressed() {
        if(findViewById(R.id.bottomNavigation).getVisibility() == View.GONE)
            findViewById(R.id.bottomNavigation).setVisibility(View.VISIBLE);

        if(fm.getBackStackEntryCount() == 0)
            super.onBackPressed();
        else {
            fm.popBackStack();
            for(Fragment f : fm.getFragments()){
                f.onResume();
            }
        }
    }

    public void setInAppUI(){
        mapFragment = new MapPageFragment();
        diaryFragment = new DiaryPageFragment();
        homeFragment = new HomePageFragment();
        workoutFragment = new WorkoutPageFragment();
        profileFragment = new ProfilePageFragment();
        settingsFragment = new SettingsFragment();
        linkAccountsFragment = new LinkAccountsFragment();
        activityTrackingFragment = new ActivityTrackingFragment();
        goalsFragment = new GoalsFragment();
        accountDataFragment = new AccountDataFragment();
        personalDataFragment = new PersonalDataFragment();
        pedometerFragment = new PedometerFragment();

        active = homeFragment;

        fm.beginTransaction().add(R.id.in_app_container, mapFragment).hide(mapFragment).commit();
        fm.beginTransaction().add(R.id.in_app_container, diaryFragment).hide(diaryFragment).commit();
        fm.beginTransaction().add(R.id.in_app_container, active).commit();
        fm.beginTransaction().add(R.id.in_app_container, workoutFragment).hide(workoutFragment).commit();
        fm.beginTransaction().add(R.id.in_app_container, profileFragment).hide(profileFragment).commit();

        // navigation listener hides the active fragment, shows the selected one and sets the selected as the new active
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setVisibility(View.VISIBLE);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            // popping all previous fragments pushed to the stack
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            if(item.getItemId() == R.id.map){
                fm.beginTransaction().hide(active).show(mapFragment).commit();
                active = mapFragment;
                active.onResume();
                return true;
            } else if(item.getItemId() == R.id.diary){
                fm.beginTransaction().hide(active).show(diaryFragment).commit();
                active = diaryFragment;
                active.onResume();
                return true;
            } else if(item.getItemId() == R.id.home){
                fm.beginTransaction().hide(active).show(homeFragment).commit();
                active = homeFragment;
                active.onResume();
                return true;
            } else if(item.getItemId() == R.id.health){
                fm.beginTransaction().hide(active).show(workoutFragment).commit();
                active = workoutFragment;
                active.onResume();
                return true;
            } else if(item.getItemId() == R.id.profile) {
                fm.beginTransaction().hide(active).show(profileFragment).commit();
                active = profileFragment;
                active.onResume();
                return true;
            }
            return false;
        });

        if(ServiceFunctional.getPedometerShouldRun(this))
            ServiceFunctional.startPedometerService(this);

        goToDesiredFragment(getIntent());
    }

    // providing one and only context available, throughout contextWrapper
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleExt.toLangIfDiff(newBase, PreferenceManager.getDefaultSharedPreferences(newBase).getString("langPref", "sys"), true, true));
    }

    // applying config changes
    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        super.applyOverrideConfiguration(getBaseContext().getResources().getConfiguration());
    }

    public void setUpPedometerFragmentUI(boolean pedometerRuns){
        if(pedometerFragment != null)
            pedometerFragment.setUpUI(pedometerRuns);
    }

    public synchronized void putDesiredFragment(String desiredFragment){
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("desiredFragment",desiredFragment).apply();
    }

    public synchronized String getDesiredFragment(){
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("desiredFragment", null);
    }

    // if intent given has it, go to desired fragment
    private void goToDesiredFragment(Intent intent){
        if(intent != null && intent.getStringExtra("desiredFragment") != null)
            if(intent.getStringExtra("desiredFragment").equals("MapFragment")){
                ((BottomNavigationView) findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.map);
                active = mapFragment;
            } else if(intent.getStringExtra("desiredFragment").equals("ActivityTrackingFragment")){
                putDesiredFragment("ActivityTrackingFragment");
                ((BottomNavigationView) findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.map);
                active = mapFragment;
            }
    }

    // if new intent is registered
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        goToDesiredFragment(intent);
    }

    private void setUpRebootKeys(){
        // determining whether device rebooted or not
        SharedPreferences savedKeys = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = savedKeys.edit();

        editor.putBoolean("reboot", ((System.currentTimeMillis() - SystemClock.elapsedRealtime()) - (savedKeys.getLong("key_oldDelta", 0))) > 100);

        editor.putLong("key_oldDelta", (System.currentTimeMillis() - SystemClock.elapsedRealtime()));
        editor.apply();
    }

    public void proceedQuestions(int order){
        if(GenderFragment.getGender(this).equals("unknown") && order < 1)
            fm.beginTransaction().hide(Objects.requireNonNull(fm.findFragmentById(R.id.in_app_container))).add(R.id.in_app_container, new GenderFragment(), null).commit();
        else if(BirthdayFragment.getBirthday(this) == -1 && order < 2)
            fm.beginTransaction().hide(Objects.requireNonNull(fm.findFragmentById(R.id.in_app_container))).add(R.id.in_app_container, new BirthdayFragment(), null).commit();
        else if(HeightFragment.getHeight(this) == -1f && order < 3)
            fm.beginTransaction().hide(Objects.requireNonNull(fm.findFragmentById(R.id.in_app_container))).add(R.id.in_app_container, new HeightFragment(), null).commit();
        else if(WeightFragment.getLastDailyAverage(this) == -1f && order < 4)
            fm.beginTransaction().hide(Objects.requireNonNull(fm.findFragmentById(R.id.in_app_container))).add(R.id.in_app_container, new WeightFragment(), null).commit();
        else if(UnitPreferenceFragment.isUnknown(this) && order < 5)
            fm.beginTransaction().hide(Objects.requireNonNull(fm.findFragmentById(R.id.in_app_container))).add(R.id.in_app_container, new UnitPreferenceFragment(), null).commit();
        else {
            for(Fragment fragment : fm.getFragments()){
                fm.beginTransaction().remove(fragment).commit();
            }
            setInAppUI();
        }
    }

    private void setUpQuestionsUI(){
        if(OpeningQuestionFragment.getShouldSkipQuestions(this))
            setInAppUI();
        else
            fm.beginTransaction().add(R.id.in_app_container, new OpeningQuestionFragment(), null).commit();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpRebootKeys();

        // applying the color mode needed
        ColorMode.applyColorMode(this, null);

        setContentView(R.layout.in_app_screen);

        // setting up network manager
        networkManager = new NetworkManager(getApplication());

        // setting up listener for firebase
        authListener = firebaseAuth -> AuthFunctional.refreshUser(this);

        // setting up the flashing animation for the no network notification
        Animation flash = new AlphaAnimation(0.0f, 1.0f);
        flash.setDuration(800); // flash duration
        flash.setStartOffset(1600); // staying visible duration
        flash.setRepeatMode(Animation.REVERSE);
        flash.setRepeatCount(Animation.INFINITE);
        findViewById(R.id.notification).startAnimation(flash);

        getSharedPreferences("pedometer", MODE_PRIVATE).registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            if(key.equals("pedometerDataChanged")){
                homeFragment.updateStepsData(null);
                pedometerFragment.updateStepsData(null);
            }
        });

        if(ServiceFunctional.getPedometerShouldRun(this))
            ServiceFunctional.startPedometerService(this);

        setUpQuestionsUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        // registering this when user comes first time or returns
        networkManager.registerConnectionObserver(this, findViewById(R.id.no_network_text_view));
        // adding the listener for firebase to change the UI
        FirebaseAuth.getInstance().addAuthStateListener(authListener);

        networkManager = new NetworkManager(getApplication());

        // setting up listener for firebase
        authListener = firebaseAuth -> AuthFunctional.refreshUser(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregistering this when something comes into the foreground or else
        networkManager.unregisterConnectionObserver(this);
        // removing the listener
        if(authListener != null)
            FirebaseAuth.getInstance().removeAuthStateListener(authListener);
    }
}