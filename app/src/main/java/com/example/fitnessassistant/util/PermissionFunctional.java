package com.example.fitnessassistant.util;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.activitytracker.ActivityTrackingFragment;

// used primary for requesting the activity recognition permission, could become generic so that it fits any permission
public class PermissionFunctional {
    public static void checkActivityRecognitionPermission(Context context, ActivityResultLauncher<String> permissionLauncher){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                // alert dialog to let user know we're requesting activity recognition
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
                ServiceFunctional.startPedometerService(context);
        } else if (ContextCompat.checkSelfPermission(context, "com.google.android.gms.permission.ACTIVITY_RECOGNITION") != PackageManager.PERMISSION_GRANTED) {
            // alert dialog to let user know we're requesting activity recognition
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
                permissionLauncher.launch(
                        "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
                );
            });
        } else
            ServiceFunctional.startPedometerService(context);
    }

    public static void checkFineLocationPermission(ActivityTrackingFragment fragment, ActivityResultLauncher<String[]> permissionLauncher, ActivityResultLauncher<String> bPermissionLauncher){
        if (ContextCompat.checkSelfPermission(fragment.requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // alert dialog to let user know we're requesting location permission
            AlertDialog.Builder builder = new AlertDialog.Builder(fragment.requireActivity());
            builder.setView(R.layout.custom_ok_alert_dialog);
            AlertDialog dialog = builder.create();
            dialog.show();
            // disables the user to cancel the given dialog
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ((AppCompatImageView)dialog.findViewById(R.id.dialog_drawable)).setImageResource(R.drawable.marker);

            ((TextView) dialog.findViewById(R.id.dialog_header)).setText(R.string.location_access);
            dialog.findViewById(R.id.dialog_message).setVisibility(View.GONE);
            dialog.findViewById(R.id.dialog_ok_button).setOnClickListener(view2 -> {
                dialog.dismiss();
                permissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            });
        } else if(ContextCompat.checkSelfPermission(fragment.requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            // alert dialog to let user know that approximate location might not work the best and they should provide us fine location for best results
            AlertDialog.Builder builder = new AlertDialog.Builder(fragment.requireActivity());
            builder.setView(R.layout.custom_ok_alert_dialog);
            AlertDialog dialog = builder.create();
            dialog.show();
            // disables the user to cancel the given dialog
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ((AppCompatImageView)dialog.findViewById(R.id.dialog_drawable)).setImageResource(R.drawable.marker);

            ((TextView) dialog.findViewById(R.id.dialog_header)).setText(R.string.location_access_denied);
            ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.approximate_location_not_enough);
            dialog.findViewById(R.id.dialog_ok_button).setOnClickListener(view2 -> dialog.dismiss());
        } else if(ContextCompat.checkSelfPermission(fragment.requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
                // alert dialog to let user know we're requesting location permission
                AlertDialog.Builder builder = new AlertDialog.Builder(fragment.requireActivity());
                builder.setView(R.layout.custom_ok_alert_dialog);
                AlertDialog dialog = builder.create();
                dialog.show();
                // disables the user to cancel the given dialog
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                ((AppCompatImageView)dialog.findViewById(R.id.dialog_drawable)).setImageResource(R.drawable.marker);

                ((TextView) dialog.findViewById(R.id.dialog_header)).setText(R.string.background_location_access);
                ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.background_location_access_message);
                dialog.findViewById(R.id.dialog_ok_button).setOnClickListener(view2 -> {
                    dialog.dismiss();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        bPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                    } else
                        fragment.onActivityTrackingStart();
                });
        } else
                fragment.onActivityTrackingStart();
    }
}
