package com.example.fitnessassistant.diary;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.activitytracker.LocationService;
import com.example.fitnessassistant.database.mdbh.MDBHNutritionGoals;
import com.example.fitnessassistant.questions.BirthdayFragment;
import com.example.fitnessassistant.questions.HeightFragment;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;
import com.example.fitnessassistant.questions.WeightFragment;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class NutritionGoals extends Fragment {
    public synchronized static void putUnsavedNutritionGoals(Context context){
        // getting last date saved in db
        String date = MDBHNutritionGoals.getInstance(context).findLatestDayInDB();

        if(date == null) {
            try {
                date = (String) DateFormat.format("yyyyMMdd", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).firstInstallTime);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        if(date != null) {
            int latestDate = Integer.parseInt(date);

            int latestDay = latestDate % 100;
            latestDate /= 100;
            int latestMonth = latestDate % 100;
            latestDate /= 100;
            int latestYear = latestDate;

            for(LocalDate localDate = LocalDate.of(latestYear, latestMonth, latestDay); localDate.isBefore(LocalDateTime.ofInstant(Calendar.getInstance().toInstant(), Calendar.getInstance().getTimeZone().toZoneId()).toLocalDate()); localDate = localDate.plusDays(1))
                MDBHNutritionGoals.getInstance(context).putNutritionGoalsData(context, localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")), getCaloriesGoal(context), getCarbsGoal(context), getFatGoal(context), getProteinGoal(context));
        }
    }

    public synchronized static void setCaloriesGoal(Context context, float calsGoal){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat("caloriesGoal", calsGoal).apply();
    }

    public synchronized static void setProteinGoal(Context context, float protein){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat("proteinGoal", protein).apply();
    }

    public synchronized static void setFatGoal(Context context, float fat){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat("fatGoal", fat).apply();
    }

    public synchronized static void setCarbsGoal(Context context, float carbs){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat("carbsGoal", carbs).apply();
    }

    public synchronized static float getCaloriesGoal(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getFloat("caloriesGoal", -1f);
    }

    public synchronized static float getProteinGoal(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getFloat("proteinGoal", -1f);
    }

    public synchronized static float getFatGoal(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getFloat("fatGoal", -1f);
    }

    public synchronized static float getCarbsGoal(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getFloat("carbsGoal", -1f);
    }

    private boolean fieldsNotEmpty(View view){
        if(((EditText) view.findViewById(R.id.carbsGoal)).getText().length() == 0 ||
                ((EditText) view.findViewById(R.id.proteinGoal)).getText().length() == 0 ||
                ((EditText) view.findViewById(R.id.fatGoal)).getText().length() == 0 ||
                ((EditText) view.findViewById(R.id.mainKcal)).getText().length() == 0) {
            Toast.makeText(requireActivity(), R.string.fill_up_all_fields, Toast.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }

    private boolean fieldsValid(View view){
        Number mainKcal, totalKcal;
        if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ)) {
            mainKcal = getFloat(((EditText) view.findViewById(R.id.mainKcal)).getText().toString());
            totalKcal = getFloat(((TextView) view.findViewById(R.id.totalKcal)).getText().toString());
        } else{
            mainKcal = Math.round(getFloat(((EditText) view.findViewById(R.id.mainKcal)).getText().toString()));
            totalKcal = Math.round(getFloat(((TextView) view.findViewById(R.id.totalKcal)).getText().toString()));
        }

        if (!mainKcal.equals(totalKcal)) {
            if (UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
                Toast.makeText(requireActivity(), R.string.total_kj_must_be_equal, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(requireActivity(), R.string.total_kcal_must_be_equal, Toast.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }

    private float getFloat(String str){
        float total = 0f;
        try {
            total = Float.parseFloat(str);
        } catch(NumberFormatException ex) {
            try {
                Number n;
                n = new DecimalFormat().parse(str);
                if(n != null)
                    total = n.floatValue();
            } catch(ParseException ignored){}
        }
        return total;
    }

    private float getCaloriesAmount(Context context){
        return LocationService.getBMR(requireActivity(), WeightFragment.getLastDailyAverage(requireActivity()), HeightFragment.getHeight(requireActivity()), BirthdayFragment.getYears(context)) * 1.425f;
    }

    private float getFatRecommendation(float calories){
        return (calories / 4f) / 9f; // 25% and 9cal per gram
    }

    private float getProteinRecommendation(float caloriesLeft){
        float weight = WeightFragment.getLastDailyAverage(requireActivity());

        if(weight == -1)
            weight = WeightFragment.getWorldwideAverageWeight(requireActivity());

        return Math.min(caloriesLeft, weight * 2.2f);
    }

    @SuppressLint("DefaultLocale")
    private void setUpOnClickListeners(View view){
        view.findViewById(R.id.cancelButton).setOnClickListener(v -> requireActivity().onBackPressed());

        EditText mainGoal = view.findViewById(R.id.mainKcal);

        if(getCaloriesGoal(requireActivity()) != -1)
            if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
                mainGoal.setText(String.format("%.1f", Math.round(getCaloriesGoal(requireActivity())) * 4.184f));
            else
                mainGoal.setText(String.format("%d", Math.round(getCaloriesGoal(requireActivity()))));

        EditText proteinGoal = view.findViewById(R.id.proteinGoal);
        EditText carbsGoal = view.findViewById(R.id.carbsGoal);
        EditText fatGoal = view.findViewById(R.id.fatGoal);

        TextView proteinCals = view.findViewById(R.id.proteinCals);

        float totalCalories = 0f;

        float pGoal = getProteinGoal(requireActivity());
        if(pGoal != -1) {
            totalCalories += pGoal * 4f;
            proteinGoal.setText(String.format("%.1f", pGoal));
            if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
                proteinCals.setText(String.format("%d", Math.round(pGoal * 4.184f * 4f)));
            else
                proteinCals.setText(String.format("%d", Math.round(pGoal * 4f)));

        }

        TextView carbCals = view.findViewById(R.id.carbsCalories);

        float cGoal = getCarbsGoal(requireActivity());
        if(cGoal != -1) {
            totalCalories += cGoal * 4f;
            carbsGoal.setText(String.format("%.1f", cGoal));
            if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
                carbCals.setText(String.format("%d", Math.round(cGoal * 4.184f * 4f)));
            else
                carbCals.setText(String.format("%d", Math.round(cGoal * 4f)));
        }

        TextView fatCals = view.findViewById(R.id.fatCalories);

        float fGoal = getFatGoal(requireActivity());
        if(fGoal != -1) {
            totalCalories += fGoal * 9f;
            fatGoal.setText(String.format("%.1f", fGoal));
            if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
                fatCals.setText(String.format("%d", Math.round(fGoal * 4.184f * 9f)));
            else
                fatCals.setText(String.format("%d", Math.round(fGoal * 9f)));
        }

        TextView totalCals = view.findViewById(R.id.totalKcal);

        if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
            totalCals.setText(String.format("%.1f", Math.round(totalCalories) * 4.184f));
        else
            totalCals.setText(String.format("%d", Math.round(totalCalories)));

        proteinGoal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                float energy, totalEnergy = 0;
                if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
                    energy = Math.round(getFloat(s.toString()) * 4f) * 4.184f;
                else
                    energy = Math.round(getFloat(s.toString()) * 4f);

                totalEnergy += energy;

                if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ)){
                    if(fatCals.getText().length() != 0)
                        totalEnergy += getFloat(fatCals.getText().toString());

                    if(carbCals.getText().length() != 0)
                        totalEnergy += getFloat(carbCals.getText().toString());

                    proteinCals.setText(String.format("%.1f", energy));
                    totalCals.setText(String.format("%.1f", totalEnergy));
                } else {
                    if(fatCals.getText().length() != 0)
                        totalEnergy += Math.round(getFloat(fatCals.getText().toString()));

                    if(carbCals.getText().length() != 0)
                        totalEnergy += Math.round(getFloat(carbCals.getText().toString()));

                    proteinCals.setText(String.format("%d", Math.round(energy)));
                    totalCals.setText(String.format("%d", Math.round(totalEnergy)));
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        fatGoal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                float energy, totalEnergy = 0;
                if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
                    energy = Math.round(getFloat(s.toString()) * 9f) * 4.184f;
                else
                    energy = Math.round(getFloat(s.toString()) * 9f);

                totalEnergy += energy;

                if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ)){
                    if(proteinCals.getText().length() != 0)
                        totalEnergy += getFloat(proteinCals.getText().toString());

                    if(carbCals.getText().length() != 0)
                        totalEnergy += getFloat(carbCals.getText().toString());

                    fatCals.setText(String.format("%.1f", energy));
                    totalCals.setText(String.format("%.1f", totalEnergy));
                } else {
                    if(proteinCals.getText().length() != 0)
                        totalEnergy += Math.round(getFloat(proteinCals.getText().toString()));

                    if(carbCals.getText().length() != 0)
                        totalEnergy += Math.round(getFloat(carbCals.getText().toString()));

                    fatCals.setText(String.format("%d", Math.round(energy)));
                    totalCals.setText(String.format("%d", Math.round(totalEnergy)));
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        carbsGoal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                float energy, totalEnergy = 0;
                if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
                    energy = Math.round(getFloat(s.toString()) * 4f) * 4.184f;
                else
                    energy = Math.round(getFloat(s.toString()) * 4f);

                totalEnergy += energy;

                if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ)){
                    if(proteinCals.getText().length() != 0)
                        totalEnergy += getFloat(proteinCals.getText().toString());

                    if(fatCals.getText().length() != 0)
                        totalEnergy += getFloat(fatCals.getText().toString());

                    carbCals.setText(String.format("%.1f", energy));
                    totalCals.setText(String.format("%.1f", totalEnergy));
                } else {
                    if(proteinCals.getText().length() != 0)
                        totalEnergy += Math.round(getFloat(proteinCals.getText().toString()));

                    if(fatCals.getText().length() != 0)
                        totalEnergy += Math.round(getFloat(fatCals.getText().toString()));

                    carbCals.setText(String.format("%d", Math.round(energy)));
                    totalCals.setText(String.format("%d", Math.round(totalEnergy)));
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        view.findViewById(R.id.setButton).setOnClickListener(v -> {
            if(fieldsNotEmpty(view) && fieldsValid(view)){
                float kGoal;
                if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
                    kGoal = getFloat(mainGoal.getText().toString());
                else
                    kGoal = Math.round(getFloat(mainGoal.getText().toString()));
                float caGoal = getFloat(carbsGoal.getText().toString());
                float faGoal = getFloat(fatGoal.getText().toString());
                float prGoal = getFloat(proteinGoal.getText().toString());

                if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
                    setCaloriesGoal(requireActivity(), kGoal / 4.184f);
                else
                    setCaloriesGoal(requireActivity(), kGoal);

                setProteinGoal(requireActivity(), prGoal);
                setCarbsGoal(requireActivity(), caGoal);
                setFatGoal(requireActivity(), faGoal);
                requireActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.moreInfo).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(R.layout.macro_recommendation_dialog);
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.findViewById(R.id.dialog_ok_button).setOnClickListener(view1 -> dialog.dismiss());

            float calories = getCaloriesAmount(requireActivity());

            if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
                ((TextView) dialog.findViewById(R.id.caloriesAmount)).setText(String.format("%.1f%s", calories * 4.184f, requireActivity().getString(R.string.kj)));
            else
                ((TextView) dialog.findViewById(R.id.caloriesAmount)).setText(String.format("%d%s", Math.round(calories), requireActivity().getString(R.string.cal)));

            float fat = getFatRecommendation(calories);

            calories -= fat * 9f;

            float protein = getProteinRecommendation(calories);

            calories -= protein * 4f;

            float carb = calories / 4f;

            ((TextView) dialog.findViewById(R.id.proteinAmount)).setText(String.format("%.1fg", protein));
            ((TextView) dialog.findViewById(R.id.fatAmount)).setText(String.format("%.1fg", fat));
            ((TextView) dialog.findViewById(R.id.carbAmount)).setText(String.format("%.1fg", carb));
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.nutrition_goals, container, false);

        if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ)){
            ((TextView) view.findViewById(R.id.mainUnit)).setText(requireActivity().getString(R.string.kj));
            ((TextView) view.findViewById(R.id.carbUnit)).setText(requireActivity().getString(R.string.kj));
            ((TextView) view.findViewById(R.id.fatUnit)).setText(requireActivity().getString(R.string.kj));
            ((TextView) view.findViewById(R.id.proteinUnit)).setText(requireActivity().getString(R.string.kj));
            ((TextView) view.findViewById(R.id.totalUnit)).setText(requireActivity().getString(R.string.kj));

            ((EditText) view.findViewById(R.id.mainKcal)).setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else{
            ((TextView) view.findViewById(R.id.mainUnit)).setText(requireActivity().getString(R.string.cal));
            ((TextView) view.findViewById(R.id.carbUnit)).setText(requireActivity().getString(R.string.cal));
            ((TextView) view.findViewById(R.id.fatUnit)).setText(requireActivity().getString(R.string.cal));
            ((TextView) view.findViewById(R.id.proteinUnit)).setText(requireActivity().getString(R.string.cal));
            ((TextView) view.findViewById(R.id.totalUnit)).setText(requireActivity().getString(R.string.cal));

            ((EditText) view.findViewById(R.id.mainKcal)).setInputType(InputType.TYPE_CLASS_NUMBER);
        }

        setUpOnClickListeners(view);

        return view;
    }
}
