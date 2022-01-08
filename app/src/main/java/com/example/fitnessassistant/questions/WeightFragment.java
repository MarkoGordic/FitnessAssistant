package com.example.fitnessassistant.questions;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.pedometer.Pedometer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

// weight is saved in KGs
public class WeightFragment extends Fragment {
    public static AtomicBoolean isGoalWeight = new AtomicBoolean(false);

    private synchronized static int getNumOfWeightUpdatedToday(Context context){
        return context.getSharedPreferences("weightNumUpdated", MODE_PRIVATE).getInt(Pedometer.getCurrentDateFormatted(), 0);
    }

    private synchronized static float getTotalWeightToday(Context context){
        return context.getSharedPreferences("weightTotal", MODE_PRIVATE).getFloat(Pedometer.getCurrentDateFormatted(), 0f);
    }

    private synchronized static void putLastDailyAverage(Context context, float dailyAverage, String date){
        context.getSharedPreferences("questions", MODE_PRIVATE).edit().putFloat("lastDailyAverage", dailyAverage).apply();
        putLastDailyAverageDate(context, date);
    }

    private synchronized static void putFirstWeightDate(Context context, String date){
        context.getSharedPreferences("questions", MODE_PRIVATE).edit().putString("initDate", date).apply();
    }

    private synchronized static void putFirstWeight(Context context, float weight){
        context.getSharedPreferences("questions", MODE_PRIVATE).edit().putFloat("initWeight", weight).apply();
        putFirstWeightDate(context, Pedometer.getCurrentDateFormatted());
    }

    private synchronized static void putGoalWeight(Context context, float weight){
        context.getSharedPreferences("questions", MODE_PRIVATE).edit().putFloat("goalWeight", weight).apply();
    }

    public synchronized static float getGoalWeight(Context context){
        return context.getSharedPreferences("questions", MODE_PRIVATE).getFloat("goalWeight", -1f);
    }

    public synchronized static void updateWeightForToday(Context context, float weight){
        int newNum = getNumOfWeightUpdatedToday(context) + 1;
        float newTotal = getTotalWeightToday(context) + weight;

        // adding one more num weight updated
        context.getSharedPreferences("weightNumUpdated", MODE_PRIVATE).edit().putInt(Pedometer.getCurrentDateFormatted(), newNum).apply();
        // adding the weight to total weight
        context.getSharedPreferences("weightTotal", MODE_PRIVATE).edit().putFloat(Pedometer.getCurrentDateFormatted(), newTotal).apply();

        putLastDailyAverage(context, newTotal / newNum, Pedometer.getCurrentDateFormatted());
    }

    public synchronized static void deleteWeightForToday(Context context){
        // removing num of weight updated for today
        context.getSharedPreferences("weightNumUpdated", MODE_PRIVATE).edit().remove(Pedometer.getCurrentDateFormatted()).apply();
        // removing weight total for today
        context.getSharedPreferences("weightTotal", MODE_PRIVATE).edit().remove(Pedometer.getCurrentDateFormatted()).apply();

        // find lastSavedWeight
        ArrayList<Pair<String,Float>> allPreviousWeights = getAllPreviousWeights(context);
        int maxDate = -1;
        float lastAverageWeightSaved = -1f;
        for(Pair<String,Float> weightSaved : allPreviousWeights){
            int date = Integer.parseInt(weightSaved.first);
            if(date > maxDate){
                maxDate = date;
                lastAverageWeightSaved = weightSaved.second;
            }
        }

        // put last saved weight or -1f
        putLastDailyAverage(context, lastAverageWeightSaved, String.valueOf(maxDate));
    }

    public synchronized static float getLastDailyAverage(Context context){
        return context.getSharedPreferences("questions", MODE_PRIVATE).getFloat("lastDailyAverage", -1f);
    }

    public synchronized static void putLastDailyAverageDate(Context context, String date){
        context.getSharedPreferences("questions", MODE_PRIVATE).edit().putString("lastDailyAverageDate", date).apply();
    }

    public synchronized static String getLastDailyAverageDate(Context context){
        return context.getSharedPreferences("questions", MODE_PRIVATE).getString("lastDailyAverageDate", "unknown");
    }

    public static synchronized float getAverageWeightToday(Context context){
        if(getNumOfWeightUpdatedToday(context) == 0)
            return 0f;

        return getTotalWeightToday(context) / getNumOfWeightUpdatedToday(context);
    }

