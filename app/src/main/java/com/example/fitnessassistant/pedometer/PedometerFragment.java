package com.example.fitnessassistant.pedometer;

import static com.example.fitnessassistant.util.TimeFunctional.getCurrentDateFormatted;
import static com.example.fitnessassistant.util.TimeFunctional.getLast7DatesFromEnd;
import static com.example.fitnessassistant.util.TimeFunctional.getMonthShort;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.database.mdbh.MDBHPedometer;
import com.example.fitnessassistant.questions.GenderFragment;
import com.example.fitnessassistant.questions.HeightFragment;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;
import com.example.fitnessassistant.questions.WeightFragment;
import com.example.fitnessassistant.util.GraphView;
import com.example.fitnessassistant.util.PermissionFunctional;
import com.example.fitnessassistant.util.ServiceFunctional;

import java.util.Calendar;
import java.util.List;

public class PedometerFragment extends Fragment {
    private Calendar graphCal = null;

    // returns stepLength in CMs
    private float getStepLength(float heightInCMs){
        if(GenderFragment.getGender(requireActivity()).equals(GenderFragment.MALE))
            return heightInCMs * 0.415f;
        else if(GenderFragment.getGender(requireActivity()).equals(GenderFragment.FEMALE))
            return heightInCMs * 0.413f;
        else
            return heightInCMs * 0.414f;
    }

    private float getCaloriesBurnedPerStep(){
        float caloriesBurnedInAMinute;
        float weight = WeightFragment.getLastDailyAverage(requireActivity());
        float height = HeightFragment.getHeight(requireActivity());

        if(weight == -1)
            weight = WeightFragment.getWorldwideAverageWeight(requireActivity());
        if(height == -1)
            height = HeightFragment.getWorldwideAverageHeight(requireActivity());

        caloriesBurnedInAMinute = (float) ((0.035f * weight) + ((1.4 * 1.4) / (height / 100) * 0.029f * weight));

        return 0.01f * caloriesBurnedInAMinute;
    }

    private float getCaloriesBurnedFromSteps(int stepCount){
            return stepCount * getCaloriesBurnedPerStep();
    }

    // returns distance walked in KMs
    private float getDistanceWalked(int stepCount){
        float height = HeightFragment.getHeight(requireActivity());
        if(height == -1)
            height = HeightFragment.getWorldwideAverageHeight(requireActivity());

        return getStepLength(height) * stepCount / 100000;
    }

    private float[] getTotals(){
        float[] totals = new float[3];

        totals[0] = MDBHPedometer.getInstance(requireContext()).getTotalSteps();
        totals[1] = getDistanceWalked(Math.round(totals[0]));
        totals[2] = getCaloriesBurnedFromSteps(Math.round(totals[0]));

        return totals;
    }

    public void setUpUI(boolean pedometerRuns){
        if(getView() != null)
            setUpStepCountingButton(getView(), pedometerRuns);
    }

    private void setUpCurrentDate(View view){
        int currentDate = Integer.parseInt(getCurrentDateFormatted());
        int day = currentDate % 100;
        currentDate /= 100;
        int month = currentDate % 100;
        currentDate /= 100;
        int year = currentDate;
        ((TextView) view.findViewById(R.id.currentDate)).setText(String.format("%s %s %s", day, getMonthShort(requireActivity(), month), year));
    }

    private void setUpGraphValues(View view, int[] vals, float maxCount){
        GraphView graphView = view.findViewById(R.id.graph);
        graphView.setGraphValues(vals);

        float[] graphYValues = graphView.getYValues();

        ((TextView) view.findViewById(R.id.value1)).setText(String.valueOf((int) graphYValues[4]));
        ((TextView) view.findViewById(R.id.value2)).setText(String.valueOf((int) graphYValues[3]));
        ((TextView) view.findViewById(R.id.value3)).setText(String.valueOf((int) graphYValues[2]));
        ((TextView) view.findViewById(R.id.value4)).setText(String.valueOf((int) graphYValues[1]));
        ((TextView) view.findViewById(R.id.value5)).setText(String.valueOf((int) graphYValues[0]));

        graphView.setBottomScaleCount(7);
        graphView.setSideScaleCount(graphYValues.length);
        graphView.setMaxCount(maxCount);
        graphView.invalidate();
    }

    private boolean isWeeklyGraph(View view){
        return view.findViewById(R.id.textLL1).getVisibility() == View.VISIBLE;
    }

