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
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;

public class WeightFragment extends Fragment {

    public static synchronized float getWeight(Context context){
        return context.getSharedPreferences("questions", MODE_PRIVATE).getFloat("weight", -1f);
    }

    public synchronized static void putWeight(Context context, float weight){
        context.getSharedPreferences("questions", MODE_PRIVATE).edit().putFloat("weight", weight).apply();
    }

    private boolean validWeight(float weightInKGs){
        return weightInKGs > 2 && weightInKGs < 700;
    }

    private void setWeightInKilograms(float LBS, EditText weightInKGs){
        float KG = (float) (LBS / 2.205);
        weightInKGs.setText(String.valueOf(KG));
    }

    private void setWeightInPounds(float KG, EditText weightInPounds){
        float LBS = (float) (KG * 2.205);
        weightInPounds.setText(String.valueOf(LBS));
    }

    private void setUpKilogramsUI(View view, CompoundButton buttonView, EditText weightInKGs, EditText weightInPounds, TextWatcher KGWatcher, TextWatcher LBSWatcher){
        buttonView.setText(R.string.kilograms);
        view.findViewById(R.id.poundsRow).setVisibility(View.GONE);
        weightInPounds.removeTextChangedListener(LBSWatcher);
        view.findViewById(R.id.kilogramsRow).setVisibility(View.VISIBLE);
        weightInKGs.addTextChangedListener(KGWatcher);
    }

    private void setUpPoundsUI(View view, CompoundButton buttonView, EditText weightInKGs, EditText weightInPounds, TextWatcher KGWatcher, TextWatcher LBSWatcher){
        buttonView.setText(R.string.pounds);
        view.findViewById(R.id.kilogramsRow).setVisibility(View.GONE);
        weightInKGs.removeTextChangedListener(KGWatcher);
        view.findViewById(R.id.poundsRow).setVisibility(View.VISIBLE);
        weightInPounds.addTextChangedListener(LBSWatcher);
    }

    private void setUpOnClickListeners(View view){
        EditText weightInKGs = view.findViewById(R.id.weightInKilograms);
        EditText weightInPounds = view.findViewById(R.id.weightInPounds);

        TextWatcher KGWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // clear any previous errors
                weightInKGs.setError(null);
                weightInPounds.setError(null);
                if(weightInKGs.getText().length() != 0){
                    // get height in inches
                    float weight = Float.parseFloat(weightInKGs.getText().toString());
                    setWeightInPounds(weight, weightInPounds);
                    if(weight < 2f || weight > 700f){
                        weightInKGs.setError(getString(R.string.weight_not_valid));
                        weightInPounds.setError(getString(R.string.weight_not_valid));
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        };

        TextWatcher LBSWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // clear any previous errors
                weightInKGs.setError(null);
                weightInPounds.setError(null);
                if(weightInPounds.getText().length() != 0){
                    // get height in inches
                    float weight = Float.parseFloat(weightInPounds.getText().toString());
                    setWeightInKilograms(weight, weightInKGs);
                    if(weight < 5f || weight > 1500f){
                        weightInKGs.setError(getString(R.string.weight_not_valid));
                        weightInPounds.setError(getString(R.string.weight_not_valid));
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) { }
        };

        setUpKilogramsUI(view, view.findViewById(R.id.unitSwitch), weightInKGs, weightInPounds, KGWatcher, LBSWatcher);

        ((SwitchCompat) view.findViewById(R.id.unitSwitch)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!isChecked)
                setUpKilogramsUI(view, buttonView, weightInKGs, weightInPounds, KGWatcher, LBSWatcher);
            else
                setUpPoundsUI(view, buttonView, weightInKGs, weightInPounds, KGWatcher, LBSWatcher);
        });

        view.findViewById(R.id.skipButton).setOnClickListener(v -> ((InAppActivity) requireActivity()).proceedQuestions(4));

        view.findViewById(R.id.proceedButton).setOnClickListener(v -> {
            if(weightInKGs.getText().length() != 0 && validWeight(Float.parseFloat(weightInKGs.getText().toString()))) {
                putWeight(requireActivity(), Float.parseFloat(weightInKGs.getText().toString()));

                // put unit preference given too
                if(((SwitchCompat) view.findViewById(R.id.unitSwitch)).isChecked())
                    UnitPreferenceFragment.putWeightUnit(requireActivity(), UnitPreferenceFragment.WEIGHT_UNIT_LBS);
                else
                    UnitPreferenceFragment.putWeightUnit(requireActivity(), UnitPreferenceFragment.WEIGHT_UNIT_KG);

                ((InAppActivity) requireActivity()).proceedQuestions(4);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weight_screen, container, false);
        setUpOnClickListeners(view);
        return view;
    }
}
