package com.example.fitnessassistant.pedometer;

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
import com.example.fitnessassistant.profile.GoalsFragment;
import com.example.fitnessassistant.questions.GenderFragment;
import com.example.fitnessassistant.questions.HeightFragment;
import com.example.fitnessassistant.questions.WeightFragment;
import com.example.fitnessassistant.util.PermissionFunctional;
import com.example.fitnessassistant.util.ServiceFunctional;

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

        caloriesBurnedInAMinute = (float) (((0.035f * weight) + ((1.4 * 1.4) / height / 100)) * 0.029f * weight);

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
        int currentDate = Integer.parseInt(Pedometer.getCurrentDateFormatted());
        int day = currentDate % 100;
        currentDate /= 100;
        int month = currentDate % 100;
        currentDate /= 100;
        int year = currentDate;
        ((TextView) view.findViewById(R.id.currentDate)).setText(String.format("%s %s %s", day, GoalsFragment.getMonthShort(month), year));
    }

    public void updateStepsData(View view){
        if(view == null)
            view = getView();

        if(view != null) {
            int currentSteps = (int) MDBHPedometer.getInstance(requireContext()).readPedometerSteps(Pedometer.getCurrentDateFormatted());
            ((TextView) view.findViewById(R.id.stepCountTextView)).setText(String.valueOf(currentSteps));
            int goalSteps = StepGoalFragment.getStepGoalForToday(requireActivity());

            ((ProgressBar) view.findViewById(R.id.stepsProgressBar)).setProgress(currentSteps * 100 / goalSteps);

            int remainingSteps = goalSteps - currentSteps;
            if(remainingSteps > 0) {
                view.findViewById(R.id.remainingStepsLayout).setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.remainingStepsTextView)).setText(String.valueOf(remainingSteps));
            } else
                view.findViewById(R.id.remainingStepsLayout).setVisibility(View.GONE);
        }
    }

    private void setUpStepCountingButton(View view, Boolean pedometerRuns){
        if(pedometerRuns == null)
            pedometerRuns = ServiceFunctional.getPedometerShouldRun(requireActivity());

        if(pedometerRuns){
            Animation flashyAnimation = new AlphaAnimation(0.4f, 1.0f);
            flashyAnimation.setDuration(400);
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
        view.findViewById(R.id.stepGoalFragmentTextView).setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new StepGoalFragment()).addToBackStack(null).commit());
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
