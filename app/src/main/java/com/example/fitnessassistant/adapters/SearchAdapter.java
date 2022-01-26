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

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder>{

    private final ArrayList<Product> products;
    private final OnItemListener listener;

    public SearchAdapter(ArrayList<Product> products, OnItemListener listener){
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new SearchViewHolder(listener, inflater.inflate(R.layout.product_field, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        holder.productName.setText(products.get(holder.getAdapterPosition()).getName());
        holder.brandName.setText(products.get(holder.getAdapterPosition()).getBrands());
        holder.calorieAmount.setText(String.valueOf(Math.round(products.get(holder.getAdapterPosition()).getEnergy_kcal_100g())));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public interface OnItemListener {
        void onItemClick(Product product);
    }

    public class SearchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView productName;
        public final TextView brandName;
        public final TextView calorieAmount;
        private final SearchAdapter.OnItemListener onItemListener;

        public SearchViewHolder(OnItemListener onItemListener, @NonNull View itemView){
            super(itemView);
            this.productName = itemView.findViewById(R.id.productName);
            this.brandName = itemView.findViewById(R.id.brandName);
            this.calorieAmount = itemView.findViewById(R.id.productCalories);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemListener.onItemClick(products.get(getAdapterPosition()));
        }
    }

    public void addProducts(ArrayList<Product> newProducts){
        products.addAll(products.size(), newProducts);
        notifyItemRangeInserted(products.size(), newProducts.size());
    }

    public void clear(){
        int size = products.size();
        products.clear();
        notifyItemRangeRemoved(0, size);
    }
}
