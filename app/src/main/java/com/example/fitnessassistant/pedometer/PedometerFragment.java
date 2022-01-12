package com.example.fitnessassistant.pedometer;

import static com.example.fitnessassistant.util.TimeFunctional.getCurrentDateFormatted;
import static com.example.fitnessassistant.util.TimeFunctional.getLast7DatesFromEnd;
import static com.example.fitnessassistant.util.TimeFunctional.getMonthShort;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.example.fitnessassistant.database.MDBHPedometer;
import com.example.fitnessassistant.questions.GenderFragment;
import com.example.fitnessassistant.questions.HeightFragment;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;
import com.example.fitnessassistant.questions.WeightFragment;
import com.example.fitnessassistant.util.PermissionFunctional;
import com.example.fitnessassistant.util.ServiceFunctional;

import java.util.Calendar;

public class PedometerFragment extends Fragment {

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
        ((TextView) view.findViewById(R.id.currentDate)).setText(String.format("%s %s %s", day, getMonthShort(month), year));
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

            for(int i = 0; i < 7; i ++){
                String[] dates = getLast7DatesFromEnd();
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
