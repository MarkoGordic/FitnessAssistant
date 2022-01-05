package com.example.fitnessassistant.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;
import com.example.fitnessassistant.questions.WeightFragment;

public class GoalsFragment extends Fragment {

    public static String getMonthShort(int numOfMonth){
        switch(numOfMonth){
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Aug";
            case 9:
                return "Sep";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
            default:
                return null;
        }
    }

    // TODO add immediate weight reload upon changing

    private void setUpOnClickListeners(View view){
        // backButton listener - calls activity's onBackPressed()
        view.findViewById(R.id.backButton).setOnClickListener(view1 -> requireActivity().onBackPressed());

        // setting up FirstWeight view
        if(WeightFragment.getFirstWeight(requireActivity()) == -1f){
            if(UnitPreferenceFragment.getWeightUnit(requireActivity()).equals(UnitPreferenceFragment.WEIGHT_UNIT_LBS))
                ((TextView) view.findViewById(R.id.startingWeightTextView)).setText(String.format("?%s", UnitPreferenceFragment.WEIGHT_UNIT_LBS));
            else
                ((TextView) view.findViewById(R.id.startingWeightTextView)).setText(String.format("?%s", UnitPreferenceFragment.WEIGHT_UNIT_KG));

            ((TextView) view.findViewById(R.id.startingWeightDateTextView)).setText("?");
        } else{
            if(UnitPreferenceFragment.getWeightUnit(requireActivity()).equals(UnitPreferenceFragment.WEIGHT_UNIT_LBS))
                ((TextView) view.findViewById(R.id.startingWeightTextView)).setText(String.format("%s%s", WeightFragment.getFirstWeight(requireActivity()), UnitPreferenceFragment.WEIGHT_UNIT_LBS));
            else
                ((TextView) view.findViewById(R.id.startingWeightTextView)).setText(String.format("%s%s", WeightFragment.getFirstWeight(requireActivity()), UnitPreferenceFragment.WEIGHT_UNIT_KG));

            int firstWeightDate = Integer.parseInt(WeightFragment.getFirstWeightDate(requireActivity()));

            int day = firstWeightDate % 100;
            firstWeightDate /= 100;
            int month = firstWeightDate % 100;
            firstWeightDate /= 100;
            int year = firstWeightDate;

            ((TextView) view.findViewById(R.id.startingWeightDateTextView)).setText(String.format("%s %s %s", day, getMonthShort(month), year));
        }

        // setting up GoalWeight view
        ((TextView) view.findViewById(R.id.goalWeightDateTextView)).setText("?");
        if(WeightFragment.getGoalWeight(requireActivity()) == -1f){
            if(UnitPreferenceFragment.getWeightUnit(requireActivity()).equals(UnitPreferenceFragment.WEIGHT_UNIT_LBS))
                ((TextView) view.findViewById(R.id.goalWeightTextView)).setText(String.format("?%s", UnitPreferenceFragment.WEIGHT_UNIT_LBS));
            else
                ((TextView) view.findViewById(R.id.goalWeightTextView)).setText(String.format("?%s", UnitPreferenceFragment.WEIGHT_UNIT_KG));
        } else{
            if(UnitPreferenceFragment.getWeightUnit(requireActivity()).equals(UnitPreferenceFragment.WEIGHT_UNIT_LBS))
                ((TextView) view.findViewById(R.id.goalWeightTextView)).setText(String.format("%s%s", WeightFragment.getGoalWeight(requireActivity()), UnitPreferenceFragment.WEIGHT_UNIT_LBS));
            else
                ((TextView) view.findViewById(R.id.goalWeightTextView)).setText(String.format("%s%s", WeightFragment.getGoalWeight(requireActivity()), UnitPreferenceFragment.WEIGHT_UNIT_KG));
        }

        // setting up LastUpdatedWeight view
        if(WeightFragment.getLastDailyAverage(requireActivity()) == -1f){
            if(UnitPreferenceFragment.getWeightUnit(requireActivity()).equals(UnitPreferenceFragment.WEIGHT_UNIT_LBS))
                ((TextView) view.findViewById(R.id.currentWeightTextView)).setText(String.format("?%s", UnitPreferenceFragment.WEIGHT_UNIT_LBS));
            else
                ((TextView) view.findViewById(R.id.currentWeightTextView)).setText(String.format("?%s", UnitPreferenceFragment.WEIGHT_UNIT_KG));

            ((TextView) view.findViewById(R.id.currentWeightDateTextView)).setText("?");
        } else{
            if(UnitPreferenceFragment.getWeightUnit(requireActivity()).equals(UnitPreferenceFragment.WEIGHT_UNIT_LBS))
                ((TextView) view.findViewById(R.id.currentWeightTextView)).setText(String.format("%s%s", WeightFragment.getLastDailyAverage(requireActivity()), UnitPreferenceFragment.WEIGHT_UNIT_LBS));
            else
                ((TextView) view.findViewById(R.id.currentWeightTextView)).setText(String.format("%s%s", WeightFragment.getLastDailyAverage(requireActivity()), UnitPreferenceFragment.WEIGHT_UNIT_KG));

            int lastUpdatedWeightDate = Integer.parseInt(WeightFragment.getFirstWeightDate(requireActivity()));

            int day = lastUpdatedWeightDate % 100;
            lastUpdatedWeightDate /= 100;
            int month = lastUpdatedWeightDate % 100;
            lastUpdatedWeightDate /= 100;
            int year = lastUpdatedWeightDate;

            ((TextView) view.findViewById(R.id.currentWeightDateTextView)).setText(String.format("%s %s %s", day, getMonthShort(month), year));
        }

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
        view.findViewById(R.id.stepGoalTextView).setOnClickListener(v -> {
            // TODO create StepGoalFragment
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.goals_screen, container, false);
        // setting up the view
        setUpOnClickListeners(view);
        return view;
    }
}
