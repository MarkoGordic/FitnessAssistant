package com.example.fitnessassistant.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.nutritiontracker.Product;

import java.util.ArrayList;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder>{
    private final ArrayList<Product> products;
    private final ArrayList<Float> quantities;
    private final MealAdapter.OnItemListener listener;
    private final int mealType;

    public MealAdapter(ArrayList<Product> products, ArrayList<Float> quantities, MealAdapter.OnItemListener listener, Integer mealType){
        this.products = products;
        this.quantities = quantities;
        this.listener = listener;
        this.mealType = mealType;
    }

    @NonNull
    @Override
    public MealAdapter.MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new MealAdapter.MealViewHolder(listener, inflater.inflate(R.layout.product_field, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MealAdapter.MealViewHolder holder, int position) {
        holder.productName.setText(products.get(holder.getAdapterPosition()).getName());
        holder.brandName.setText(products.get(holder.getAdapterPosition()).getBrands());
        holder.calorieAmount.setText(String.valueOf(Math.round(products.get(holder.getAdapterPosition()).getEnergy_kcal_100g() * quantities.get(holder.getAdapterPosition()))));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public interface OnItemListener {
        void onItemClick(Product product, float quantity, int mealType);
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
            onItemListener.onItemClick(products.get(getAdapterPosition()), quantities.get(getAdapterPosition()), mealType);
        }
    }
}
