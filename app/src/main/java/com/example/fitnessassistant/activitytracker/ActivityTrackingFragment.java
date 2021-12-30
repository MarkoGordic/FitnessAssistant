package com.example.fitnessassistant.activitytracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.example.fitnessassistant.util.PermissionFunctional;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import kotlin.jvm.internal.markers.KMutableList;

public class ActivityTrackingFragment extends Fragment implements OnMapReadyCallback {

    // launcher for the Activity Recognition Permission
    public final ActivityResultLauncher<String[]> fineLocationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
        Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

        if(fineLocationGranted != null && fineLocationGranted)
            onActivityTrackingStart();
        else if (coarseLocationGranted != null && coarseLocationGranted){
            // alert dialog to let user know that approximate location might not work the best and they should provide us fine location for best results
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
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
        } else {
            // creates an alert dialog with rationale shown
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(R.layout.custom_ok_alert_dialog);
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ((AppCompatImageView)dialog.findViewById(R.id.dialog_drawable)).setImageResource(R.drawable.exclamation);

            ((TextView) dialog.findViewById(R.id.dialog_header)).setText(R.string.location_access_denied);
            dialog.findViewById(R.id.dialog_ok_button).setOnClickListener(view2 -> dialog.dismiss());

            // showing messages (one case if user selected don't ask again, other if user just selected deny)
            if(!shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION))
                ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.location_access_access_message_denied_forever);
            else
                ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.location_access_access_message_denied);

        }
    });

    private GoogleMap googleMap = null;
    private MapView mapView;
    // TODO ne mozes ih cuvati ovde, memory leak je, samo pozovi requireView().findViewById gde ti treba
