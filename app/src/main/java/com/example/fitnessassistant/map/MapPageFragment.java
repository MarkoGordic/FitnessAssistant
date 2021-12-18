package com.example.fitnessassistant.map;

import android.Manifest;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.pedometer.Pedometer;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MapPageFragment extends Fragment {
    // launcher for the permission
    public final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if(result) // if permission is granted, pedometer can start
            Toast.makeText(requireContext(), R.string.pedometer_can_start, Toast.LENGTH_LONG).show();
        else {
            // creates an alert dialog with rationale shown
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
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

            // redirects user to home
            ((BottomNavigationView) requireActivity().findViewById(R.id.bottomNavigation)).setSelectedItemId(R.id.home);
        }
    });
    private Pedometer pedometer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.map_screen, container, false);
        // setting up pedometer
        pedometer = new Pedometer(requireContext(), view.findViewById(R.id.stepCountTextView));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // re-registering the pedometer sensor
        pedometer.reRegisterSensor();
    }
}
