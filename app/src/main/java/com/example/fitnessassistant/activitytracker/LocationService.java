package com.example.fitnessassistant.activitytracker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.notifications.NotificationController;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

// TODO : When user clicks on foreground notification, he needs to be redirected directly to this fragment

public class LocationService extends Service {
    MutableLiveData<Boolean> isTracking = new MutableLiveData<>();
    MutableLiveData<ArrayList<LatLng>> pathHistory = new MutableLiveData<>();
    private boolean isRunning = false;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private void initializeData(){
        isTracking.postValue(true);
        pathHistory.postValue(new ArrayList<>());
    }

    /*@Override
    public void onCreate() {
        super.onCreate();
        initializeData();
        fusedLocationProviderClient = new FusedLocationProviderClient(this);

        isTracking.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            List<Location> locations = locationResult.getLocations();

            // We proceed if tracking is currently on
            if(isTracking){
                // Adding every location since last call
                for(int i = 0; i < locations.size(); i++){
                    addNewPathPoint(locations.get(i));
                }
            }
        }
    };

    // !! IMPORTANT, Android Studio usually even after checking permission thick we haven't check
    //               we need to suppress that warning using SuppressLint
    @SuppressLint("MissingPermission")
    private void updateTrackingStatus(boolean isTracking){
        if(this.isTracking){
            // TODO : Check if we have location permissions
            if(true){
                // Interval between location updates
                int updateInterval = 5000;
                int fastestUpdateInterval = 2000;
                LocationRequest request = LocationRequest
                        .create()
                        .setInterval(updateInterval)
                        .setFastestInterval(fastestUpdateInterval)
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                fusedLocationProviderClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    // method for adding new GPS point to path list
    private void addNewPathPoint(Location location){
        // converting location to LatLng variable
        LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());

        pathHistory.get(pathHistory.size() - 1).add(newPoint);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getStringExtra("state")){
            case "start_or_resume_service":
                if(!isRunning){
                    startForegroundService();
                    isRunning = true;
                }
                break;
            case "pause_service":
            case "stop_service":
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void startForegroundService(){
        pathHistory.add(new ArrayList<>());

        Notification notification = pushActivityTrackingNotification(this, "Activity Tracking", "00:00:00");
        startForeground(27, notification);
    }*/

    // TODO Put logo, Add translation later
    public static Notification pushActivityTrackingNotification(Context context, String textTitle, String textContent){
        Intent intent = new Intent(context, InAppActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = NotificationController.createNotification(context, "ActivityTracking", textTitle, textContent, pendingIntent, false,true, false);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(27, notification);

        return notification;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
