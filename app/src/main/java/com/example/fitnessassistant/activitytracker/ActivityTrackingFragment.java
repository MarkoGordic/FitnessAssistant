package com.example.fitnessassistant.activitytracker;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.database.RealtimeDB;
import com.example.fitnessassistant.database.mdbh.MDBHActivityTracker;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;
import com.example.fitnessassistant.util.PermissionFunctional;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Calendar;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class ActivityTrackingFragment extends Fragment implements OnMapReadyCallback {

    // launcher for the Location Permission
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
                ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.location_access_message_denied_forever);
            else
                ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.location_access_message_denied);

        }
    });
    // launcher for Background Location Permission
    ActivityResultLauncher<String> backgroundLocationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> onActivityTrackingStart());

    private GoogleMap googleMap = null;
    private MapView mapView;

    private boolean followUser = true;

    private boolean isTracking = false;
    public static Vector<Vector<LatLng>> pathHistory = new Vector<>();

    private final int polylineColor = Color.BLUE;
    private final float polylineWidth = 8f;

    private long currentTimeInMilliseconds = 0L;

    private AlertDialog alertDialog = null;

    // method which is used to set up observers
    @SuppressLint("DefaultLocale")
    private void subscribeToObservers(){
        LocationService.shouldShowAccuracyAlert.observe(getViewLifecycleOwner(), show -> {
            if(show){
                if(alertDialog == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                    builder.setView(R.layout.loading_dialog);
                    alertDialog = builder.create();
                    alertDialog.show();

                    alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    ((AppCompatImageView) alertDialog.findViewById(R.id.dialog_drawable)).setImageResource(R.drawable.marker);

                    ((TextView) alertDialog.findViewById(R.id.dialog_header)).setText(R.string.location_accuracy);
                    ((TextView) alertDialog.findViewById(R.id.dialog_message)).setText(R.string.location_accuracy_message);

                    ((Button) alertDialog.findViewById(R.id.dialog_button)).setText(R.string.force_start);
                    alertDialog.findViewById(R.id.dialog_button).setOnClickListener(view2 -> {
                        alertDialog.dismiss();
                        LocationService.shouldStart.postValue(true);
                        LocationService.serviceRunning = true;

                        LocationService.shouldShowAccuracyAlert.postValue(false);
                    });
                } else if(!alertDialog.isShowing()){
                    alertDialog.show();
                }
            } else {
                if (alertDialog != null)
                    alertDialog.dismiss();
            }
        });

        LocationService.accuracy.observe(getViewLifecycleOwner(), this::changeAccuracy);

        // For service updates
        LocationService.isTracking.observe(getViewLifecycleOwner(), this::updateTracking);

        // For map updates
        LocationService.pathHistory.observe(getViewLifecycleOwner(), newPath -> {
            if(!LocationService.serviceKilled){
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
            if(UnitPreferenceFragment.getDistanceUnit(requireContext()).equals(UnitPreferenceFragment.DISTANCE_UNIT_MILE)){
                ((TextView) requireView().findViewById(R.id.distanceUnit)).setText(requireContext().getString(R.string.mi));
                ((TextView) requireView().findViewById(R.id.distanceTraveled)).setText(String.format("%.2f", newDistance * 0.621371f));
            }else{
                ((TextView) requireView().findViewById(R.id.distanceUnit)).setText(requireContext().getString(R.string.km));
                ((TextView) requireView().findViewById(R.id.distanceTraveled)).setText(String.format("%.2f", newDistance));
            }
        });

        // For speed updates
        LocationService.currentSpeed.observe(getViewLifecycleOwner(), newSpeed -> {
            if(UnitPreferenceFragment.getDistanceUnit(requireContext()).equals(UnitPreferenceFragment.DISTANCE_UNIT_MILE)){
                ((TextView) requireView().findViewById(R.id.currentSpeedUnit)).setText(requireContext().getString(R.string.mi_h));
                ((TextView) requireView().findViewById(R.id.currentSpeed)).setText(String.format("%.1f", newSpeed * 0.621371f));
            }else{
                ((TextView) requireView().findViewById(R.id.currentSpeedUnit)).setText(requireContext().getString(R.string.km_h));
                ((TextView) requireView().findViewById(R.id.currentSpeed)).setText(String.format("%.1f", newSpeed));
            }
        });

        // For average speed updates
        LocationService.averageSpeed.observe(getViewLifecycleOwner(), newAverageSpeed -> {
            if(UnitPreferenceFragment.getDistanceUnit(requireContext()).equals(UnitPreferenceFragment.DISTANCE_UNIT_MILE)){
                ((TextView) requireView().findViewById(R.id.averageSpeedUnit)).setText(requireContext().getString(R.string.mi_h));
                ((TextView) requireView().findViewById(R.id.averageSpeed)).setText(String.format("%.1f", newAverageSpeed * 0.621371f));
            }else{
                ((TextView) requireView().findViewById(R.id.averageSpeedUnit)).setText(requireContext().getString(R.string.km_h));
                ((TextView) requireView().findViewById(R.id.averageSpeed)).setText(String.format("%.1f", newAverageSpeed));
            }
        });

        // For calories updates
        LocationService.caloriesBurnt.observe(getViewLifecycleOwner(), newCalories -> {
            if(UnitPreferenceFragment.getEnergyUnit(requireContext()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ)){
                ((TextView) requireView().findViewById(R.id.energyUnit)).setText(requireContext().getString(R.string.kj));
                ((TextView) requireView().findViewById(R.id.caloriesBurned)).setText(String.format("%.1f" , newCalories * 4.2f));
            }else{
                ((TextView) requireView().findViewById(R.id.energyUnit)).setText(requireContext().getString(R.string.cal));
                ((TextView) requireView().findViewById(R.id.caloriesBurned)).setText(String.valueOf(Math.round(newCalories)));
            }
        });

        // For pace updates
        LocationService.pace.observe(getViewLifecycleOwner(), newPace -> {
            if(UnitPreferenceFragment.getDistanceUnit(requireContext()).equals(UnitPreferenceFragment.DISTANCE_UNIT_MILE)){
                ((TextView) requireView().findViewById(R.id.paceUnit)).setText(requireContext().getString(R.string.mi_min));
                ((TextView) requireView().findViewById(R.id.currentPace)).setText(String.format("%.2f", newPace * 0.621371f));
            }else{
                ((TextView) requireView().findViewById(R.id.paceUnit)).setText(requireContext().getString(R.string.km_min));
                ((TextView) requireView().findViewById(R.id.currentPace)).setText(String.format("%.2f", newPace));
            }
        });

        // For average pace updates
        LocationService.averagePace.observe(getViewLifecycleOwner(), newAveragePace -> {
            if(UnitPreferenceFragment.getDistanceUnit(requireContext()).equals(UnitPreferenceFragment.DISTANCE_UNIT_MILE)){
                ((TextView) requireView().findViewById(R.id.averagePaceUnit)).setText(requireContext().getString(R.string.mi_min));
                ((TextView) requireView().findViewById(R.id.averagePace)).setText(String.format("%.2f", newAveragePace * 0.621371f));
            }else{
                ((TextView) requireView().findViewById(R.id.averagePaceUnit)).setText(requireContext().getString(R.string.km_min));
                ((TextView) requireView().findViewById(R.id.averagePace)).setText(String.format("%.2f", newAveragePace));
            }
        });

    }

    private void toggleActivityTracking(){
        if(isTracking) {
            updateLocationService("pause_service");
        } else {
            updateLocationService("start_or_resume_service");
        }
    }

    private void promptCancelTrackingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(R.layout.cancel_tracking_dialog);
        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Drawable walk = AppCompatResources.getDrawable(requireActivity(), R.drawable.walk);
        if(walk != null)
            walk.setTint(requireActivity().getColor(R.color.SpaceCadet));
        ((AppCompatImageView) dialog.findViewById(R.id.dialog_drawable)).setImageDrawable(walk);

        dialog.findViewById(R.id.dialog_exit_save_button).setOnClickListener(v -> {
            dialog.dismiss();
            forceFocusPathOnMap();
            exitAndSaveActivity();
        });

        dialog.findViewById(R.id.dialog_exit_button).setOnClickListener(v -> {
            dialog.dismiss();
            stopActivity();
        });

        dialog.findViewById(R.id.dialog_cancel_button).setOnClickListener(v -> dialog.dismiss());
    }

    private void stopActivity(){
        updateLocationService("stop_service");
        requireActivity().onBackPressed();
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
        }
    }

    private void focusUserOnMap(){
        if(!pathHistory.isEmpty() && !pathHistory.get(pathHistory.size() - 1).isEmpty() && followUser && !LocationService.serviceKilled && googleMap != null){
            float mapZoom = 18f;
            googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            pathHistory.get(pathHistory.size() - 1).get(pathHistory.get(pathHistory.size() - 1).size() - 1),
                            mapZoom
                    )
            );
        }
    }

    private void forceFocusPathOnMap(){
        if(!LocationService.serviceKilled && pathHistory.size() >= 1) {
            LatLngBounds.Builder pathBounds = new LatLngBounds.Builder();
            for (int i = 0; i < pathHistory.size(); i++) {
                for (int j = 0; j < pathHistory.get(i).size(); j++) {
                    pathBounds.include(pathHistory.get(i).get(j));
                }
            }

            googleMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                            pathBounds.build(),
                            mapView.getWidth(),
                            mapView.getHeight(),
                            (int) (mapView.getHeight() * 0.05f)
                    )
            );
        }
    }

    private void focusPathOnMap(){
        if(!LocationService.serviceKilled && pathHistory.size() >= 1) {
            LatLngBounds.Builder pathBounds = new LatLngBounds.Builder();
            for (int i = 0; i < pathHistory.size(); i++) {
                for (int j = 0; j < pathHistory.get(i).size(); j++) {
                    pathBounds.include(pathHistory.get(i).get(j));
                }
            }

            googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(
                            pathBounds.build(),
                            mapView.getWidth(),
                            mapView.getHeight(),
                            (int) (mapView.getHeight() * 0.05f)
                    )
            );
        }
    }

    private void exitAndSaveActivity(){
        googleMap.snapshot(bitmap->{
            if(bitmap != null){
                long dateRecorded = Calendar.getInstance().getTimeInMillis();
                float averageSpeed = 0f;
                double distance = 0f;
                int calories = 0;

                if(LocationService.averageSpeed.getValue() != null)
                    averageSpeed = LocationService.averageSpeed.getValue();

                if(LocationService.totalDistanceInKm.getValue() != null)
                    distance = LocationService.totalDistanceInKm.getValue();

                MDBHActivityTracker myDB = new MDBHActivityTracker(requireContext());
                myDB.addNewActivity(dateRecorded, averageSpeed, distance, calories, bitmap);
            }

            stopActivity();
        });
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
        if(!pathHistory.isEmpty() && pathHistory.get(pathHistory.size() - 1).size() > 1 && googleMap != null){
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

    private void revealDown(View view){
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0.0f);
        view.animate()
                .translationY(0)
                .alpha(1.0f)
                .setListener(null);
    }

    private void hideUp(View view){
        view.animate()
                .alpha(0.0f)
                .translationY(-view.getHeight())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE);
                    }
                });
    }

    private void revealUp(View view){
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0.0f);
        view.animate()
                .translationY(0)
                .alpha(1.0f)
                .setListener(null);
    }

    private void hideDown(View view){
        view.animate()
                .alpha(0.0f)
                .translationY(view.getHeight())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE);
                    }
                });
    }

    private void changeAccuracy(float accuracy){
        if(getView() != null) {
            if (accuracy <= 10) {
                ((AppCompatImageButton) getView().findViewById(R.id.accuracyButton)).setColorFilter(requireContext().getColor(R.color.Green));
                getView().findViewById(R.id.accuracyExplanation).getBackground().setTint(requireContext().getColor(R.color.Green));
            } else if (accuracy <= 30) {
                ((AppCompatImageButton) getView().findViewById(R.id.accuracyButton)).setColorFilter(requireContext().getColor(R.color.Yellow));
                getView().findViewById(R.id.accuracyExplanation).getBackground().setTint(requireContext().getColor(R.color.Yellow));
            } else {
                ((AppCompatImageButton) getView().findViewById(R.id.accuracyButton)).setColorFilter(requireContext().getColor(R.color.Red));
                getView().findViewById(R.id.accuracyExplanation).getBackground().setTint(requireContext().getColor(R.color.Red));
            }
        }
    }

    private void setUpOnClickListeners(View view){
        // stopButton onClickListener
        view.findViewById(R.id.pauseButton).setOnClickListener(v -> onActivityTrackingPause());

        view.findViewById(R.id.accuracyButton).setOnClickListener(v -> {
            view.findViewById(R.id.accuracyButton).setClickable(false);
            Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
            fadeOut.setDuration(500);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {
                    if(view.findViewById(R.id.accuracyExplanation) != null) {
                        view.findViewById(R.id.accuracyExplanation).setVisibility(View.GONE);
                        view.findViewById(R.id.accuracyButton).setClickable(true);
                    }
                }
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
            fadeIn.setDuration(500);
            fadeIn.setFillAfter(true);
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    if(view.findViewById(R.id.accuracyExplanation) != null) {
                        new Handler().postDelayed(() -> {
                            if (view.findViewById(R.id.accuracyExplanation) != null) {
                                view.findViewById(R.id.accuracyExplanation).startAnimation(fadeOut);
                            }
                        }, 1500);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.findViewById(R.id.accuracyExplanation).setVisibility(View.VISIBLE);
            view.findViewById(R.id.accuracyExplanation).startAnimation(fadeIn);
        });

        // startButton onClickListener
        view.findViewById(R.id.startButton).setOnClickListener(v -> PermissionFunctional.checkFineLocationPermission(this, fineLocationPermissionLauncher, backgroundLocationPermissionLauncher));

        // set up downClose on click listener
        view.findViewById(R.id.downClose).setOnClickListener(view1 -> requireActivity().onBackPressed());

        // set up downClose on touch listener (for swiping)
        view.findViewById(R.id.downClose).setOnTouchListener((view1, event) -> {
            if(event.getAction() == MotionEvent.ACTION_UP)
                view1.performClick(); // call onClickListener
            return false;
        });

        view.findViewById(R.id.stopTracking).setOnClickListener(v -> {
            // TODO stopTracking functionality
            promptCancelTrackingDialog();
        });

        // set up layoutClose on touch listener (for swiping)
        view.findViewById(R.id.layoutClose).setOnTouchListener(new View.OnTouchListener() {
            // xCord and yCord of event registered
            float xCord;
            float yCord;
            // len, used to determine what the event actually was
            final float len = getResources().getDisplayMetrics().densityDpi / 6f;
            // this is used to determine if the layout is closed or not
            boolean closedLayout = false;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    // when button gets pressed, get cords of the click
                    xCord = event.getX();
                    yCord = event.getY();
                } else if(event.getAction() == MotionEvent.ACTION_UP) {
                    // when button gets released, set cords to amount moved from ACTION_DOWN's click cords
                    xCord -= event.getX();
                    yCord -= event.getY();

                    // if layout is not closed and user clicks/swipes down
                    if (!closedLayout && yCord < len) {
                        closedLayout = true;
                        hideDown(requireView().findViewById(R.id.allStats));
                        ((ImageView) view).setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.up));
                    } else if (closedLayout && yCord > -len){
                        closedLayout = false;
                        revealUp(requireView().findViewById(R.id.allStats));
                        ((ImageView) view).setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.down));
                    }
                    view.performClick();
                }
                return false;
            }
        });

        view.findViewById(R.id.centerButton).setOnClickListener(v -> {
            followUser = true;
            focusUserOnMap();
        });

        view.findViewById(R.id.wholePathButton).setOnClickListener(v -> {
            //followUser = false;
            //focusPathOnMap();
            RealtimeDB.restoreUserActivities(requireContext());
        });

        view.findViewById(R.id.mapTypeButton).setOnClickListener(new View.OnClickListener() {
            boolean choosingMapType = false;
            @Override
            public void onClick(View v) {
                if(!choosingMapType){
                    choosingMapType = true;
                    revealDown(view.findViewById(R.id.mapTypeLayout));
                } else{
                    choosingMapType = false;
                    hideUp(view.findViewById(R.id.mapTypeLayout));
                }
            }
        });

        Drawable roadmap = AppCompatResources.getDrawable(requireActivity(), R.drawable.default_map);
        Drawable satellite = AppCompatResources.getDrawable(requireActivity(), R.drawable.globe);
        Drawable terrain = AppCompatResources.getDrawable(requireActivity(), R.drawable.mountains);
        if(roadmap != null)
            roadmap.setTint(requireActivity().getColor(R.color.MangoTango));
        if(satellite != null)
            satellite.setTint(requireActivity().getColor(R.color.SpaceCadet));
        if(terrain != null)
            terrain.setTint(requireActivity().getColor(R.color.SpaceCadet));

        ((AppCompatImageButton) view.findViewById(R.id.roadmapButton)).setImageDrawable(roadmap);
        ((AppCompatImageButton) view.findViewById(R.id.satelliteButton)).setImageDrawable(satellite);
        ((AppCompatImageButton) view.findViewById(R.id.terrainButton)).setImageDrawable(terrain);

        view.findViewById(R.id.roadmapButton).setOnClickListener(v -> {
            if(googleMap.getMapType() != GoogleMap.MAP_TYPE_NORMAL) {
                if (roadmap != null)
                    roadmap.setTint(requireActivity().getColor(R.color.MangoTango));
                if (satellite != null)
                    satellite.setTint(requireActivity().getColor(R.color.SpaceCadet));
                if (terrain != null)
                    terrain.setTint(requireActivity().getColor(R.color.SpaceCadet));

                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                ((AppCompatImageButton) view.findViewById(R.id.roadmapButton)).setImageDrawable(roadmap);
                ((AppCompatImageButton) view.findViewById(R.id.satelliteButton)).setImageDrawable(satellite);
                ((AppCompatImageButton) view.findViewById(R.id.terrainButton)).setImageDrawable(terrain);
            }
        });

        view.findViewById(R.id.satelliteButton).setOnClickListener(v -> {
            if(googleMap.getMapType() != GoogleMap.MAP_TYPE_SATELLITE) {
                if (roadmap != null)
                    roadmap.setTint(requireActivity().getColor(R.color.SpaceCadet));
                if (satellite != null)
                    satellite.setTint(requireActivity().getColor(R.color.MangoTango));
                if (terrain != null)
                    terrain.setTint(requireActivity().getColor(R.color.SpaceCadet));

                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                ((AppCompatImageButton) view.findViewById(R.id.roadmapButton)).setImageDrawable(roadmap);
                ((AppCompatImageButton) view.findViewById(R.id.satelliteButton)).setImageDrawable(satellite);
                ((AppCompatImageButton) view.findViewById(R.id.terrainButton)).setImageDrawable(terrain);
            }
        });

        view.findViewById(R.id.terrainButton).setOnClickListener(v -> {
            if(googleMap.getMapType() != GoogleMap.MAP_TYPE_TERRAIN) {
                if (roadmap != null)
                    roadmap.setTint(requireActivity().getColor(R.color.SpaceCadet));
                if (satellite != null)
                    satellite.setTint(requireActivity().getColor(R.color.SpaceCadet));
                if (terrain != null)
                    terrain.setTint(requireActivity().getColor(R.color.MangoTango));

                googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                ((AppCompatImageButton) view.findViewById(R.id.roadmapButton)).setImageDrawable(roadmap);
                ((AppCompatImageButton) view.findViewById(R.id.satelliteButton)).setImageDrawable(satellite);
                ((AppCompatImageButton) view.findViewById(R.id.terrainButton)).setImageDrawable(terrain);
            }
        });

        view.findViewById(R.id.spotifyButton).setOnClickListener(v -> openSpotifyApp());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_screen, container, false);

        requireActivity().findViewById(R.id.bottomNavigation).setVisibility(View.GONE);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);

        setUpOnClickListeners(view);
        subscribeToObservers();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        requireActivity().findViewById(R.id.bottomNavigation).setVisibility(View.VISIBLE);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;

        // Setting up map listeners
        googleMap.setOnCameraMoveStartedListener(reason -> {
            if(reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE || reason == GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION)
                followUser = false;
        });

        addWholePathToMap();
        focusUserOnMap();
    }

    private void updateLocationService(String state){
        Intent serviceIntent = new Intent(getContext(), LocationService.class);
        serviceIntent.putExtra("state", state);
        requireContext().startService(serviceIntent);
    }

    // TODO check if more precise Exceptions could be caught
    private void openSpotifyApp(){
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(new ComponentName("com.spotify.music","com.spotify.music.MainActivity"));
            startActivity(intent);
        } catch (Exception e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=")));
            }catch (android.content.ActivityNotFoundException anfe){
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.spotify.music")));
            }
        }
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

        if(!LocationService.serviceKilled && googleMap != null){
            addWholePathToMap();
            focusUserOnMap();
        }
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
