package com.example.fitnessassistant.questions;

import static android.content.Context.MODE_PRIVATE;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;

import java.util.Calendar;

public class BirthdayFragment extends Fragment {
    private int year;
    private int month;
    private int day;

    public static synchronized int getBirthday(Context context){
        return context.getSharedPreferences("questions", MODE_PRIVATE).getInt("birthday", -1);
    }

    public synchronized static void putBirthday(Context context, int birthday){
        context.getSharedPreferences("questions", MODE_PRIVATE).edit().putInt("birthday", birthday).apply();
    }

    private boolean validDate(View view){
        if(year > Calendar.getInstance().get(Calendar.YEAR)) {
            view.findViewById(R.id.dateGiven).requestFocus();
            ((TextView) view.findViewById(R.id.dateGiven)).setError(getString(R.string.date_entered_not_valid));
            return false;
        } else if (year == Calendar.getInstance().get(Calendar.YEAR))
            if(month > Calendar.getInstance().get(Calendar.MONTH)) {
                view.findViewById(R.id.dateGiven).requestFocus();
                ((TextView) view.findViewById(R.id.dateGiven)).setError(getString(R.string.date_entered_not_valid));
                return false;
            } else if (month == Calendar.getInstance().get(Calendar.MONTH))
                if(day > Calendar.getInstance().get(Calendar.DAY_OF_MONTH)){
                    view.findViewById(R.id.dateGiven).requestFocus();
                    ((TextView) view.findViewById(R.id.dateGiven)).setError(getString(R.string.date_entered_not_valid));
                    return false;
                } else
                    return true;
            else
                return true;
        else
            return true;
    }

    private void updateDate(View view){
        ((TextView) view.findViewById(R.id.dateGiven)).setText(
                new StringBuilder()
                    .append(day >= 10 ? day : "0" + day).append("-")
                    .append(month >= 9 ? month + 1 : "0" + (month + 1)).append("-")
                    .append(year)
        );
    }

    private boolean yearValid(View view){
        int yearsPassed = Calendar.getInstance().get(Calendar.YEAR) - year;
        int monthDifference = Calendar.getInstance().get(Calendar.MONTH) - month;
        int dayDifference = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - day;

        if(yearsPassed < 14){
            view.findViewById(R.id.dateGiven).requestFocus();
            ((TextView) view.findViewById(R.id.dateGiven)).setError(getString(R.string.too_young));
            return false;
        } else if(yearsPassed == 14)
            if(monthDifference < 0){
                view.findViewById(R.id.dateGiven).requestFocus();
                ((TextView) view.findViewById(R.id.dateGiven)).setError(getString(R.string.too_young));
                return false;
            } else if(monthDifference == 0)
                if(dayDifference < 0){
                    view.findViewById(R.id.dateGiven).requestFocus();
                    ((TextView) view.findViewById(R.id.dateGiven)).setError(getString(R.string.too_young));
                    return false;
                } else {
                    congratulateBirthdayIfToday(dayDifference, monthDifference);
                    return true;
                }
            else {
                congratulateBirthdayIfToday(dayDifference, monthDifference);
                return true;
            }
        else {
            congratulateBirthdayIfToday(dayDifference, monthDifference);
            return true;
        }
    }

    private void congratulateBirthdayIfToday(int dayDifference, int monthDifference){
        if(dayDifference == 0 && monthDifference == 0)
            Toast.makeText(requireActivity(), R.string.happy_birthday, Toast.LENGTH_LONG).show();
    }

    private void setUpOnClickListeners(View view){
        year = Calendar.getInstance().get(Calendar.YEAR);
        month = Calendar.getInstance().get(Calendar.MONTH);
        day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        updateDate(view);

        view.findViewById(R.id.dateGiven).setOnClickListener(v ->  new DatePickerDialog(requireActivity(), (view1, y, m, d) -> {
            year = y;
            month = m;
            day = d;
            updateDate(view);
        }, year, month, day).show());
        
        view.findViewById(R.id.skipButton).setOnClickListener(v -> ((InAppActivity) requireActivity()).proceedQuestions(2));

        view.findViewById(R.id.proceedButton).setOnClickListener(v -> {
            if(validDate(view) && yearValid(view)) {
                putBirthday(requireActivity(), year * 10000 + month * 100 + day);
                ((InAppActivity) requireActivity()).proceedQuestions(2);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.birthday_screen, container, false);
        setUpOnClickListeners(view);
        return view;
    }
}
