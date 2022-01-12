package com.example.fitnessassistant.profile;

import static com.example.fitnessassistant.util.TimeFunctional.getMonthShort;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.pedometer.StepGoalFragment;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;
import com.example.fitnessassistant.questions.WeightFragment;

public class GoalsFragment extends Fragment {

    private void setUpWeights(View view){
        // setting up Units
        if(UnitPreferenceFragment.getWeightUnit(requireActivity()).equals(UnitPreferenceFragment.WEIGHT_UNIT_LBS)) {
            ((TextView) view.findViewById(R.id.startingWeightUnit)).setText(UnitPreferenceFragment.WEIGHT_UNIT_LBS);
            ((TextView) view.findViewById(R.id.goalWeightUnit)).setText(UnitPreferenceFragment.WEIGHT_UNIT_LBS);
            ((TextView) view.findViewById(R.id.lastUpdatedUnit)).setText(UnitPreferenceFragment.WEIGHT_UNIT_LBS);
        } else {
            ((TextView) view.findViewById(R.id.startingWeightUnit)).setText(UnitPreferenceFragment.WEIGHT_UNIT_KG);
            ((TextView) view.findViewById(R.id.goalWeightUnit)).setText(UnitPreferenceFragment.WEIGHT_UNIT_KG);
            ((TextView) view.findViewById(R.id.lastUpdatedUnit)).setText(UnitPreferenceFragment.WEIGHT_UNIT_KG);
        }

        // setting up FirstWeight
        if(WeightFragment.getFirstWeight(requireActivity()) == -1f){
            ((TextView) view.findViewById(R.id.startingWeightNumber)).setText("?");

            ((TextView) view.findViewById(R.id.startingWeightDateTextView)).setText("?");
        } else{
            ((TextView) view.findViewById(R.id.startingWeightNumber)).setText(String.valueOf(WeightFragment.getFirstWeight(requireActivity())));

            int firstWeightDate = Integer.parseInt(WeightFragment.getFirstWeightDate(requireActivity()));
            int day = firstWeightDate % 100;
            firstWeightDate /= 100;
            int month = firstWeightDate % 100;
            firstWeightDate /= 100;
            int year = firstWeightDate;
            ((TextView) view.findViewById(R.id.startingWeightDateTextView)).setText(String.format("%s %s %s", day, getMonthShort(month), year));
        }

        // setting up GoalWeight
        ((TextView) view.findViewById(R.id.goalWeightDateTextView)).setText("?");
        if(WeightFragment.getGoalWeight(requireActivity()) == -1f)
            ((TextView) view.findViewById(R.id.goalWeightNumber)).setText("?");
        else
            ((TextView) view.findViewById(R.id.goalWeightNumber)).setText(String.valueOf(WeightFragment.getGoalWeight(requireActivity())));

        // setting up LastUpdatedWeight
        if(WeightFragment.getLastDailyAverage(requireActivity()) == -1f){
            ((TextView) view.findViewById(R.id.lastUpdatedNumber)).setText("?");
            ((TextView) view.findViewById(R.id.lastUpdatedWeightDateTextView)).setText("?");
        } else{
            ((TextView) view.findViewById(R.id.lastUpdatedNumber)).setText(String.valueOf(WeightFragment.getLastDailyAverage(requireActivity())));

            int lastUpdatedWeightDate = Integer.parseInt(WeightFragment.getLastDailyAverageDate(requireActivity()));
            int day = lastUpdatedWeightDate % 100;
            lastUpdatedWeightDate /= 100;
            int month = lastUpdatedWeightDate % 100;
            lastUpdatedWeightDate /= 100;
            int year = lastUpdatedWeightDate;
            ((TextView) view.findViewById(R.id.lastUpdatedWeightDateTextView)).setText(String.format("%s %s %s", day, getMonthShort(month), year));
        }
    }

    private void setUpOnClickListeners(View view){
        // backButton listener - calls activity's onBackPressed()
        view.findViewById(R.id.backButton).setOnClickListener(view1 -> requireActivity().onBackPressed());

        view.findViewById(R.id.startWeightLayout).setOnClickListener(v -> {
            if(WeightFragment.getFirstWeight(requireActivity()) == -1f)
                Toast.makeText(requireActivity(), R.string.set_first_weight, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(requireActivity(), R.string.first_weight_immutable, Toast.LENGTH_SHORT).show();
        });

        // using weight fragment for CurrentWeight
        view.findViewById(R.id.currentWeightLayout).setOnClickListener(v -> {
            InAppActivity.useNewPersonalDataFragments.set(true);
            WeightFragment.isGoalWeight.set(false);
            requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new WeightFragment()).addToBackStack(null).commit();
        });

        // using weight fragment for GoalWeight
        view.findViewById(R.id.goalWeightLayout).setOnClickListener(v -> {
            InAppActivity.useNewPersonalDataFragments.set(true);
            WeightFragment.isGoalWeight.set(true);
            requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new WeightFragment()).addToBackStack(null).commit();
        });

        // used for setting your step goals
        view.findViewById(R.id.stepGoalTextView).setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new StepGoalFragment()).addToBackStack(null).commit());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.goals_screen, container, false);
        // setting up the view
        setUpOnClickListeners(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getView() != null)
            setUpWeights(getView());
    }
}
