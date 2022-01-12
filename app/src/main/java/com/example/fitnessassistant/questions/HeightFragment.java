package com.example.fitnessassistant.questions;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;

// height is saved in CMs
public class HeightFragment extends Fragment {

    public static synchronized float getWorldwideAverageHeight(Context context){
        if(GenderFragment.getGender(context).equals(GenderFragment.MALE))
            return 171f;
        else if(GenderFragment.getGender(context).equals(GenderFragment.FEMALE))
            return 159f;
        else
            return 165f;
    }

    public static synchronized float getHeight(Context context){
        return context.getSharedPreferences("questions", MODE_PRIVATE).getFloat("height", -1f);
    }

    public synchronized static void putHeight(Context context, float height){
        context.getSharedPreferences("questions", MODE_PRIVATE).edit().putFloat("height", height).apply();
    }

    private void setHeightInInches(float CMs, EditText feet, EditText inches){
        float INs = CMs / 2.54f;
        feet.setText(String.valueOf((int) (INs / 12f)));
        inches.setText(String.valueOf(Float.valueOf(INs - (int)(INs / 12) * 12)));
    }

    private void setHeightInCentimeters(float INs, EditText centimeters){
        float CMs = INs * 2.54f;
        centimeters.setText(String.valueOf(CMs));
    }

    private boolean validHeight(float CMs){
        return CMs > 50f && CMs < 260f;
    }

    private void setUpFeetInchesUI(View view, CompoundButton buttonView, EditText heightInCMs, EditText heightInFeet, EditText heightInInches, TextWatcher CMWatcher, TextWatcher INWatcher, TextWatcher FTWatcher){
        buttonView.setText(R.string.inches);
        view.findViewById(R.id.centimetersRow).setVisibility(View.GONE);
        heightInCMs.removeTextChangedListener(CMWatcher);
        view.findViewById(R.id.feetInhesRow).setVisibility(View.VISIBLE);
        heightInFeet.addTextChangedListener(FTWatcher);
        heightInInches.addTextChangedListener(INWatcher);
    }

    private void setUpCentimetersUI(View view, CompoundButton buttonView, EditText heightInCMs, EditText heightInFeet, EditText heightInInches, TextWatcher CMWatcher, TextWatcher INWatcher, TextWatcher FTWatcher){
        buttonView.setText(R.string.centimeters);
        view.findViewById(R.id.centimetersRow).setVisibility(View.VISIBLE);
        heightInFeet.removeTextChangedListener(FTWatcher);
        heightInInches.removeTextChangedListener(INWatcher);
        view.findViewById(R.id.feetInhesRow).setVisibility(View.GONE);
        heightInCMs.addTextChangedListener(CMWatcher);
    }

