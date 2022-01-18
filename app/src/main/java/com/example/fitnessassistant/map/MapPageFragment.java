package com.example.fitnessassistant.map;

import static com.example.fitnessassistant.util.TimeFunctional.getMonthShort;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.activitytracker.ActivityTrackingFragment;
import com.example.fitnessassistant.activitytracker.LocationService;
import com.example.fitnessassistant.database.mdbh.MDBHActivityTracker;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class MapPageFragment extends Fragment {
    public static final AtomicBoolean activityChosen = new AtomicBoolean(false);
    public static final int RUNNING_SELECTED = 1;
    public static final int WALKING_SELECTED = 2;
    public static final int CYCLING_SELECTED = 3;
    public static final int NONE_SELECTED = 0;


    public void goToActivityTrackingFragment(){
        requireActivity().getSupportFragmentManager().beginTransaction().hide(this).setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down).add(R.id.in_app_container, InAppActivity.activityTrackingFragment).addToBackStack(null).commit();
    }

    private void setSelected(View view, int selected){
        AppCompatImageButton walking = view.findViewById(R.id.walkingActivity);
        AppCompatImageButton running = view.findViewById(R.id.runningActivity);
        AppCompatImageButton cycling = view.findViewById(R.id.cyclingActivity);
        switch(selected){
            case RUNNING_SELECTED:
                ViewCompat.setBackgroundTintList(running, ContextCompat.getColorStateList(requireActivity(), R.color.SpaceCadet));
                ViewCompat.setBackgroundTintList(walking, ContextCompat.getColorStateList(requireActivity(), R.color.LightGrayColor));
                ViewCompat.setBackgroundTintList(cycling, ContextCompat.getColorStateList(requireActivity(), R.color.LightGrayColor));
                break;
            case WALKING_SELECTED:
                ViewCompat.setBackgroundTintList(walking, ContextCompat.getColorStateList(requireActivity(), R.color.SpaceCadet));
                ViewCompat.setBackgroundTintList(running, ContextCompat.getColorStateList(requireActivity(), R.color.LightGrayColor));
                ViewCompat.setBackgroundTintList(cycling, ContextCompat.getColorStateList(requireActivity(), R.color.LightGrayColor));
                break;
            case CYCLING_SELECTED:
                ViewCompat.setBackgroundTintList(cycling, ContextCompat.getColorStateList(requireActivity(), R.color.SpaceCadet));
                ViewCompat.setBackgroundTintList(running, ContextCompat.getColorStateList(requireActivity(), R.color.LightGrayColor));
                ViewCompat.setBackgroundTintList(walking, ContextCompat.getColorStateList(requireActivity(), R.color.LightGrayColor));
                break;
            case NONE_SELECTED:
                ViewCompat.setBackgroundTintList(walking, ContextCompat.getColorStateList(requireActivity(), R.color.LightGrayColor));
                ViewCompat.setBackgroundTintList(running, ContextCompat.getColorStateList(requireActivity(), R.color.LightGrayColor));
                ViewCompat.setBackgroundTintList(cycling, ContextCompat.getColorStateList(requireActivity(), R.color.LightGrayColor));
                break;
        }
    }

    private void setUpOnClickListeners(View view){
        view.findViewById(R.id.walkingActivity).setOnClickListener(v -> {
            if(!activityChosen.get()) {
                setSelected(view, WALKING_SELECTED);
                goToActivityTrackingFragment();
                ActivityTrackingFragment.activityType = WALKING_SELECTED;
            } else if(ActivityTrackingFragment.activityType == WALKING_SELECTED){
                goToActivityTrackingFragment();
            }
        });

        view.findViewById(R.id.runningActivity).setOnClickListener(v -> {
            if(!activityChosen.get()) {
                setSelected(view, RUNNING_SELECTED);
                goToActivityTrackingFragment();
                ActivityTrackingFragment.activityType = RUNNING_SELECTED;
            } else if(ActivityTrackingFragment.activityType == RUNNING_SELECTED){
                goToActivityTrackingFragment();
            }
        });

        view.findViewById(R.id.cyclingActivity).setOnClickListener(v -> {
            if(!activityChosen.get()) {
                setSelected(view, CYCLING_SELECTED);
                goToActivityTrackingFragment();
                ActivityTrackingFragment.activityType = CYCLING_SELECTED;
            } else if(ActivityTrackingFragment.activityType == CYCLING_SELECTED){
                goToActivityTrackingFragment();
            }
        });

        view.findViewById(R.id.showAll).setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, InAppActivity.activityRecyclerFragment).addToBackStack(null).commit());

        view.findViewById(R.id.showAll).setOnTouchListener(new View.OnTouchListener() {
            float yCord;
            final float len = getResources().getDisplayMetrics().densityDpi / 6f;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    yCord = event.getY();
                } else if(event.getAction() == MotionEvent.ACTION_UP){
                    yCord -= event.getY();

                    // if it's a solid swipe DOWN
                    if(yCord < -len * 4)
                        view.performClick();
                }
                return false;
            }
        });
    }

    @SuppressLint("DefaultLocale")
    private void setUpData(View view){
        if(getActivity() != null) {
            float[] totals = MDBHActivityTracker.getInstance(getActivity()).getTotals();

            float totalCaloriesBurnt = totals[0];
            float totalRunningDistance = totals[1];
            float totalWalkingDistance = totals[2];
            float totalCyclingDistance = totals[3];

            if(UnitPreferenceFragment.getDistanceUnit(getActivity()).equals(UnitPreferenceFragment.DISTANCE_UNIT_MILE)){
                ((TextView) view.findViewById(R.id.totalWalkDistance)).setText(String.format("%.1f\n%s", totalWalkingDistance * 0.621371f, getActivity().getString(R.string.mi)));
                ((TextView) view.findViewById(R.id.totalRunDistance)).setText(String.format("%.1f\n%s", totalRunningDistance * 0.621371f, getActivity().getString(R.string.mi)));
                ((TextView) view.findViewById(R.id.totalCycleDistance)).setText(String.format("%.1f\n%s", totalCyclingDistance * 0.621371f, getActivity().getString(R.string.mi)));
            } else{
                ((TextView) view.findViewById(R.id.totalWalkDistance)).setText(String.format("%.1f\n%s", totalWalkingDistance, getActivity().getString(R.string.km)));
                ((TextView) view.findViewById(R.id.totalRunDistance)).setText(String.format("%.1f\n%s", totalRunningDistance, getActivity().getString(R.string.km)));
                ((TextView) view.findViewById(R.id.totalCycleDistance)).setText(String.format("%.1f\n%s", totalCyclingDistance, getActivity().getString(R.string.km)));
            }

            if(UnitPreferenceFragment.getEnergyUnit(getActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
                ((TextView) view.findViewById(R.id.totalCaloriesBurnt)).setText(String.format("%.1f\n%s", totalCaloriesBurnt * 4.184f, getActivity().getString(R.string.kj)));
            else
                ((TextView) view.findViewById(R.id.totalCaloriesBurnt)).setText(String.format("%.1f\n%s", totalCaloriesBurnt, getActivity().getString(R.string.cal)));

            List<String> data = MDBHActivityTracker.getInstance(getActivity()).getPersonalBests();
            if(!data.isEmpty()) {
                float maxDistanceWalk = Float.parseFloat(data.get(2));
                String maxDistanceWalkDate = data.get(3);

                float maxDistanceRun = Float.parseFloat(data.get(0));
                String maxDistanceRunDate = data.get(1);

                float maxDistanceBike = Float.parseFloat(data.get(4));
                String maxDistanceBikeDate = data.get(5);

                int maxCaloriesBurnt = Integer.parseInt(data.get(6));
                String maxCaloriesBurntDate = data.get(7);

                String maxDuration = data.get(8);
                String maxDurationDate = data.get(9);

                if(maxDistanceWalkDate == null || maxDistanceWalk == 0)
                    view.findViewById(R.id.achievement1).setVisibility(View.GONE);
                else{
                    view.findViewById(R.id.achievement1).setVisibility(View.VISIBLE);
                    long dateInMillis = Long.parseLong(maxDistanceWalkDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(dateInMillis);

                    ((TextView) view.findViewById(R.id.achievementDate1)).setText(String.format("%d %s %d", cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR)));

                    if(UnitPreferenceFragment.getDistanceUnit(getActivity()).equals(UnitPreferenceFragment.DISTANCE_UNIT_MILE))
                        ((TextView) view.findViewById(R.id.achievementHeader1)).setText(String.format("%.1f%s", maxDistanceWalk * 0.621371f, getActivity().getString(R.string.mi)));
                    else
                        ((TextView) view.findViewById(R.id.achievementHeader1)).setText(String.format("%.1f%s", maxDistanceWalk, getActivity().getString(R.string.km)));
                }

                if(maxDistanceRunDate == null || maxDistanceRun == 0)
                    view.findViewById(R.id.achievement2).setVisibility(View.GONE);
                else{
                    view.findViewById(R.id.achievement2).setVisibility(View.VISIBLE);
                    long dateInMillis = Long.parseLong(maxDistanceRunDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(dateInMillis);

                    ((TextView) view.findViewById(R.id.achievementDate2)).setText(String.format("%d %s %d", cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR)));
                    if(UnitPreferenceFragment.getDistanceUnit(getActivity()).equals(UnitPreferenceFragment.DISTANCE_UNIT_MILE))
                        ((TextView) view.findViewById(R.id.achievementHeader2)).setText(String.format("%.1f%s", maxDistanceRun * 0.621371f, getActivity().getString(R.string.mi)));
                    else
                        ((TextView) view.findViewById(R.id.achievementHeader2)).setText(String.format("%.1f%s", maxDistanceRun, getActivity().getString(R.string.km)));
                }

                if(maxDistanceBikeDate == null || maxDistanceBike == 0)
                    view.findViewById(R.id.achievement3).setVisibility(View.GONE);
                else{
                    view.findViewById(R.id.achievement3).setVisibility(View.VISIBLE);
                    long dateInMillis = Long.parseLong(maxDistanceBikeDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(dateInMillis);

                    ((TextView) view.findViewById(R.id.achievementDate3)).setText(String.format("%d %s %d", cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR)));
                    if(UnitPreferenceFragment.getDistanceUnit(getActivity()).equals(UnitPreferenceFragment.DISTANCE_UNIT_MILE))
                        ((TextView) view.findViewById(R.id.achievementHeader3)).setText(String.format("%.1f%s", maxDistanceBike * 0.621371f, getActivity().getString(R.string.mi)));
                    else
                        ((TextView) view.findViewById(R.id.achievementHeader3)).setText(String.format("%.1f%s", maxDistanceBike, getActivity().getString(R.string.km)));
                }

                if(maxCaloriesBurntDate == null || maxCaloriesBurnt == 0)
                    view.findViewById(R.id.achievement4).setVisibility(View.GONE);
                else{
                    view.findViewById(R.id.achievement4).setVisibility(View.VISIBLE);
                    long dateInMillis = Long.parseLong(maxCaloriesBurntDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(dateInMillis);

                    ((TextView) view.findViewById(R.id.achievementDate4)).setText(String.format("%d %s %d", cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR)));
                    if(UnitPreferenceFragment.getEnergyUnit(getActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
                        ((TextView) view.findViewById(R.id.achievementHeader4)).setText(String.format("%.1f%s", maxCaloriesBurnt * 4.184f, getActivity().getString(R.string.kj)));
                    else
                        ((TextView) view.findViewById(R.id.achievementHeader4)).setText(String.format("%d%s", maxCaloriesBurnt, getActivity().getString(R.string.cal)));
                }

                if(maxDurationDate == null || maxDuration == null)
                    view.findViewById(R.id.achievement5).setVisibility(View.GONE);
                else {
                    view.findViewById(R.id.achievement5).setVisibility(View.VISIBLE);
                    long dateInMillis = Long.parseLong(maxDurationDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(dateInMillis);
                    ((TextView) view.findViewById(R.id.achievementDate5)).setText(String.format("%d %s %d", cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR)));
                    ((TextView) view.findViewById(R.id.achievementHeader5)).setText(maxDuration);
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_screen, container, false);
        setUpOnClickListeners(view);

        RecyclerView recyclerView = view.findViewById(R.id.activityRecycler);
        recyclerView.setAdapter(((InAppActivity) requireActivity()).smallActivityAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()){
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });

        LocationService.serviceKilled.observe(getViewLifecycleOwner(), isKilled -> {
            setUpData(view);

            if(isKilled) {
                activityChosen.set(false);
                setSelected(view, NONE_SELECTED);
            } else{
                if(activityChosen.get()){
                    if(ActivityTrackingFragment.activityType == RUNNING_SELECTED)
                        setSelected(view, RUNNING_SELECTED);
                    else if(ActivityTrackingFragment.activityType == WALKING_SELECTED)
                        setSelected(view, WALKING_SELECTED);
                    else if(ActivityTrackingFragment.activityType == CYCLING_SELECTED)
                        setSelected(view, CYCLING_SELECTED);
                } else
                    setSelected(view, NONE_SELECTED);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity() != null) {
            String desiredFragment = ((InAppActivity) getActivity()).getDesiredFragment();
            if (desiredFragment != null) {
                if (desiredFragment.equals("ActivityTrackingFragment")) {
                    ((InAppActivity) getActivity()).putDesiredFragment(null);
                    goToActivityTrackingFragment();
                }
            }
        }

        ((InAppActivity) requireActivity()).updateActivityRecyclerUI(null);
        if(getView() != null)
            setUpData(getView());
    }
}
