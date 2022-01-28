package com.example.fitnessassistant.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.nutritiontracker.Product;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;

import java.time.LocalDate;
import java.util.ArrayList;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder>{
    private final ArrayList<Product> products;
    private final ArrayList<Float> quantities;
    private final Context context;
    private final MealAdapter.OnItemListener listener;
    private final int mealType;
    private final LocalDate date;

    public MealAdapter(Context context, ArrayList<Product> products, ArrayList<Float> quantities, MealAdapter.OnItemListener listener, Integer mealType, LocalDate date){
        this.products = products;
        this.quantities = quantities;
        this.context = context;
        this.listener = listener;
        this.mealType = mealType;
        this.date = date;
    }

    @NonNull
    @Override
    public MealAdapter.MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new MealAdapter.MealViewHolder(listener, inflater.inflate(R.layout.product_field, parent, false));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull MealAdapter.MealViewHolder holder, int position) {
        holder.productName.setText(products.get(holder.getAdapterPosition()).getName());
        holder.brandName.setText(products.get(holder.getAdapterPosition()).getBrands());
        if(UnitPreferenceFragment.getEnergyUnit(context).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ))
            holder.calorieAmount.setText(String.format("%.1f", Math.round(products.get(holder.getAdapterPosition()).getEnergy_kcal_100g() * quantities.get(holder.getAdapterPosition())) * 4.184f));
        else
            holder.calorieAmount.setText(String.format("%d", Math.round(products.get(holder.getAdapterPosition()).getEnergy_kcal_100g() * quantities.get(holder.getAdapterPosition()))));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public interface OnItemListener {
        void onItemClick(Product product, float quantity, int mealType, LocalDate date);
    }

    public class MealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView productName;
        public final TextView brandName;
        public final TextView calorieAmount;
        private final MealAdapter.OnItemListener onItemListener;

        public MealViewHolder(MealAdapter.OnItemListener onItemListener, @NonNull View itemView){
            super(itemView);
            this.productName = itemView.findViewById(R.id.productName);
            this.brandName = itemView.findViewById(R.id.brandName);
            this.calorieAmount = itemView.findViewById(R.id.productCalories);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemListener.onItemClick(products.get(getAdapterPosition()), quantities.get(getAdapterPosition()), mealType, date);
        }
    }
}
