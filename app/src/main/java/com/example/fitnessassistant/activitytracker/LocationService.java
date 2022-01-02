package com.example.fitnessassistant.activitytracker;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.MutableLiveData;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Objects;
import java.util.Vector;

public class LocationService extends LifecycleService {
    private static final int ACTIVITY_TRACKING_ID = 27;
    public static MutableLiveData<Boolean> isTracking = new MutableLiveData<>();
    public static MutableLiveData<Vector<Vector<LatLng>>> pathHistory = new MutableLiveData<>();

    public static MutableLiveData<Long> timeInSeconds = new MutableLiveData<>();
    public static MutableLiveData<Long> timeInMilliseconds = new MutableLiveData<>();

    private LatLng lastLatLng = null;
    public static MutableLiveData<Double> totalDistanceInKm = new MutableLiveData<>();

    public static MutableLiveData<Float> currentSpeed = new MutableLiveData<>();
    public static MutableLiveData<Float> averageSpeed = new MutableLiveData<>();
    private int speedUpdateCount = 0;
    private float speedSum = 0f;

    private boolean isTimerEnabled = false;
    private long segmentTime = 0L;
    private long totalTime = 0L;
    private long startTime = 0L;
    private long lastSecondTimestamp = 0L;
    private final int timerDelayMs = 100;
    final Handler handler = new Handler();

    private boolean serviceRunning = false;
    public static boolean serviceKilled = false;
    private boolean createNewPath = false;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private void initializeVariables(){
        isTracking.postValue(true);
        timeInMilliseconds.postValue(0L);
        timeInSeconds.postValue(0L);
        totalDistanceInKm.postValue(0D);
        currentSpeed.postValue(0F);
        averageSpeed.postValue(0F);
    }

    private void resetVariables(){
        initializeVariables();
        pathHistory.postValue(new Vector<>());
        ActivityTrackingFragment.pathHistory = new Vector<>();
        lastSecondTimestamp = 0L;
        startTime = 0L;
        totalTime = 0L;
        segmentTime = 0L;
        isTimerEnabled = false;
        speedSum = 0f;
        speedUpdateCount = 0;
        lastLatLng = null;
    }

