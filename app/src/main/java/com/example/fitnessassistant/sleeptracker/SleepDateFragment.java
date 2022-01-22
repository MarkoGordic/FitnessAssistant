package com.example.fitnessassistant.sleeptracker;

import static com.example.fitnessassistant.util.TimeFunctional.getMonthLong;
import static com.example.fitnessassistant.util.TimeFunctional.getMonthShort;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.database.data.SleepSegment;
import com.example.fitnessassistant.database.mdbh.MDBHSleepTracker;
import com.example.fitnessassistant.util.RangeSeekBar;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

public class SleepDateFragment extends Fragment {
    private final int day;
    private final int month;
    private final int year;

    private RangeSeekBar rangeSeekBar;

    private int plusHours;
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 96;
    public static int VALUE_BLOCK;

    private int selectedQuality;
    public static final int QUALITY_NONE = -1;
    public static final int QUALITY_AWFUL = 1;
    public static final int QUALITY_BAD = 2;
    public static final int QUALITY_NEUTRAL = 3;
    public static final int QUALITY_GOOD = 4;
    public static final int QUALITY_EXCELLENT = 5;

    public SleepDateFragment(int day, int month, int year){
        this.day = day;
        this.month = month;
        this.year = year;
    }

    @SuppressLint("DefaultLocale")
    private String getTime(int value, boolean isStartTime){
        int minutes = value * 15;
        int hours = minutes / 60;
        minutes %= 60;

        hours += plusHours;

        if((isStartTime && hours == 24 && minutes == 0))
            return "23:59";
        else if(!isStartTime && hours == 48 && minutes == 0)
            return "23:59";

        return String.format("%02d:%02d", hours % 24, minutes);
    }

