package com.example.fitnessassistant.map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.network.NetworkManager;
import com.example.fitnessassistant.pedometer.Pedometer;
import com.example.fitnessassistant.util.AuthFunctional;
import com.google.firebase.auth.FirebaseAuth;

public class MapPageFragment extends Fragment {
    // launcher for permissions
    private final ActivityResultLauncher<String> permissionResult = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if(result) // if permission is granted, button should not be visible
            requireView().findViewById(R.id.startPedometerButton).setVisibility(View.GONE);
    });
    private FirebaseAuth.AuthStateListener authListener;
    private NetworkManager networkManager;
    private Pedometer pedometer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.map_screen, container, false);

        // sets up OCListener for the button / hides the button for requesting permissions for pedometer
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            view.findViewById(R.id.startPedometerButton).setVisibility(View.VISIBLE);
            view.findViewById(R.id.startPedometerButton).setOnClickListener(view1 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // checks if the rationale should be shown
                    if(ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACTIVITY_RECOGNITION)){
                        // creates an alert dialog with rationale shown
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                        builder.setTitle(getString(R.string.activity_recognition_permission))
                                .setMessage(getString(R.string.activity_recognition_rationale))
                                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> permissionResult.launch(Manifest.permission.ACTIVITY_RECOGNITION));
                        builder.create().show();
                    } else
                        permissionResult.launch(Manifest.permission.ACTIVITY_RECOGNITION);
                } else // tell user he needs android (Q)10 for activity recognition
                    Toast.makeText(getContext(), getString(R.string.android_q_needed), Toast.LENGTH_LONG).show();
            });
        }

        // setting up pedometer
        pedometer = new Pedometer(requireContext(), view.findViewById(R.id.stepCountTextView));

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        networkManager = new NetworkManager(requireActivity().getApplication());

        // setting up listener for firebase
        authListener = firebaseAuth -> AuthFunctional.refreshUser(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        // registering this activity when user comes first time or returns
        networkManager.registerConnectionObserver(getActivity(), requireView().findViewById(R.id.mapScreen));
        // adding the listener for firebase to change the UI
        FirebaseAuth.getInstance().addAuthStateListener(authListener);
        // re-registering the pedometer sensor
        pedometer.reRegisterSensor();
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
