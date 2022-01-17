package com.example.fitnessassistant.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

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
import com.example.fitnessassistant.activitytracker.LocationService;

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
                // TODO obavestiti da je walking
            }
//          else if(TODO if walking){
//                goToActivityTrackingFragment();
//            }
        });

        view.findViewById(R.id.runningActivity).setOnClickListener(v -> {
            if(!activityChosen.get()) {
                setSelected(view, RUNNING_SELECTED);
                goToActivityTrackingFragment();
                // TODO obavestit da je running
            }
//          else if(TODO if running){
//                goToActivityTrackingFragment();
//            }
        });

        view.findViewById(R.id.cyclingActivity).setOnClickListener(v -> {
            if(!activityChosen.get()) {
                setSelected(view, CYCLING_SELECTED);
                goToActivityTrackingFragment();
                // TODO obavestiti da je cycling
            }
//          else if(TODO if walking){
//                goToActivityTrackingFragment();
//            }
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
            if(isKilled) {
                activityChosen.set(false);
                setSelected(view, NONE_SELECTED);
            } else{
                if(activityChosen.get()){
//                    if(TODO running)
//                        setSelected(view, RUNNING_SELECTED);
//                    else if(TODO walking)
//                        setSelected(view, WALKING_SELECTED);
//                    else if(TODO cycling)
//                        setSelected(view, CYCLING_SELECTED);
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
    }
}
