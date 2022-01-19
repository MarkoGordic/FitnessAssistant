package com.example.fitnessassistant.database;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;

import java.util.concurrent.atomic.AtomicBoolean;

public class BackupFragment extends Fragment {
    private static final AtomicBoolean isBackupVisible = new AtomicBoolean(true);

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
            if(isBackupVisible.get()) {
                // TODO check switches for backup
            } else{
                // TODO check switches for restore
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.backup_screen, container, false);
        // setting up the view
        setUpOnClickListeners(view);
        return view;
    }
}
