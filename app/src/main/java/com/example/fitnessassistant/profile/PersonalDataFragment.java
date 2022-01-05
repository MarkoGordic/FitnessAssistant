package com.example.fitnessassistant.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.questions.BirthdayFragment;
import com.example.fitnessassistant.questions.GenderFragment;
import com.example.fitnessassistant.questions.HeightFragment;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;
import com.example.fitnessassistant.questions.WeightFragment;

public class PersonalDataFragment extends Fragment {

    private void setUpOnClickListeners(View view){
        // backButton listener - calls activity's onBackPressed()
        view.findViewById(R.id.backButton).setOnClickListener(view1 -> requireActivity().onBackPressed());

        // heightTextView listener - calls new HeightFragment
        view.findViewById(R.id.heightTextView).setOnClickListener(v -> {
            InAppActivity.useNewPersonalDataFragments.set(true);
            requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new HeightFragment()).addToBackStack(null).commit();
        });

        // weightTextView listener - calls new WeightFragment
        view.findViewById(R.id.weightTextView).setOnClickListener(v -> {
            InAppActivity.useNewPersonalDataFragments.set(true);
            requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new WeightFragment()).addToBackStack(null).commit();
        });

        // genderTextView listener - calls new GenderFragment
        view.findViewById(R.id.genderTextView).setOnClickListener(v -> {
            InAppActivity.useNewPersonalDataFragments.set(true);
            requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new GenderFragment()).addToBackStack(null).commit();
        });

        // unitsPreferenceTextView listener - calls new UnitsPreferenceFragment
        view.findViewById(R.id.unitsPreferenceTextView).setOnClickListener(v -> {
            InAppActivity.useNewPersonalDataFragments.set(true);
            requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new UnitPreferenceFragment()).addToBackStack(null).commit();
        });

        // birthdateTextView listener - calls new BirthdayFragment
        view.findViewById(R.id.birthdateTextView).setOnClickListener(v -> {
            InAppActivity.useNewPersonalDataFragments.set(true);
            requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new BirthdayFragment()).addToBackStack(null).commit();
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.personal_data_screen, container, false);
        // setting up the view
        setUpOnClickListeners(view);
        return view;
    }
}
