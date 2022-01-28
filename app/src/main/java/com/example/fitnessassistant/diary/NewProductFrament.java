package com.example.fitnessassistant.diary;

import static com.example.fitnessassistant.diary.NutritionGoals.getFloat;
import static com.example.fitnessassistant.util.TimeFunctional.getMonthShort;
import static java.time.temporal.ChronoUnit.DAYS;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.database.mdbh.MDBHNutritionTracker;
import com.example.fitnessassistant.nutritiontracker.APISearch;
import com.example.fitnessassistant.nutritiontracker.Product;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;
import com.example.fitnessassistant.util.CustomSpinner;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class NewProductFrament extends Fragment {
    private CustomSpinner spinner;
    private CustomSpinner dateSpinner;
    private String name = null;
    private float calories = -1f;
    private float carbs = -1f;
    private float sugar = -1f;
    private float fat = -1f;
    private float protein = -1f;

    @SuppressLint("DefaultLocale")
    private void setOnClickListeners(View view){
        view.findViewById(R.id.backButton).setOnClickListener(v -> requireActivity().onBackPressed());

        view.findViewById(R.id.productName).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(R.layout.custom_two_button_alert_dialog);
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.findViewById(R.id.dialog_drawable).setVisibility(View.GONE);
            dialog.findViewById(R.id.dialog_header).setVisibility(View.GONE);
            dialog.findViewById(R.id.dialog_message).setVisibility(View.GONE);

            EditText input = dialog.findViewById(R.id.dialog_input);
            if(name != null)
                input.setText(name);
            input.setHint(null);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);

            dialog.findViewById(R.id.dialog_negative_button).setOnClickListener(view2 -> dialog.dismiss());

            ((Button) dialog.findViewById(R.id.dialog_positive_button)).setText(R.string.set);
            dialog.findViewById(R.id.dialog_positive_button).setOnClickListener(view2 -> {
                if(input.getText().toString().isEmpty()){
                    Toast.makeText(requireActivity(), R.string.field_cannot_be_empty, Toast.LENGTH_SHORT).show();
                } else{
                    name = input.getText().toString();
                    ((TextView) view.findViewById(R.id.productName)).setText(name);
                    dialog.dismiss();
                }
            });
        });

        view.findViewById(R.id.caloriesAmount1).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(R.layout.custom_two_button_alert_dialog);
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.findViewById(R.id.dialog_drawable).setVisibility(View.GONE);
            dialog.findViewById(R.id.dialog_header).setVisibility(View.GONE);
            dialog.findViewById(R.id.dialog_message).setVisibility(View.GONE);

            EditText input = dialog.findViewById(R.id.dialog_input);
            if(calories != -1f) {
                if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
                    input.setText(String.format("%.1f", calories * 4.184f));
                else
                    input.setText(String.format("%d", Math.round(calories)));
            }
            input.setHint(null);
            if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ)) {
                input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            } else {
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
            input.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);

            dialog.findViewById(R.id.dialog_negative_button).setOnClickListener(view2 -> dialog.dismiss());

            ((Button) dialog.findViewById(R.id.dialog_positive_button)).setText(R.string.set);
            dialog.findViewById(R.id.dialog_positive_button).setOnClickListener(view2 -> {
                if(input.getText().toString().isEmpty()){
                    Toast.makeText(requireActivity(), R.string.field_cannot_be_empty, Toast.LENGTH_SHORT).show();
                } else{
                    if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ)){
                        ((TextView) view.findViewById(R.id.energyUnit)).setText(R.string.kilojoules);
                        ((TextView) view.findViewById(R.id.caloriesAmount1)).setText(String.format("%.1f", getFloat(input.getText().toString())));
                        calories = getFloat(input.getText().toString()) / 4.184f;
                    } else {
                        ((TextView) view.findViewById(R.id.energyUnit)).setText(R.string.calories);
                        ((TextView) view.findViewById(R.id.caloriesAmount1)).setText(String.format("%d", Integer.parseInt(input.getText().toString())));
                        calories = getFloat(input.getText().toString());
                    }
                    dialog.dismiss();
                }
            });
        });

        view.findViewById(R.id.setCarbs).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(R.layout.custom_two_button_alert_dialog);
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.findViewById(R.id.dialog_drawable).setVisibility(View.GONE);
            dialog.findViewById(R.id.dialog_header).setVisibility(View.GONE);
            dialog.findViewById(R.id.dialog_message).setVisibility(View.GONE);

            EditText input = dialog.findViewById(R.id.dialog_input);
            if(carbs != -1f)
                input.setText(String.format("%.1f", carbs));
            input.setHint(null);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);

            dialog.findViewById(R.id.dialog_negative_button).setOnClickListener(view2 -> dialog.dismiss());

            ((Button) dialog.findViewById(R.id.dialog_positive_button)).setText(R.string.set);
            dialog.findViewById(R.id.dialog_positive_button).setOnClickListener(view2 -> {
                if(input.getText().toString().isEmpty()){
                    Toast.makeText(requireActivity(), R.string.field_cannot_be_empty, Toast.LENGTH_SHORT).show();
                } else{
                    ((TextView) view.findViewById(R.id.carbsAmount1)).setText(String.format("%.1f", getFloat(input.getText().toString())));
                    carbs = getFloat(input.getText().toString());
                    dialog.dismiss();
                }
            });
        });

        view.findViewById(R.id.setSugar).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(R.layout.custom_two_button_alert_dialog);
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.findViewById(R.id.dialog_drawable).setVisibility(View.GONE);
            dialog.findViewById(R.id.dialog_header).setVisibility(View.GONE);
            dialog.findViewById(R.id.dialog_message).setVisibility(View.GONE);

            EditText input = dialog.findViewById(R.id.dialog_input);
            if(sugar != -1f)
                input.setText(String.format("%.1f", sugar));
            input.setHint(null);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);

            dialog.findViewById(R.id.dialog_negative_button).setOnClickListener(view2 -> dialog.dismiss());

            ((Button) dialog.findViewById(R.id.dialog_positive_button)).setText(R.string.set);
            dialog.findViewById(R.id.dialog_positive_button).setOnClickListener(view2 -> {
                if(input.getText().toString().isEmpty()){
                    Toast.makeText(requireActivity(), R.string.field_cannot_be_empty, Toast.LENGTH_SHORT).show();
                } else{
                    ((TextView) view.findViewById(R.id.sugarAmount)).setText(String.format("%.1f", getFloat(input.getText().toString())));
                    sugar = getFloat(input.getText().toString());
                    dialog.dismiss();
                }
            });
        });

        view.findViewById(R.id.setFat).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(R.layout.custom_two_button_alert_dialog);
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.findViewById(R.id.dialog_drawable).setVisibility(View.GONE);
            dialog.findViewById(R.id.dialog_header).setVisibility(View.GONE);
            dialog.findViewById(R.id.dialog_message).setVisibility(View.GONE);

            EditText input = dialog.findViewById(R.id.dialog_input);
            if(fat != -1f)
                input.setText(String.format("%.1f", fat));
            input.setHint(null);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);

            dialog.findViewById(R.id.dialog_negative_button).setOnClickListener(view2 -> dialog.dismiss());

            ((Button) dialog.findViewById(R.id.dialog_positive_button)).setText(R.string.set);
            dialog.findViewById(R.id.dialog_positive_button).setOnClickListener(view2 -> {
                if(input.getText().toString().isEmpty()){
                    Toast.makeText(requireActivity(), R.string.field_cannot_be_empty, Toast.LENGTH_SHORT).show();
                } else{
                    ((TextView) view.findViewById(R.id.fatAmount1)).setText(String.format("%.1f", getFloat(input.getText().toString())));
                    fat = getFloat(input.getText().toString());
                    dialog.dismiss();
                }
            });
        });

        view.findViewById(R.id.setProtein).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(R.layout.custom_two_button_alert_dialog);
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.findViewById(R.id.dialog_drawable).setVisibility(View.GONE);
            dialog.findViewById(R.id.dialog_header).setVisibility(View.GONE);
            dialog.findViewById(R.id.dialog_message).setVisibility(View.GONE);

            EditText input = dialog.findViewById(R.id.dialog_input);
            if(protein != -1f)
                input.setText(String.format("%.1f", protein));
            input.setHint(null);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);

            dialog.findViewById(R.id.dialog_negative_button).setOnClickListener(view2 -> dialog.dismiss());

            ((Button) dialog.findViewById(R.id.dialog_positive_button)).setText(R.string.set);
            dialog.findViewById(R.id.dialog_positive_button).setOnClickListener(view2 -> {
                if(input.getText().toString().isEmpty()){
                    Toast.makeText(requireActivity(), R.string.field_cannot_be_empty, Toast.LENGTH_SHORT).show();
                } else{
                    ((TextView) view.findViewById(R.id.proteinAmount1)).setText(String.format("%.1f", getFloat(input.getText().toString())));
                    protein = getFloat(input.getText().toString());
                    dialog.dismiss();
                }
            });
        });

        view.findViewById(R.id.forwardButton).setOnClickListener(v -> {
            int daysBefore = dateSpinner.getSelectedItemPosition();
            LocalDate date = LocalDate.now();
            date = date.minusDays(daysBefore);

            String dateFormatted = (String) DateFormat.format("yyyyMMdd", Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));

            if (spinner.getSelectedItemPosition() > 0 && spinner.getSelectedItemPosition() < 5) {
                if(name != null && calories != -1f && carbs != -1f && protein != -1f && fat != -1f && sugar != -1f){
                    Product newProduct = new Product(requireActivity(), name, APISearch.barcodeDetected, calories, sugar, carbs, protein, fat);
                    APISearch.barcodeDetected = null;
                    MDBHNutritionTracker.getInstance(requireActivity()).addNewProduct(newProduct.getId(), newProduct.getName(), newProduct.nutrimentsToDBString(), newProduct.getBarcode(), newProduct.getBrands());
                    InAppActivity.diaryFragment.putProduct(newProduct, 1f, dateFormatted, spinner.getSelectedItemPosition() + 100);
                    requireActivity().onBackPressed();
                } else {
                    Toast.makeText(requireActivity(), R.string.fill_up_all_fields, Toast.LENGTH_SHORT).show();
                }
            } else
                Toast.makeText(requireActivity(), R.string.select_a_meal, Toast.LENGTH_SHORT).show();
        });
    }

    @SuppressLint("DefaultLocale")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.meal_creation_screen, container, false);

        setOnClickListeners(view);

        StringSpinnerAdapter adapter = new StringSpinnerAdapter(requireActivity(), R.layout.string_spinner_layout, new String[]{
                "?",
                requireActivity().getString(R.string.breakfast),
                requireActivity().getString(R.string.lunch),
                requireActivity().getString(R.string.dinner),
                requireActivity().getString(R.string.snack),
        });
        adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        spinner = view.findViewById(R.id.mealSpinner);
        spinner.setAdapter(adapter);
        spinner.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            @Override
            public void onSpinnerOpened() { spinner.setSelected(true); }
            @Override
            public void onSpinnerClosed() { spinner.setSelected(false); }
        });

        dateSpinner = view.findViewById(R.id.dateSpinner);

        LocalDate start = LocalDate.now();
        LocalDate now = LocalDate.now();

        try {
            start = Instant.ofEpochMilli(requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0).firstInstallTime).atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String[] dates = new String[(int) DAYS.between(start, now) + 1];

        int i = 0;
        for (; now.atStartOfDay().isAfter(start.atStartOfDay()); now = now.minusDays(1), i++)
            dates[i] = String.format("%02d %s %d", now.getDayOfMonth(), getMonthShort(requireActivity(), now.getMonthValue()), now.getYear());

        if (i == (int) DAYS.between(start, LocalDate.now()))
            dates[i] = String.format("%02d %s %d", now.getDayOfMonth(), getMonthShort(requireActivity(), now.getMonthValue()), now.getYear());

        StringSpinnerAdapter adapter1 = new StringSpinnerAdapter(requireActivity(), R.layout.date_spinner_layout, dates);
        adapter1.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        dateSpinner.setAdapter(adapter1);
        dateSpinner.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            @Override
            public void onSpinnerOpened() {
                spinner.setSelected(true);
            }

            @Override
            public void onSpinnerClosed() {
                spinner.setSelected(false);
            }
        });

        dateSpinner.setSelection(0);
        dateSpinner.setVisibility(View.VISIBLE);

        ((TextView) view.findViewById(R.id.productName)).setText("?");
        ((TextView) view.findViewById(R.id.caloriesAmount1)).setText("?");
        ((TextView) view.findViewById(R.id.carbsAmount1)).setText("?");
        ((TextView) view.findViewById(R.id.fatAmount1)).setText("?");
        ((TextView) view.findViewById(R.id.proteinAmount1)).setText("?");
        ((TextView) view.findViewById(R.id.sugarAmount)).setText("?");

        return view;
    }
}