    @SuppressLint("DefaultLocale")
    private void setUpGraph(View view, Boolean weeklyGraph){
        if(weeklyGraph == null)
            weeklyGraph = isWeeklyGraph(view);
        else
            graphCal = Calendar.getInstance(); //  resets calendar

        if(weeklyGraph) {
            Calendar tempCalendar = (Calendar) graphCal.clone();
            tempCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            ((TextView) view.findViewById(R.id.graphDate)).setText(String.format("%02d %s %d", tempCalendar.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), tempCalendar.get(Calendar.MONTH) + 1), tempCalendar.get(Calendar.YEAR)));
            tempCalendar.add(Calendar.DAY_OF_YEAR, 6);
            ((TextView) view.findViewById(R.id.graphDate)).setText(String.format("%s - %02d %s %d", ((TextView) view.findViewById(R.id.graphDate)).getText(), tempCalendar.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), tempCalendar.get(Calendar.MONTH) + 1), tempCalendar.get(Calendar.YEAR)));

            ((TextView) view.findViewById(R.id.graph_header)).setText(requireActivity().getString(R.string.weekly_graph));

            view.findViewById(R.id.textLL1).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textLL2).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textLL3).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textLL4).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textLL5).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textLL6).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textLL7).setVisibility(View.VISIBLE);
            view.findViewById(R.id.numberLL1).setVisibility(View.GONE);
            view.findViewById(R.id.numberLL2).setVisibility(View.GONE);
            view.findViewById(R.id.numberLL3).setVisibility(View.GONE);
            view.findViewById(R.id.numberLL4).setVisibility(View.GONE);
            view.findViewById(R.id.numberLL5).setVisibility(View.GONE);
            view.findViewById(R.id.numberLL6).setVisibility(View.GONE);
            view.findViewById(R.id.numberLL7).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.textLL1)).setText(R.string.mon_short);
            ((TextView) view.findViewById(R.id.textLL2)).setText(R.string.tue_short);
            ((TextView) view.findViewById(R.id.textLL3)).setText(R.string.wed_short);
            ((TextView) view.findViewById(R.id.textLL4)).setText(R.string.thu_short);
            ((TextView) view.findViewById(R.id.textLL5)).setText(R.string.fri_short);
            ((TextView) view.findViewById(R.id.textLL6)).setText(R.string.sat_short);
            ((TextView) view.findViewById(R.id.textLL7)).setText(R.string.sun_short);

            int[] weeklyVals = null;
            int currentDate = Integer.parseInt((String) DateFormat.format("yyyyMMdd", graphCal));

            switch(graphCal.get(Calendar.DAY_OF_WEEK)){
                case Calendar.SUNDAY:
                    weeklyVals = new int[7];
                    weeklyVals[6] = (int) MDBHPedometer.getInstance(requireActivity()).readPedometerSteps(String.valueOf(currentDate));
                    currentDate--;
                case Calendar.SATURDAY:
                    if(weeklyVals == null)
                        weeklyVals = new int[6];
                    weeklyVals[5] = (int) MDBHPedometer.getInstance(requireActivity()).readPedometerSteps(String.valueOf(currentDate));
                    currentDate--;
                case Calendar.FRIDAY:
                    if(weeklyVals == null)
                        weeklyVals = new int[5];
                    weeklyVals[4] = (int) MDBHPedometer.getInstance(requireActivity()).readPedometerSteps(String.valueOf(currentDate));
                    currentDate--;
                case Calendar.THURSDAY:
                    if(weeklyVals == null)
                        weeklyVals = new int[4];
                    weeklyVals[3] = (int) MDBHPedometer.getInstance(requireActivity()).readPedometerSteps(String.valueOf(currentDate));
                    currentDate--;
                case Calendar.WEDNESDAY:
                    if(weeklyVals == null)
                        weeklyVals = new int[3];
                    weeklyVals[2] = (int) MDBHPedometer.getInstance(requireActivity()).readPedometerSteps(String.valueOf(currentDate));
                    currentDate--;
                case Calendar.TUESDAY:
                    if(weeklyVals == null)
                        weeklyVals = new int[2];
                    weeklyVals[1] = (int) MDBHPedometer.getInstance(requireActivity()).readPedometerSteps(String.valueOf(currentDate));
                    currentDate--;
                case Calendar.MONDAY:
                    if(weeklyVals == null)
                        weeklyVals = new int[1];
                    weeklyVals[0] = (int) MDBHPedometer.getInstance(requireActivity()).readPedometerSteps(String.valueOf(currentDate));
            }

            setUpGraphValues(view, weeklyVals, 7f);
        } else {
            ((TextView) view.findViewById(R.id.graphDate)).setText(String.format("%s %d", getMonthShort(requireActivity(),graphCal.get(Calendar.MONTH) + 1), graphCal.get(Calendar.YEAR)));

            ((TextView) view.findViewById(R.id.graph_header)).setText(requireActivity().getString(R.string.monthly_graph));

            view.findViewById(R.id.textLL1).setVisibility(View.GONE);
            view.findViewById(R.id.textLL2).setVisibility(View.GONE);
            view.findViewById(R.id.textLL3).setVisibility(View.GONE);
            view.findViewById(R.id.textLL4).setVisibility(View.GONE);
            view.findViewById(R.id.textLL5).setVisibility(View.GONE);
            view.findViewById(R.id.textLL6).setVisibility(View.GONE);
            view.findViewById(R.id.textLL7).setVisibility(View.GONE);
            view.findViewById(R.id.numberLL1).setVisibility(View.VISIBLE);
            view.findViewById(R.id.numberLL2).setVisibility(View.VISIBLE);
            view.findViewById(R.id.numberLL3).setVisibility(View.VISIBLE);
            view.findViewById(R.id.numberLL4).setVisibility(View.VISIBLE);
            view.findViewById(R.id.numberLL5).setVisibility(View.VISIBLE);
            view.findViewById(R.id.numberLL6).setVisibility(View.VISIBLE);
            view.findViewById(R.id.numberLL7).setVisibility(View.VISIBLE);

            int[] monthlyVals = new int[graphCal.get(Calendar.DAY_OF_MONTH)];
            int[] bottomGraphDisplay = { 0, 0, 0, 0, 0, 0, 0 };
            switch(graphCal.getActualMaximum(Calendar.DAY_OF_MONTH)){
                case 31:
                    bottomGraphDisplay = new int[]{ 1, 6, 11, 16, 21, 26, 31 };
                    break;
                case 30:
                    bottomGraphDisplay = new int[]{ 1, 6, 11, 16, 20, 25, 30 };
                    break;
                case 29:
                    bottomGraphDisplay = new int[]{ 1, 6, 11, 15, 19, 24, 29 };
                    break;
                case 28:
                    bottomGraphDisplay = new int[]{ 1, 6, 10, 15, 19, 23, 28 };
                    break;
            }

            int currentDate = Integer.parseInt((String) DateFormat.format("yyyyMMdd", graphCal));
            for(int currentDay = graphCal.get(Calendar.DAY_OF_MONTH); currentDay >= 1; currentDay--){
                monthlyVals[currentDay - 1] = (int) MDBHPedometer.getInstance(requireActivity()).readPedometerSteps(String.valueOf(currentDate));
                currentDate--;
            }

            setUpGraphValues(view,  monthlyVals, (float) graphCal.getActualMaximum(Calendar.DAY_OF_MONTH));

            ((TextView) view.findViewById(R.id.numberLL1)).setText(String.format("%02d", bottomGraphDisplay[0]));
            ((TextView) view.findViewById(R.id.numberLL2)).setText(String.format("%02d", bottomGraphDisplay[1]));
            ((TextView) view.findViewById(R.id.numberLL3)).setText(String.format("%02d", bottomGraphDisplay[2]));
            ((TextView) view.findViewById(R.id.numberLL4)).setText(String.format("%02d", bottomGraphDisplay[3]));
            ((TextView) view.findViewById(R.id.numberLL5)).setText(String.format("%02d", bottomGraphDisplay[4]));
            ((TextView) view.findViewById(R.id.numberLL6)).setText(String.format("%02d", bottomGraphDisplay[5]));
            ((TextView) view.findViewById(R.id.numberLL7)).setText(String.format("%02d", bottomGraphDisplay[6]));
        }
    }

    @SuppressLint("DefaultLocale")
    public void updateStepsData(View view){
        if(view == null)
            view = getView();

        if(view != null) {
            int currentSteps = (int) MDBHPedometer.getInstance(requireContext()).readPedometerSteps(getCurrentDateFormatted());
            ((TextView) view.findViewById(R.id.stepCountTextView)).setText(String.valueOf(currentSteps));
            int goalSteps = StepGoalFragment.getStepGoalForToday(requireActivity());

            ((ProgressBar) view.findViewById(R.id.stepsProgressBar)).setProgress(currentSteps * 100 / goalSteps);

            int remainingSteps = goalSteps - currentSteps;
            if(remainingSteps > 0) {
                view.findViewById(R.id.remainingStepsLayout).setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.remainingStepsTextView)).setText(String.valueOf(remainingSteps));
            } else
                view.findViewById(R.id.remainingStepsLayout).setVisibility(View.GONE);

            if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ)) {
                ((TextView) view.findViewById(R.id.caloriesUnit)).setText(R.string.kj);
                float kilojoulesBurned = getCaloriesBurnedFromSteps(currentSteps) * 4.184f;
                if(kilojoulesBurned > 10000) {
                    ((TextView) view.findViewById(R.id.caloriesNumber)).setText(String.format("%d", (int) kilojoulesBurned));
                } else
                    ((TextView) view.findViewById(R.id.caloriesNumber)).setText(String.format("%.1f", kilojoulesBurned));
            } else{
                ((TextView) view.findViewById(R.id.caloriesUnit)).setText(R.string.cal);
                if(getCaloriesBurnedFromSteps(currentSteps) > 10000) {
                    ((TextView) view.findViewById(R.id.caloriesNumber)).setText(String.format("%d", (int) getCaloriesBurnedFromSteps(currentSteps)));
                } else
                    ((TextView) view.findViewById(R.id.caloriesNumber)).setText(String.format("%.1f", getCaloriesBurnedFromSteps(currentSteps)));
            }

            if(UnitPreferenceFragment.getDistanceUnit(requireActivity()).equals(UnitPreferenceFragment.DISTANCE_UNIT_MILE)){
                ((TextView) view.findViewById(R.id.distanceUnit)).setText(R.string.mi);
                ((TextView) view.findViewById(R.id.distanceNumber)).setText(String.format("%.1f", getDistanceWalked(currentSteps) / 1.609f));
            } else{
                ((TextView) view.findViewById(R.id.distanceUnit)).setText(R.string.km);
                ((TextView) view.findViewById(R.id.distanceNumber)).setText(String.format("%.1f", getDistanceWalked(currentSteps)));
            }

            int[] steps = new int[7];
            int[] stepGoals = new int[7];

            steps[0] = (int) MDBHPedometer.getInstance(requireActivity()).readPedometerSteps(getCurrentDateFormatted());
            stepGoals[0] = StepGoalFragment.getStepGoalForToday(requireActivity());

            String[] dates = getLast7DatesFromEnd();
            for(int i = 1; i < 7; i ++){
                steps[i] = (int) MDBHPedometer.getInstance(requireActivity()).readPedometerSteps(dates[i]);
                stepGoals[i] = MDBHPedometer.getInstance(requireActivity()).readPedometerStepGoal(dates[i]);
            }

            ((TextView) view.findViewById(R.id.firstStep)).setText(String.format("%d/%d", steps[0], stepGoals[0]));
            ((ProgressBar) view.findViewById(R.id.firstProgress)).setProgress((int) (100.0f * steps[0] / stepGoals[0]));

            ((TextView) view.findViewById(R.id.secondStep)).setText(String.format("%d/%d", steps[1], stepGoals[1]));
            ((ProgressBar) view.findViewById(R.id.secondProgress)).setProgress((int) (100.0f * steps[1] / stepGoals[1]));

            ((TextView) view.findViewById(R.id.thirdStep)).setText(String.format("%d/%d", steps[2], stepGoals[2]));
            ((ProgressBar) view.findViewById(R.id.thirdProgress)).setProgress((int) (100.0f * steps[2] / stepGoals[2]));

            ((TextView) view.findViewById(R.id.fourthStep)).setText(String.format("%d/%d", steps[3], stepGoals[3]));
            ((ProgressBar) view.findViewById(R.id.fourthProgress)).setProgress((int) (100.0f * steps[3] / stepGoals[3]));

            ((TextView) view.findViewById(R.id.fifthStep)).setText(String.format("%d/%d", steps[4], stepGoals[4]));
            ((ProgressBar) view.findViewById(R.id.fifthProgress)).setProgress((int) (100.0f * steps[4] / stepGoals[4]));

            ((TextView) view.findViewById(R.id.sixthStep)).setText(String.format("%d/%d", steps[5], stepGoals[5]));
            ((ProgressBar) view.findViewById(R.id.sixthProgress)).setProgress((int) (100.0f * steps[5] / stepGoals[5]));

            ((TextView) view.findViewById(R.id.seventhStep)).setText(String.format("%d/%d", steps[6], stepGoals[6]));
            ((ProgressBar) view.findViewById(R.id.seventhProgress)).setProgress((int) (100.0f * steps[6] / stepGoals[6]));

            switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY:
                    ((TextView) view.findViewById(R.id.bigFirst)).setText(R.string.mo);
                    ((TextView) view.findViewById(R.id.bigSecond)).setText(R.string.su);
                    ((TextView) view.findViewById(R.id.bigThird)).setText(R.string.sa);
                    ((TextView) view.findViewById(R.id.bigFourth)).setText(R.string.fr);
                    ((TextView) view.findViewById(R.id.bigFifth)).setText(R.string.th);
                    ((TextView) view.findViewById(R.id.bigSixth)).setText(R.string.we);
                    ((TextView) view.findViewById(R.id.bigSeventh)).setText(R.string.tu);
                    break;
                case Calendar.TUESDAY:
                    ((TextView) view.findViewById(R.id.bigFirst)).setText(R.string.tu);
                    ((TextView) view.findViewById(R.id.bigSecond)).setText(R.string.mo);
                    ((TextView) view.findViewById(R.id.bigThird)).setText(R.string.su);
                    ((TextView) view.findViewById(R.id.bigFourth)).setText(R.string.sa);
                    ((TextView) view.findViewById(R.id.bigFifth)).setText(R.string.fr);
                    ((TextView) view.findViewById(R.id.bigSixth)).setText(R.string.th);
                    ((TextView) view.findViewById(R.id.bigSeventh)).setText(R.string.we);
                    break;
                case Calendar.WEDNESDAY:
                    ((TextView) view.findViewById(R.id.bigFirst)).setText(R.string.we);
                    ((TextView) view.findViewById(R.id.bigSecond)).setText(R.string.tu);
                    ((TextView) view.findViewById(R.id.bigThird)).setText(R.string.mo);
                    ((TextView) view.findViewById(R.id.bigFourth)).setText(R.string.su);
                    ((TextView) view.findViewById(R.id.bigFifth)).setText(R.string.sa);
                    ((TextView) view.findViewById(R.id.bigSixth)).setText(R.string.fr);
                    ((TextView) view.findViewById(R.id.bigSeventh)).setText(R.string.th);
                    break;
                case Calendar.THURSDAY:
                    ((TextView) view.findViewById(R.id.bigFirst)).setText(R.string.th);
                    ((TextView) view.findViewById(R.id.bigSecond)).setText(R.string.we);
                    ((TextView) view.findViewById(R.id.bigThird)).setText(R.string.tu);
                    ((TextView) view.findViewById(R.id.bigFourth)).setText(R.string.mo);
                    ((TextView) view.findViewById(R.id.bigFifth)).setText(R.string.su);
                    ((TextView) view.findViewById(R.id.bigSixth)).setText(R.string.sa);
                    ((TextView) view.findViewById(R.id.bigSeventh)).setText(R.string.fr);
                    break;
                case Calendar.FRIDAY:
                    ((TextView) view.findViewById(R.id.bigFirst)).setText(R.string.fr);
                    ((TextView) view.findViewById(R.id.bigSecond)).setText(R.string.th);
                    ((TextView) view.findViewById(R.id.bigThird)).setText(R.string.we);
                    ((TextView) view.findViewById(R.id.bigFourth)).setText(R.string.tu);
                    ((TextView) view.findViewById(R.id.bigFifth)).setText(R.string.mo);
                    ((TextView) view.findViewById(R.id.bigSixth)).setText(R.string.su);
                    ((TextView) view.findViewById(R.id.bigSeventh)).setText(R.string.sa);
                    break;
                case Calendar.SATURDAY:
                    ((TextView) view.findViewById(R.id.bigFirst)).setText(R.string.sa);
                    ((TextView) view.findViewById(R.id.bigSecond)).setText(R.string.fr);
                    ((TextView) view.findViewById(R.id.bigThird)).setText(R.string.th);
                    ((TextView) view.findViewById(R.id.bigFourth)).setText(R.string.we);
                    ((TextView) view.findViewById(R.id.bigFifth)).setText(R.string.tu);
                    ((TextView) view.findViewById(R.id.bigSixth)).setText(R.string.mo);
                    ((TextView) view.findViewById(R.id.bigSeventh)).setText(R.string.su);
                    break;
                case Calendar.SUNDAY:
                    ((TextView) view.findViewById(R.id.bigFirst)).setText(R.string.su);
                    ((TextView) view.findViewById(R.id.bigSecond)).setText(R.string.sa);
                    ((TextView) view.findViewById(R.id.bigThird)).setText(R.string.fr);
                    ((TextView) view.findViewById(R.id.bigFourth)).setText(R.string.th);
                    ((TextView) view.findViewById(R.id.bigFifth)).setText(R.string.we);
                    ((TextView) view.findViewById(R.id.bigSixth)).setText(R.string.tu);
                    ((TextView) view.findViewById(R.id.bigSeventh)).setText(R.string.mo);
                    break;
            }

            if(graphCal == null)
                graphCal = Calendar.getInstance();

            setUpGraph(view, null);

            View finalView = view;
            view.findViewById(R.id.weeklyMonthlySwitch).setOnClickListener(new View.OnClickListener() {
                boolean weekly = true;
                @Override
                public void onClick(View v) {
                    weekly = !weekly;
                    setUpGraph(finalView, weekly);
                }
            });

            view.findViewById(R.id.dateBefore).setOnClickListener(v -> {
                Boolean weeklyGraph = null;
                if(isWeeklyGraph(finalView)) {
                    graphCal.add(Calendar.WEEK_OF_YEAR, -1);

                    if(graphCal.get(Calendar.WEEK_OF_YEAR) == Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) && graphCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR))
                        weeklyGraph = true;
                    else
                        if(graphCal.before(Calendar.getInstance()))
                            graphCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                        else
                            graphCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                } else {
                    graphCal.add(Calendar.MONTH, -1);

                    if(graphCal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && graphCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR))
                        weeklyGraph = false;
                    else
                        if(graphCal.before(Calendar.getInstance()))
                            graphCal.set(Calendar.DAY_OF_MONTH, graphCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                        else
                            graphCal.set(Calendar.DAY_OF_MONTH, 1);
                }

                setUpGraph(finalView, weeklyGraph);
            });

            view.findViewById(R.id.dateAfter).setOnClickListener(v -> {
                Boolean weeklyGraph = null;
                if(isWeeklyGraph(finalView)) {
                    graphCal.add(Calendar.WEEK_OF_YEAR, 1);

                    if(graphCal.get(Calendar.WEEK_OF_YEAR) == Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) && graphCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR))
                        weeklyGraph = true;
                    else
                        if(graphCal.before(Calendar.getInstance()))
                            graphCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                        else
                            graphCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                } else {
                    graphCal.add(Calendar.MONTH, 1);

                    if(graphCal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && graphCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR))
                        weeklyGraph = false;
                    else
                        if(graphCal.before(Calendar.getInstance()))
                            graphCal.set(Calendar.DAY_OF_MONTH, graphCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                        else
                            graphCal.set(Calendar.DAY_OF_MONTH, 1);
                }

                setUpGraph(finalView, weeklyGraph);
            });

            float[] totals = getTotals();
            ((TextView) view.findViewById(R.id.totalSteps)).setText(String.format("%d\n%s", (int) totals[0], requireActivity().getString(R.string.steps_small)));

            if(UnitPreferenceFragment.getDistanceUnit(requireActivity()).equals(UnitPreferenceFragment.DISTANCE_UNIT_MILE))
                ((TextView) view.findViewById(R.id.totalDistance)).setText(String.format("%.1f\n%s", totals[1], requireActivity().getString(R.string.mi)));
            else
                ((TextView) view.findViewById(R.id.totalDistance)).setText(String.format("%.1f\n%s", totals[1], requireActivity().getString(R.string.km)));

            if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
                ((TextView) view.findViewById(R.id.totalCalories)).setText(String.format("%.1f\n%s", totals[2], requireActivity().getString(R.string.kj)));
            else
                ((TextView) view.findViewById(R.id.totalCalories)).setText(String.format("%.1f\n%s", totals[2], requireActivity().getString(R.string.cal)));

            List<String> streakList = MDBHPedometer.getInstance(requireActivity()).getMaxStreak();
            int maxStreak = Integer.parseInt(streakList.get(0));
            String streakDateStart = streakList.get(1);
            String streakDateEnd = streakList.get(2);

            List<String> stepsList = MDBHPedometer.getInstance(requireActivity()).getMaxSteps();
            int maxSteps = (int) Float.parseFloat(stepsList.get(0));
            String maxStepsDate = stepsList.get(1);

            if(streakDateStart == null || streakDateEnd == null || maxStreak == 0)
                view.findViewById(R.id.achievement2).setVisibility(View.GONE);
            else {
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

                ((TextView) view.findViewById(R.id.achievementDate2)).setText(String.format("%d %s %d - %d %s %d", startDay, getMonthShort(requireActivity(), startMonth), startYear, endDay, getMonthShort(requireActivity(), endMonth), endYear));
                ((TextView) view.findViewById(R.id.achievementHeader2)).setText(String.format("%d-%s\n%s", maxStreak, requireActivity().getString(R.string.day), requireActivity().getString(R.string.streak)));

                view.findViewById(R.id.achievement2).setVisibility(View.VISIBLE);
            }

            if(maxStepsDate == null || maxSteps == 0)
                view.findViewById(R.id.achievement1).setVisibility(View.GONE);
            else {
                int date = Integer.parseInt(maxStepsDate);
                int day = date % 100;
                date /= 100;
                int month = date % 100;
                date /= 100;
                int year = date;

                ((TextView) view.findViewById(R.id.achievementDate1)).setText(String.format("%d %s %d", day, getMonthShort(requireActivity(), month), year));
                ((TextView) view.findViewById(R.id.achievementHeader1)).setText(String.format("%d\n%s", maxSteps, requireActivity().getString(R.string.steps_in_a_day)));

                view.findViewById(R.id.achievement1).setVisibility(View.VISIBLE);
            }

        }
    }

    private void setUpStepCountingButton(View view, Boolean pedometerRuns){
        if(pedometerRuns == null)
            pedometerRuns = ServiceFunctional.getPedometerShouldRun(requireActivity());

        if(pedometerRuns){
            Animation flashyAnimation = new AlphaAnimation(0.0f, 1.0f);
            flashyAnimation.setDuration(400);
            flashyAnimation.setStartOffset(400);
            flashyAnimation.setRepeatMode(Animation.REVERSE);
            flashyAnimation.setRepeatCount(Animation.INFINITE);
            view.findViewById(R.id.pedometerLive).setVisibility(View.VISIBLE);
            view.findViewById(R.id.pedometerLive).startAnimation(flashyAnimation);
            ((TextView) view.findViewById(R.id.stepCountingButton)).setText(R.string.stop);
            Drawable stopDrawable = AppCompatResources.getDrawable(requireActivity(), R.drawable.pause);
            if(stopDrawable != null)
                stopDrawable.setTint(requireActivity().getColor(R.color.DarkBlueYonder));
            ((TextView) view.findViewById(R.id.stepCountingButton)).setCompoundDrawablesWithIntrinsicBounds(stopDrawable,null,null,null);
            view.findViewById(R.id.stepCountingButton).setOnClickListener(view1 -> {
                ServiceFunctional.setPedometerShouldRun(requireActivity(), false);
                ServiceFunctional.stopPedometerService(requireActivity());
            });
        } else{
            view.findViewById(R.id.pedometerLive).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.stepCountingButton)).setText(R.string.start);
            Drawable startDrawable = AppCompatResources.getDrawable(requireActivity(), R.drawable.play);
            if(startDrawable != null)
                startDrawable.setTint(requireActivity().getColor(R.color.DarkBlueYonder));
            ((TextView) view.findViewById(R.id.stepCountingButton)).setCompoundDrawablesWithIntrinsicBounds(startDrawable,null,null,null);
            view.findViewById(R.id.stepCountingButton).setOnClickListener(view1 -> PermissionFunctional.checkActivityRecognitionPermission(requireActivity(), ((InAppActivity) requireActivity()).activityRecognitionPermissionLauncher));
        }
    }

    private void setUpOnClickListeners(View view){
        setUpStepCountingButton(view, null);

        // used for setting your step goals
        view.findViewById(R.id.targetSteps).setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new StepGoalFragment()).addToBackStack(null).commit());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.pedometer_screen, container, false);
        setUpOnClickListeners(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getView() != null) {
            updateStepsData(getView());
            setUpCurrentDate(getView());
        }
    }
}
