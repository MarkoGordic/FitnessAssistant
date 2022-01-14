package com.example.fitnessassistant.pedometer;

import static com.example.fitnessassistant.util.TimeFunctional.getCurrentDateFormatted;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.database.mdbh.MDBHPedometer;
import com.example.fitnessassistant.util.ServiceFunctional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class StepGoalFragment extends Fragment {

    public synchronized static void putUnsavedStepGoals(Context context){
        // getting last date saved in db
        String date = MDBHPedometer.getInstance(context).findLatestDayInDB();


        if(date == null) {
            try {
                date = (String) DateFormat.format("yyyyMMdd", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).firstInstallTime);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        if(date != null) {
            int latestDate = Integer.parseInt(date);

            int latestDay = latestDate % 100;
            latestDate /= 100;
            int latestMonth = latestDate % 100;
            latestDate /= 100;
            int latestYear = latestDate;

            for(LocalDate localDate = LocalDate.of(latestYear, latestMonth, latestDay); localDate.isBefore(LocalDateTime.ofInstant(Calendar.getInstance().toInstant(), Calendar.getInstance().getTimeZone().toZoneId()).toLocalDate());localDate = localDate.plusDays(1)){
                switch (localDate.getDayOfWeek()){
                    case MONDAY:
                        MDBHPedometer.getInstance(context).putPedometerData(context, localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")), null, getMondayStepGoal(context));
                        break;
                    case TUESDAY:
                        MDBHPedometer.getInstance(context).putPedometerData(context, localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")), null, getTuesdayStepGoal(context));
                        break;
                    case WEDNESDAY:
                        MDBHPedometer.getInstance(context).putPedometerData(context, localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")), null, getWednesdayStepGoal(context));
                        break;
                    case THURSDAY:
                        MDBHPedometer.getInstance(context).putPedometerData(context, localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")), null, getThursdayStepGoal(context));
                        break;
                    case FRIDAY:
                        MDBHPedometer.getInstance(context).putPedometerData(context, localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")), null, getFridayStepGoal(context));
                        break;
                    case SATURDAY:
                        MDBHPedometer.getInstance(context).putPedometerData(context, localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")), null, getSaturdayStepGoal(context));
                        break;
                    case SUNDAY:
                        MDBHPedometer.getInstance(context).putPedometerData(context, localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")), null, getSundayStepGoal(context));
                        break;
                }
            }
        }
    }

    public synchronized static void putMondayStepGoal(Context context, int stepGoal){
        context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit().putInt("mondayStepGoal", stepGoal).apply();
    }

    public synchronized static void putTuesdayStepGoal(Context context, int stepGoal){
        context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit().putInt("tuesdayStepGoal", stepGoal).apply();
    }

    public synchronized static void putWednesdayStepGoal(Context context, int stepGoal){
        context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit().putInt("wednesdayStepGoal", stepGoal).apply();
    }

    public synchronized static void putThursdayStepGoal(Context context, int stepGoal){
        context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit().putInt("thursdayStepGoal", stepGoal).apply();
    }

    public synchronized static void putFridayStepGoal(Context context, int stepGoal){
        context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit().putInt("fridayStepGoal", stepGoal).apply();
    }

    public synchronized static void putSaturdayStepGoal(Context context, int stepGoal){
        context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit().putInt("saturdayStepGoal", stepGoal).apply();
    }

    public synchronized static void putSundayStepGoal(Context context, int stepGoal){
        context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit().putInt("sundayStepGoal", stepGoal).apply();
    }

    public synchronized static int getMondayStepGoal(Context context){
        return context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).getInt("mondayStepGoal", 10000);
    }

    public synchronized static int getTuesdayStepGoal(Context context){
        return context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).getInt("tuesdayStepGoal", 10000);
    }

    public synchronized static int getWednesdayStepGoal(Context context){
        return context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).getInt("wednesdayStepGoal", 10000);
    }

    public synchronized static int getThursdayStepGoal(Context context){
        return context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).getInt("thursdayStepGoal", 10000);
    }

    public synchronized static int getFridayStepGoal(Context context){
        return context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).getInt("fridayStepGoal", 10000);
    }

    public synchronized static int getSaturdayStepGoal(Context context){
        return context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).getInt("saturdayStepGoal", 10000);
    }

    public synchronized static int getSundayStepGoal(Context context){
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

    private boolean allFieldsProperlyFilled(View view){
        EditText mondayEdt = view.findViewById(R.id.mondayEdt);
        EditText tuesdayEdt = view.findViewById(R.id.tuesdayEdt);
        EditText wednesdayEdt = view.findViewById(R.id.wednesdayEdt);
        EditText thursdayEdt = view.findViewById(R.id.thursdayEdt);
        EditText fridayEdt = view.findViewById(R.id.fridayEdt);
        EditText saturdayEdt = view.findViewById(R.id.saturdayEdt);
        EditText sundayEdt = view.findViewById(R.id.sundayEdt);

        boolean correctInput = true;

        if (mondayEdt.getText().length() == 0) {
            mondayEdt.setError(requireActivity().getString(R.string.step_goal_cannot_be_empty));
            correctInput = false;
        } else if (mondayEdt.getText().toString().equals("0")) {
            mondayEdt.setError(requireActivity().getString(R.string.step_goal_cannot_be_zero));
            correctInput = false;
        }

        if (tuesdayEdt.getText().length() == 0) {
            tuesdayEdt.setError(requireActivity().getString(R.string.step_goal_cannot_be_empty));
            correctInput = false;
        } else if (tuesdayEdt.getText().toString().equals("0")) {
            tuesdayEdt.setError(requireActivity().getString(R.string.step_goal_cannot_be_zero));
            correctInput = false;
        }

        if (wednesdayEdt.getText().length() == 0) {
            wednesdayEdt.setError(requireActivity().getString(R.string.step_goal_cannot_be_empty));
            correctInput = false;
        } else if (wednesdayEdt.getText().toString().equals("0")) {
            wednesdayEdt.setError(requireActivity().getString(R.string.step_goal_cannot_be_zero));
            correctInput = false;
        }

        if (thursdayEdt.getText().length() == 0) {
            thursdayEdt.setError(requireActivity().getString(R.string.step_goal_cannot_be_empty));
            correctInput = false;
        } else if (thursdayEdt.getText().toString().equals("0")) {
            thursdayEdt.setError(requireActivity().getString(R.string.step_goal_cannot_be_zero));
            correctInput = false;
        }

        if (fridayEdt.getText().length() == 0) {
            fridayEdt.setError(requireActivity().getString(R.string.step_goal_cannot_be_empty));
            correctInput = false;
        } else if (fridayEdt.getText().toString().equals("0")) {
            fridayEdt.setError(requireActivity().getString(R.string.step_goal_cannot_be_zero));
            correctInput = false;
        }

        if (saturdayEdt.getText().length() == 0) {
            saturdayEdt.setError(requireActivity().getString(R.string.step_goal_cannot_be_empty));
            correctInput = false;
        } else if (saturdayEdt.getText().toString().equals("0")) {
            saturdayEdt.setError(requireActivity().getString(R.string.step_goal_cannot_be_zero));
            correctInput = false;
        }

        if (sundayEdt.getText().length() == 0) {
            sundayEdt.setError(requireActivity().getString(R.string.step_goal_cannot_be_empty));
            correctInput = false;
        } else if (sundayEdt.getText().toString().equals("0")) {
            sundayEdt.setError(requireActivity().getString(R.string.step_goal_cannot_be_zero));
            correctInput = false;
        }

        return  correctInput;
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
            if(allFieldsProperlyFilled(view)){
                putMondayStepGoal(requireActivity(), Integer.parseInt(mondayEdt.getText().toString()));
                putTuesdayStepGoal(requireActivity(), Integer.parseInt(tuesdayEdt.getText().toString()));
                putWednesdayStepGoal(requireActivity(), Integer.parseInt(wednesdayEdt.getText().toString()));
                putThursdayStepGoal(requireActivity(), Integer.parseInt(thursdayEdt.getText().toString()));
                putFridayStepGoal(requireActivity(), Integer.parseInt(fridayEdt.getText().toString()));
                putSaturdayStepGoal(requireActivity(), Integer.parseInt(saturdayEdt.getText().toString()));
                putSundayStepGoal(requireActivity(), Integer.parseInt(sundayEdt.getText().toString()));

                if (ServiceFunctional.getPedometerShouldRun(requireActivity())) {
                    Pedometer.updatePedometerWidgetData(requireActivity(), ((int) MDBHPedometer.getInstance(requireContext()).readPedometerSteps(getCurrentDateFormatted())), getStepGoalForToday(requireActivity()));
                    Pedometer.pushPedometerNotification(requireActivity(), ((int) MDBHPedometer.getInstance(requireContext()).readPedometerSteps(getCurrentDateFormatted())) + " " + requireActivity().getString(R.string.steps_small), requireActivity().getString(R.string.your_today_goal) + " " + getStepGoalForToday(requireActivity()) + ".");
                }
                requireActivity().onBackPressed();
            }
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
