package com.example.fitnessassistant.questions;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;

import java.util.concurrent.atomic.AtomicReference;

public class GenderFragment extends Fragment {
    public static final String MALE = "male";
    public static final String FEMALE = "female";
    public static final String OTHER = "other";

    public static synchronized String getGender(Context context){
        return context.getSharedPreferences("questions", MODE_PRIVATE).getString("gender", "unknown");
    }

    public synchronized static void putGender(Context context, String gender){
        context.getSharedPreferences("questions", MODE_PRIVATE).edit().putString("gender", gender).apply();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpOnClickListeners(View view){
        Button male = view.findViewById(R.id.maleButton);
        Button female = view.findViewById(R.id.femaleButton);
        Button other = view.findViewById(R.id.otherButton);
        AtomicReference<String> genderChosen = new AtomicReference<>("unknown");

        // setting up text with emojis and onClickListeners
        male.setText(String.format("\uD83D\uDC68\n\n%s", male.getText()));
        male.setOnTouchListener((v, event) -> {
            genderChosen.set(MALE);
            male.setPressed(true);
            female.setPressed(false);
            other.setPressed(false);
            return true;
        });
        female.setText(String.format("\uD83D\uDC69\n\n%s", female.getText()));
        female.setOnTouchListener((v, event) -> {
            genderChosen.set(FEMALE);
            female.setPressed(true);
            male.setPressed(false);
            other.setPressed(false);
            return true;
        });
        other.setText(String.format("❓\n\n%s", other.getText()));
        other.setOnTouchListener((v, event) -> {
            genderChosen.set(OTHER);
            other.setPressed(true);
            male.setPressed(false);
            female.setPressed(false);
            return true;
        });

        // triggering onTouch programmatically
        switch (getGender(requireActivity())) {
            case MALE:
                male.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                male.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                break;
            case FEMALE:
                female.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                female.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                break;
            case OTHER:
                other.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                other.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
                break;
        }

        if(!InAppActivity.useNewPersonalDataFragments.get()) {
            view.findViewById(R.id.skipButton).setOnClickListener(v -> ((InAppActivity) requireActivity()).proceedQuestions(1));

            view.findViewById(R.id.proceedButton).setOnClickListener(v -> {
                if (genderChosen.get().equals("unknown"))
                    Toast.makeText(requireActivity(), R.string.select_a_gender, Toast.LENGTH_SHORT).show();
                else {
                    putGender(requireActivity(), genderChosen.get());
                    ((InAppActivity) requireActivity()).proceedQuestions(1);
                }
            });
        } else{
            InAppActivity.useNewPersonalDataFragments.set(false);
            ((AppCompatButton) view.findViewById(R.id.skipButton)).setText(R.string.do_not_change);
            ((AppCompatButton) view.findViewById(R.id.proceedButton)).setText(R.string.change);

            view.findViewById(R.id.skipButton).setOnClickListener(v -> requireActivity().onBackPressed());
            view.findViewById(R.id.proceedButton).setOnClickListener(v -> {
                if (genderChosen.get().equals("unknown"))
                    Toast.makeText(requireActivity(), R.string.select_a_gender, Toast.LENGTH_SHORT).show();
                else {
                    putGender(requireActivity(), genderChosen.get());
                    requireActivity().onBackPressed();
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gender_screen, container, false);
        setUpOnClickListeners(view);
        return view;
    }
}