    public static synchronized float getFirstWeight(Context context){
        return context.getSharedPreferences("questions", MODE_PRIVATE).getFloat("initWeight", -1f);
    }

    public static synchronized String getFirstWeightDate(Context context){
        return context.getSharedPreferences("questions", MODE_PRIVATE).getString("initDate", "unknown");
    }

    public static synchronized ArrayList<Pair<String,Float>> getAllPreviousWeights(Context context){
        Map<String, ?> allNumUpdated = context.getSharedPreferences("weightNumUpdated", MODE_PRIVATE).getAll();
        Map<String, ?> allWeightTotal = context.getSharedPreferences("weightTotal", MODE_PRIVATE).getAll();

        Iterator<? extends Map.Entry<String, ?>> it = allNumUpdated.entrySet().iterator();
        ArrayList<Pair<String,Float>> averageWeights = new ArrayList<>();
        while(it.hasNext()){
            Map.Entry<String, ?> pairs = it.next();
            int numUpdated = (int) allNumUpdated.get(pairs.getKey());
            float weightTotal = (float) allWeightTotal.get(pairs.getKey());
            averageWeights.add(new Pair<>(pairs.getKey(), weightTotal / numUpdated));
        }
        return averageWeights;
    }

    public static synchronized List<String> getWeightsForDB(Context context){
        Map<String, ?> allNumUpdated = context.getSharedPreferences("weightNumUpdated", MODE_PRIVATE).getAll();
        Map<String, ?> allWeightTotal = context.getSharedPreferences("weightTotal", MODE_PRIVATE).getAll();

        Iterator<? extends Map.Entry<String, ?>> it = allNumUpdated.entrySet().iterator();
        ArrayList<String> weightsDB = new ArrayList<>();
        while(it.hasNext()){
            Map.Entry<String, ?> pairs = it.next();
            int numUpdated = (int) allNumUpdated.get(pairs.getKey());
            float weightTotal = (float) allWeightTotal.get(pairs.getKey());
            weightsDB.add(pairs.getKey() + "#" + numUpdated + "#" + weightTotal);
        }
        return weightsDB;
    }

    public static synchronized void putPreviousWeights(Context context, List<String> weightsDB){
        SharedPreferences.Editor numUpdated = context.getSharedPreferences("weightNumUpdated", MODE_PRIVATE).edit();
        SharedPreferences.Editor weightTotal = context.getSharedPreferences("weightTotal", MODE_PRIVATE).edit();
        numUpdated.clear();
        weightTotal.clear();

        for(String s : weightsDB) {
            StringTokenizer tokenizer = new StringTokenizer(s, "#");
            String date = tokenizer.nextToken();
            int num = Integer.parseInt(tokenizer.nextToken());
            float total = Float.parseFloat(tokenizer.nextToken());
            numUpdated.putInt(date, num);
            weightTotal.putFloat(date, total);
        }

        numUpdated.apply();
        weightTotal.apply();
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

        if(!InAppActivity.useNewPersonalDataFragments.get()) {
            if (getLastDailyAverage(requireActivity()) != -1f)
                weightInKGs.setText(String.valueOf(getLastDailyAverage(requireActivity())));

            view.findViewById(R.id.skipButton).setOnClickListener(v -> ((InAppActivity) requireActivity()).proceedQuestions(4));

            view.findViewById(R.id.proceedButton).setOnClickListener(v -> {
                if (weightInKGs.getText().length() != 0 && validWeight(Float.parseFloat(weightInKGs.getText().toString()))) {
                    putFirstWeight(requireActivity(), Float.parseFloat(weightInKGs.getText().toString()));
                    putFirstWeightDate(requireActivity(), Pedometer.getCurrentDateFormatted());
                    updateWeightForToday(requireActivity(), Float.parseFloat(weightInKGs.getText().toString()));

                    // put unit preference given too
                    if (((SwitchCompat) view.findViewById(R.id.unitSwitch)).isChecked())
                        UnitPreferenceFragment.putWeightUnit(requireActivity(), UnitPreferenceFragment.WEIGHT_UNIT_LBS);
                    else
                        UnitPreferenceFragment.putWeightUnit(requireActivity(), UnitPreferenceFragment.WEIGHT_UNIT_KG);

                    ((InAppActivity) requireActivity()).proceedQuestions(4);
                }
            });
        } else{
            InAppActivity.useNewPersonalDataFragments.set(false);
            ((AppCompatButton) view.findViewById(R.id.skipButton)).setText(R.string.do_not_change);
            ((AppCompatButton) view.findViewById(R.id.proceedButton)).setText(R.string.change);

            if(!isGoalWeight.get()) {
                if (getLastDailyAverage(requireActivity()) != -1f)
                    weightInKGs.setText(String.valueOf(getLastDailyAverage(requireActivity())));

                view.findViewById(R.id.updatingWeightTextView).setVisibility(View.VISIBLE);

                if(getNumOfWeightUpdatedToday(requireActivity()) != 0){
                    view.findViewById(R.id.deleteWeightsButton).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.deleteWeightsButton).setOnClickListener(v -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                        builder.setView(R.layout.custom_two_button_alert_dialog);
                        AlertDialog dialog = builder.create();
                        dialog.show();

                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        ((AppCompatImageView) dialog.findViewById(R.id.dialog_drawable)).setImageResource(R.drawable.scale);

                        ((TextView) dialog.findViewById(R.id.dialog_header)).setText(R.string.delete_entered_weights);
                        ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.delete_weights_message);
                        dialog.findViewById(R.id.dialog_input).setVisibility(View.GONE);

                        dialog.findViewById(R.id.dialog_negative_button).setOnClickListener(view2 -> dialog.dismiss());
                        ((Button) dialog.findViewById(R.id.dialog_positive_button)).setText(R.string.delete);
                        dialog.findViewById(R.id.dialog_positive_button).setOnClickListener(view2 -> {
                            deleteWeightForToday(requireActivity());
                            dialog.dismiss();
                            v.setVisibility(View.GONE);
                            weightInKGs.getText().clear();
                            weightInPounds.getText().clear();
                        });
                    });
                }

