package com.example.fitnessassistant.diary;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.nutritiontracker.Product;
import com.example.fitnessassistant.util.PieView;

public class ProductFragment extends Fragment {
    private final Product product;

    public ProductFragment(Product product){
        this.product = product;
    }

    @SuppressLint("DefaultLocale")
    private void setProductMacros(View view){
        float totalKcal = product.getEnergy_kcal_100g();
        float carbs = product.getCarbohydrates_100g();
        float fat = product.getFat_100g();
        float protein = product.getProteins_100g();

        ((TextView) view.findViewById(R.id.caloriesAmount)).setText(String.format("%d", Math.round(totalKcal)));

        ((TextView) view.findViewById(R.id.carbsAmount)).setText(String.format("%.1fg", carbs));
        ((TextView) view.findViewById(R.id.fatAmount)).setText(String.format("%.1fg", fat));
        ((TextView) view.findViewById(R.id.proteinAmount)).setText(String.format("%.1fg", protein));

        float carbKcal = carbs * 4f;
        float fatKcal = fat * 9f;
        float proteinKcal = protein * 4f;

        float maxKcal = Math.max(proteinKcal, Math.max(carbKcal, fatKcal));

        int carbPercent = Math.round(carbs * 4f / totalKcal * 100f);
        int proteinPercent = Math.round(protein * 4f / totalKcal * 100f);
        int fatPercent = Math.round(fat * 9f / totalKcal * 100f);

        if(maxKcal == carbKcal)
            carbPercent = 100 - proteinPercent - fatPercent;
        else if (maxKcal == fatKcal)
            fatPercent = 100 - proteinPercent - carbPercent;
        else if (maxKcal == proteinKcal)
            proteinPercent = 100 - carbPercent - fatPercent;

        ((TextView) view.findViewById(R.id.carbsPercent)).setText(String.format("%d%%", carbPercent));
        ((TextView) view.findViewById(R.id.fatPercent)).setText(String.format("%d%%", fatPercent));
        ((TextView) view.findViewById(R.id.proteinPercent)).setText(String.format("%d%%", proteinPercent));

        int[] data = new int[]{ carbPercent, proteinPercent, fatPercent };
        int[] colors = new int[]{ requireActivity().getColor(R.color.Green), requireActivity().getColor(R.color.Red), requireActivity().getColor(R.color.Yellow) };

        PieView pie = view.findViewById(R.id.productPie);

        pie.setData(data.length, data, colors, requireActivity().getColor(R.color.backgroundColor));
        pie.invalidate();
    }


    private void setUpProductView(View view){
        ((TextView) view.findViewById(R.id.productName)).setText(product.getName());
        setProductMacros(view);
    }

    private void setUpOnClickListeners(View view){
        view.findViewById(R.id.backButton).setOnClickListener(v -> requireActivity().onBackPressed());

        view.findViewById(R.id.forwardButton).setOnClickListener(v -> {

        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.product_screen, container, false);

        // setting up the view
        setUpProductView(view);
        setUpOnClickListeners(view);

        return view;
    }
}
