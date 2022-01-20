package com.example.fitnessassistant.home;

import static com.example.fitnessassistant.util.TimeFunctional.getCurrentDateFormatted;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.database.data.SleepSegment;
import com.example.fitnessassistant.database.mdbh.MDBHPedometer;
import com.example.fitnessassistant.database.mdbh.MDBHSleepTracker;
import com.example.fitnessassistant.pedometer.StepGoalFragment;
import com.example.fitnessassistant.util.ClockView;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class HomePageFragment extends Fragment {

    private void goToPedometerFragment(){
        requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, InAppActivity.pedometerFragment).addToBackStack(null).commit();
    }

    public void updateStepsData(View view){
        if(view == null)
            view = getView();

        if(view != null) {
            ((TextView) view.findViewById(R.id.stepsActivityTV)).setText(String.valueOf((int) MDBHPedometer.getInstance(requireContext()).readPedometerSteps(getCurrentDateFormatted())));
            ((ProgressBar) view.findViewById(R.id.dailyStepsProgressBar)).setProgress((int) (100 * MDBHPedometer.getInstance(requireContext()).readPedometerSteps(getCurrentDateFormatted()) / StepGoalFragment.getStepGoalForToday(requireActivity())));
        }
    }

    @SuppressLint("DefaultLocale")
    private void setUpClock(View view){
        ClockView clock = view.findViewById(R.id.clock);
        SleepSegment todaySleepSegment = MDBHSleepTracker.getInstance(requireActivity()).getSleepSegmentForDateFromDB(getCurrentDateFormatted());
        if(todaySleepSegment != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(todaySleepSegment.getStartTime());
            float startHours = cal.get(Calendar.HOUR);
            startHours += cal.get(Calendar.MINUTE) / 60f;
            startHours += cal.get(Calendar.SECOND) / 3600f;

            cal.setTimeInMillis(todaySleepSegment.getEndTime());
            float endHours = cal.get(Calendar.HOUR);
            endHours += cal.get(Calendar.MINUTE) / 60f;
            endHours += cal.get(Calendar.SECOND) / 3600f;

            clock.setStartHours(startHours);
            clock.setHoursSlept(endHours - startHours);
            ((TextView) view.findViewById(R.id.hoursSlept)).setText(String.format("%.1f\n%s", endHours - startHours, requireActivity().getString(R.string.hours_small)));
        } else{
            clock.setStartHours(0f);
            clock.setHoursSlept(0f);
            ((TextView) view.findViewById(R.id.hoursSlept)).setText(String.format("?\n%s", requireActivity().getString(R.string.hours_small)));
        }
        clock.setFontSize(11f);
        clock.setNumPadding(5f);
        clock.invalidate();
    }

    // TODO call when received sleep updates
    private void updateSleepData(View view){
        if(view == null)
            view = getView();

        if(view != null)
            setUpClock(view);
    }

    // gives welcome message based on time
    private void setUpUI(View view){
        ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout)).setRefreshing(true);

        updateStepsData(view);
        updateSleepData(view);

        TextView welcomeTextView = view.findViewById(R.id.welcomeMessageTextView); // TextView in top right corner for welcome message

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());

        int systemHours = calendar.get(Calendar.HOUR_OF_DAY);

        if (systemHours >= 6 && systemHours < 12)
            welcomeTextView.setText(String.format("%s,", getString(R.string.good_morning)));
        else if(systemHours >= 12 && systemHours < 18)
            welcomeTextView.setText(String.format("%s,", getString(R.string.good_afternoon)));
        else if(systemHours >= 18 && systemHours < 22)
            welcomeTextView.setText(String.format("%s,", getString(R.string.good_evening)));
        else
            welcomeTextView.setText(String.format("%s,", getString(R.string.good_night)));

        if(FirebaseAuth.getInstance().getCurrentUser() != null) // greet the user, don't just say welcome :)
            ((TextView) view.findViewById(R.id.userTextView)).setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        else
            Toast.makeText(requireContext(), R.string.unable_to_refresh, Toast.LENGTH_SHORT).show();
        ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout)).setRefreshing(false);

        Picasso.with(requireActivity()).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).placeholder(R.drawable.user_focused).resize(60,60).centerCrop().into((ImageView) requireView().findViewById(R.id.pfp));
    }

    private void setUpOnClickListeners(View view) {
        // swipeRefreshLayout refresh listener - refreshes for 1.5s while updating UI
        ((SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout)).setOnRefreshListener(() -> setUpUI(view));

        // pedometerFragment listener - goes to PedometerFragment
        view.findViewById(R.id.pedometerFragment).setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, InAppActivity.pedometerFragment).addToBackStack(null).commit());

        // sleepFragment listener - goes to SleepFragment
        view.findViewById(R.id.sleepFragment).setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, InAppActivity.sleepFragment).addToBackStack(null).commit());
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
            setUpUI(getView());
        if(getActivity() != null) {
            String desiredFragment = ((InAppActivity) getActivity()).getDesiredFragment();
            if (desiredFragment != null) {
                if (desiredFragment.equals("PedometerFragment")) {
                    ((InAppActivity) getActivity()).putDesiredFragment(null);
                    goToPedometerFragment();
                }
            }
        }
    }
}
