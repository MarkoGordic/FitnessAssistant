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
import com.example.fitnessassistant.sleeptracker.SleepDateFragment;
import com.example.fitnessassistant.sleeptracker.SleepFragment;
import com.example.fitnessassistant.util.ClockView;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class HomePageFragment extends Fragment {

    private void goToSleepFragment(){
        requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, InAppActivity.sleepFragment).addToBackStack(null).commit();
    }

    private void goToSleepDateFragment(){
        boolean addSleep = true, addSleepDate = true;
        for(Fragment f : requireActivity().getSupportFragmentManager().getFragments()){
            if(f instanceof SleepFragment) {
                addSleep = false;
            } else if (f instanceof SleepDateFragment) {
                addSleepDate = false;
            }
        }

        Calendar cal = Calendar.getInstance();
        String date = InAppActivity.getSleepDate(requireActivity());
        int day = cal.get(Calendar.DAY_OF_MONTH), month = cal.get(Calendar.MONTH) + 1, year = cal.get(Calendar.YEAR);
        if(date != null) {
            int dateInt = Integer.parseInt(date);
            day = dateInt % 100;
            dateInt /= 100;
            month = dateInt % 100;
            dateInt /= 100;
            year = dateInt;
        }

        if(addSleep)
            requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, InAppActivity.sleepFragment).addToBackStack(null).hide(InAppActivity.sleepFragment).add(R.id.in_app_container, new SleepDateFragment(day, month, year)).addToBackStack(null).commit();
        else if(addSleepDate)
            requireActivity().getSupportFragmentManager().beginTransaction().hide(InAppActivity.sleepFragment).add(R.id.in_app_container, new SleepDateFragment(day, month, year)).addToBackStack(null).commit();
    }

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

    public void updateNutritionData(View view){
        if(view == null)
            view = getView();

        if(view != null){
            // TODO
        }
    }

    @SuppressLint("DefaultLocale")
    private void setUpClock(View view){
        ClockView clock = view.findViewById(R.id.clock);
        SleepSegment todaySleepSegment = MDBHSleepTracker.getInstance(requireActivity()).getSleepSegmentForDateFromDB(getCurrentDateFormatted());
        if(todaySleepSegment != null) {
            Calendar cal =  Calendar.getInstance();
            cal.setTimeInMillis(todaySleepSegment.getStartTime());
            float startHours = cal.get(Calendar.HOUR_OF_DAY);
            startHours += cal.get(Calendar.MINUTE) / 60f;
            startHours += cal.get(Calendar.SECOND) / 3600f;
            startHours += cal.get(Calendar.MILLISECOND) / 3600000f;
            float hoursSlept = todaySleepSegment.getDuration() / 3600000f;

            clock.setStartHours(startHours);
            clock.setHoursSlept(hoursSlept);
            ((TextView) view.findViewById(R.id.hoursSlept)).setText(String.format("%.1f", hoursSlept));
        } else{
            clock.setStartHours(0f);
            clock.setHoursSlept(0f);
            ((TextView) view.findViewById(R.id.hoursSlept)).setText("?");
        }
        clock.setFontSize(11f);
        clock.setNumPadding(5f);
        clock.invalidate();
    }

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
        view.findViewById(R.id.pedometerFragment).setOnClickListener(v -> goToPedometerFragment());

        // sleepFragment listener - goes to SleepFragment
        view.findViewById(R.id.sleepFragment).setOnClickListener(v -> goToSleepFragment());
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
                switch (desiredFragment) {
                    case "PedometerFragment":
                        ((InAppActivity) getActivity()).putDesiredFragment(null);
                        goToPedometerFragment();
                        break;
                    case "SleepDateFragment":
                        ((InAppActivity) getActivity()).putDesiredFragment(null);
                        goToSleepDateFragment();
                        break;
                    case "SleepFragment":
                        ((InAppActivity) getActivity()).putDesiredFragment(null);
                        goToSleepFragment();
                        break;
                }
            }
        }
    }
}
