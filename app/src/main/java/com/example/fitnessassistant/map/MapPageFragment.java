package com.example.fitnessassistant.map;

import static java.lang.Math.abs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.pedometer.Pedometer;
import com.example.fitnessassistant.util.PermissionFunctional;
import com.example.fitnessassistant.util.ServiceFunctional;


public class MapPageFragment extends Fragment {

    public void setUpUI(boolean pedometerRuns){
        if(getView() != null)
            setUpStepCountingButton(getView(), pedometerRuns);
    }

    private void setUpStepCountingButton(View view, Boolean pedometerRuns){
        if(pedometerRuns == null)
            pedometerRuns = ServiceFunctional.getPedometerShouldRun(requireActivity());

        if(pedometerRuns){
            ((AppCompatButton) view.findViewById(R.id.stepCountingButton)).setText(R.string.stop_counting);
            view.findViewById(R.id.stepCountingButton).setOnClickListener(view1 -> {
                ServiceFunctional.setPedometerShouldRun(requireActivity(), false);
                ServiceFunctional.stopPedometerService(requireActivity());
            });
        } else{
            ((AppCompatButton) view.findViewById(R.id.stepCountingButton)).setText(R.string.start_counting);
            view.findViewById(R.id.stepCountingButton).setOnClickListener(view1 -> PermissionFunctional.checkActivityRecognitionPermission(requireActivity(), ((InAppActivity) requireActivity()).activityRecognitionPermissionLauncher));
        }
    }

    public void goToActivityTrackingFragment(){
        requireActivity().getSupportFragmentManager().beginTransaction().hide(MapPageFragment.this).setCustomAnimations(R.anim.slide_up, R.anim.slide_down, R.anim.slide_up, R.anim.slide_down).add(R.id.in_app_container, InAppActivity.activityTrackingFragment).addToBackStack(null).commit();
        requireActivity().findViewById(R.id.bottomNavigation).setVisibility(View.GONE);
    }

    private void showKeyboard(EditText editText){
        InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(editText, InputMethodManager.HIDE_IMPLICIT_ONLY | InputMethodManager.SHOW_FORCED);
    }