    private void calculateNewDistanceInKm(LatLng newLocation){
        if(lastLatLng != null) {
            double lat1 = lastLatLng.latitude;
            double lng1 = lastLatLng.longitude;
            double lat2 = newLocation.latitude;
            double lng2 = newLocation.longitude;

            final int R = 6371;
            // Radius of the earth in km
            double dLat = deg2rad(lat2 - lat1);
            // deg2rad below
            double dLon = deg2rad(lng2 - lng1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            // calculating final distance
            double distance = R * c;

            // Updating total distance
            if (totalDistanceInKm.getValue() != null)
                totalDistanceInKm.postValue(totalDistanceInKm.getValue() + distance);
        }
    }

    // method which converts degrees to radians
    private double deg2rad(double deg)
    {
        return deg * (Math.PI / 180);
    }

    // method for starting on screen stopwatch
    private void startTimer(){
        createNewPath = true;
        isTracking.postValue(true);
        startTime = System.currentTimeMillis();
        isTimerEnabled = true;

        // Starting background handle to calculate time
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(isTimerEnabled){
                    segmentTime = System.currentTimeMillis() - startTime;
                    timeInMilliseconds.postValue(totalTime + segmentTime);

                    if(timeInMilliseconds.getValue() != null && timeInSeconds.getValue() != null)
                        if(timeInMilliseconds.getValue() >= lastSecondTimestamp + 1000L){
                            timeInSeconds.postValue(timeInSeconds.getValue() + 1);
                            lastSecondTimestamp += 1000L;
                        }

                    handler.postDelayed(this, timerDelayMs);
                }else
                    totalTime += segmentTime;
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initializeVariables();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        isTracking.observe(this, aBoolean -> {
            if(isTracking.getValue() != null) {
                updateTrackingStatus(isTracking.getValue());
                updateNotification();
            }
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

    // method for adding new GPS point to path list
    private void addNewPathPoint(Location location){
        // converting location to LatLng variable
        LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());

        // calculating new distance
        calculateNewDistanceInKm(newPoint);

        // updating speed variables
        speedUpdateCount++;
        speedSum += location.getSpeed() * 3.6f;
        currentSpeed.postValue(location.getSpeed() * 3.6f);
        averageSpeed.postValue(speedSum / speedUpdateCount);

        // saving new point as last
        lastLatLng = newPoint;

        if(pathHistory.getValue() == null){
            Vector<Vector<LatLng>> temp = new Vector<>();
            Vector<LatLng> temp1 = new Vector<>();
            temp1.add(newPoint);
            temp.add(temp1);
            pathHistory.postValue(temp);
        }
        else{
            Vector<Vector<LatLng>> temp = pathHistory.getValue();
            if(createNewPath){
                Vector<LatLng> temp1 = new Vector<>();
                temp1.add(newPoint);
                temp.add(temp1);
                createNewPath = false;
            } else{
                Vector<LatLng> temp1 = temp.get(temp.size() - 1);
                temp1.add(newPoint);
            }
            pathHistory.postValue(temp);
        }
    }

    private void updateTrackingStatus(boolean isTracking){
        if (isTracking) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

    private void killService(){
        serviceKilled = true;
        serviceRunning = true;
        resetVariables();
        pauseService();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (Objects.requireNonNull(intent).getStringExtra("state")){
            case "start_or_resume_service":
                if(!serviceRunning){
                    startForegroundService();
                    serviceRunning = true;
                } else {
                    startTimer();
                }
                break;
            case "pause_service":
                pauseService();
                break;
            case "stop_service":
                killService();
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void startForegroundService(){
        serviceKilled = false;
        startTimer();
        isTracking.postValue(true);

        // Pushing base notification
        Notification notification = pushActivityTrackingNotification(this, null, "00:00:00");
        startForeground(ACTIVITY_TRACKING_ID, notification);

        timeInSeconds.observe(this, aLong -> {
            if(!serviceKilled)
                updateNotification();
        });
    }

    private void pauseService(){
        isTracking.postValue(false);
        isTimerEnabled = false;
    }

    private void updateNotification(){
        PendingIntent pendingIntent;

        if(isTracking.getValue() != null && timeInMilliseconds.getValue() != null && !serviceKilled)
            if(isTracking.getValue()) {
                Intent pauseIntent = new Intent(this, LocationService.class);
                pauseIntent.putExtra("state", "pause_service");
                pendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                pushActivityTrackingNotification(this, pendingIntent, ActivityTrackingFragment.getFormattedTimer(false, timeInMilliseconds.getValue()));
            }else{
                Intent resumeIntent = new Intent(this, LocationService.class);
                resumeIntent.putExtra("state", "start_or_resume_service");
                pendingIntent = PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                pushActivityTrackingNotification(this, pendingIntent, ActivityTrackingFragment.getFormattedTimer(false, timeInMilliseconds.getValue()));
            }
    }

    // TODO Put logo
    private Notification pushActivityTrackingNotification(Context context, PendingIntent action, String contentText){
        Intent intent = new Intent(context, InAppActivity.class);
        intent.putExtra("desiredFragment", "ActivityTrackingFragment");
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, ACTIVITY_TRACKING_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "ActivityTracking")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(false)
                .setOngoing(true)
                // TODO translate this, and handle restarting on locale change
                .setContentTitle("FitnessAssistant Tracking")
                .setContentText(contentText)
                .setContentIntent(pendingIntent)
                .setShowWhen(false);

        // icons get displayed in Android versions < 7,... we can never see them
        if(action != null) {
            notificationBuilder.clearActions();
            if (isTracking.getValue() != null)
                if (isTracking.getValue())
                    notificationBuilder.addAction(R.drawable.pause, context.getText(R.string.pause_activity_tracking), action);
                else
                    notificationBuilder.addAction(R.drawable.play, context.getText(R.string.resume_activity_tracking), action);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if(!serviceKilled)
            notificationManager.notify(ACTIVITY_TRACKING_ID, notificationBuilder.build());

        return notificationBuilder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(@NonNull Intent intent) {
        super.onBind(intent);
        return null;
    }
}