//    private TextView distance;
//    private TextView speed;
//    private TextView averageSpeed;

    private final DecimalFormat distanceFormat = new DecimalFormat("#.##");
    private final DecimalFormat speedFormat = new DecimalFormat("#.#");

    private boolean isTracking = false;
    private Vector<Vector<LatLng>> pathHistory = new Vector<>();

    private final int polylineColor = Color.BLUE;
    private final float polylineWidth = 7f;

    private long currentTimeInMilliseconds = 0L;

    // method which is used to set up observers
    private void subscribeToObservers(){
        // For service updates
        LocationService.isTracking.observe(getViewLifecycleOwner(), this::updateTracking);

        // For map updates
        LocationService.pathHistory.observe(getViewLifecycleOwner(), newPath -> {
            if(newPath.get(newPath.size() - 1).get(newPath.get(newPath.size() - 1).size() - 1) != null) {
                pathHistory = newPath;
                addLatestPathToMap();
                focusUserOnMap();
            }
        });

        // For timer updates
        LocationService.timeInMilliseconds.observe(getViewLifecycleOwner(), aLong -> {
            currentTimeInMilliseconds = aLong;
            String formattedTime = ActivityTrackingFragment.getFormattedTimer(true, currentTimeInMilliseconds);
            ((TextView) requireView().findViewById(R.id.tvTimer)).setText(formattedTime);
        });

        // For distance updates
        LocationService.totalDistanceInKm.observe(getViewLifecycleOwner(), newDistance -> {

            // Can i use requireContext here ?
            if(PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("distanceUnit", "km").equals("km")){
                //distance.setText(String.valueOf(distanceFormat.format(newDistance)));
            }else{
                // In case user wants miles, we need to convert distance value
                //distance.setText(distanceFormat.format(newDistance * 0.621371));
            }
        });

        // For speed updates
        LocationService.currentSpeed.observe(getViewLifecycleOwner(), newSpeed -> {
            if(PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("speedUnit", "km").equals("km")){
                //speed.setText(String.valueOf(speedFormat.format(newSpeed)));
            }else{
                //speed.setText(speedFormat.format(newSpeed * 0.621371));
            }
        });

        // For average speed updates
        LocationService.averageSpeed.observe(getViewLifecycleOwner(), newAverageSpeed -> {
            if(PreferenceManager.getDefaultSharedPreferences(requireContext()).getString("speedUnit", "km").equals("km")){
                //averageSpeed.setText(String.valueOf(speedFormat.format(newAverageSpeed)));
            }else{
                //averageSpeed.setText(speedFormat.format(newAverageSpeed * 0.621371));
            }
        });
    }

    // TODO app crashes on resume service (probably Vector<>) and app crashes if permission is closed in app settings

    private void toggleActivityTracking(){
        if(isTracking) {
            updateLocationService("pause_service");
        } else {
            updateLocationService("start_or_resume_service");
        }
    }

    private void updateTracking(Boolean tracking){
        this.isTracking = tracking;

        if(getView() != null) {
            if (isTracking) {
                getView().findViewById(R.id.startButton).setVisibility(View.GONE);
                getView().findViewById(R.id.pauseButton).setVisibility(View.VISIBLE);
            } else {
                getView().findViewById(R.id.pauseButton).setVisibility(View.GONE);
                getView().findViewById(R.id.startButton).setVisibility(View.VISIBLE);
            }

            getView().findViewById(R.id.stopTracking).setOnClickListener(v -> {
                // TODO stopTracking functionality
            });
        }
    }

    private void focusUserOnMap(){
        if(!pathHistory.isEmpty() && !pathHistory.get(pathHistory.size() - 1).isEmpty()){
            float mapZoom = 18f;
            // TODO : FATAL EXCEPTION - Ubacivanje u vector nije dobro, potrebno je opet proveriti da li je sve kako treba
            googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            pathHistory.get(pathHistory.size() - 1).get(pathHistory.get(pathHistory.size() - 1).size() - 1),
                            mapZoom
                    )
            );
        }
    }

    private void addWholePathToMap(){
        for(int i = 0; i < pathHistory.size(); i++){
            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(polylineColor)
                    .width(polylineWidth)
                    .addAll(pathHistory.get(i));

            googleMap.addPolyline(polylineOptions);
        }
    }

    private void addLatestPathToMap(){
        if(!pathHistory.isEmpty() && pathHistory.get(pathHistory.size() - 1).size() > 1){
            LatLng preLastLatLng = pathHistory.get(pathHistory.size() - 1).get(pathHistory.get(pathHistory.size() - 1).size() - 2);
            LatLng lastLatLng = pathHistory.get(pathHistory.size() - 1).get(pathHistory.get(pathHistory.size() - 1).size() - 1);

            PolylineOptions polylineOptions = new PolylineOptions()
                    .color(polylineColor)
                    .width(polylineWidth)
                    .add(preLastLatLng)
                    .add(lastLatLng);

            googleMap.addPolyline(polylineOptions);
        }
    }

    private void onActivityTrackingPause(){
        isTracking = true;
        toggleActivityTracking();

        requireView().findViewById(R.id.startButton).setVisibility(View.GONE);
        requireView().findViewById(R.id.pauseButton).setVisibility(View.VISIBLE);
    }

    public void onActivityTrackingStart(){
        isTracking = false;
        toggleActivityTracking();

        requireView().findViewById(R.id.startButton).setVisibility(View.GONE);
        requireView().findViewById(R.id.pauseButton).setVisibility(View.VISIBLE);
    }

    private void setUpOnClickListeners(View view){
        // stopButton onClickListener
        view.findViewById(R.id.pauseButton).setOnClickListener(v -> onActivityTrackingPause());

        // startButton onClickListener
        view.findViewById(R.id.startButton).setOnClickListener(v -> PermissionFunctional.checkFineLocationPermission(this, fineLocationPermissionLauncher));

        // set up downClose on click listener
        view.findViewById(R.id.downClose).setOnClickListener(view1 -> Toast.makeText(requireContext(), R.string.swipe_down_to_close_the_map, Toast.LENGTH_SHORT).show());

        // set up downClose on touch listener (for swiping)
        view.findViewById(R.id.downClose).setOnTouchListener(new View.OnTouchListener() {
            // xCord and yCord of event registered
            float xCord;
            float yCord;
            // len, used to determine what the event actually was
            final float len = getResources().getDisplayMetrics().densityDpi / 6;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    // when button gets pressed, get cords of the click
                    xCord = event.getX();
                    yCord = event.getY();
                } else if(event.getAction() == MotionEvent.ACTION_UP){
                    // when button gets released, set cords to amount moved from ACTION_DOWN's click cords
                    xCord -= event.getX();
                    yCord -= event.getY();

                    // if it's a solid swipe DOWN (<)
                    if(yCord < len * 5)
                        requireActivity().onBackPressed();
                    else
                        view.performClick(); // call onClickListener
                }
                return false;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_screen, container, false);

        // TODO: Link distance TextView here
        // TODO: Link speed TextView here
        // TODO: Link average speed TextView here
        // TODO: After linking, uncomment lines on top of this class

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);

        setUpOnClickListeners(view);
        subscribeToObservers();

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        addWholePathToMap();
    }

    private void updateLocationService(String state){
        Intent serviceIntent = new Intent(getContext(), LocationService.class);
        serviceIntent.putExtra("state", state);
        requireContext().startService(serviceIntent);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        updateTracking(isTracking);
    }

    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public static String getFormattedTimer(boolean showMilliseconds, long totalTimeInMilliseconds){
        String formattedTimer = "";
        long totalTimeInMs = totalTimeInMilliseconds;
        long hours = TimeUnit.MILLISECONDS.toHours(totalTimeInMs);
        totalTimeInMs -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalTimeInMs);
        totalTimeInMs -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalTimeInMs);
        if(!showMilliseconds){
            if(hours < 10)
                formattedTimer += "0";
            formattedTimer += String.valueOf(hours);

            formattedTimer += ":";

            if(minutes < 10)
                formattedTimer += "0";
            formattedTimer += String.valueOf(minutes);

            formattedTimer += ":";

            if(seconds < 10)
                formattedTimer += "0";
            formattedTimer += String.valueOf(seconds);
        }
        else{
            if(hours < 10)
                formattedTimer += "0";
            formattedTimer += String.valueOf(hours);

            formattedTimer += ":";

            if(minutes < 10)
                formattedTimer += "0";
            formattedTimer += String.valueOf(minutes);

            formattedTimer += ":";

            if(seconds < 10)
                formattedTimer += "0";
            formattedTimer += String.valueOf(seconds);

            formattedTimer += ":";

            totalTimeInMs -= TimeUnit.SECONDS.toMillis(seconds);
            totalTimeInMs /= 10;

            if(totalTimeInMs < 10)
                formattedTimer += "0";
            formattedTimer += String.valueOf(totalTimeInMs);
        }

        return formattedTimer;
    }
}
