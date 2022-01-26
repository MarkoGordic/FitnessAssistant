package com.example.fitnessassistant.diary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.nutritiontracker.Product;
import com.example.fitnessassistant.questions.WeightFragment;
import com.example.fitnessassistant.util.PieView;

public class ProductFragment extends Fragment {
    private final Product product;
    private float amountChosen;

    public ProductFragment(Product product){
        this.product = product;
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
    private void setProductMacros(View view, float amountChosen){
        float totalKcal = product.getEnergy_kcal_100g() * amountChosen;
        float carbs = product.getCarbohydrates_100g() * amountChosen;
        float fat = product.getFat_100g() * amountChosen;
        float protein = product.getProteins_100g() * amountChosen;

        ((TextView) view.findViewById(R.id.caloriesAmount)).setText(String.format("%d", Math.round(totalKcal)));
        ((TextView) view.findViewById(R.id.caloriesAmount1)).setText(String.format("%d", Math.round(totalKcal)));
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
                    if(amount / 1000f > 0.2f * ((WeightFragment.getAverageWeightToday(requireActivity()) == 0f) ? WeightFragment.getWorldwideAverageWeight(requireActivity()) : WeightFragment.getAverageWeightToday(requireActivity())))
                        Toast.makeText(requireActivity(), R.string.amount_probably_not_that_large, Toast.LENGTH_SHORT).show();
                    else {
                        dialog.dismiss();
                        setUpProductView(view, Float.parseFloat(input.getText().toString()));
                    }
                }
            });
        });

        view.findViewById(R.id.forwardButton).setOnClickListener(v -> {

        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.product_screen, container, false);

        // setting up the view
        ((TextView) view.findViewById(R.id.productName)).setText(product.getName());
        setProductMacroPercentages(view);

        setUpProductView(view, 100f);
        setUpOnClickListeners(view);

        return view;
    }
}
