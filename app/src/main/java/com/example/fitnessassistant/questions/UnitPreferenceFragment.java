package com.example.fitnessassistant.questions;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;

public class UnitPreferenceFragment extends Fragment {
    public static final String WEIGHT_UNIT_KG = "kg";
    public static final String WEIGHT_UNIT_LBS = "lbs";
    public static final String HEIGHT_UNIT_CM = "cm";
    public static final String HEIGHT_UNIT_FT_IN = "ft_in";
    public static final String DISTANCE_UNIT_KM = "km";
    public static final String DISTANCE_UNIT_MILE = "mile";
    public static final String ENERGY_UNIT_KJ = "kj";
    public static final String ENERGY_UNIT_CAL = "cal";
    public static final String FLUID_UNIT_GALLON = "gallon";
    public static final String FLUID_UNIT_LITRE = "litre";

    public synchronized static boolean isUnknown(Context context){
        return getFluidUnit(context).equals("unknown")
                || getEnergyUnit(context).equals("unknown")
                || getDistanceUnit(context).equals("unknown")
                || getWeightUnit(context).equals("unknown")
                || getHeightUnit(context).equals("unknown");
    }

    public static synchronized String getFluidUnit(Context context){
        return context.getSharedPreferences("questions", MODE_PRIVATE).getString("fluidUnit", "unknown");
    }

    public synchronized static void putFluidUnit(Context context, String fluidUnit){
        context.getSharedPreferences("questions", MODE_PRIVATE).edit().putString("fluidUnit", fluidUnit).apply();
    }

    public static synchronized String getEnergyUnit(Context context){
        return context.getSharedPreferences("questions", MODE_PRIVATE).getString("energyUnit", "unknown");
    }

    public synchronized static void putEnergyUnit(Context context, String energyUnit){
        context.getSharedPreferences("questions", MODE_PRIVATE).edit().putString("energyUnit", energyUnit).apply();
    }

    public static synchronized String getDistanceUnit(Context context){
        return context.getSharedPreferences("questions", MODE_PRIVATE).getString("distanceUnit", "unknown");
    }

    public synchronized static void putDistanceUnit(Context context, String distanceUnit){
        context.getSharedPreferences("questions", MODE_PRIVATE).edit().putString("distanceUnit", distanceUnit).apply();
    }

    public static synchronized String getWeightUnit(Context context){
        return context.getSharedPreferences("questions", MODE_PRIVATE).getString("weightUnit", "unknown");
    }

    public synchronized static void putWeightUnit(Context context, String weightUnit){
        context.getSharedPreferences("questions", MODE_PRIVATE).edit().putString("weightUnit", weightUnit).apply();
    }

    public static synchronized String getHeightUnit(Context context){
        return context.getSharedPreferences("questions", MODE_PRIVATE).getString("heightUnit", "unknown");
    }

    public synchronized static void putHeightUnit(Context context, String heightUnit){
        context.getSharedPreferences("questions", MODE_PRIVATE).edit().putString("heightUnit", heightUnit).apply();
    }

    private void setUpOnClickListeners(View view){
        ((SwitchCompat) view.findViewById(R.id.heightSwitch)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                buttonView.setText(R.string.inches);
            else
                buttonView.setText(R.string.centimeters);
        });

        ((SwitchCompat) view.findViewById(R.id.weightSwitch)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                buttonView.setText(R.string.pounds);
            else
                buttonView.setText(R.string.kilograms);
        });

        ((SwitchCompat) view.findViewById(R.id.distanceSwitch)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                buttonView.setText(R.string.miles);
            else
                buttonView.setText(R.string.kilometres);
        });

        ((SwitchCompat) view.findViewById(R.id.energySwitch)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                buttonView.setText(R.string.kilojoules);
            else
                buttonView.setText(R.string.calories);
        });

        ((SwitchCompat) view.findViewById(R.id.fluidSwitch)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked)
                buttonView.setText(R.string.gallons);
            else
                buttonView.setText(R.string.litres);
        });

        if(getHeightUnit(requireActivity()).equals(HEIGHT_UNIT_CM))
            ((SwitchCompat) view.findViewById(R.id.heightSwitch)).setChecked(false);
        else if(getHeightUnit(requireActivity()).equals(HEIGHT_UNIT_FT_IN))
            ((SwitchCompat) view.findViewById(R.id.heightSwitch)).setChecked(true);

        if(getWeightUnit(requireActivity()).equals(WEIGHT_UNIT_KG))
            ((SwitchCompat) view.findViewById(R.id.weightSwitch)).setChecked(false);
        else if(getWeightUnit(requireActivity()).equals(WEIGHT_UNIT_LBS))
            ((SwitchCompat) view.findViewById(R.id.weightSwitch)).setChecked(true);

        view.findViewById(R.id.skipButton).setOnClickListener(v -> ((InAppActivity) requireActivity()).proceedQuestions(5));

        view.findViewById(R.id.proceedButton).setOnClickListener(v -> {
            if(!((SwitchCompat) view.findViewById(R.id.heightSwitch)).isChecked())
                putHeightUnit(requireActivity(), HEIGHT_UNIT_CM);
            else
                putHeightUnit(requireActivity(), HEIGHT_UNIT_FT_IN);

            if(!((SwitchCompat) view.findViewById(R.id.weightSwitch)).isChecked())
                putWeightUnit(requireActivity(), WEIGHT_UNIT_KG);
            else
                putWeightUnit(requireActivity(), WEIGHT_UNIT_LBS);

            if(!((SwitchCompat) view.findViewById(R.id.distanceSwitch)).isChecked())
                putDistanceUnit(requireActivity(), DISTANCE_UNIT_KM);
            else
                putDistanceUnit(requireActivity(), DISTANCE_UNIT_MILE);

            if(!((SwitchCompat) view.findViewById(R.id.energySwitch)).isChecked())
                putEnergyUnit(requireActivity(), ENERGY_UNIT_CAL);
            else
                putEnergyUnit(requireActivity(), ENERGY_UNIT_KJ);

            if(!((SwitchCompat) view.findViewById(R.id.fluidSwitch)).isChecked())
                putFluidUnit(requireActivity(), FLUID_UNIT_LITRE);
            else
                putFluidUnit(requireActivity(), FLUID_UNIT_GALLON);

            if(OpeningQuestionFragment.everythingIsKnown(requireActivity()))
                OpeningQuestionFragment.putShouldSkipQuestions(requireActivity(), true);

            ((InAppActivity) requireActivity()).proceedQuestions(5);
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.unit_preference_screen, container, false);
        setUpOnClickListeners(view);
        return view;
    }
}
