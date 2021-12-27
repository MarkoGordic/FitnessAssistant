package com.example.fitnessassistant.activitytracker;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.MutableLiveData;
import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.notifications.NotificationController;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Objects;
import java.util.Vector;

// TODO : When user clicks on foreground notification, he needs to be redirected directly to this fragment

public class LocationService extends LifecycleService {
    public static MutableLiveData<Boolean> isTracking = new MutableLiveData<>();
    public static MutableLiveData<Vector<Vector<LatLng>>> pathHistory = new MutableLiveData<>();
    private boolean isRunning = false;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private void addNewPathSegment(){
        if(pathHistory.getValue() != null) {
            Vector<Vector<LatLng>> temp = pathHistory.getValue();
            Vector<LatLng> temp1 = new Vector<>();
            temp1.add(null);
            temp.add(temp1);
            pathHistory.postValue(temp);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isTracking.postValue(true);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        isTracking.observe(this, aBoolean -> {
            if(isTracking.getValue() != null)
                updateTrackingStatus(isTracking.getValue());
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            List<Location> locations = locationResult.getLocations();

            // We proceed if tracking is currently on
            if(isTracking.getValue() != null) {
                if (isTracking.getValue()) {
                    // Adding every location since last call
                    for (int i = 0; i < locations.size(); i++) {
                        addNewPathPoint(locations.get(i));
                    }
                }
            }
        }
    };

    private void updateTrackingStatus(boolean isTracking){
        if (isTracking) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

        if(pathHistory.getValue() == null){
            Vector<Vector<LatLng>> temp = new Vector<>();
            Vector<LatLng> temp1 = new Vector<>();
            temp1.add(newPoint);
            temp.add(temp1);
            pathHistory.postValue(temp);
        }
        else{
            Vector<Vector<LatLng>> temp = pathHistory.getValue();
            Vector<LatLng> temp1 = temp.get(temp.size() - 1);
            if(temp1.get(temp1.size() - 1) == null){
                temp1.add(temp1.size() - 1, newPoint);
            } else{
                temp1.add(newPoint);
            }
            temp.add(temp1);
            pathHistory.postValue(temp);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (Objects.requireNonNull(intent).getStringExtra("state")){
            case "start_or_resume_service":
                if(!isRunning){
                    startForegroundService();
                    isRunning = true;
                } else {
                    startForegroundService();
                }
                break;
            case "pause_service":
                pauseService();
            case "stop_service":
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void startForegroundService(){
        addNewPathSegment();
        isTracking.postValue(true);

        Notification notification = pushActivityTrackingNotification(this, "Activity Tracking", "00:00:00");
        startForeground(27, notification);
    }

    private void pauseService(){
        isTracking.postValue(false);
    }

    // TODO Put logo, Add translation later
    public static Notification pushActivityTrackingNotification(Context context, String textTitle, String textContent){
        Intent intent = new Intent(context, InAppActivity.class);
        // TODO add intentExtra desiredFragment
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = NotificationController.createNotification(context, "ActivityTracking", textTitle, textContent, pendingIntent, false,true, false);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(27, notification);

        return notification;
    }

    @Nullable
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        super.onBind(intent);
        return null;
    }
}
