package com.example.fitnessassistant.sleeptracker;

import static com.example.fitnessassistant.util.TimeFunctional.getCurrentDateFormatted;
import static com.example.fitnessassistant.util.TimeFunctional.getMonthShort;

import android.annotation.SuppressLint;
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
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.database.data.SleepSegment;
import com.example.fitnessassistant.database.mdbh.MDBHSleepTracker;
import com.example.fitnessassistant.util.GraphView;
import com.example.fitnessassistant.util.PermissionFunctional;
import com.example.fitnessassistant.util.ServiceFunctional;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class SleepFragment extends Fragment {
    private Calendar graphCal;

    private void setUpCurrentDate(View view){
        int currentDate = Integer.parseInt(getCurrentDateFormatted());
        int day = currentDate % 100;
        currentDate /= 100;
        int month = currentDate % 100;
        currentDate /= 100;
        int year = currentDate;
        ((TextView) view.findViewById(R.id.currentDate)).setText(String.format("%s %s %s", day, getMonthShort(requireActivity(), month), year));
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
                    monthlyVals[currentDay - 1] = (int) Math.round(TimeUnit.MILLISECONDS.toHours(sleepSegment.getEndTime() - sleepSegment.getStartTime()));
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

    // TODO call when received sleep updates
    public void setUpSleepData(View view){
        if(view == null)
            view = getView();

        if(view != null) {

            if(graphCal == null)
                graphCal = Calendar.getInstance();

            setUpGraph(view, null);

            View finalView = view;
            view.findViewById(R.id.weeklyMonthlySwitch).setOnClickListener(new View.OnClickListener() {
                boolean weekly = true;
                @Override
                public void onClick(View v) {
                    weekly = !weekly;
                    setUpGraph(finalView, weekly);
                }
            });

            view.findViewById(R.id.dateBefore).setOnClickListener(v -> {
                Boolean weeklyGraph = null;
                if(isWeeklyGraph(finalView)) {
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

                setUpGraph(finalView, weeklyGraph);
            });

            view.findViewById(R.id.dateAfter).setOnClickListener(v -> {
                Boolean weeklyGraph = null;
                if(isWeeklyGraph(finalView)) {
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

                setUpGraph(finalView, weeklyGraph);
            });
        }
    }

    private void setUpOnClickListeners(View view){
        setUpSleepButton(view, null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.sleep_screen, container, false);
        setUpOnClickListeners(view);
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