                view.findViewById(R.id.skipButton).setOnClickListener(v -> requireActivity().onBackPressed());
                view.findViewById(R.id.proceedButton).setOnClickListener(v -> {
                    if (weightInKGs.getText().length() != 0 && validWeight(Float.parseFloat(weightInKGs.getText().toString()))) {
                        if (getFirstWeight(requireActivity()) == -1f) {
                            putFirstWeight(requireActivity(), Float.parseFloat(weightInKGs.getText().toString()));
                            putFirstWeightDate(requireActivity(), Pedometer.getCurrentDateFormatted());
                        }
                        updateWeightForToday(requireActivity(), Float.parseFloat(weightInKGs.getText().toString()));

                        // put unit preference given too
                        if (((SwitchCompat) view.findViewById(R.id.unitSwitch)).isChecked())
                            UnitPreferenceFragment.putWeightUnit(requireActivity(), UnitPreferenceFragment.WEIGHT_UNIT_LBS);
                        else
                            UnitPreferenceFragment.putWeightUnit(requireActivity(), UnitPreferenceFragment.WEIGHT_UNIT_KG);

                        requireActivity().onBackPressed();
                    }
                });
            } else{
                isGoalWeight.set(false);

                ((TextView) view.findViewById(R.id.enterYourWeightTextView)).setText(R.string.change_your_goal_weight);

                if(getGoalWeight(requireActivity()) != -1f)
                    weightInKGs.setText(String.valueOf(getGoalWeight(requireActivity())));

                view.findViewById(R.id.skipButton).setOnClickListener(v -> requireActivity().onBackPressed());
                view.findViewById(R.id.proceedButton).setOnClickListener(v -> {
                    if (weightInKGs.getText().length() != 0 && validWeight(Float.parseFloat(weightInKGs.getText().toString()))) {
                        putGoalWeight(requireActivity(), Float.parseFloat(weightInKGs.getText().toString()));

                        // put unit preference given too
                        if (((SwitchCompat) view.findViewById(R.id.unitSwitch)).isChecked())
                            UnitPreferenceFragment.putWeightUnit(requireActivity(), UnitPreferenceFragment.WEIGHT_UNIT_LBS);
                        else
                            UnitPreferenceFragment.putWeightUnit(requireActivity(), UnitPreferenceFragment.WEIGHT_UNIT_KG);

                        requireActivity().onBackPressed();
                    }
                });
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.weight_screen, container, false);
        setUpOnClickListeners(view);
        return view;
    }
}
