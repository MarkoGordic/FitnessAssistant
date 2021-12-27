package com.example.fitnessassistant.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.util.PermissionFunctional;
import com.example.fitnessassistant.util.ServiceFunctional;

//  todo add step goal
//      add questions...

public class MapPageFragment extends Fragment {

    public void setUpUI(boolean pedometerRuns){
        if(getView() != null)
            setUpOnClickListeners(getView(), pedometerRuns);
    }

    private void setUpOnClickListeners(View view, Boolean pedometerRuns){
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_screen, container, false);
        setUpOnClickListeners(view, null);
        return view;
    }
}
