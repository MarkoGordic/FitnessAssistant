package com.example.fitnessassistant.diary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.nutritiontracker.BarcodeScanner;

public class DiaryPageFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.diary_screen, container, false);

        view.findViewById(R.id.mindzascanner).setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction().hide(DiaryPageFragment.this).add(R.id.in_app_container, new BarcodeScanner()).addToBackStack(null).commit());

        // returning the view
        return view;
    }
}