    private void closeKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = requireActivity().getCurrentFocus();
        if(focusedView != null)
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
    }

    private void setUpOnClickListeners(View view){
        setUpStepCountingButton(view, null);

        // settingUpStepGoal
        ((TextView) view.findViewById(R.id.stepCountTextView)).setText(String.valueOf((int) requireContext().getSharedPreferences("pedometer", Context.MODE_PRIVATE).getFloat(Pedometer.getCurrentDateFormatted(), 0)));
        ((TextView) view.findViewById(R.id.stepGoalTextView)).setText(String.valueOf(requireContext().getSharedPreferences("pedometer", Context.MODE_PRIVATE).getInt("dailyStepGoal", 10000)));
        ((EditText) view.findViewById(R.id.edtStepGoal)).setText(String.valueOf(requireContext().getSharedPreferences("pedometer", Context.MODE_PRIVATE).getInt("dailyStepGoal", 10000)));

        // set up mapButton on click listener
        view.findViewById(R.id.mapSwipeUp).setOnClickListener(view1 -> Toast.makeText(requireContext(), R.string.swipe_up_to_use_the_map, Toast.LENGTH_SHORT).show());

        // set up mapButton on touch listener (for swiping)
        view.findViewById(R.id.mapSwipeUp).setOnTouchListener(new View.OnTouchListener() {
            // xCord and yCord of event registered
            float xCord;
            float yCord;
            // len, used to determine what the event actually was
            final float len = getResources().getDisplayMetrics().densityDpi / 6f;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    // when button gets pressed, get cords of the click
                    xCord = event.getX();
                    yCord = event.getY();
                } else if(event.getAction() == MotionEvent.ACTION_UP){
                    // when button gets released, set cords to amount moved from ACTION_DOWN's click cords
                    xCord -= event.getX();
                    yCord -= event.getY();

                    // if it's a solid swipe UP (>) and if it's not too much to the side
                    if(yCord > len * 4 && abs(xCord) < len * 8)
                        goToActivityTrackingFragment();
                    else
                        view.performClick(); // call onClickListener
                }
                return false;
            }
        });

        // request focus if pencil is pressed
        view.findViewById(R.id.changeStepGoal).setOnClickListener(v -> {
            showKeyboard(view.findViewById(R.id.edtStepGoal));
            view.findViewById(R.id.edtStepGoal).requestFocus();
        });

        // used to put cursor on end
        view.findViewById(R.id.edtStepGoal).setOnClickListener(v -> {
            if(!((EditText) v).getText().toString().isEmpty())
                ((EditText) v).setSelection(((EditText) v).getText().length());
            v.requestFocus();
        });
        view.findViewById(R.id.edtStepGoal).setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
                return true;
            }
            return false;
        });
        view.findViewById(R.id.edtStepGoal).setOnKeyListener((v, keyCode, event) -> {
            v.performClick();
            return false;
        });

        // onFocusChanged listener
        view.findViewById(R.id.edtStepGoal).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            String lastSaved = null;
            boolean isSaved = false;
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(lastSaved == null)
                    lastSaved = ((EditText) v).getText().toString();
                if (hasFocus) {
                    showKeyboard((EditText) v);
                    ((ImageView) view.findViewById(R.id.changeStepGoal)).setImageDrawable(ContextCompat.getDrawable(MapPageFragment.this.requireActivity(), R.drawable.check));
                    view.findViewById(R.id.changeStepGoal).setOnClickListener(v1 -> {
                        if(!((EditText) v).getText().toString().isEmpty()) {
                            isSaved = true;
                            lastSaved = ((EditText) v).getText().toString();
                            // trim front zeros
                            ((EditText) v).setText(((EditText) v).getText().toString().replaceFirst("^0+(?!$)", ""));
                            int newStepGoal = Integer.parseInt(((EditText) view.findViewById(R.id.edtStepGoal)).getText().toString());
                            MapPageFragment.this.requireContext().getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit().putInt("dailyStepGoal", newStepGoal).apply();
                            ((TextView) view.findViewById(R.id.stepGoalTextView)).setText(String.valueOf(newStepGoal));
                            if (ServiceFunctional.getPedometerShouldRun(MapPageFragment.this.requireActivity())) {
                                Pedometer.updatePedometerWidgetData(MapPageFragment.this.requireActivity(), ((int) MapPageFragment.this.requireContext().getSharedPreferences("pedometer", Context.MODE_PRIVATE).getFloat(Pedometer.getCurrentDateFormatted(), 0)), newStepGoal);
                                Pedometer.pushPedometerNotification(MapPageFragment.this.requireActivity(), ((int) MapPageFragment.this.requireContext().getSharedPreferences("pedometer", Context.MODE_PRIVATE).getFloat(Pedometer.getCurrentDateFormatted(), 0)) + " " + MapPageFragment.this.requireContext().getString(R.string.steps_small), MapPageFragment.this.requireContext().getString(R.string.your_today_goal) + " " + newStepGoal + ".");
                            }
                        }
                        v.clearFocus();
                    });
                } else {
                    closeKeyboard();
                    ((EditText) v).setText(lastSaved);
                    ((ImageView) view.findViewById(R.id.changeStepGoal)).setImageDrawable(ContextCompat.getDrawable(MapPageFragment.this.requireActivity(), R.drawable.edit));
                    view.findViewById(R.id.changeStepGoal).setOnClickListener(v1 -> v.requestFocus());
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_screen, container, false);
        setUpOnClickListeners(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity() != null) {
            String desiredFragment = ((InAppActivity) getActivity()).getDesiredFragment();
            if (desiredFragment != null) {
                if (desiredFragment.equals("ActivityTrackingFragment")) {
                    ((InAppActivity) getActivity()).putDesiredFragment(null);
                    goToActivityTrackingFragment();
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        closeKeyboard();
    }
}
