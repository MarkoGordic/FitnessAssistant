package com.example.fitnessassistant.profile;

import static com.example.fitnessassistant.util.TimeFunctional.getMonthShort;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.activitytracker.LocationService;
import com.example.fitnessassistant.database.mdbh.MDBHActivityTracker;
import com.example.fitnessassistant.database.mdbh.MDBHPedometer;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;

import java.util.Calendar;
import java.util.List;

public class PersonalBestsFragment extends Fragment {

    @SuppressLint("DefaultLocale")
    public void updateStepsData(View view) {
        if (view == null)
            view = getView();

        if(view != null) {
            List<String> streakList = MDBHPedometer.getInstance(requireActivity()).getMaxStreak();
            int maxStreak = Integer.parseInt(streakList.get(0));
            String streakDateStart = streakList.get(1);
            String streakDateEnd = streakList.get(2);

            List<String> stepsList = MDBHPedometer.getInstance(requireActivity()).getMaxSteps();
            int maxSteps = (int) Float.parseFloat(stepsList.get(0));
            String maxStepsDate = stepsList.get(1);

            if(streakDateStart == null || streakDateEnd == null || maxStreak == 0) {
                ((ImageView) view.findViewById(R.id.trophy2)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((TextView) view.findViewById(R.id.achievementDate2)).setText(requireActivity().getString(R.string.locked));
                ((TextView) view.findViewById(R.id.achievementHeader2)).setText("");
            } else {
                int startDate = Integer.parseInt(streakDateStart);
                int startDay = startDate % 100;
                startDate /= 100;
                int startMonth = startDate % 100;
                startDate /= 100;
                int startYear = startDate;
                int endDate = Integer.parseInt(streakDateEnd);
                int endDay = endDate % 100;
                endDate /= 100;
                int endMonth = endDate % 100;
                endDate /= 100;
                int endYear = endDate;

                ((ImageView) view.findViewById(R.id.trophy2)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.BlueYonder)));
                ((TextView) view.findViewById(R.id.achievementDate2)).setText(String.format("%d %s %d - %d %s %d", startDay, getMonthShort(requireActivity(), startMonth), startYear, endDay, getMonthShort(requireActivity(), endMonth), endYear));
                ((TextView) view.findViewById(R.id.achievementHeader2)).setText(String.format("%d-%s\n%s", maxStreak, requireActivity().getString(R.string.day), requireActivity().getString(R.string.streak)));
            }

            if(maxStepsDate == null || maxSteps == 0) {
                ((ImageView) view.findViewById(R.id.trophy1)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((TextView) view.findViewById(R.id.achievementDate1)).setText(requireActivity().getString(R.string.locked));
                ((TextView) view.findViewById(R.id.achievementHeader1)).setText("");
            } else {
                int date = Integer.parseInt(maxStepsDate);
                int day = date % 100;
                date /= 100;
                int month = date % 100;
                date /= 100;
                int year = date;

                ((ImageView) view.findViewById(R.id.trophy1)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.BlueYonder)));
                ((TextView) view.findViewById(R.id.achievementDate1)).setText(String.format("%d %s %d", day, getMonthShort(requireActivity(), month), year));
                ((TextView) view.findViewById(R.id.achievementHeader1)).setText(String.format("%d\n%s", maxSteps, requireActivity().getString(R.string.steps_in_a_day)));
            }
        }
    }

    @SuppressLint("DefaultLocale")
    public void updateActivityData(View view) {
        if (view == null)
            view = getView();

        if(view != null && getActivity() != null) {
            List<String> data = MDBHActivityTracker.getInstance(getActivity()).getPersonalBests();
            if(!data.isEmpty()) {
                float maxDistanceWalk = Float.parseFloat(data.get(2));
                String maxDistanceWalkDate = data.get(3);

                float maxDistanceRun = Float.parseFloat(data.get(0));
                String maxDistanceRunDate = data.get(1);

                float maxDistanceBike = Float.parseFloat(data.get(4));
                String maxDistanceBikeDate = data.get(5);

                float maxCaloriesBurnt = Float.parseFloat(data.get(6));
                String maxCaloriesBurntDate = data.get(7);

                String maxDuration = data.get(8);
                String maxDurationDate = data.get(9);

                if(maxDistanceWalkDate == null || maxDistanceWalk == 0f) {
                    ((ImageView) view.findViewById(R.id.trophy3)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((TextView) view.findViewById(R.id.achievementDate3)).setText(getActivity().getString(R.string.locked));
                    ((TextView) view.findViewById(R.id.achievementHeader3)).setText("");
                } else{
                    long dateInMillis = Long.parseLong(maxDistanceWalkDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(dateInMillis);

                    ((ImageView) view.findViewById(R.id.trophy3)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.BlueYonder)));
                    ((TextView) view.findViewById(R.id.achievementDate3)).setText(String.format("%d %s %d", cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR)));

                    if(UnitPreferenceFragment.getDistanceUnit(getActivity()).equals(UnitPreferenceFragment.DISTANCE_UNIT_MILE))
                        ((TextView) view.findViewById(R.id.achievementHeader3)).setText(String.format("%.1f%s", maxDistanceWalk * 0.621371f, getActivity().getString(R.string.mi)));
                    else
                        ((TextView) view.findViewById(R.id.achievementHeader3)).setText(String.format("%.1f%s", maxDistanceWalk, getActivity().getString(R.string.km)));
                }

                if(maxDistanceRunDate == null || maxDistanceRun == 0f) {
                    ((ImageView) view.findViewById(R.id.trophy4)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((TextView) view.findViewById(R.id.achievementDate4)).setText(getActivity().getString(R.string.locked));
                    ((TextView) view.findViewById(R.id.achievementHeader4)).setText("");
                } else{
                    long dateInMillis = Long.parseLong(maxDistanceRunDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(dateInMillis);

                    ((ImageView) view.findViewById(R.id.trophy4)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.BlueYonder)));
                    ((TextView) view.findViewById(R.id.achievementDate4)).setText(String.format("%d %s %d", cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR)));
                    if(UnitPreferenceFragment.getDistanceUnit(getActivity()).equals(UnitPreferenceFragment.DISTANCE_UNIT_MILE))
                        ((TextView) view.findViewById(R.id.achievementHeader4)).setText(String.format("%.1f%s", maxDistanceRun * 0.621371f, getActivity().getString(R.string.mi)));
                    else
                        ((TextView) view.findViewById(R.id.achievementHeader4)).setText(String.format("%.1f%s", maxDistanceRun, getActivity().getString(R.string.km)));
                }

                if(maxDistanceBikeDate == null || maxDistanceBike == 0f) {
                    ((ImageView) view.findViewById(R.id.trophy5)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((TextView) view.findViewById(R.id.achievementDate5)).setText(getActivity().getString(R.string.locked));
                    ((TextView) view.findViewById(R.id.achievementHeader5)).setText("");
                } else{
                    long dateInMillis = Long.parseLong(maxDistanceBikeDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(dateInMillis);

                    ((ImageView) view.findViewById(R.id.trophy5)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.BlueYonder)));
                    ((TextView) view.findViewById(R.id.achievementDate5)).setText(String.format("%d %s %d", cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR)));
                    if(UnitPreferenceFragment.getDistanceUnit(getActivity()).equals(UnitPreferenceFragment.DISTANCE_UNIT_MILE))
                        ((TextView) view.findViewById(R.id.achievementHeader5)).setText(String.format("%.1f%s", maxDistanceBike * 0.621371f, getActivity().getString(R.string.mi)));
                    else
                        ((TextView) view.findViewById(R.id.achievementHeader5)).setText(String.format("%.1f%s", maxDistanceBike, getActivity().getString(R.string.km)));
                }

                if(maxCaloriesBurntDate == null || maxCaloriesBurnt == 0f) {
                    ((ImageView) view.findViewById(R.id.trophy6)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((TextView) view.findViewById(R.id.achievementDate6)).setText(getActivity().getString(R.string.locked));
                    ((TextView) view.findViewById(R.id.achievementHeader6)).setText("");
                } else{
                    long dateInMillis = Long.parseLong(maxCaloriesBurntDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(dateInMillis);

                    ((ImageView) view.findViewById(R.id.trophy6)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.BlueYonder)));
                    ((TextView) view.findViewById(R.id.achievementDate6)).setText(String.format("%d %s %d", cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR)));
                    if(UnitPreferenceFragment.getEnergyUnit(getActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
                        ((TextView) view.findViewById(R.id.achievementHeader6)).setText(String.format("%.1f%s", maxCaloriesBurnt * 4.184f, getActivity().getString(R.string.kj)));
                    else
                        ((TextView) view.findViewById(R.id.achievementHeader6)).setText(String.format("%.1f%s", maxCaloriesBurnt, getActivity().getString(R.string.cal)));
                }

                if(maxDurationDate == null || maxDuration == null) {
                    ((ImageView) view.findViewById(R.id.trophy7)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((TextView) view.findViewById(R.id.achievementDate7)).setText(getActivity().getString(R.string.locked));
                    ((TextView) view.findViewById(R.id.achievementHeader7)).setText("");
                } else {
                    long dateInMillis = Long.parseLong(maxDurationDate);
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(dateInMillis);
                    ((ImageView) view.findViewById(R.id.trophy7)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.BlueYonder)));
                    ((TextView) view.findViewById(R.id.achievementDate7)).setText(String.format("%d %s %d", cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR)));
                    ((TextView) view.findViewById(R.id.achievementHeader7)).setText(maxDuration);
                }
            } else{
                ((ImageView) view.findViewById(R.id.trophy3)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((TextView) view.findViewById(R.id.achievementDate3)).setText(getActivity().getString(R.string.locked));
                ((TextView) view.findViewById(R.id.achievementHeader3)).setText("");

                ((ImageView) view.findViewById(R.id.trophy4)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((TextView) view.findViewById(R.id.achievementDate4)).setText(getActivity().getString(R.string.locked));
                ((TextView) view.findViewById(R.id.achievementHeader4)).setText("");

                ((ImageView) view.findViewById(R.id.trophy5)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((TextView) view.findViewById(R.id.achievementDate5)).setText(getActivity().getString(R.string.locked));
                ((TextView) view.findViewById(R.id.achievementHeader5)).setText("");

                ((ImageView) view.findViewById(R.id.trophy6)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((TextView) view.findViewById(R.id.achievementDate6)).setText(getActivity().getString(R.string.locked));
                ((TextView) view.findViewById(R.id.achievementHeader6)).setText("");

                ((ImageView) view.findViewById(R.id.trophy7)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((TextView) view.findViewById(R.id.achievementDate7)).setText(getActivity().getString(R.string.locked));
                ((TextView) view.findViewById(R.id.achievementHeader7)).setText("");
            }
        }
    }

    private void setUpOnClickListeners(View view) {
        // backButton listener - calls activity's onBackPressed()
        view.findViewById(R.id.backButton).setOnClickListener(view1 -> requireActivity().onBackPressed());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.personal_bests_screen, container, false);
        // setting up the view
        setUpOnClickListeners(view);

        LocationService.serviceKilled.observe(getViewLifecycleOwner(), isKilled -> updateActivityData(view));

        // if we got back from dark/light mode switched we go to the settings
        if(PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean("theme_changed", false)){
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit();
            editor.putBoolean("theme_changed", false);
            editor.apply();
            SettingsFragment.restartApp(requireContext(), 0);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getView() != null){
            updateStepsData(getView());
            updateActivityData(getView());
        }
    }
}
