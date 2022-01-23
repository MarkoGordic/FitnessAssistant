package com.example.fitnessassistant.database;

import static com.example.fitnessassistant.util.TimeFunctional.getMonthShort;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
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
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.util.AuthFunctional;

import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackupFragment extends Fragment {
    private static final AtomicBoolean isBackupVisible = new AtomicBoolean(true);

    private static final AtomicBoolean personalData = new AtomicBoolean(false);
    private static final AtomicBoolean goalsData = new AtomicBoolean(false);
    private static final AtomicBoolean activityData = new AtomicBoolean(false);
    private static final AtomicBoolean pedometerData = new AtomicBoolean(false);
    private static final AtomicBoolean sleepData = new AtomicBoolean(false);

    @SuppressLint("DefaultLocale")
    private final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> {
        if(getView() != null) {
            Calendar cal = Calendar.getInstance();
            String lastBackup = requireActivity().getString(R.string.last_backup);
            switch (key) {
                case "pedometer_backup":
                    cal.setTimeInMillis(Long.parseLong(sharedPreferences.getString("pedometer_backup", "n#/").substring(2)));
                    ((TextView) getView().findViewById(R.id.pedometerBackupDate)).setText(String.format("%s: %02d %s %d %02d:%02d", lastBackup, cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
                    break;
                case "preferences_backup":
                    cal.setTimeInMillis(Long.parseLong(sharedPreferences.getString("preferences_backup", "n#/").substring(2)));
                    ((TextView) getView().findViewById(R.id.preferencesBackupDate)).setText(String.format("%s %02d %s %d %02d:%02d", lastBackup, cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
                    break;
                case "goals_backup":
                    cal.setTimeInMillis(Long.parseLong(sharedPreferences.getString("goals_backup", "n#/").substring(2)));
                    ((TextView) getView().findViewById(R.id.goalsBackupDate)).setText(String.format("%s %02d %s %d %02d:%02d", lastBackup, cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
                    break;
                case "sleep_backup":
                    cal.setTimeInMillis(Long.parseLong(sharedPreferences.getString("sleep_backup", "n#/").substring(2)));
                    ((TextView) getView().findViewById(R.id.sleepBackupDate)).setText(String.format("%s %02d %s %d %02d:%02d", lastBackup, cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
                    break;
                case "activities_backup":
                    cal.setTimeInMillis(Long.parseLong(sharedPreferences.getString("activities_backup", "n#/").substring(2)));
                    ((TextView) getView().findViewById(R.id.activitiesBackupDate)).setText(String.format("%s %02d %s %d %02d:%02d", lastBackup, cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
                    break;
            }
        }
    };

    private void setBools(View view){
        personalData.set(((SwitchCompat) view.findViewById(R.id.personalDataSwitch)).isChecked());
        goalsData.set(((SwitchCompat) view.findViewById(R.id.goalsSwitch)).isChecked());
        activityData.set(((SwitchCompat) view.findViewById(R.id.activitiesSwitch)).isChecked());
        pedometerData.set(((SwitchCompat) view.findViewById(R.id.pedometerDataSwitch)).isChecked());
        sleepData.set(((SwitchCompat) view.findViewById(R.id.sleepDataSwitch)).isChecked());
    }

    private boolean atLeastOneSwitchChecked(View view){
        return ((SwitchCompat) view.findViewById(R.id.personalDataSwitch)).isChecked() ||
                ((SwitchCompat) view.findViewById(R.id.goalsSwitch)).isChecked() ||
                ((SwitchCompat) view.findViewById(R.id.activitiesSwitch)).isChecked() ||
                ((SwitchCompat) view.findViewById(R.id.pedometerDataSwitch)).isChecked() ||
                ((SwitchCompat) view.findViewById(R.id.sleepDataSwitch)).isChecked();
                // TODO add more
    }

    private void resetBools(){
        personalData.set(false);
        goalsData.set(false);
        activityData.set(false);
        pedometerData.set(false);
        sleepData.set(false);
    }

    @SuppressLint("DefaultLocale")
    private void setUpDates(View view){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        Calendar cal = Calendar.getInstance();
        String lastBackup = requireActivity().getString(R.string.last_backup);
        if(!sharedPreferences.getString("pedometer_backup", "n#/").equals("n#/")) {
            cal.setTimeInMillis(Long.parseLong(sharedPreferences.getString("pedometer_backup", "n#/").substring(2)));
            ((TextView) view.findViewById(R.id.pedometerBackupDate)).setText(String.format("%s: %02d %s %d %02d:%02d", lastBackup, cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
        } else
            ((TextView) view.findViewById(R.id.pedometerBackupDate)).setText(String.format("%s: %s", lastBackup, requireActivity().getString(R.string.n_a)));

        if(!sharedPreferences.getString("preferences_backup", "n#/").equals("n#/")) {
            cal.setTimeInMillis(Long.parseLong(sharedPreferences.getString("preferences_backup", "n#/").substring(2)));
            ((TextView) view.findViewById(R.id.preferencesBackupDate)).setText(String.format("%s %02d %s %d %02d:%02d", lastBackup, cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
        } else
            ((TextView) view.findViewById(R.id.preferencesBackupDate)).setText(String.format("%s: %s", lastBackup, requireActivity().getString(R.string.n_a)));

        if(!sharedPreferences.getString("goals_backup", "n#/").equals("n#/")) {
            cal.setTimeInMillis(Long.parseLong(sharedPreferences.getString("goals_backup", "n#/").substring(2)));
            ((TextView) view.findViewById(R.id.goalsBackupDate)).setText(String.format("%s %02d %s %d %02d:%02d", lastBackup, cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
        } else
            ((TextView) view.findViewById(R.id.goalsBackupDate)).setText(String.format("%s: %s", lastBackup, requireActivity().getString(R.string.n_a)));

        if(!sharedPreferences.getString("sleep_backup", "n#/").equals("n#/")) {
            cal.setTimeInMillis(Long.parseLong(sharedPreferences.getString("sleep_backup", "n#/").substring(2)));
            ((TextView) view.findViewById(R.id.sleepBackupDate)).setText(String.format("%s %02d %s %d %02d:%02d", lastBackup, cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
        } else
            ((TextView) view.findViewById(R.id.sleepBackupDate)).setText(String.format("%s: %s", lastBackup, requireActivity().getString(R.string.n_a)));

        if(!sharedPreferences.getString("activities_backup", "n#/").equals("n#/")) {
            cal.setTimeInMillis(Long.parseLong(sharedPreferences.getString("activities_backup", "n#/").substring(2)));
            ((TextView) view.findViewById(R.id.activitiesBackupDate)).setText(String.format("%s %02d %s %d %02d:%02d", lastBackup, cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
        } else
            ((TextView) view.findViewById(R.id.activitiesBackupDate)).setText(String.format("%s: %s", lastBackup, requireActivity().getString(R.string.n_a)));

        // TODO add more data
    }

    private void setUpOnClickListeners(View view){
        // backButton listener - calls activity's onBackPressed()
        view.findViewById(R.id.backButton).setOnClickListener(view1 -> requireActivity().onBackPressed());

        TextView header = view.findViewById(R.id.backupHeader);

        view.findViewById(R.id.backBackupButton).setOnClickListener(v -> header.animate()
                .translationX(-view.getWidth())
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if(isBackupVisible.get()){
                            isBackupVisible.set(false);
                            header.setText(R.string.restore);
                        } else{
                            isBackupVisible.set(true);
                            header.setText(R.string.backup);
                        }
                        header.setX(view.getWidth());
                        header.animate()
                                .translationX(0)
                                .alpha(1.0f)
                                .setListener(null);
                    }
                }));
        view.findViewById(R.id.frontBackupButton).setOnClickListener(v -> header.animate()
                .translationX(view.getWidth())
                .alpha(0.0f)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if(isBackupVisible.get()){
                            isBackupVisible.set(false);
                            header.setText(R.string.restore);
                        } else{
                            isBackupVisible.set(true);
                            header.setText(R.string.backup);
                        }
                        header.setX(-view.getWidth());
                        header.animate()
                                .translationX(0)
                                .alpha(1.0f)
                                .setListener(null);
                    }
                }));

        view.findViewById(R.id.proceedButton).setOnClickListener(v -> {
            if(AuthFunctional.currentlyOnline) {
                if(atLeastOneSwitchChecked(view)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                    builder.setView(R.layout.custom_two_button_alert_dialog);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    Drawable drawable;
                    if(isBackupVisible.get()){
                        drawable = AppCompatResources.getDrawable(requireActivity(), R.drawable.backup);
                        ((TextView) dialog.findViewById(R.id.dialog_header)).setText(R.string.backup);
                        ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.backup_message);
                        ((Button) dialog.findViewById(R.id.dialog_positive_button)).setText(R.string.save);
                    } else{
                        drawable = AppCompatResources.getDrawable(requireActivity(), R.drawable.restore);
                        ((TextView) dialog.findViewById(R.id.dialog_header)).setText(R.string.restore);
                        ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.restore_message);
                        ((Button) dialog.findViewById(R.id.dialog_positive_button)).setText(R.string.to_restore);
                    }

                    if (drawable != null)
                        drawable.setTint(requireActivity().getColor(R.color.SpaceCadet));

                    ((AppCompatImageView) dialog.findViewById(R.id.dialog_drawable)).setImageDrawable(drawable);

                    dialog.findViewById(R.id.dialog_input).setVisibility(View.GONE);

                    dialog.findViewById(R.id.dialog_negative_button).setOnClickListener(view2 -> dialog.dismiss());
                    dialog.findViewById(R.id.dialog_positive_button).setOnClickListener(view2 -> {
                        dialog.dismiss();
                        setBools(view);
                        view.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.progressTV).setVisibility(View.VISIBLE);
                        if (isBackupVisible.get()) {
                            ((TextView) view.findViewById(R.id.progressTV)).setText(requireActivity().getString(R.string.backing_up));
                            new TaskRunner().executeAsync(new BackupTask(requireActivity()), result -> {
                                resetBools();
                                view.findViewById(R.id.progressBar).setVisibility(View.GONE);
                                view.findViewById(R.id.progressTV).setVisibility(View.GONE);
                            });
                        } else {
                            ((TextView) view.findViewById(R.id.progressTV)).setText(requireActivity().getString(R.string.restoring));
                            new TaskRunner().executeAsync(new RestoreTask(requireActivity()), result -> {
                                resetBools();
                                view.findViewById(R.id.progressBar).setVisibility(View.GONE);
                                view.findViewById(R.id.progressTV).setVisibility(View.GONE);
                            });
                        }
                    });
                } else
                    Toast.makeText(requireActivity(), R.string.select_one, Toast.LENGTH_SHORT).show();
            } else
                AuthFunctional.quickFlash(requireActivity(), requireActivity().findViewById(R.id.notification));
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.backup_screen, container, false);
        // setting up the view
        setUpOnClickListeners(view);
        setUpDates(view);

        view.findViewById(R.id.progressBar).setVisibility(View.GONE);

        PreferenceManager.getDefaultSharedPreferences(requireActivity()).registerOnSharedPreferenceChangeListener(listener);

        return view;
    }

    private static class TaskRunner {
        public interface Callback<x>{
            void onComplete(x Result);
        }

        public<x> void executeAsync(Callable<x> callable, Callback<x> callback){
            Executors.newSingleThreadExecutor().execute(() -> {
                try{
                    final x result = callable.call();

                    new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(result));
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }
    }

    private static class BackupTask implements Callable<Void>{
        private final Context context;

        public BackupTask(Context context){
            this.context = context;
        }

        @Override
        public Void call() {
            if(personalData.get())
                RealtimeDB.saveUserPreferences(context);
            if(goalsData.get())
                RealtimeDB.saveUserGoals(context);
            if(activityData.get())
                RealtimeDB.saveUserActivities(context);
            if(pedometerData.get())
                RealtimeDB.savePedometerData(context);
            if(sleepData.get()) {
                // TODO
            }
            return null;
        }
    }

    private static class RestoreTask implements Callable<Void>{
        private final Context context;

        public RestoreTask(Context context){
            this.context = context;
        }

        @Override
        public Void call() {
            if(personalData.get())
                RealtimeDB.restoreUserPreferences(context);
            if(goalsData.get())
                RealtimeDB.restoreUserGoals(context);
            if(activityData.get())
                RealtimeDB.restoreUserActivities(context);
            if(pedometerData.get())
                RealtimeDB.restorePedometerData(context);
            if(sleepData.get()) {
                // TODO
            }
            return null;
        }
    }
}
