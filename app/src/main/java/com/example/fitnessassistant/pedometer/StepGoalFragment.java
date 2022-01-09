package com.example.fitnessassistant.pedometer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.util.ServiceFunctional;

import java.util.Calendar;

public class StepGoalFragment extends Fragment {

    private synchronized void putMondayStepGoal(int stepGoal){
        requireActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit().putInt("mondayStepGoal", stepGoal).apply();
    }

    private synchronized void putTuesdayStepGoal(int stepGoal){
        requireActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit().putInt("tuesdayStepGoal", stepGoal).apply();
    }

    private synchronized void putWednesdayStepGoal(int stepGoal){
        requireActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit().putInt("wednesdayStepGoal", stepGoal).apply();
    }

    private synchronized void putThursdayStepGoal(int stepGoal){
        requireActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit().putInt("thursdayStepGoal", stepGoal).apply();
    }

    private synchronized void putFridayStepGoal(int stepGoal){
        requireActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit().putInt("fridayStepGoal", stepGoal).apply();
    }

    private synchronized void putSaturdayStepGoal(int stepGoal){
        requireActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit().putInt("saturdayStepGoal", stepGoal).apply();
    }

    private synchronized void putSundayStepGoal(int stepGoal){
        requireActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit().putInt("sundayStepGoal", stepGoal).apply();
    }

