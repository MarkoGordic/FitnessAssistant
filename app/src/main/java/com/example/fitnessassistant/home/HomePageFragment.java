package com.example.fitnessassistant.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.pedometer.Pedometer;
import com.example.fitnessassistant.pedometer.StepGoalFragment;
import com.example.fitnessassistant.util.PermissionFunctional;
import com.example.fitnessassistant.util.ServiceFunctional;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class HomePageFragment extends Fragment {

    public void setUpUI(boolean pedometerRuns){
        if(getView() != null)
            setUpStepCountingButton(getView(), pedometerRuns);
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

    // gives welcome message based on time
    private void greetUserAndShowSteps(View view){
        ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout)).setRefreshing(true);
        ((TextView) view.findViewById(R.id.stepCountTextView)).setText(String.valueOf((int) requireContext().getSharedPreferences("pedometer", Context.MODE_PRIVATE).getFloat(Pedometer.getCurrentDateFormatted(), 0)));
        ((TextView) view.findViewById(R.id.stepGoalTextView)).setText(String.valueOf(StepGoalFragment.getStepGoalForToday(requireActivity())));


        TextView welcomeTextView = view.findViewById(R.id.welcomeMessageTextView); // TextView in top right corner for welcome message

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());

        int systemHours = calendar.get(Calendar.HOUR_OF_DAY);

        if (systemHours >= 6 && systemHours < 12)
            welcomeTextView.setText(getString(R.string.good_morning));
        else if(systemHours >= 12 && systemHours < 18)
            welcomeTextView.setText(getString(R.string.good_afternoon));
        else if(systemHours >= 18 && systemHours < 22)
            welcomeTextView.setText(getString(R.string.good_evening));
        else
            welcomeTextView.setText(getString(R.string.good_night));
        if(FirebaseAuth.getInstance().getCurrentUser() != null) // greet the user, don't just say welcome :)
            welcomeTextView.setText(String.format("%s, %s", welcomeTextView.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
        else
            Toast.makeText(requireContext(), R.string.unable_to_refresh, Toast.LENGTH_SHORT).show();
        ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout)).setRefreshing(false);
    }

    private void setUpOnClickListeners(View view){
        setUpStepCountingButton(view, null);

        // swipeRefreshLayout refresh listener - refreshes for 1.5s while updating UI
        ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout)).setOnRefreshListener(() -> greetUserAndShowSteps(view));

        // used for setting your step goals
        view.findViewById(R.id.stepGoalFragmentTextView).setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new StepGoalFragment()).addToBackStack(null).commit());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.home_screen, container, false);
        setUpOnClickListeners(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getView() != null)
            greetUserAndShowSteps(getView());
    }
}
