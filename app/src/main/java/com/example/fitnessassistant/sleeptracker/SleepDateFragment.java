package com.example.fitnessassistant.sleeptracker;

import static com.example.fitnessassistant.util.TimeFunctional.getMonthLong;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.util.RangeSeekBar;

public class SleepDateFragment extends Fragment {
    private final int day;
    private final int month;
    private final int year;


    public SleepDateFragment(int day, int month, int year){
        this.day = day;
        this.month = month;
        this.year = year;
    }

    @SuppressLint("DefaultLocale")
    private void setUpOnClickListeners(View view){
        view.findViewById(R.id.backButton).setOnClickListener(v -> requireActivity().onBackPressed());

        ((TextView) view.findViewById(R.id.sleepDateHeader)).setText(String.format("%d %s %d", day, getMonthLong(requireActivity(), month), year));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.sleep_data_screen, container, false);
        // setting up the view
        setUpOnClickListeners(view);

        RangeSeekBar rangeSeekBar = new RangeSeekBar(requireActivity());

        rangeSeekBar.setOnRangeSeekBarChangeListener((bar, minValue, maxValue) -> System.out.println("MIN VALUE:" + minValue + ", MAX VALUE: " + maxValue));

        rangeSeekBar.setRangeValues(15, 90);
        rangeSeekBar.setSelectedMinValue(20);
        rangeSeekBar.setSelectedMaxValue(40);

        LinearLayout rangeSeekBarLayout = view.findViewById(R.id.rangeSeekBar);
        rangeSeekBarLayout.addView(rangeSeekBar);

        return view;
    }
}
