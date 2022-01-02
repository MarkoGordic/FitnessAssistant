package com.example.fitnessassistant.questions;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;

public class OpeningQuestionFragment extends Fragment {

    public static boolean getShouldSkipQuestions(Context context){
        return context.getSharedPreferences("questions", MODE_PRIVATE).getBoolean("shouldSkipQuestions", false);
    }

    public static void putShouldSkipQuestions(Context context, boolean shouldSkipQuestions){
        context.getSharedPreferences("questions", MODE_PRIVATE).edit().putBoolean("shouldSkipQuestions", shouldSkipQuestions).apply();
    }

    public static boolean everythingIsKnown(Context context){
        return !GenderFragment.getGender(context).equals("unknown")
                && BirthdayFragment.getBirthday(context) != -1
                && WeightFragment.getWeight(context) != -1f
                && HeightFragment.getHeight(context) != -1f
                && !UnitPreferenceFragment.isUnknown(context);
    }

    private void setUpOnClickListeners(View view){
        view.findViewById(R.id.skipButton).setOnClickListener(v -> {
            if(((AppCompatCheckBox) view.findViewById(R.id.dontAskAgainCheckBox)).isChecked())
                putShouldSkipQuestions(requireContext(), true);
            ((InAppActivity) requireActivity()).proceedQuestions(5);
        });

        view.findViewById(R.id.proceedButton).setOnClickListener(v -> {
            if(((AppCompatCheckBox) view.findViewById(R.id.dontAskAgainCheckBox)).isChecked())
                putShouldSkipQuestions(requireContext(), true);
            ((InAppActivity) requireActivity()).proceedQuestions(0);
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.opening_question_screen, container, false);
        setUpOnClickListeners(view);
        return view;
    }
}