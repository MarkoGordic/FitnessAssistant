package com.example.fitnessassistant.diary;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.adapters.SearchAdapter;
import com.example.fitnessassistant.nutritiontracker.APISearch;
import com.example.fitnessassistant.nutritiontracker.BarcodeScanner;
import com.example.fitnessassistant.nutritiontracker.Product;

public class DiaryPageFragment extends Fragment implements SearchAdapter.OnItemListener {
    private RecyclerView recyclerView;

    @Override
    public void onItemClick(Product product) {
        if(product != null){
            // todo baciti na novi fragment
            System.out.println("PRODUCT: " + product.getName());
        }
    }

    private void subscribeToObservers(){
        APISearch.products.observe(getViewLifecycleOwner(), products -> {
            if(!products.isEmpty()) {
                SearchAdapter adapter = new SearchAdapter(products, this);
                recyclerView.setAdapter(adapter);
                recyclerView.setVisibility(View.VISIBLE);
            } else
                recyclerView.setVisibility(View.GONE);
        });

        APISearch.barcodeProduct.observe(getViewLifecycleOwner(), this::onItemClick);
    }

    private void setUpOnClickListeners(View view){
        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            searchView.setSelected(hasFocus);
            searchView.setIconified(!hasFocus);
            if(!hasFocus){
                recyclerView.setVisibility(View.GONE);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                APISearch.getInstance().searchAPI(query, requireContext(), false, false);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // TODO perform API Search
                // Only local API search is available due to API's TOS
                APISearch.getInstance().searchAPI(query, requireContext(), false, true);
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
        subscribeToObservers();

        recyclerView = view.findViewById(R.id.searchRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()){
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });

        return view;
    }
}