    private void setUpQuality(View view, int quality){
        switch(quality) {
            case QUALITY_AWFUL:
                selectedQuality = QUALITY_AWFUL;
                ((AppCompatImageView) view.findViewById(R.id.smiley1)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.Awful)));
                ((AppCompatImageView) view.findViewById(R.id.smiley2)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley3)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley4)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley5)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                break;
            case QUALITY_BAD:
                selectedQuality = QUALITY_BAD;
                ((AppCompatImageView) view.findViewById(R.id.smiley1)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley2)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.Bad)));
                ((AppCompatImageView) view.findViewById(R.id.smiley3)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley4)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley5)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                break;
            case QUALITY_NEUTRAL:
                selectedQuality = QUALITY_NEUTRAL;
                ((AppCompatImageView) view.findViewById(R.id.smiley1)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley2)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley3)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.Neutral)));
                ((AppCompatImageView) view.findViewById(R.id.smiley4)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley5)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                break;
            case QUALITY_GOOD:
                selectedQuality = QUALITY_GOOD;
                ((AppCompatImageView) view.findViewById(R.id.smiley1)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley2)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley3)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley4)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.Good)));
                ((AppCompatImageView) view.findViewById(R.id.smiley5)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                break;
            case QUALITY_EXCELLENT:
                selectedQuality = QUALITY_EXCELLENT;
                ((AppCompatImageView) view.findViewById(R.id.smiley1)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley2)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley3)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley4)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley5)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.Excellent)));
                break;
            default:
                selectedQuality = QUALITY_NONE;
                ((AppCompatImageView) view.findViewById(R.id.smiley1)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley2)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley3)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley4)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                ((AppCompatImageView) view.findViewById(R.id.smiley5)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                break;
        }
    }

    @SuppressLint("DefaultLocale")
    private void setUpOnClickListeners(View view){
        view.findViewById(R.id.backButton).setOnClickListener(v -> requireActivity().onBackPressed());

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);

        ((TextView) view.findViewById(R.id.sleepDateHeader)).setText(String.format("%02d %s %d", cal.get(Calendar.DAY_OF_MONTH), getMonthLong(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR)));
        ((TextView) view.findViewById(R.id.endDate)).setText(String.format("%02d %s %d", cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR)));
        cal.add(Calendar.DAY_OF_MONTH, -1);
        ((TextView) view.findViewById(R.id.startDate)).setText(String.format("%02d %s %d", cal.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), cal.get(Calendar.MONTH) + 1), cal.get(Calendar.YEAR)));

        rangeSeekBar = view.findViewById(R.id.rangeSeekBar);
        rangeSeekBar.setRangeValues(MIN_VALUE, MAX_VALUE);
        rangeSeekBar.setNotifyWhileDragging(true);

        rangeSeekBar.setOnRangeSeekBarChangeListener((bar, minValue, maxValue) -> {
            ((TextView) view.findViewById(R.id.startTime)).setText(getTime(minValue, true));
            ((TextView) view.findViewById(R.id.endTime)).setText(getTime(maxValue, false));
        });

        view.findViewById(R.id.smiley1).setOnClickListener(v -> setUpQuality(view, QUALITY_AWFUL));
        view.findViewById(R.id.smiley2).setOnClickListener(v -> setUpQuality(view, QUALITY_BAD));
        view.findViewById(R.id.smiley3).setOnClickListener(v -> setUpQuality(view, QUALITY_NEUTRAL));
        view.findViewById(R.id.smiley4).setOnClickListener(v -> setUpQuality(view, QUALITY_GOOD));
        view.findViewById(R.id.smiley5).setOnClickListener(v -> setUpQuality(view, QUALITY_EXCELLENT));

        view.findViewById(R.id.plusOne).setOnClickListener(v -> {
            if(plusHours < 24) {
                plusHours += 1;
                VALUE_BLOCK -= 4;
                rangeSeekBar.setSelectedMaxValue(rangeSeekBar.getSelectedMaxValue() - 4);
                rangeSeekBar.setSelectedMinValue(rangeSeekBar.getSelectedMinValue() - 4);
                ((TextView) view.findViewById(R.id.startTime)).setText(getTime(rangeSeekBar.getSelectedMinValue(), true));
                ((TextView) view.findViewById(R.id.endTime)).setText(getTime(rangeSeekBar.getSelectedMaxValue(), false));
            }
        });

        view.findViewById(R.id.minusOne).setOnClickListener(v -> {
            if(plusHours > 0) {
                plusHours -= 1;
                VALUE_BLOCK += 4;
                rangeSeekBar.setSelectedMaxValue(rangeSeekBar.getSelectedMaxValue() + 4);
                rangeSeekBar.setSelectedMinValue(rangeSeekBar.getSelectedMinValue() + 4);
                ((TextView) view.findViewById(R.id.startTime)).setText(getTime(rangeSeekBar.getSelectedMinValue(), true));
                ((TextView) view.findViewById(R.id.endTime)).setText(getTime(rangeSeekBar.getSelectedMaxValue(), false));
            }
        });

        view.findViewById(R.id.saveButton).setOnClickListener(v -> {
            Calendar cal1 = Calendar.getInstance();
            cal1.set(Calendar.YEAR, year);
            cal1.set(Calendar.MONTH, month - 1);
            cal1.set(Calendar.DAY_OF_MONTH, day);

            StringTokenizer tokenizer1 = new StringTokenizer(((TextView) view.findViewById(R.id.startTime)).getText().toString(),":");
            StringTokenizer tokenizer2 = new StringTokenizer(((TextView) view.findViewById(R.id.endTime)).getText().toString(),":");

            int startHours = Integer.parseInt(tokenizer1.nextToken());
            int startMinutes = Integer.parseInt(tokenizer1.nextToken());

            int endHours = Integer.parseInt(tokenizer2.nextToken());
            int endMinutes = Integer.parseInt(tokenizer2.nextToken());

            cal1.set(Calendar.HOUR_OF_DAY, endHours);
            cal1.set(Calendar.MINUTE, endMinutes);
            long endMillis =  cal1.getTimeInMillis();
            String date = (String) DateFormat.format("yyyyMMdd", cal1);

            cal1.add(Calendar.DAY_OF_MONTH, -1);

            cal1.set(Calendar.HOUR_OF_DAY, startHours);
            cal1.set(Calendar.MINUTE, startMinutes);
            long startMillis =  cal1.getTimeInMillis();

            MDBHSleepTracker.getInstance(requireActivity()).addNewSleepSegment(requireActivity(), startMillis, endMillis, endMillis - startMillis, date, selectedQuality, 1, true);
            SleepFragment.calendarAdapter.setAddedQuality(SleepFragment.getDayPosition(day), selectedQuality);
            requireActivity().onBackPressed();
        });
    }

    @SuppressLint("DefaultLocale")
    private void setUpUI(View view){
        SleepSegment todaySleepSegment = MDBHSleepTracker.getInstance(requireActivity()).getSleepSegmentForDateFromDB((String) DateFormat.format("yyyyMMdd", Date.from(LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant())));

        rangeSeekBar = view.findViewById(R.id.rangeSeekBar);

        if(todaySleepSegment != null) {
            setUpQuality(view, todaySleepSegment.getQuality());

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(todaySleepSegment.getStartTime());
            String startTime = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
            ((TextView) view.findViewById(R.id.startTime)).setText(startTime);

            int startHours = cal.get(Calendar.HOUR_OF_DAY);
            int startMinutes = cal.get(Calendar.MINUTE);

            cal.setTimeInMillis(todaySleepSegment.getEndTime());
            String endTime = String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
            ((TextView) view.findViewById(R.id.endTime)).setText(endTime);

            int endHours = cal.get(Calendar.HOUR_OF_DAY);
            int endMinutes = cal.get(Calendar.MINUTE);

            int minutesPassed = endMinutes - startMinutes;
            int hoursPassed = endHours + 24 - startHours;

            if(minutesPassed < 0){
                minutesPassed += 60;
                hoursPassed -= 1;
            }

            plusHours = startHours;
            VALUE_BLOCK = (24 - plusHours) * 4;

            rangeSeekBar.setSelectedMaxValue(Math.round((hoursPassed * 60 + minutesPassed) / 15f));
            rangeSeekBar.setSelectedMinValue(Math.round(startMinutes / 15f));
        } else{
            selectedQuality = QUALITY_NONE;
            ((TextView) view.findViewById(R.id.startTime)).setText(getTime(MIN_VALUE, true));
            ((TextView) view.findViewById(R.id.endTime)).setText(getTime(MAX_VALUE, false));
            plusHours = 12;
            VALUE_BLOCK = 48;
            rangeSeekBar.setSelectedMinValue(MIN_VALUE);
            rangeSeekBar.setSelectedMaxValue(MAX_VALUE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.sleep_data_screen, container, false);

        // setting up the view
        setUpOnClickListeners(view);
        setUpUI(view);

        return view;
    }
}