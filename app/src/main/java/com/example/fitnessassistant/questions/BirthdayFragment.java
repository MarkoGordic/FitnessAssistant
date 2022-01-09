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
import androidx.appcompat.widget.AppCompatButton;
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
        int birthday = getBirthday(requireActivity());
        if(birthday != -1){
            day = birthday % 100;
            birthday /= 100;
            month = birthday % 100;
            birthday /= 100;
            year = birthday;
        } else {
            day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            month = Calendar.getInstance().get(Calendar.MONTH);
            year = Calendar.getInstance().get(Calendar.YEAR);
        }

        updateDate(view);

        view.findViewById(R.id.dateGiven).setOnClickListener(v -> new DatePickerDialog(requireActivity(), (view1, y, m, d) -> {
            year = y;
            month = m;
            day = d;
            updateDate(view);
        }, year, month, day).show());

        if(!InAppActivity.useNewPersonalDataFragments.get()) {
            view.findViewById(R.id.skipButton).setOnClickListener(v -> ((InAppActivity) requireActivity()).proceedQuestions(2));

            view.findViewById(R.id.proceedButton).setOnClickListener(v -> {
                if (validDate(view) && yearValid(view)) {
                    putBirthday(requireActivity(), year * 10000 + month * 100 + day);
                    ((InAppActivity) requireActivity()).proceedQuestions(2);
                }
            });
        } else{
            InAppActivity.useNewPersonalDataFragments.set(false);
            ((AppCompatButton) view.findViewById(R.id.skipButton)).setText(R.string.cancel);
            ((AppCompatButton) view.findViewById(R.id.proceedButton)).setText(R.string.set);

            view.findViewById(R.id.skipButton).setOnClickListener(v -> requireActivity().onBackPressed());
            view.findViewById(R.id.proceedButton).setOnClickListener(v -> {
                if (validDate(view) && yearValid(view)) {
                    putBirthday(requireActivity(), year * 10000 + month * 100 + day);
                    requireActivity().onBackPressed();
                }
            });
        }
    }

    public static synchronized int getYears(Context context){
        int birthday = getBirthday(context);
        if(birthday != -1) {
            int day = birthday % 100;
            birthday /= 100;
            int month = birthday % 100;
            birthday /= 100;
            int year = birthday;
            int yearsPassed = Calendar.getInstance().get(Calendar.YEAR) - year;
            int monthDifference = Calendar.getInstance().get(Calendar.MONTH) - month;
            int dayDifference = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) - day;

            if (monthDifference > 0) {
                return yearsPassed;
            } else if (monthDifference == 0) {
                if (dayDifference >= 0)
                    return yearsPassed;
                else
                    return yearsPassed - 1;
            } else
                return yearsPassed - 1;
        } else
            return -1;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.birthday_screen, container, false);
        setUpOnClickListeners(view);
        return view;
    }
}
