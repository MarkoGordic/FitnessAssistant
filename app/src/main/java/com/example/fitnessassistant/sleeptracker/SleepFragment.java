package com.example.fitnessassistant.sleeptracker;

import static com.example.fitnessassistant.util.TimeFunctional.getCurrentDateFormatted;
import static com.example.fitnessassistant.util.TimeFunctional.getMonthShort;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.adapters.CalendarAdapter;
import com.example.fitnessassistant.database.data.SleepSegment;
import com.example.fitnessassistant.database.mdbh.MDBHSleepTracker;
import com.example.fitnessassistant.util.ClockView;
import com.example.fitnessassistant.util.GraphView;
import com.example.fitnessassistant.util.PermissionFunctional;
import com.example.fitnessassistant.util.ServiceFunctional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SleepFragment extends Fragment implements CalendarAdapter.OnItemListener{
    private Calendar graphCal;
    private static LocalDate selectedDate;
    public static CalendarAdapter calendarAdapter;
    public static int previousPositionSelected;
    private int day;
    private int month;
    private int year;

    @SuppressLint("DefaultLocale")
    private void setUpCurrentDate(View view){
        int currentDate = Integer.parseInt(getCurrentDateFormatted());
        int day = currentDate % 100;
        currentDate /= 100;
        int month = currentDate % 100;
        currentDate /= 100;
        int year = currentDate;
        ((TextView) view.findViewById(R.id.currentDate)).setText(String.format("%02d %s %d", day, getMonthShort(requireActivity(), month), year));
    }

    public void setUpUI(boolean sleepRuns){
        if(getView() != null)
            setUpSleepButton(getView(), sleepRuns);
    }

    private void setUpSleepButton(View view, Boolean sleepRuns) {
        if (sleepRuns == null)
            sleepRuns = ServiceFunctional.getSleepTrackerShouldRun(requireActivity());

        if (sleepRuns) {
            Animation flashyAnimation = new AlphaAnimation(0.0f, 1.0f);
            flashyAnimation.setDuration(400);
            flashyAnimation.setStartOffset(400);
            flashyAnimation.setRepeatMode(Animation.REVERSE);
            flashyAnimation.setRepeatCount(Animation.INFINITE);
            view.findViewById(R.id.sleepLive).setVisibility(View.VISIBLE);
            view.findViewById(R.id.sleepLive).startAnimation(flashyAnimation);
            ((TextView) view.findViewById(R.id.sleepButton)).setText(R.string.stop);
            Drawable stopDrawable = AppCompatResources.getDrawable(requireActivity(), R.drawable.pause);
            if (stopDrawable != null)
                stopDrawable.setTint(requireActivity().getColor(R.color.DarkBlueYonder));
            ((TextView) view.findViewById(R.id.sleepButton)).setCompoundDrawablesWithIntrinsicBounds(stopDrawable, null, null, null);
            view.findViewById(R.id.sleepButton).setOnClickListener(view1 -> {
                ServiceFunctional.setSleepTrackerShouldRun(requireActivity(), false);
                ServiceFunctional.stopSleepTrackerService(requireActivity());
            });
        } else {
            view.findViewById(R.id.sleepLive).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.sleepButton)).setText(R.string.start);
            Drawable startDrawable = AppCompatResources.getDrawable(requireActivity(), R.drawable.play);
            if (startDrawable != null)
                startDrawable.setTint(requireActivity().getColor(R.color.DarkBlueYonder));
            ((TextView) view.findViewById(R.id.sleepButton)).setCompoundDrawablesWithIntrinsicBounds(startDrawable, null, null, null);
            view.findViewById(R.id.sleepButton).setOnClickListener(view1 -> PermissionFunctional.checkSleepActivityRecognitionPermission(requireActivity(), ((InAppActivity) requireActivity()).sleepActivityRecognitionPermissionLauncher));
        }
    }

    private void setUpGraphValues(View view, int[] vals, float maxCount){
        GraphView graphView = view.findViewById(R.id.graph);

        int[] array1 = { 4 };
        int[] array2 = { 1, 2, 3, 4, 5, 6 };

        graphView.setArrayValues(array1, array2);
        graphView.setGraphValues(vals);

        float[] graphYValues = graphView.getYValues();

        ((TextView) view.findViewById(R.id.value1)).setText(String.valueOf((int) graphYValues[4]));
        ((TextView) view.findViewById(R.id.value2)).setText(String.valueOf((int) graphYValues[3]));
        ((TextView) view.findViewById(R.id.value3)).setText(String.valueOf((int) graphYValues[2]));
        ((TextView) view.findViewById(R.id.value4)).setText(String.valueOf((int) graphYValues[1]));
        ((TextView) view.findViewById(R.id.value5)).setText(String.valueOf((int) graphYValues[0]));

        graphView.setBottomScaleCount(7);
        graphView.setSideScaleCount(graphYValues.length);
        graphView.setMaxCount(maxCount);
        graphView.invalidate();
    }

    private boolean isWeeklyGraph(View view){
        return view.findViewById(R.id.textLL1).getVisibility() == View.VISIBLE;
    }

    @SuppressLint("DefaultLocale")
    private void setUpGraph(View view, Boolean weeklyGraph){
        if(weeklyGraph == null)
            weeklyGraph = isWeeklyGraph(view);
        else
            graphCal = Calendar.getInstance(); //  resets calendar

        if(weeklyGraph) {
            Calendar tempCalendar = (Calendar) graphCal.clone();
            tempCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            ((TextView) view.findViewById(R.id.graphDate)).setText(String.format("%02d %s %d", tempCalendar.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), tempCalendar.get(Calendar.MONTH) + 1), tempCalendar.get(Calendar.YEAR)));
            tempCalendar.add(Calendar.DAY_OF_YEAR, 6);
            ((TextView) view.findViewById(R.id.graphDate)).setText(String.format("%s - %02d %s %d", ((TextView) view.findViewById(R.id.graphDate)).getText(), tempCalendar.get(Calendar.DAY_OF_MONTH), getMonthShort(requireActivity(), tempCalendar.get(Calendar.MONTH) + 1), tempCalendar.get(Calendar.YEAR)));

            ((TextView) view.findViewById(R.id.graph_header)).setText(requireActivity().getString(R.string.weekly_graph));

            view.findViewById(R.id.textLL1).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textLL2).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textLL3).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textLL4).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textLL5).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textLL6).setVisibility(View.VISIBLE);
            view.findViewById(R.id.textLL7).setVisibility(View.VISIBLE);
            view.findViewById(R.id.numberLL1).setVisibility(View.GONE);
            view.findViewById(R.id.numberLL2).setVisibility(View.GONE);
            view.findViewById(R.id.numberLL3).setVisibility(View.GONE);
            view.findViewById(R.id.numberLL4).setVisibility(View.GONE);
            view.findViewById(R.id.numberLL5).setVisibility(View.GONE);
            view.findViewById(R.id.numberLL6).setVisibility(View.GONE);
            view.findViewById(R.id.numberLL7).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.textLL1)).setText(R.string.mon_short);
            ((TextView) view.findViewById(R.id.textLL2)).setText(R.string.tue_short);
            ((TextView) view.findViewById(R.id.textLL3)).setText(R.string.wed_short);
            ((TextView) view.findViewById(R.id.textLL4)).setText(R.string.thu_short);
            ((TextView) view.findViewById(R.id.textLL5)).setText(R.string.fri_short);
            ((TextView) view.findViewById(R.id.textLL6)).setText(R.string.sat_short);
            ((TextView) view.findViewById(R.id.textLL7)).setText(R.string.sun_short);

            int[] weeklyVals = null;
            int currentDate = Integer.parseInt((String) DateFormat.format("yyyyMMdd", graphCal));

            SleepSegment sleepSegment;
            switch(graphCal.get(Calendar.DAY_OF_WEEK)){
                case Calendar.SUNDAY:
                    weeklyVals = new int[7];
                    sleepSegment = MDBHSleepTracker.getInstance(requireActivity()).getSleepSegmentForDateFromDB(String.valueOf(currentDate));
                    if(sleepSegment == null)
                        weeklyVals[6] = 0;
                    else
                        weeklyVals[6] = Math.round(TimeUnit.MILLISECONDS.toHours(sleepSegment.getEndTime() - sleepSegment.getStartTime()));
                    currentDate--;
                case Calendar.SATURDAY:
                    if(weeklyVals == null)
                        weeklyVals = new int[6];
                    sleepSegment = MDBHSleepTracker.getInstance(requireActivity()).getSleepSegmentForDateFromDB(String.valueOf(currentDate));
                    if(sleepSegment == null)
                        weeklyVals[5] = 0;
                    else
                        weeklyVals[5] = Math.round(TimeUnit.MILLISECONDS.toHours(sleepSegment.getEndTime() - sleepSegment.getStartTime()));
                    currentDate--;
                case Calendar.FRIDAY:
                    if(weeklyVals == null)
                        weeklyVals = new int[5];
                    sleepSegment = MDBHSleepTracker.getInstance(requireActivity()).getSleepSegmentForDateFromDB(String.valueOf(currentDate));
                    if(sleepSegment == null)
                        weeklyVals[4] = 0;
                    else
                        weeklyVals[4] = Math.round(TimeUnit.MILLISECONDS.toHours(sleepSegment.getEndTime() - sleepSegment.getStartTime()));
                    currentDate--;
                case Calendar.THURSDAY:
                    if(weeklyVals == null)
                        weeklyVals = new int[4];
                    sleepSegment = MDBHSleepTracker.getInstance(requireActivity()).getSleepSegmentForDateFromDB(String.valueOf(currentDate));
                    if(sleepSegment == null)
                        weeklyVals[3] = 0;
                    else
                        weeklyVals[3] = Math.round(TimeUnit.MILLISECONDS.toHours(sleepSegment.getEndTime() - sleepSegment.getStartTime()));
                    currentDate--;
                case Calendar.WEDNESDAY:
                    if(weeklyVals == null)
                        weeklyVals = new int[3];
                    sleepSegment = MDBHSleepTracker.getInstance(requireActivity()).getSleepSegmentForDateFromDB(String.valueOf(currentDate));
                    if(sleepSegment == null)
                        weeklyVals[2] = 0;
                    else
                        weeklyVals[2] = Math.round(TimeUnit.MILLISECONDS.toHours(sleepSegment.getEndTime() - sleepSegment.getStartTime()));
                    currentDate--;
                case Calendar.TUESDAY:
                    if(weeklyVals == null)
                        weeklyVals = new int[2];
                    sleepSegment = MDBHSleepTracker.getInstance(requireActivity()).getSleepSegmentForDateFromDB(String.valueOf(currentDate));
                    if(sleepSegment == null)
                        weeklyVals[1] = 0;
                    else
                        weeklyVals[1] = Math.round(TimeUnit.MILLISECONDS.toHours(sleepSegment.getEndTime() - sleepSegment.getStartTime()));
                    currentDate--;
                case Calendar.MONDAY:
                    if(weeklyVals == null)
                        weeklyVals = new int[1];
                    sleepSegment = MDBHSleepTracker.getInstance(requireActivity()).getSleepSegmentForDateFromDB(String.valueOf(currentDate));
                    if(sleepSegment == null)
                        weeklyVals[0] = 0;
                    else
                        weeklyVals[0] = Math.round(TimeUnit.MILLISECONDS.toHours(sleepSegment.getEndTime() - sleepSegment.getStartTime()));
            }

            setUpGraphValues(view, weeklyVals, 7f);
        } else {
            ((TextView) view.findViewById(R.id.graphDate)).setText(String.format("%s %d", getMonthShort(requireActivity(),graphCal.get(Calendar.MONTH) + 1), graphCal.get(Calendar.YEAR)));

            ((TextView) view.findViewById(R.id.graph_header)).setText(requireActivity().getString(R.string.monthly_graph));

            view.findViewById(R.id.textLL1).setVisibility(View.GONE);
            view.findViewById(R.id.textLL2).setVisibility(View.GONE);
            view.findViewById(R.id.textLL3).setVisibility(View.GONE);
            view.findViewById(R.id.textLL4).setVisibility(View.GONE);
            view.findViewById(R.id.textLL5).setVisibility(View.GONE);
            view.findViewById(R.id.textLL6).setVisibility(View.GONE);
            view.findViewById(R.id.textLL7).setVisibility(View.GONE);
            view.findViewById(R.id.numberLL1).setVisibility(View.VISIBLE);
            view.findViewById(R.id.numberLL2).setVisibility(View.VISIBLE);
            view.findViewById(R.id.numberLL3).setVisibility(View.VISIBLE);
            view.findViewById(R.id.numberLL4).setVisibility(View.VISIBLE);
            view.findViewById(R.id.numberLL5).setVisibility(View.VISIBLE);
            view.findViewById(R.id.numberLL6).setVisibility(View.VISIBLE);
            view.findViewById(R.id.numberLL7).setVisibility(View.VISIBLE);

            int[] monthlyVals = new int[graphCal.get(Calendar.DAY_OF_MONTH)];
            int[] bottomGraphDisplay = { 0, 0, 0, 0, 0, 0, 0 };
            switch(graphCal.getActualMaximum(Calendar.DAY_OF_MONTH)){
                case 31:
                    bottomGraphDisplay = new int[]{ 1, 6, 11, 16, 21, 26, 31 };
                    break;
                case 30:
                    bottomGraphDisplay = new int[]{ 1, 6, 11, 16, 20, 25, 30 };
                    break;
                case 29:
                    bottomGraphDisplay = new int[]{ 1, 6, 11, 15, 19, 24, 29 };
                    break;
                case 28:
                    bottomGraphDisplay = new int[]{ 1, 6, 10, 15, 19, 23, 28 };
                    break;
            }

            int currentDate = Integer.parseInt((String) DateFormat.format("yyyyMMdd", graphCal));
            for(int currentDay = graphCal.get(Calendar.DAY_OF_MONTH); currentDay >= 1; currentDay--){
                SleepSegment sleepSegment = MDBHSleepTracker.getInstance(requireActivity()).getSleepSegmentForDateFromDB(String.valueOf(currentDate));
                if(sleepSegment == null)
                    monthlyVals[currentDay - 1] = 0;
                else
                    monthlyVals[currentDay - 1] = Math.round(TimeUnit.MILLISECONDS.toHours(sleepSegment.getEndTime() - sleepSegment.getStartTime()));
                currentDate--;
            }

            setUpGraphValues(view,  monthlyVals, (float) graphCal.getActualMaximum(Calendar.DAY_OF_MONTH));

            ((TextView) view.findViewById(R.id.numberLL1)).setText(String.format("%02d", bottomGraphDisplay[0]));
            ((TextView) view.findViewById(R.id.numberLL2)).setText(String.format("%02d", bottomGraphDisplay[1]));
            ((TextView) view.findViewById(R.id.numberLL3)).setText(String.format("%02d", bottomGraphDisplay[2]));
            ((TextView) view.findViewById(R.id.numberLL4)).setText(String.format("%02d", bottomGraphDisplay[3]));
            ((TextView) view.findViewById(R.id.numberLL5)).setText(String.format("%02d", bottomGraphDisplay[4]));
            ((TextView) view.findViewById(R.id.numberLL6)).setText(String.format("%02d", bottomGraphDisplay[5]));
            ((TextView) view.findViewById(R.id.numberLL7)).setText(String.format("%02d", bottomGraphDisplay[6]));
        }
    }

    @SuppressLint("DefaultLocale")
    private void setUpClockAndQuality(View view){
        ClockView clock = view.findViewById(R.id.clock);
        String date = (String) DateFormat.format("yyyyMMdd", Date.from(LocalDate.of(year, month, day).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        SleepSegment todaySleepSegment = MDBHSleepTracker.getInstance(requireActivity()).getSleepSegmentForDateFromDB(date);
        ((TextView) view.findViewById(R.id.selectedDate)).setText(String.format("%02d %s %d", day, getMonthShort(requireActivity(), month), year));
        if(todaySleepSegment != null) {
            float startHours = todaySleepSegment.getStartTime() / 3600000f;
            float hoursSlept = todaySleepSegment.getDuration() / 3600000f;

            clock.setStartHours(startHours);
            clock.setHoursSlept(hoursSlept);
            ((TextView) view.findViewById(R.id.hoursSlept)).setText(String.format("%.1f", hoursSlept));

            switch(todaySleepSegment.getQuality()) {
                case SleepDateFragment.QUALITY_AWFUL:
                    ((AppCompatImageView) view.findViewById(R.id.smiley1)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.Awful)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley2)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley3)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley4)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley5)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    break;
                case SleepDateFragment.QUALITY_BAD:
                    ((AppCompatImageView) view.findViewById(R.id.smiley1)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley2)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.Bad)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley3)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley4)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley5)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    break;
                case SleepDateFragment.QUALITY_NEUTRAL:
                    ((AppCompatImageView) view.findViewById(R.id.smiley1)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley2)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley3)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.Neutral)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley4)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley5)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    break;
                case SleepDateFragment.QUALITY_GOOD:
                    ((AppCompatImageView) view.findViewById(R.id.smiley1)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley2)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley3)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley4)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.Good)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley5)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    break;
                case SleepDateFragment.QUALITY_EXCELLENT:
                    ((AppCompatImageView) view.findViewById(R.id.smiley1)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley2)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley3)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley4)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley5)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.Excellent)));
                    break;
                default:
                    ((AppCompatImageView) view.findViewById(R.id.smiley1)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley2)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley3)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley4)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    ((AppCompatImageView) view.findViewById(R.id.smiley5)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
                    break;
            }
        } else{
            clock.setStartHours(0f);
            clock.setHoursSlept(0f);
            ((AppCompatImageView) view.findViewById(R.id.smiley1)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
            ((AppCompatImageView) view.findViewById(R.id.smiley2)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
            ((AppCompatImageView) view.findViewById(R.id.smiley3)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
            ((AppCompatImageView) view.findViewById(R.id.smiley4)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
            ((AppCompatImageView) view.findViewById(R.id.smiley5)).setImageTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.LightGrayColor)));
            ((TextView) view.findViewById(R.id.hoursSlept)).setText("?");
        }
        clock.setFontSize(14f);
        clock.setNumPadding(6f);
        clock.invalidate();
    }

    public void setUpSleepData(View view){
        if(view == null)
            view = getView();

        if(view != null) {
            if(graphCal == null)
                graphCal = Calendar.getInstance();

            setUpGraph(view, null);
            setUpClockAndQuality(view);
        }
    }

    private void setUpOnClickListeners(View view){
        setUpSleepButton(view, null);

        view.findViewById(R.id.weeklyMonthlySwitch).setOnClickListener(new View.OnClickListener() {
            boolean weekly = true;
            @Override
            public void onClick(View v) {
                weekly = !weekly;
                setUpGraph(view, weekly);
            }
        });

        view.findViewById(R.id.dateBefore).setOnClickListener(v -> {
            Boolean weeklyGraph = null;
            if(isWeeklyGraph(view)) {
                graphCal.add(Calendar.WEEK_OF_YEAR, -1);

                if(graphCal.get(Calendar.WEEK_OF_YEAR) == Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) && graphCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR))
                    weeklyGraph = true;
                else
                if(graphCal.before(Calendar.getInstance()))
                    graphCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                else
                    graphCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            } else {
                graphCal.add(Calendar.MONTH, -1);

                if(graphCal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && graphCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR))
                    weeklyGraph = false;
                else
                if(graphCal.before(Calendar.getInstance()))
                    graphCal.set(Calendar.DAY_OF_MONTH, graphCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                else
                    graphCal.set(Calendar.DAY_OF_MONTH, 1);
            }

            setUpGraph(view, weeklyGraph);
        });

        view.findViewById(R.id.dateAfter).setOnClickListener(v -> {
            Boolean weeklyGraph = null;
            if(isWeeklyGraph(view)) {
                graphCal.add(Calendar.WEEK_OF_YEAR, 1);

                if(graphCal.get(Calendar.WEEK_OF_YEAR) == Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) && graphCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR))
                    weeklyGraph = true;
                else
                if(graphCal.before(Calendar.getInstance()))
                    graphCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                else
                    graphCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            } else {
                graphCal.add(Calendar.MONTH, 1);

                if(graphCal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) && graphCal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR))
                    weeklyGraph = false;
                else
                if(graphCal.before(Calendar.getInstance()))
                    graphCal.set(Calendar.DAY_OF_MONTH, graphCal.getActualMaximum(Calendar.DAY_OF_MONTH));
                else
                    graphCal.set(Calendar.DAY_OF_MONTH, 1);
            }

            setUpGraph(view, weeklyGraph);
        });

        view.findViewById(R.id.previousMonth).setOnClickListener(v -> {
            selectedDate = selectedDate.minusMonths(1);
            setMonthView(view);
        });

        view.findViewById(R.id.nextMonth).setOnClickListener(v -> {
            if((selectedDate.getYear() == Calendar.getInstance().get(Calendar.YEAR) && selectedDate.getMonthValue() < Calendar.getInstance().get(Calendar.MONTH) + 1) || (selectedDate.getYear() < Calendar.getInstance().get(Calendar.YEAR))) {
                selectedDate = selectedDate.plusMonths(1);
                setMonthView(view);
            }
        });

        view.findViewById(R.id.sleepDateFragment).setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new SleepDateFragment(day, month, year)).addToBackStack(null).commit());
    }

    public static int getDayPosition(int day){
        return day + selectedDate.withDayOfMonth(1).getDayOfWeek().getValue() - 2;
    }

    public static int getPositionDay(int position){
        return position - selectedDate.withDayOfMonth(1).getDayOfWeek().getValue() + 2;
    }

    private ArrayList<Integer> qualitiesOfMonthArray(LocalDate date){
        ArrayList<Integer> qualitiesInMonth = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = date.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for(int i = 2; i <= 43; i++){
            if (i > dayOfWeek && i <= daysInMonth + dayOfWeek) {
                SleepSegment sleepSegment = MDBHSleepTracker.getInstance(requireActivity()).getSleepSegmentForDateFromDB((String) DateFormat.format("yyyyMMdd", Date.from(LocalDate.of( yearMonth.getYear(), yearMonth.getMonth(),i - dayOfWeek).atStartOfDay(ZoneId.systemDefault()).toInstant())));
                if(sleepSegment == null)
                    qualitiesInMonth.add(null);
                else
                    qualitiesInMonth.add(sleepSegment.getQuality());
            } else
                qualitiesInMonth.add(null);
        }

        return qualitiesInMonth;
    }

    private ArrayList<String> daysInMonthArray(LocalDate date){
        ArrayList<String> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int daysInMonth = yearMonth.lengthOfMonth();

        LocalDate firstOfMonth = date.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        for(int i = 2; i <= 43; i++){
            if(i <= dayOfWeek || i > daysInMonth + dayOfWeek){
                daysInMonthArray.add("");
            } else{
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }

        return daysInMonthArray;
    }

    @Override
    public void onItemClick(int position, String dayText) {
        if(!dayText.equals("")) {
            if(position != previousPositionSelected) {
                if (selectedDate.getYear() == Calendar.getInstance().get(Calendar.YEAR) && selectedDate.getMonthValue() == Calendar.getInstance().get(Calendar.MONTH) + 1) {
                    if (getPositionDay(position) <= Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
                        day = Integer.parseInt(dayText);
                        month = selectedDate.getMonthValue();
                        year = selectedDate.getYear();
                        setUpClockAndQuality(requireView());

                        if (previousPositionSelected >= 0)
                            calendarAdapter.notifyItemChanged(previousPositionSelected, -42455);
                        calendarAdapter.notifyItemChanged(position, 42455);
                    }
                } else {
                    day = Integer.parseInt(dayText);
                    month = selectedDate.getMonthValue();
                    year = selectedDate.getYear();
                    setUpClockAndQuality(requireView());

                    if (previousPositionSelected >= 0)
                        calendarAdapter.notifyItemChanged(previousPositionSelected, -42455);
                    calendarAdapter.notifyItemChanged(position, 42455);
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private void setMonthView(View view){
        ((TextView) view.findViewById(R.id.monthYearTextView)).setText(String.format("%s %d", getMonthShort(requireActivity(), selectedDate.getMonthValue()), selectedDate.getYear()));
        ArrayList<String> daysInMonth = daysInMonthArray(selectedDate);
        ArrayList<Integer> qualitiesOfMonth = qualitiesOfMonthArray(selectedDate);

        previousPositionSelected = -1;

        calendarAdapter = new CalendarAdapter(requireActivity(), daysInMonth, qualitiesOfMonth, this);

        RecyclerView calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        calendarRecyclerView.setHasFixedSize(true);

        calendarRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 7){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        calendarRecyclerView.setAdapter(calendarAdapter);

        calendarRecyclerView.post(() -> {
            int side = calendarRecyclerView.getMeasuredWidth();

            ViewGroup.LayoutParams lp = calendarRecyclerView.getLayoutParams();
            lp.width = side;
            lp.height = Math.round(side * 6f / 7f);
            calendarRecyclerView.setLayoutParams(lp);

            if(selectedDate.getMonthValue() == month && selectedDate.getYear() == year){
                onItemClick(day + selectedDate.withDayOfMonth(1).getDayOfWeek().getValue() - 2, String.valueOf(day));
            }
            calendarAdapter.notifyItemChanged(10, 0); // dummy notify for animation to occur
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.sleep_screen, container, false);
        setUpOnClickListeners(view);

        selectedDate = LocalDate.now();

        day = selectedDate.getDayOfMonth();
        month = selectedDate.getMonthValue();
        year = selectedDate.getYear();

        setMonthView(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getView() != null) {
            setUpSleepData(getView());
            setUpCurrentDate(getView());
        }
    }
}
