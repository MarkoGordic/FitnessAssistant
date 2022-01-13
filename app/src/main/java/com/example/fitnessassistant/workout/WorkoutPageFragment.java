package com.example.fitnessassistant.workout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.sleeptracker.SleepTracker;

public class WorkoutPageFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // returning the view
        return inflater.inflate(R.layout.workout_screen, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        requireActivity().startService(new Intent(requireContext(), SleepTracker.class));
        super.onCreate(savedInstanceState);
    }
}
