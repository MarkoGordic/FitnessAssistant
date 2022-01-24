package com.example.fitnessassistant.diary;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.nutritiontracker.BarcodeScanner;
import com.example.fitnessassistant.nutritiontracker.Product;

public class DiaryPageFragment extends Fragment {
    public static Product currentProduct;

    private void setUpOnClickListeners(View view){
        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            searchView.setSelected(hasFocus);
            searchView.setIconified(!hasFocus);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO perform API Search
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO perform API Search
                return false;
            }
        });

        view.findViewById(R.id.qrCodeScanner).setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction().hide(DiaryPageFragment.this).add(R.id.in_app_container, new BarcodeScanner()).addToBackStack(null).commit());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.diary_screen, container, false);
        setUpOnClickListeners(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO ne znam jel id != 0 dobra provera da ga nije nasao
        if(currentProduct != null && getView() != null && currentProduct.getId() != 0){
            ((TextView) getView().findViewById(R.id.currentProduct)).setText(
                    String.format("ID: %s\nProtein: %s\nCarbs: %s\nFats: %s", currentProduct.getId(), currentProduct.getProteins_100g(), currentProduct.getCarbohydrates_100g(), currentProduct.getFat_100g())
            );
        }
    }
}