    private void setUpOnClickListeners(View view){
        EditText heightInCMs = view.findViewById(R.id.heightInCMs);
        EditText heightInFeet = view.findViewById(R.id.heightInFeet);
        EditText heightInInches = view.findViewById(R.id.heightInInches);

        TextWatcher CMWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // clear any previous errors
                heightInCMs.setError(null);
                heightInFeet.setError(null);
                heightInInches.setError(null);
                if(s.length() != 0){
                    float height = Float.parseFloat(s.toString());
                    setHeightInInches(height, heightInFeet, heightInInches);
                    if(height < 50f || height > 260f){
                        heightInCMs.setError(getString(R.string.height_not_valid));
                        heightInFeet.setError(getString(R.string.height_not_valid));
                        heightInInches.setError(getString(R.string.height_not_valid));
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        };

        TextWatcher INWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // clear any previous errors
                heightInCMs.setError(null);
                heightInFeet.setError(null);
                heightInInches.setError(null);
                if(heightInInches.getText().length() != 0 && heightInFeet.getText().length() != 0){
                    // get height in inches
                    float inches = Float.parseFloat(s.toString());
                    int feet = Integer.parseInt(heightInFeet.getText().toString()) * 12;

                    // if inches is higher than it should be
                    if(inches >= 12f){
                        heightInFeet.setText(String.valueOf(Integer.parseInt(heightInFeet.getText().toString()) + (int)(inches / 12)));
                        heightInInches.setText(String.valueOf(inches - (int)(inches / 12) * 12));
                    }

                    float height = feet + inches;
                    setHeightInCentimeters(height, heightInCMs);
                    if(height < 20f || height > 100f){
                        heightInCMs.setError(getString(R.string.height_not_valid));
                        heightInFeet.setError(getString(R.string.height_not_valid));
                        heightInInches.setError(getString(R.string.height_not_valid));
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        };

        TextWatcher FTWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // clear any previous errors
                heightInCMs.setError(null);
                heightInFeet.setError(null);
                heightInInches.setError(null);
                if(heightInFeet.getText().length() != 0 && heightInInches.getText().length() != 0){
                    // get height in inches
                    float height = Integer.parseInt(s.toString()) * 12 + Float.parseFloat(heightInInches.getText().toString());
                    setHeightInCentimeters(height, heightInCMs);
                    if(height < 20f || height > 100f){
                        heightInCMs.setError(getString(R.string.height_not_valid));
                        heightInFeet.setError(getString(R.string.height_not_valid));
                        heightInInches.setError(getString(R.string.height_not_valid));
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        };

        if(UnitPreferenceFragment.getHeightUnit(requireActivity()).equals(UnitPreferenceFragment.HEIGHT_UNIT_FT_IN))
            setUpFeetInchesUI(view, view.findViewById(R.id.unitSwitch), heightInCMs, heightInFeet, heightInInches, CMWatcher, INWatcher, FTWatcher);
        else
            setUpCentimetersUI(view, view.findViewById(R.id.unitSwitch), heightInCMs, heightInFeet, heightInInches, CMWatcher, INWatcher, FTWatcher);

        if(getHeight(requireActivity()) != -1f)
            heightInCMs.setText(String.valueOf(getHeight(requireActivity())));

        ((SwitchCompat) view.findViewById(R.id.unitSwitch)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!isChecked)
                setUpCentimetersUI(view, buttonView, heightInCMs, heightInFeet, heightInInches, CMWatcher, INWatcher, FTWatcher);
            else
                setUpFeetInchesUI(view, buttonView, heightInCMs, heightInFeet, heightInInches, CMWatcher, INWatcher, FTWatcher);
        });

        if(!InAppActivity.useNewPersonalDataFragments.get()) {
            view.findViewById(R.id.skipButton).setOnClickListener(v -> ((InAppActivity) requireActivity()).proceedQuestions(3));

            view.findViewById(R.id.proceedButton).setOnClickListener(v -> {
                if (heightInCMs.getText().length() != 0 && validHeight(Float.parseFloat(heightInCMs.getText().toString()))) {
                    putHeight(requireActivity(), Float.parseFloat(heightInCMs.getText().toString()));

                    // put unit preference given too
                    if (((SwitchCompat) view.findViewById(R.id.unitSwitch)).isChecked())
                        UnitPreferenceFragment.putHeightUnit(requireActivity(), UnitPreferenceFragment.HEIGHT_UNIT_FT_IN);
                    else
                        UnitPreferenceFragment.putHeightUnit(requireActivity(), UnitPreferenceFragment.HEIGHT_UNIT_CM);

                    ((InAppActivity) requireActivity()).proceedQuestions(3);
                }
            });
        } else {
            InAppActivity.useNewPersonalDataFragments.set(false);
            ((AppCompatButton) view.findViewById(R.id.skipButton)).setText(R.string.do_not_change);
            ((AppCompatButton) view.findViewById(R.id.proceedButton)).setText(R.string.change);

            view.findViewById(R.id.skipButton).setOnClickListener(v -> requireActivity().onBackPressed());
            view.findViewById(R.id.proceedButton).setOnClickListener(v -> {
                if (heightInCMs.getText().length() != 0 && validHeight(Float.parseFloat(heightInCMs.getText().toString()))) {
                    putHeight(requireActivity(), Float.parseFloat(heightInCMs.getText().toString()));

                    // put unit preference given too
                    if (((SwitchCompat) view.findViewById(R.id.unitSwitch)).isChecked())
                        UnitPreferenceFragment.putHeightUnit(requireActivity(), UnitPreferenceFragment.HEIGHT_UNIT_FT_IN);
                    else
                        UnitPreferenceFragment.putHeightUnit(requireActivity(), UnitPreferenceFragment.HEIGHT_UNIT_CM);

                    requireActivity().onBackPressed();
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.height_screen, container, false);
        setUpOnClickListeners(view);
        return view;
    }
}
