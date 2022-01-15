package com.example.fitnessassistant.map;

import static java.lang.Math.abs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.adapters.ActivityAdapter;
import com.example.fitnessassistant.database.mdbh.MDBHActivityTracker;


public class MapPageFragment extends Fragment {

    public void goToActivityTrackingFragment(){
        requireActivity().getSupportFragmentManager().beginTransaction().hide(this).setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down).add(R.id.in_app_container, InAppActivity.activityTrackingFragment).addToBackStack(null).commit();
    }

    private void setUpOnClickListeners(View view){
        // set up mapButton on click listener
        view.findViewById(R.id.mapSwipeUp).setOnClickListener(view1 -> Toast.makeText(requireContext(), R.string.swipe_up_to_use_the_map, Toast.LENGTH_SHORT).show());

        // set up mapButton on touch listener (for swiping)
        view.findViewById(R.id.mapSwipeUp).setOnTouchListener(new View.OnTouchListener() {
            // xCord and yCord of event registered
            float xCord;
            float yCord;
            // len, used to determine what the event actually was
            final float len = getResources().getDisplayMetrics().densityDpi / 6f;

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

                    // if it's a solid swipe UP (>) and if it's not too much to the side
                    if(yCord > len * 4 && abs(xCord) < len * 8)
                        goToActivityTrackingFragment();
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
        View view = inflater.inflate(R.layout.map_screen, container, false);
        setUpOnClickListeners(view);

        ActivityAdapter activityAdapter = new ActivityAdapter(requireActivity(), MDBHActivityTracker.getInstance(requireActivity()).readActivitiesDataForRecyclerDB());
        RecyclerView recyclerView = view.findViewById(R.id.activityRecycler);
        recyclerView.setAdapter(activityAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

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
    }
}
