package com.example.fitnessassistant.activitytracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class ActivityTrackingFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap = null;
    private MapView mapView;
    private TextView stopwatch = null;

    private boolean isTracking = false;
    private Vector<Vector<LatLng>> pathHistory = new Vector<>();

    private final int polylineColor = Color.BLUE;
    private final float polylineWidth = 7f;

    private long currentTimeInMilliseconds = 0L;

    // method which is used to set up observers
    private void subscribeToObservers(){
        LocationService.isTracking.observe(getViewLifecycleOwner(), this::updateTracking);

        LocationService.pathHistory.observe(getViewLifecycleOwner(), newPath -> {
            pathHistory = newPath;
            addLatestPathToMap();
            focusUserOnMap();
        });

        LocationService.timeInMilliseconds.observe(getViewLifecycleOwner(), aLong -> {
            currentTimeInMilliseconds = aLong;
            String formattedTime = ActivityTrackingFragment.getFormattedTimer(true, currentTimeInMilliseconds);
            stopwatch.setText(formattedTime);
        });
    }

    private void toggleActivityTracking(){
        if(isTracking) {
            updateLocationService("pause_service");
        } else {
            updateLocationService("start_or_resume_service");
        }
    }

    // TODO ovo se moze koristiti samo ukoliko smo stalno u app i na ovom fragmentu, u suprotnom ces morati da se igras sa sharedPreferences :/
    private void updateTracking(Boolean tracking){
        this.isTracking = tracking;

        /*if(isTracking){
            requireView().findViewById(R.id.startButton).setVisibility(View.INVISIBLE);
        }
        else {
            requireView().findViewById(R.id.pauseButton).setVisibility(View.INVISIBLE);
        }*/

        requireView().findViewById(R.id.stopTracking).setOnClickListener(v -> {
            // TODO stopTracking functionality
        });
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_screen, container, false);

        stopwatch = view.findViewById(R.id.tvTimer);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);

        // TODO : Find better solution for this
        view.findViewById(R.id.pauseButton).setOnClickListener(v -> {
            isTracking = true;
            toggleActivityTracking();
        });
        view.findViewById(R.id.startButton).setOnClickListener(v -> {
            isTracking = false;
            toggleActivityTracking();
        });

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
