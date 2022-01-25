package com.example.fitnessassistant.diary;

import static com.example.fitnessassistant.util.TimeFunctional.getMonthLong;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.adapters.SearchAdapter;
import com.example.fitnessassistant.nutritiontracker.APISearch;
import com.example.fitnessassistant.nutritiontracker.BarcodeScanner;
import com.example.fitnessassistant.nutritiontracker.Product;
import com.example.fitnessassistant.util.PermissionFunctional;

import java.time.LocalDate;

public class DiaryPageFragment extends Fragment implements SearchAdapter.OnItemListener {
    public final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (!result) {
            // creates an alert dialog with rationale shown
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setView(R.layout.custom_ok_alert_dialog);
            AlertDialog dialog = builder.create();
            dialog.show();

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ((AppCompatImageView) dialog.findViewById(R.id.dialog_drawable)).setImageResource(R.drawable.exclamation);

            ((TextView) dialog.findViewById(R.id.dialog_header)).setText(R.string.camera_access_denied);
            dialog.findViewById(R.id.dialog_ok_button).setOnClickListener(view2 -> dialog.dismiss());

            // showing messages (one case if user selected don't ask again, other if user just selected deny)
            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACTIVITY_RECOGNITION))
                ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.camera_access_message_denied_forever);
            else
                ((TextView) dialog.findViewById(R.id.dialog_message)).setText(R.string.camera_access_message_denied);
        }
    });

    private RecyclerView recyclerView;
    private LocalDate currentDay;
    private SearchView searchView;
    public static int currentPage = -1;

    @SuppressLint("DefaultLocale")
    private void setUpCurrentDay(View view){
        ((TextView) view.findViewById(R.id.currentDay)).setText(String.format("%02d %s %d", currentDay.getDayOfMonth(), getMonthLong(requireActivity(), currentDay.getMonthValue()), currentDay.getYear()));
    }

    @Override
    public void onItemClick(Product product) {
        if(product == null)
            Toast.makeText(requireActivity(), R.string.product_not_found , Toast.LENGTH_SHORT).show();
        else
            requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new ProductFragment(product)).addToBackStack(null).commit();
    }

    private void subscribeToObservers(View view){
        APISearch.products.observe(getViewLifecycleOwner(), products -> {
            if(products.isEmpty()){
                if(recyclerView.getAdapter() == null)
                    ((TextView) view.findViewById(R.id.loadMore)).setText(requireActivity().getString(R.string.no_results));
                else
                    ((TextView) view.findViewById(R.id.loadMore)).setText(requireActivity().getString(R.string.no_more_results));

                currentPage = -1;
                view.findViewById(R.id.loadMore).setClickable(false);
            } else {
                if (recyclerView.getAdapter() == null){
                    currentPage = 1;
                    recyclerView.setAdapter(new SearchAdapter(products, this));
                    view.findViewById(R.id.searchRecyclerLayout).setVisibility(View.VISIBLE);
                } else {
                    ((SearchAdapter) recyclerView.getAdapter()).addProducts(products);
                }

                if (products.size() < 24){
                    currentPage = -1;
                    ((TextView) view.findViewById(R.id.loadMore)).setText(requireActivity().getString(R.string.no_more_results));
                    view.findViewById(R.id.loadMore).setClickable(false);
                } else{
                    ((TextView) view.findViewById(R.id.loadMore)).setText(requireActivity().getString(R.string.load_more));
                    view.findViewById(R.id.loadMore).setOnClickListener(v -> {
                        currentPage++;
                        APISearch.getInstance().searchAPI(searchView.getQuery().toString(), requireContext(), false, false, currentPage);
                    });
                }
            }
        });

        APISearch.barcodeProduct.observe(getViewLifecycleOwner(), this::onItemClick);
    }

    private void setUpOnClickListeners(View view){
        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = view.findViewById(R.id.searchView);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));

        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        Drawable search = ContextCompat.getDrawable(requireActivity(),R.drawable.search);
        if (search != null)
            search.setTint(requireActivity().getColor(R.color.SpaceCadet));
        searchIcon.setImageDrawable(search);

        SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setHintTextColor(requireActivity().getColor(R.color.LightGrayColor));
        searchAutoComplete.setTextColor(requireActivity().getColor(R.color.SpaceCadet));

        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            searchView.setSelected(hasFocus);
            searchView.setIconified(!hasFocus);
            if(!hasFocus)
                view.findViewById(R.id.searchRecyclerLayout).setVisibility(View.GONE);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                APISearch.getInstance().searchAPI(query, requireContext(), false, false, 1);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // Only local API search is available due to API's TOS
                APISearch.getInstance().searchAPI(query, requireContext(), false, true, 1);
                return false;
            }
        });

        view.findViewById(R.id.qrCodeScanner).setOnClickListener(v -> {
            if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                requireActivity().getSupportFragmentManager().beginTransaction().hide(DiaryPageFragment.this).add(R.id.in_app_container, new BarcodeScanner()).addToBackStack(null).commit();
            else
                PermissionFunctional.checkCameraPermission(requireContext(), cameraPermissionLauncher);
        });

        view.findViewById(R.id.dayBefore).setOnClickListener(v -> {
            currentDay = currentDay.minusDays(1);
            setUpCurrentDay(view);
        });

        view.findViewById(R.id.dayAfter).setOnClickListener(v -> {
            currentDay = currentDay.plusDays(1);
            setUpCurrentDay(view);
        });

        view.findViewById(R.id.loadMore).setOnClickListener(v -> {
            currentPage++;
            APISearch.getInstance().searchAPI(searchView.getQuery().toString(), requireContext(), false, false, currentPage);
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.diary_screen, container, false);

        setUpOnClickListeners(view);
        subscribeToObservers(view);

        currentDay = LocalDate.now();
        setUpCurrentDay(view);

        recyclerView = view.findViewById(R.id.searchRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()){
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });
        recyclerView.setHasFixedSize(true);

        return view;
    }
}
