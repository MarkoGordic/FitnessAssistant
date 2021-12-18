package com.example.fitnessassistant.util;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;

// used primary for requesting the activity recognition permission, could become generic so that it fits any permission
public class PermissionFunctional {
    private final Fragment fragment;
    private final ActivityResultLauncher<String> permissionLauncher;

    public PermissionFunctional(Fragment fragment, ActivityResultLauncher<String> launcher){
        this.fragment = fragment;
        // launcher for permissions
        permissionLauncher = launcher;
    }

    public void checkActivityRecognitionPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(fragment.requireContext(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                // alert dialog to let user know we're requesting activity recognition
                AlertDialog.Builder builder = new AlertDialog.Builder(fragment.requireContext());
                builder.setView(R.layout.custom_ok_alert_dialog);
                AlertDialog dialog = builder.create();
                dialog.show();
                // disables the user to cancel the given dialog
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                ((AppCompatImageView)dialog.findViewById(R.id.dialog_drawable)).setImageResource(R.drawable.device);

                ((TextView) dialog.findViewById(R.id.dialog_header)).setText(R.string.activity_recognition_access);
                dialog.findViewById(R.id.dialog_message).setVisibility(View.GONE);
                dialog.findViewById(R.id.dialog_ok_button).setOnClickListener(view2 -> {
                    dialog.dismiss();
                    permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
                });
            } else
                Toast.makeText(fragment.requireContext(), R.string.pedometer_can_start, Toast.LENGTH_LONG).show();                          // this is probably always false
        } else if (ContextCompat.checkSelfPermission(fragment.requireContext(), "com.google.android.gms.permission.ACTIVITY_RECOGNITION") != PackageManager.PERMISSION_GRANTED) {
            // alert dialog to let user know we're requesting activity recognition
            AlertDialog.Builder builder = new AlertDialog.Builder(fragment.requireContext());
            builder.setView(R.layout.custom_ok_alert_dialog);
            AlertDialog dialog = builder.create();
            dialog.show();
            // disables the user to cancel the given dialog
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ((AppCompatImageView)dialog.findViewById(R.id.dialog_drawable)).setImageResource(R.drawable.device);

            ((TextView) dialog.findViewById(R.id.dialog_header)).setText(R.string.activity_recognition_access);
            dialog.findViewById(R.id.dialog_message).setVisibility(View.GONE);
            dialog.findViewById(R.id.dialog_ok_button).setOnClickListener(view2 -> {
                dialog.dismiss();
                permissionLauncher.launch("com.google.android.gms.permission.ACTIVITY_RECOGNITION");
            });
        } else
            Toast.makeText(fragment.requireContext(), R.string.pedometer_can_start, Toast.LENGTH_LONG).show();
    }
}
