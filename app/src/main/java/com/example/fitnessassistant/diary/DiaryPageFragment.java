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
import com.example.fitnessassistant.database.mdbh.MDBHNutritionGoals;
import com.example.fitnessassistant.nutritiontracker.APISearch;
import com.example.fitnessassistant.nutritiontracker.BarcodeScanner;
import com.example.fitnessassistant.nutritiontracker.Product;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;
import com.example.fitnessassistant.util.AuthFunctional;
import com.example.fitnessassistant.util.EndlessScrollListener;
import com.example.fitnessassistant.util.PermissionFunctional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private EndlessScrollListener listener;
    public static final AtomicBoolean shouldReceiveProducts = new AtomicBoolean(true);
    private static final AtomicBoolean performingSearch = new AtomicBoolean(false);
    public static final AtomicBoolean shouldRestoreState = new AtomicBoolean(false);

    // booleans for saving state of search when going to diff fragments
    public static final AtomicBoolean activityOnBackPressed = new AtomicBoolean(false);
    public static final AtomicBoolean onDiaryFragment = new AtomicBoolean(false);

    @SuppressLint("DefaultLocale")
    private void setUpCurrentDay(View view){
        ((TextView) view.findViewById(R.id.currentDay)).setText(String.format("%02d %s %d", currentDay.getDayOfMonth(), getMonthLong(requireActivity(), currentDay.getMonthValue()), currentDay.getYear()));
    }

    @Override
    public void onItemClick(Product product) {
        // TODO change
        if(product == null)
            Toast.makeText(requireActivity(), R.string.product_not_found , Toast.LENGTH_SHORT).show();
        else {
            shouldRestoreState.set(true);
            requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new ProductFragment(product)).addToBackStack(null).commit();
        }
    }

    @SuppressLint("DefaultLocale")
    private void setUpCalories(View view){
        float caloriesGoal;
        if(currentDay.getYear() == Calendar.getInstance().get(Calendar.YEAR) && currentDay.getDayOfYear() == Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
            caloriesGoal = Math.round(NutritionGoals.getCaloriesGoal(requireActivity()));
        else
            caloriesGoal = MDBHNutritionGoals.getInstance(requireActivity()).readCaloriesForDate(currentDay.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        // TODO change remaining also
        if (UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ)) {
            ((TextView) view.findViewById(R.id.unitRemaining)).setText(requireActivity().getString(R.string.kilojoules_remaining));

            if(caloriesGoal != -1)
                ((TextView) view.findViewById(R.id.goalCalories)).setText(String.format("%.1f", Math.round(caloriesGoal) * 4.184f));
            else
                ((TextView) view.findViewById(R.id.goalCalories)).setText("?");
        } else {
            ((TextView) view.findViewById(R.id.unitRemaining)).setText(requireActivity().getString(R.string.calories_remaining));

            if(caloriesGoal != -1)
                ((TextView) view.findViewById(R.id.goalCalories)).setText(String.format("%d", Math.round(caloriesGoal)));
            else
                ((TextView) view.findViewById(R.id.goalCalories)).setText("?");
        }
        ((TextView) view.findViewById(R.id.intakeCalories)).setText("?");
        ((TextView) view.findViewById(R.id.remainingCalories)).setText("?");
    }

    private void subscribeToObservers(View view){
        APISearch.products.observe(getViewLifecycleOwner(), products -> {
            performingSearch.set(false);
            view.findViewById(R.id.searchBar).setVisibility(View.GONE);
            view.findViewById(R.id.loadMore).setVisibility(View.VISIBLE);
            view.findViewById(R.id.loading).setVisibility(View.INVISIBLE);
            if(shouldReceiveProducts.get()) {
                activityOnBackPressed.set(true);
                view.findViewById(R.id.searchRecyclerLayout).setVisibility(View.VISIBLE);
                requireActivity().findViewById(R.id.bottomNavigation).setVisibility(View.GONE);
                if (products.isEmpty()) {
                    if (recyclerView.getAdapter() == null)
                        ((TextView) view.findViewById(R.id.loadMore)).setText(requireActivity().getString(R.string.no_results));
                    else
                        ((TextView) view.findViewById(R.id.loadMore)).setText(requireActivity().getString(R.string.no_more_results));

                    listener.noMorePages();
                } else {
                    if (recyclerView.getAdapter() == null) {
                        recyclerView.setAdapter(new SearchAdapter(products, this));
                    } else {
                        ((SearchAdapter) recyclerView.getAdapter()).addProducts(products);
                    }

                    if (products.size() < 24) {
                        ((TextView) view.findViewById(R.id.loadMore)).setText(requireActivity().getString(R.string.no_more_results));
                        listener.noMorePages();
                    } else {
                        ((TextView) view.findViewById(R.id.loadMore)).setText(requireActivity().getString(R.string.swipe_down_for_more));
                        listener.notifyMorePages();
                    }
                }
            } else {
                view.findViewById(R.id.searchRecyclerLayout).setVisibility(View.GONE);
                if(isVisible())
                    requireActivity().findViewById(R.id.bottomNavigation).setVisibility(View.VISIBLE);
            }
        });

        APISearch.barcodeProduct.observe(getViewLifecycleOwner(), this::onItemClick);
    }

    private void setUpOnClickListeners(View view){
        // setting up search
        SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = view.findViewById(R.id.searchView);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));

        // search icon changed
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
        Drawable search = ContextCompat.getDrawable(requireActivity(),R.drawable.search);
        if (search != null)
            search.setTint(requireActivity().getColor(R.color.SpaceCadet));
        searchIcon.setImageDrawable(search);

        // search text color changed
        SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setHintTextColor(requireActivity().getColor(R.color.LightGrayColor));
        searchAutoComplete.setTextColor(requireActivity().getColor(R.color.SpaceCadet));

        // focus listener
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            searchView.setSelected(hasFocus);
            if(!hasFocus) {
                shouldReceiveProducts.set(false);
                if(!shouldRestoreState.get()) {
                    if (recyclerView.getAdapter() != null) {
                        ((SearchAdapter) recyclerView.getAdapter()).clear();
                        recyclerView.setAdapter(null);
                    }
                    view.findViewById(R.id.searchRecyclerLayout).setVisibility(View.GONE);
                    requireActivity().findViewById(R.id.bottomNavigation).setVisibility(View.VISIBLE);
                } else {
                    shouldRestoreState.set(false);
                    activityOnBackPressed.set(true);
                }
            }
        });

        // query listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!query.isEmpty() && !performingSearch.get()) {
                    if (recyclerView.getAdapter() != null)
                        recyclerView.setAdapter(null);

                    view.findViewById(R.id.searchRecyclerLayout).setVisibility(View.GONE);
                    requireActivity().findViewById(R.id.bottomNavigation).setVisibility(View.VISIBLE);
                    shouldReceiveProducts.set(true);
                    performingSearch.set(true);
                    view.findViewById(R.id.searchBar).setVisibility(View.VISIBLE);
                    APISearch.getInstance().searchAPI(query, requireContext(), false, false, 1);
                } else
                    shouldReceiveProducts.set(false);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // Only local API search is available due to API's TOS
                if(!shouldRestoreState.get()) {
                    if (recyclerView.getAdapter() != null)
                        recyclerView.setAdapter(null);

                    view.findViewById(R.id.searchRecyclerLayout).setVisibility(View.GONE);
                    requireActivity().findViewById(R.id.bottomNavigation).setVisibility(View.VISIBLE);

                    if(!query.isEmpty() && !performingSearch.get()) {
                        shouldReceiveProducts.set(true);
                        performingSearch.set(true);
                        APISearch.getInstance().searchAPI(query, requireContext(), false, true, 1);
                    } else
                        shouldReceiveProducts.set(false);
                }

                return false;
            }
        });

        view.findViewById(R.id.qrCodeScanner).setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                shouldRestoreState.set(true);
                requireActivity().getSupportFragmentManager().beginTransaction().hide(DiaryPageFragment.this).add(R.id.in_app_container, new BarcodeScanner()).addToBackStack(null).commit();
            } else
                PermissionFunctional.checkCameraPermission(requireContext(), cameraPermissionLauncher);
        });

        view.findViewById(R.id.dayBefore).setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();

            try {
                cal.setTimeInMillis(requireActivity().getPackageManager().getPackageInfo(requireActivity().getPackageName(), 0).firstInstallTime);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (currentDay.getYear() > cal.get(Calendar.YEAR) ||
                    (currentDay.getYear() == cal.get(Calendar.YEAR) && currentDay.getMonthValue() > cal.get(Calendar.MONTH) + 1) ||
                    (currentDay.getYear() == cal.get(Calendar.YEAR) && currentDay.getMonthValue() == cal.get(Calendar.MONTH) + 1 && currentDay.getDayOfMonth() > cal.get(Calendar.DAY_OF_MONTH))) {
                currentDay = currentDay.minusDays(1);
                setUpCurrentDay(view);
                setUpCalories(view);
            }
        });

        view.findViewById(R.id.dayAfter).setOnClickListener(v -> {
            if(currentDay.getYear() < Calendar.getInstance().get(Calendar.YEAR) ||
                    (currentDay.getYear() == Calendar.getInstance().get(Calendar.YEAR) && currentDay.getMonthValue() < Calendar.getInstance().get(Calendar.MONTH) + 1) ||
                    (currentDay.getYear() == Calendar.getInstance().get(Calendar.YEAR) && currentDay.getMonthValue() == Calendar.getInstance().get(Calendar.MONTH) + 1) && currentDay.getDayOfMonth() < Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
                currentDay = currentDay.plusDays(1);
                setUpCurrentDay(view);
                setUpCalories(view);
            }
        });

        view.findViewById(R.id.three_dots).setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new NutritionGoals()).addToBackStack(null).commit());
    }

    public void clearRecycler(){
        requireActivity().findViewById(R.id.bottomNavigation).setVisibility(View.VISIBLE);
        requireView().findViewById(R.id.searchRecyclerLayout).setVisibility(View.GONE);
        recyclerView.setAdapter(null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.diary_screen, container, false);

        setUpOnClickListeners(view);
        subscribeToObservers(view);

        currentDay = LocalDate.now();
        setUpCurrentDay(view);

        listener = new EndlessScrollListener(pageNumber -> {
            view.findViewById(R.id.loadMore).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.loading).setVisibility(View.VISIBLE);
            shouldReceiveProducts.set(true);
            performingSearch.set(true);
            APISearch.getInstance().searchAPI(searchView.getQuery().toString(), requireContext(), false, false, pageNumber);
        });

        recyclerView = view.findViewById(R.id.searchRecycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()){
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });
        recyclerView.setHasFixedSize(true); // all items have same height (they don't vary or change)
        recyclerView.addOnScrollListener(listener);

        return view;
    }

    public void updateNutritionData(View view){
        if(view == null)
            view = getView();

        if(view != null){
            currentDay = LocalDate.now();
            setUpCurrentDay(view);
            setUpCalories(view);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getView() != null) {
            setUpCalories(getView());
        }
    }
}