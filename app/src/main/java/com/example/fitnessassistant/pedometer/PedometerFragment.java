package com.example.fitnessassistant.pedometer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.util.PermissionFunctional;
import com.example.fitnessassistant.util.ServiceFunctional;

public class PedometerFragment extends Fragment {

    public void setUpUI(boolean pedometerRuns){
        if(getView() != null)
            setUpStepCountingButton(getView(), pedometerRuns);
    }

    public void updateStepsData(View view){
        if(view == null)
            view = getView();

        if(view != null) {
            ((TextView) view.findViewById(R.id.stepCountTextView)).setText(String.valueOf((int) requireContext().getSharedPreferences("pedometer", Context.MODE_PRIVATE).getFloat(Pedometer.getCurrentDateFormatted(), 0)));
            ((TextView) view.findViewById(R.id.stepGoalTextView)).setText(String.valueOf(StepGoalFragment.getStepGoalForToday(requireActivity())));
        }
    }

    private void setUpStepCountingButton(View view, Boolean pedometerRuns){
        if(pedometerRuns == null)
            pedometerRuns = ServiceFunctional.getPedometerShouldRun(requireActivity());

        if(pedometerRuns){
            ((AppCompatButton) view.findViewById(R.id.stepCountingButton)).setText(R.string.stop_counting);
            view.findViewById(R.id.stepCountingButton).setOnClickListener(view1 -> {
                ServiceFunctional.setPedometerShouldRun(requireActivity(), false);
                ServiceFunctional.stopPedometerService(requireActivity());
            });
        } else{
            ((AppCompatButton) view.findViewById(R.id.stepCountingButton)).setText(R.string.start_counting);
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
        if(getView() != null)
            updateStepsData(getView());
    }
}
