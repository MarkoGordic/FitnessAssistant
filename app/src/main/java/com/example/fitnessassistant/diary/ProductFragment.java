package com.example.fitnessassistant.diary;

import static com.example.fitnessassistant.util.TimeFunctional.getMonthShort;
import static java.time.temporal.ChronoUnit.DAYS;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.InAppActivity;
import com.example.fitnessassistant.R;
import com.example.fitnessassistant.database.mdbh.MDBHNutritionTracker;
import com.example.fitnessassistant.nutritiontracker.Product;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;
import com.example.fitnessassistant.questions.WeightFragment;
import com.example.fitnessassistant.util.CustomSpinner;
import com.example.fitnessassistant.util.PieView;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class ProductFragment extends Fragment {
    private final Product product;
    private float amountChosen;
    private final int mealType;
    private final LocalDate localDate;
    private CustomSpinner spinner;
    private CustomSpinner dateSpinner;

    public ProductFragment(Product product, Float quantity, Integer mealType, LocalDate localDate){
        if(quantity == null)
            quantity = 100f;
        if(mealType == null)
            mealType = -1;
        if(localDate == null)
            localDate = LocalDate.now();

        this.product = product;
        this.amountChosen = quantity;
        this.mealType = mealType;
        this.localDate = localDate;
    }

    @SuppressLint("DefaultLocale")
    private void setProductDetails(View view, float amountChosen){
        ((TextView) view.findViewById(R.id.fiberAmount)).setText(String.format("%.1f", product.getFiber_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.sugarAmount)).setText(String.format("%.1f", product.getSugars_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.saturatedFatAmount)).setText(String.format("%.1f", product.getSaturated_fat_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.monoUnsaturatedFatAmount)).setText(String.format("%.1f", product.getMonounsaturated_fat_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.polyUnsaturatedFatAmount)).setText(String.format("%.1f", product.getPolyunsaturated_fat_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.transFatAmount)).setText(String.format("%.1f", product.getTrans_fat_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.caseinAmount)).setText(String.format("%.1f", product.getCasein_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.omega3FatAmount)).setText(String.format("%.1f", product.getOmega_3_fat_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.omega6FatAmount)).setText(String.format("%.1f", product.getOmega_6_fat_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.omega9FatAmount)).setText(String.format("%.1f", product.getOmega_9_fat_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.starchAmount)).setText(String.format("%.1f", product.getStarch_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.glucoseAmount)).setText(String.format("%.1f", product.getGlucose_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.fructoseAmount)).setText(String.format("%.1f", product.getFructose_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.sucroseAmount)).setText(String.format("%.1f", product.getSucrose_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.lactoseAmount)).setText(String.format("%.1f", product.getLactose_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.maltoseAmount)).setText(String.format("%.1f", product.getMaltose_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.cholesterolAmount)).setText(String.format("%.1f", product.getCholesterol_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.caffeineAmount)).setText(String.format("%.1f", product.getCaffeine_100g() * 1000f * amountChosen));
        ((TextView) view.findViewById(R.id.taurineAmount)).setText(String.format("%.1f", product.getTaurine_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.alcoholAmount)).setText(String.format("%.1f", product.getAlcohol_100g() * amountChosen));
        ((TextView) view.findViewById(R.id.potassiumAmount)).setText(String.format("%.1f", product.getPotassium_100g() * 1000f * amountChosen));
        ((TextView) view.findViewById(R.id.sodiumAmount)).setText(String.format("%.1f", product.getSodium_100g() * 1000f * amountChosen));
        ((TextView) view.findViewById(R.id.saltAmount)).setText(String.format("%.1f", product.getSalt_100g() * 1000f * amountChosen));
        ((TextView) view.findViewById(R.id.calciumAmount)).setText(String.format("%.1f", product.getCalcium_100g() * 1000f * amountChosen));
        ((TextView) view.findViewById(R.id.magnesiumAmount)).setText(String.format("%.1f", product.getMagnesium_100g() * 1000f * amountChosen));
        ((TextView) view.findViewById(R.id.zincAmount)).setText(String.format("%.1f", product.getZinc_100g() * 1000000f * amountChosen));
        ((TextView) view.findViewById(R.id.ironAmount)).setText(String.format("%.1f", product.getIron_100g() * 1000000f * amountChosen));
        ((TextView) view.findViewById(R.id.iodineAmount)).setText(String.format("%.1f", product.getIodine_100g() * 1000000f * amountChosen));
        ((TextView) view.findViewById(R.id.fluorideAmount)).setText(String.format("%.1f", product.getFluoride_100g() * 1000000f * amountChosen));
        ((TextView) view.findViewById(R.id.copperAmount)).setText(String.format("%.1f", product.getCopper_100g() * 1000000f * amountChosen));
        ((TextView) view.findViewById(R.id.manganeseAmount)).setText(String.format("%.1f", product.getManganese_100g() * 1000000f * amountChosen));
        ((TextView) view.findViewById(R.id.chlorideAmount)).setText(String.format("%.1f", product.getChloride_100g() * 1000000f * amountChosen));
        ((TextView) view.findViewById(R.id.vitaminB1Amount)).setText(String.format("%.1f", product.getVitamin_b1_100g() * 1000000f * amountChosen));
        ((TextView) view.findViewById(R.id.vitaminB2Amount)).setText(String.format("%.1f", product.getVitamin_b2_100g() * 1000000f * amountChosen));
        ((TextView) view.findViewById(R.id.vitaminB3Amount)).setText(String.format("%.1f", product.getVitamin_pp_100g() * 1000f * amountChosen));
        ((TextView) view.findViewById(R.id.vitaminB6Amount)).setText(String.format("%.1f", product.getVitamin_b6_100g() * 1000000f * amountChosen));
        ((TextView) view.findViewById(R.id.vitaminB7Amount)).setText(String.format("%.1f", product.getBiotin_100g() * 1000000f * amountChosen));
        ((TextView) view.findViewById(R.id.vitaminB9Amount)).setText(String.format("%.1f", product.getVitamin_b9_100g() * 1000000f * amountChosen));
        ((TextView) view.findViewById(R.id.vitaminB12Amount)).setText(String.format("%.1f", product.getVitamin_b12_100g() * 1000000f * amountChosen));
        ((TextView) view.findViewById(R.id.vitaminCAmount)).setText(String.format("%.1f", product.getVitamin_c_100g() * 1000f * amountChosen));
        ((TextView) view.findViewById(R.id.vitaminDAmount)).setText(String.format("%.1f", product.getVitamin_d_100g() * 1000000f * amountChosen));
        ((TextView) view.findViewById(R.id.vitaminEAmount)).setText(String.format("%.1f", product.getVitamin_e_100g() * 1000000f * amountChosen));
        ((TextView) view.findViewById(R.id.vitaminKAmount)).setText(String.format("%.1f", product.getVitamin_k_100g() * 1000000f * amountChosen));
        ((TextView) view.findViewById(R.id.vitaminAAmount)).setText(String.format("%.1f", product.getVitamin_a_100g() * 1000000f * amountChosen));
    }

    @SuppressLint("DefaultLocale")
    private void setProductMacroPercentages(View view){
        float totalKcal = product.getEnergy_kcal_100g();
        float carbs = product.getCarbohydrates_100g();
        float fat = product.getFat_100g();
        float protein = product.getProteins_100g();

        int carbPercent = 0;
        int proteinPercent = 0;
        int fatPercent = 0;

        if(totalKcal > 0){
            carbPercent = Math.round(carbs * 4f / totalKcal * 100f);
            proteinPercent = Math.round(protein * 4f / totalKcal * 100f);
            fatPercent = Math.round(fat * 9f / totalKcal * 100f);

            float carbKcal = carbs * 4f;
            float fatKcal = fat * 9f;
            float proteinKcal = protein * 4f;

            float maxKcal = Math.max(proteinKcal, Math.max(carbKcal, fatKcal));

            if(maxKcal == carbKcal)
                carbPercent = 100 - proteinPercent - fatPercent;
            else if (maxKcal == fatKcal)
                fatPercent = 100 - proteinPercent - carbPercent;
            else if (maxKcal == proteinKcal)
                proteinPercent = 100 - carbPercent - fatPercent;
        }

        ((TextView) view.findViewById(R.id.carbsPercent)).setText(String.format("%d%%", carbPercent));
        ((TextView) view.findViewById(R.id.fatPercent)).setText(String.format("%d%%", fatPercent));
        ((TextView) view.findViewById(R.id.proteinPercent)).setText(String.format("%d%%", proteinPercent));

        int[] data = new int[]{ carbPercent, proteinPercent, fatPercent };
        int[] colors = new int[]{ requireActivity().getColor(R.color.Green), requireActivity().getColor(R.color.Red), requireActivity().getColor(R.color.Yellow) };

        PieView pie = view.findViewById(R.id.productPie);

        pie.setData(data.length, data, colors, requireActivity().getColor(R.color.backgroundColor));
        pie.invalidate();
    }

    @SuppressLint("DefaultLocale")
    private void setProductGoalPercents(View view, float kcal, float carb, float fat, float pro){
        float kcalGoal = NutritionGoals.getCaloriesGoal(requireActivity());
        float carbGoal = NutritionGoals.getCarbsGoal(requireActivity());
        float fatGoal = NutritionGoals.getFatGoal(requireActivity());
        float proGoal = NutritionGoals.getProteinGoal(requireActivity());

        if(kcalGoal > 0) {
            if (UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
                ((TextView) view.findViewById(R.id.calorieGoal)).setText(String.format("%.1f", Math.round(kcalGoal) * 4.184f));
            else
                ((TextView) view.findViewById(R.id.calorieGoal)).setText(String.format("%d", Math.round(kcalGoal)));

            float kcalGoalPercent =  (kcal / kcalGoal) * 100f;

            if (kcalGoalPercent > 100f)
                kcalGoalPercent = 100f;

            ((TextView) view.findViewById(R.id.caloriePercentage)).setText(String.format("%d%%", Math.round(kcalGoalPercent)));
            ((ProgressBar) view.findViewById(R.id.caloriesProgress)).setProgress(Math.round(kcalGoalPercent));
        } else {
            ((TextView) view.findViewById(R.id.calorieGoal)).setText("?");
            ((TextView) view.findViewById(R.id.caloriePercentage)).setText("?");
        }

        if(carbGoal > 0){
            float carbGoalPercent =  (carb / carbGoal) * 100f;

            if(carbGoalPercent > 100f)
                carbGoalPercent = 100f;

            ((TextView) view.findViewById(R.id.carbsGoal)).setText(String.format("%.1fg", carbGoal));
            ((TextView) view.findViewById(R.id.carbsPercentage)).setText(String.format("%d%%", Math.round(carbGoalPercent)));
            ((ProgressBar) view.findViewById(R.id.carbsProgress)).setProgress(Math.round(carbGoalPercent));
        } else {
            ((TextView) view.findViewById(R.id.carbsPercentage)).setText("?");
            ((TextView) view.findViewById(R.id.carbsGoal)).setText("?g");
        }

        if(fatGoal > 0){
            float fatGoalPercent = (fat / fatGoal) * 100f;

            if(fatGoalPercent > 100f)
                fatGoalPercent = 100f;

            ((TextView) view.findViewById(R.id.fatGoal)).setText(String.format("%.1fg", fatGoal));
            ((TextView) view.findViewById(R.id.fatPercentage)).setText(String.format("%d%%", Math.round(fatGoalPercent)));
            ((ProgressBar) view.findViewById(R.id.fatProgress)).setProgress(Math.round(fatGoalPercent));
        } else {
            ((TextView) view.findViewById(R.id.fatGoal)).setText("?g");
            ((TextView) view.findViewById(R.id.fatPercentage)).setText("?");
        }

        if(proGoal > 0) {
            float proGoalPercent = (pro / proGoal) * 100f;

            if (proGoalPercent > 100f)
                proGoalPercent = 100f;

            ((TextView) view.findViewById(R.id.proteinGoal)).setText(String.format("%.1fg", proGoal));
            ((TextView) view.findViewById(R.id.proteinPercentage)).setText(String.format("%d%%", Math.round(proGoalPercent)));
            ((ProgressBar) view.findViewById(R.id.proteinProgress)).setProgress(Math.round(proGoalPercent));
        } else {
            ((TextView) view.findViewById(R.id.proteinPercentage)).setText("?");
            ((TextView) view.findViewById(R.id.proteinGoal)).setText("?g");
        }
    }

    @SuppressLint("DefaultLocale")
    private void setProductMacros(View view, float amountChosen){
        float totalKcal = product.getEnergy_kcal_100g() * amountChosen;
        float carbs = product.getCarbohydrates_100g() * amountChosen;
        float fat = product.getFat_100g() * amountChosen;
        float protein = product.getProteins_100g() * amountChosen;

        setProductGoalPercents(view, totalKcal, carbs, fat, protein);

        if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ)) {
            float kilojoules = Math.round(totalKcal) *  4.184f;
            ((TextView) view.findViewById(R.id.caloriesAmount)).setText(String.format("%.1f", kilojoules));
            ((TextView) view.findViewById(R.id.caloriesAmount1)).setText(String.format("%.1f", kilojoules));
            ((TextView) view.findViewById(R.id.energyUnit)).setText(requireActivity().getString(R.string.kj));
        } else{
            ((TextView) view.findViewById(R.id.caloriesAmount)).setText(String.format("%d", Math.round(totalKcal)));
            ((TextView) view.findViewById(R.id.caloriesAmount1)).setText(String.format("%d", Math.round(totalKcal)));
        }

        ((TextView) view.findViewById(R.id.carbsAmount)).setText(String.format("%.1fg", carbs));
        ((TextView) view.findViewById(R.id.carbsAmount1)).setText(String.format("%.1f", carbs));
        ((TextView) view.findViewById(R.id.fatAmount)).setText(String.format("%.1fg", fat));
        ((TextView) view.findViewById(R.id.fatAmount1)).setText(String.format("%.1f", fat));
        ((TextView) view.findViewById(R.id.proteinAmount)).setText(String.format("%.1fg", protein));
        ((TextView) view.findViewById(R.id.proteinAmount1)).setText(String.format("%.1f", protein));
    }


    @SuppressLint("DefaultLocale")
    private void setUpProductView(View view, float gramsChosen){
        amountChosen = gramsChosen / 100f;
        ((TextView) view.findViewById(R.id.amountChosen)).setText(String.format("%.3f", gramsChosen));
        setProductMacros(view, amountChosen);
        setProductDetails(view, amountChosen);
    }

    @SuppressLint("DefaultLocale")
    private void setUpOnClickListeners(View view){
        view.findViewById(R.id.backButton).setOnClickListener(v -> requireActivity().onBackPressed());

        LinearLayout detailsLay = view.findViewById(R.id.detailsLayout);
        view.findViewById(R.id.showMoreDetails).setOnClickListener(new View.OnClickListener() {
            boolean layoutVisible = false;
            @Override
            public void onClick(View v) {
                if(layoutVisible) {
                    ViewCompat.setZ(detailsLay, -10);
                    detailsLay.animate()
                            .translationY(-view.getWidth())
                            .alpha(0.0f)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    detailsLay.setVisibility(View.GONE);
                                }
                            });
                    layoutVisible = false;
                } else {
                    ViewCompat.setZ(detailsLay, -10);
                    detailsLay.setY(-view.getWidth());
                    detailsLay.setVisibility(View.VISIBLE);
                    detailsLay.animate()
                            .translationY(0)
                            .alpha(1.0f)
                            .setListener(null);
                    layoutVisible = true;
                }
            }
        });

        view.findViewById(R.id.amountChosen).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setView(R.layout.custom_two_button_alert_dialog);
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.findViewById(R.id.dialog_drawable).setVisibility(View.GONE);

            ((TextView) dialog.findViewById(R.id.dialog_header)).setText(R.string.amount);
            ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.amount_message);
            EditText input = dialog.findViewById(R.id.dialog_input);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.setHint(R.string.amount_hint);
            input.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);

            dialog.findViewById(R.id.dialog_negative_button).setOnClickListener(view2 -> dialog.dismiss());

            ((Button) dialog.findViewById(R.id.dialog_positive_button)).setText(R.string.set);
            dialog.findViewById(R.id.dialog_positive_button).setOnClickListener(view2 -> {
                if(input.getText().toString().isEmpty()){
                    Toast.makeText(requireActivity(), R.string.amount_cannot_be_empty, Toast.LENGTH_SHORT).show();
                } else{
                    float amount = Float.parseFloat(input.getText().toString());
                    // if amount is more than 20 % of user's weight
                    if(amount / 1000f > 0.2f * ((WeightFragment.getLastDailyAverage(requireActivity()) == -1f) ? WeightFragment.getWorldwideAverageWeight(requireActivity()) : WeightFragment.getLastDailyAverage(requireActivity())))
                        Toast.makeText(requireActivity(), R.string.amount_probably_not_that_large, Toast.LENGTH_SHORT).show();
                    else {
                        dialog.dismiss();
                        setUpProductView(view, Float.parseFloat(input.getText().toString()));
                    }
                }
            });
        });

        view.findViewById(R.id.forwardButton).setOnClickListener(v -> {
            int daysBefore = dateSpinner.getSelectedItemPosition();
            LocalDate date = LocalDate.now();
            date = date.minusDays(daysBefore);

            String dateFormatted = (String) DateFormat.format("yyyyMMdd", Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()));

            if (spinner.getSelectedItemPosition() > 0 && spinner.getSelectedItemPosition() < 5) {
                MDBHNutritionTracker.getInstance(requireActivity()).addNewProduct(product.getId(), product.getName(), product.nutrimentsToDBString(), product.getBarcode(), product.getBrands());
                InAppActivity.diaryFragment.putProduct(product, amountChosen, dateFormatted, spinner.getSelectedItemPosition() + 100);
            } else
                Toast.makeText(requireActivity(), R.string.select_a_meal, Toast.LENGTH_SHORT).show();

            requireActivity().onBackPressed();
        });
    }

    @SuppressLint("DefaultLocale")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.product_screen, container, false);

        // setting up the view
        ((TextView) view.findViewById(R.id.productName)).setText(product.getName());
        setProductMacroPercentages(view);

        setUpProductView(view, amountChosen);
        setUpOnClickListeners(view);

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

        switch(mealType){
            case MDBHNutritionTracker.BREAKFAST:
                spinner.setSelection(1);
                break;
            case MDBHNutritionTracker.LUNCH:
                spinner.setSelection(2);
                break;
            case MDBHNutritionTracker.DINNER:
                spinner.setSelection(3);
                break;
            case MDBHNutritionTracker.SNACK:
                spinner.setSelection(4);
                break;
            default:
                spinner.setSelection(0);
        }

        LocalDate start = localDate;
        LocalDate now = LocalDate.now();

        try {
            start = Instant.ofEpochMilli(requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0).firstInstallTime).atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String[] dates = new String[(int) DAYS.between(start, now) + 1];
        int indexSelected = 0;

        int i = 0;
        for(; now.atStartOfDay().isAfter(start.atStartOfDay()); now = now.minusDays(1), i++){
            dates[i] = String.format("%02d %s %d", now.getDayOfMonth(), getMonthShort(requireActivity(), now.getMonthValue()), now.getYear());
            if(now.getYear() == localDate.getYear()
                && now.getMonthValue() == localDate.getMonthValue()
                && now.getDayOfMonth() == localDate.getDayOfMonth()){
                indexSelected = i;
            }
        }

        if(i == (int) DAYS.between(start, LocalDate.now())){
            dates[i] = String.format("%02d %s %d", now.getDayOfMonth(), getMonthShort(requireActivity(), now.getMonthValue()), now.getYear());
            if(now.getYear() == localDate.getYear()
                    && now.getMonthValue() == localDate.getMonthValue()
                    && now.getDayOfMonth() == localDate.getDayOfMonth()){
                indexSelected = i;
            }
        }

        StringSpinnerAdapter adapter1 = new StringSpinnerAdapter(requireActivity(), R.layout.date_spinner_layout, dates);
        adapter1.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        dateSpinner = view.findViewById(R.id.dateSpinner);

        dateSpinner.setAdapter(adapter1);
        dateSpinner.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            @Override
            public void onSpinnerOpened() { spinner.setSelected(true); }
            @Override
            public void onSpinnerClosed() { spinner.setSelected(false); }
        });

        dateSpinner.setSelection(indexSelected);

        return view;
    }
}