    private synchronized static int getMondayStepGoal(Context context){
        return context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).getInt("mondayStepGoal", 10000);
    }

    private synchronized static int getTuesdayStepGoal(Context context){
        return context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).getInt("tuesdayStepGoal", 10000);
    }

    private synchronized static int getWednesdayStepGoal(Context context){
        return context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).getInt("wednesdayStepGoal", 10000);
    }

    private synchronized static int getThursdayStepGoal(Context context){
        return context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).getInt("thursdayStepGoal", 10000);
    }

    private synchronized static int getFridayStepGoal(Context context){
        return context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).getInt("fridayStepGoal", 10000);
    }

    private synchronized static int getSaturdayStepGoal(Context context){
        return context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).getInt("saturdayStepGoal", 10000);
    }

    private synchronized static int getSundayStepGoal(Context context){
        return context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).getInt("sundayStepGoal", 10000);
    }

    public static int getStepGoalForToday(Context context){
        switch(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)){
            case Calendar.MONDAY:
                return getMondayStepGoal(context);
            case Calendar.TUESDAY:
                return getTuesdayStepGoal(context);
            case Calendar.WEDNESDAY:
                return getWednesdayStepGoal(context);
            case Calendar.THURSDAY:
                return getThursdayStepGoal(context);
            case Calendar.FRIDAY:
                return getFridayStepGoal(context);
            case Calendar.SATURDAY:
                return getSaturdayStepGoal(context);
            case Calendar.SUNDAY:
                return getSundayStepGoal(context);
            default:
                return 10000;
        }
    }

    private boolean allFieldsFilled(View view){
        EditText mondayEdt = view.findViewById(R.id.mondayEdt);
        EditText tuesdayEdt = view.findViewById(R.id.tuesdayEdt);
        EditText wednesdayEdt = view.findViewById(R.id.wednesdayEdt);
        EditText thursdayEdt = view.findViewById(R.id.thursdayEdt);
        EditText fridayEdt = view.findViewById(R.id.fridayEdt);
        EditText saturdayEdt = view.findViewById(R.id.saturdayEdt);
        EditText sundayEdt = view.findViewById(R.id.sundayEdt);

        return mondayEdt.getText().length() != 0 &&
                tuesdayEdt.getText().length() != 0 &&
                wednesdayEdt.getText().length() != 0 &&
                thursdayEdt.getText().length() != 0 &&
                fridayEdt.getText().length() != 0 &&
                saturdayEdt.getText().length() != 0 &&
                sundayEdt.getText().length() != 0;
    }

    private void setUpOnClickListeners(View view){
        EditText allEdt = view.findViewById(R.id.allEdt);
        EditText mondayEdt = view.findViewById(R.id.mondayEdt);
        EditText tuesdayEdt = view.findViewById(R.id.tuesdayEdt);
        EditText wednesdayEdt = view.findViewById(R.id.wednesdayEdt);
        EditText thursdayEdt = view.findViewById(R.id.thursdayEdt);
        EditText fridayEdt = view.findViewById(R.id.fridayEdt);
        EditText saturdayEdt = view.findViewById(R.id.saturdayEdt);
        EditText sundayEdt = view.findViewById(R.id.sundayEdt);

        mondayEdt.setText(String.valueOf(getMondayStepGoal(requireActivity())));
        tuesdayEdt.setText(String.valueOf(getTuesdayStepGoal(requireActivity())));
        wednesdayEdt.setText(String.valueOf(getWednesdayStepGoal(requireActivity())));
        thursdayEdt.setText(String.valueOf(getThursdayStepGoal(requireActivity())));
        fridayEdt.setText(String.valueOf(getFridayStepGoal(requireActivity())));
        saturdayEdt.setText(String.valueOf(getSaturdayStepGoal(requireActivity())));
        sundayEdt.setText(String.valueOf(getSundayStepGoal(requireActivity())));

        view.findViewById(R.id.setAllButton).setOnClickListener(v ->{
            if(allEdt.getText().length() != 0){
                int allSteps = Integer.parseInt(allEdt.getText().toString());
                mondayEdt.setText(String.valueOf(allSteps));
                tuesdayEdt.setText(String.valueOf(allSteps));
                tuesdayEdt.setText(String.valueOf(allSteps));
                wednesdayEdt.setText(String.valueOf(allSteps));
                thursdayEdt.setText(String.valueOf(allSteps));
                fridayEdt.setText(String.valueOf(allSteps));
                saturdayEdt.setText(String.valueOf(allSteps));
                sundayEdt.setText(String.valueOf(allSteps));
            }
        });

        view.findViewById(R.id.cancelButton).setOnClickListener(v -> requireActivity().onBackPressed());
        view.findViewById(R.id.setButton).setOnClickListener(v -> {
            if(allFieldsFilled(view)){
                putMondayStepGoal(Integer.parseInt(mondayEdt.getText().toString()));
                putTuesdayStepGoal(Integer.parseInt(tuesdayEdt.getText().toString()));
                putWednesdayStepGoal(Integer.parseInt(wednesdayEdt.getText().toString()));
                putThursdayStepGoal(Integer.parseInt(thursdayEdt.getText().toString()));
                putFridayStepGoal(Integer.parseInt(fridayEdt.getText().toString()));
                putSaturdayStepGoal(Integer.parseInt(saturdayEdt.getText().toString()));
                putSundayStepGoal(Integer.parseInt(sundayEdt.getText().toString()));

                if (ServiceFunctional.getPedometerShouldRun(requireActivity())) {
                    Pedometer.updatePedometerWidgetData(requireActivity(), ((int) requireActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE).getFloat(Pedometer.getCurrentDateFormatted(), 0)), getStepGoalForToday(requireActivity()));
                    Pedometer.pushPedometerNotification(requireActivity(), ((int) requireActivity().getSharedPreferences("pedometer", Context.MODE_PRIVATE).getFloat(Pedometer.getCurrentDateFormatted(), 0)) + " " + requireActivity().getString(R.string.steps_small), requireActivity().getString(R.string.your_today_goal) + " " + getStepGoalForToday(requireActivity()) + ".");
                }
                requireActivity().onBackPressed();
            } else
                Toast.makeText(requireActivity(), R.string.fill_up_all_fields, Toast.LENGTH_LONG).show();
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.step_goal_screen, container, false);
        // setting up the view
        setUpOnClickListeners(view);
        return view;
    }
}
