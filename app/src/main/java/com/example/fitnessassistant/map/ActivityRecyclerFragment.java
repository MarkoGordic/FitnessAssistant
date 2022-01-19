package com.example.fitnessassistant.map;


import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.adapters.SpinnerAdapter;
import com.example.fitnessassistant.database.data.ActivityRecycler;
import com.example.fitnessassistant.util.ActivitySelection;
import com.example.fitnessassistant.util.CustomSpinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActivityRecyclerFragment extends Fragment {
    public static List<ActivityRecycler> activitiesCpy;
    public static List<ActivityRecycler> activitiesSortedByDate;
    public static List<ActivityRecycler> activitiesSortedByDuration;
    public static List<ActivityRecycler> activitiesSortedByDistance;
    public static List<ActivityRecycler> activitiesSortedByCalories;
    public static List<ActivityRecycler> activitiesSortedBySpeed;
    private static final AtomicBoolean down = new AtomicBoolean(true);

    public static void updateLists(List<ActivityRecycler> newList){
        activitiesSortedByDate = new ArrayList<>(newList);
        activitiesSortedByDuration = new ArrayList<>(newList);
        activitiesSortedByDistance = new ArrayList<>(newList);
        activitiesSortedByCalories = new ArrayList<>(newList);
        activitiesSortedBySpeed = new ArrayList<>(newList);

        activitiesSortedByDate.sort((o1, o2) -> (int) (o2.getDate() - o1.getDate()));
        activitiesSortedByCalories.sort((o1, o2) -> Float.compare(o2.getCaloriesBurnt(), o1.getCaloriesBurnt()));
        activitiesSortedByDistance.sort((o1, o2) -> Float.compare(o2.getDistance(), o1.getDistance()));
        activitiesSortedBySpeed.sort((o1, o2) -> Float.compare(o2.getAverageSpeed(), o1.getAverageSpeed()));
        activitiesSortedByDuration.sort((o1, o2) -> {
            StringTokenizer tokenizer1 = new StringTokenizer(o2.getDuration(), ":");
            StringTokenizer tokenizer2 = new StringTokenizer(o1.getDuration(), ":");
            while (tokenizer1.hasMoreElements()) {
                int first = Integer.parseInt(tokenizer1.nextToken());
                int second = Integer.parseInt(tokenizer2.nextToken());
                if (first != second)
                    return first - second;
            }
            return 0;
        });

        activitiesCpy = new ArrayList<>(activitiesSortedByDate);

        if(!down.get()) {
            Collections.reverse(activitiesSortedByCalories);
            Collections.reverse(activitiesSortedByDistance);
            Collections.reverse(activitiesSortedByDuration);
            Collections.reverse(activitiesSortedBySpeed);
            Collections.reverse(activitiesSortedByDate);
        }

        InAppActivity.mapFragment.setUpData(null);
        InAppActivity.personalBestsFragment.updateActivityData(null);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateView(int position){
        switch(position){
            case 0:
                ((InAppActivity) requireActivity()).activityAdapter.setList(activitiesCpy);
                break;
            case 1:
                ((InAppActivity) requireActivity()).activityAdapter.setList(activitiesSortedByDate);
                break;
            case 2:
                ((InAppActivity) requireActivity()).activityAdapter.setList(activitiesSortedByDuration);
                break;
            case 3:
                ((InAppActivity) requireActivity()).activityAdapter.setList(activitiesSortedByDistance);
                break;
            case 4:
                ((InAppActivity) requireActivity()).activityAdapter.setList(activitiesSortedByCalories);
                break;
            case 5:
                ((InAppActivity) requireActivity()).activityAdapter.setList(activitiesSortedBySpeed);
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_recycler_screen, container, false);

        updateLists(InAppActivity.activities);

        view.findViewById(R.id.backButton).setOnClickListener(v -> requireActivity().onBackPressed());

        ActivitySelection[] selections = { new ActivitySelection(requireActivity().getString(R.string.sort_by), null),
                new ActivitySelection(requireActivity().getString(R.string.date), AppCompatResources.getDrawable(requireActivity(), R.drawable.calendar)),
                new ActivitySelection(requireActivity().getString(R.string.time), AppCompatResources.getDrawable(requireActivity(), R.drawable.hourglass_end)),
                new ActivitySelection(requireActivity().getString(R.string.distance), AppCompatResources.getDrawable(requireActivity(), R.drawable.road)),
                new ActivitySelection(requireActivity().getString(R.string.calories), AppCompatResources.getDrawable(requireActivity(), R.drawable.fire)),
                new ActivitySelection(requireActivity().getString(R.string.average_speed), AppCompatResources.getDrawable(requireActivity(), R.drawable.dashboard))};

        SpinnerAdapter adapter = new SpinnerAdapter(requireActivity(), R.layout.spinner_layout, selections);
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        final CustomSpinner spinner = view.findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            @Override
            public void onSpinnerOpened() {
                spinner.setSelected(true);
            }
            @Override
            public void onSpinnerClosed() {
                spinner.setSelected(false);
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { updateView(position); }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        view.findViewById(R.id.sortButton).setOnClickListener(v -> {
            Drawable d;
            if(down.get()){
                down.set(false);
                d = AppCompatResources.getDrawable(requireActivity(),R.drawable.up);
            } else{
                down.set(true);
                d = AppCompatResources.getDrawable(requireActivity(),R.drawable.down);
            }

            if(d != null)
                d.setTint(requireActivity().getColor(R.color.backgroundColor));
            ((ImageButton) v).setImageDrawable(d);

            Collections.reverse(activitiesSortedByDate);
            Collections.reverse(activitiesSortedByDistance);
            Collections.reverse(activitiesSortedByDuration);
            Collections.reverse(activitiesSortedByCalories);
            Collections.reverse(activitiesSortedBySpeed);

            updateView(spinner.getSelectedItemPosition());
        });

        RecyclerView recyclerView = view.findViewById(R.id.activityRecycler);

        recyclerView.setAdapter(((InAppActivity) requireActivity()).activityAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()){
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ((InAppActivity) requireActivity()).updateActivityRecyclerUI(null);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onPause() {
        super.onPause();

        InAppActivity.activities = new ArrayList<>(activitiesCpy);
        ((InAppActivity) requireActivity()).smallActivityAdapter.setList(InAppActivity.activities);

        ((InAppActivity) requireActivity()).updateActivityRecyclerUI(null);
    }
}
