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
import android.text.format.DateFormat;
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
import com.example.fitnessassistant.adapters.MealAdapter;
import com.example.fitnessassistant.adapters.SearchAdapter;
import com.example.fitnessassistant.database.mdbh.MDBHNutritionGoals;
import com.example.fitnessassistant.database.mdbh.MDBHNutritionTracker;
import com.example.fitnessassistant.nutritiontracker.APISearch;
import com.example.fitnessassistant.nutritiontracker.BarcodeScanner;
import com.example.fitnessassistant.nutritiontracker.Product;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;
import com.example.fitnessassistant.util.EndlessScrollListener;
import com.example.fitnessassistant.util.PermissionFunctional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class DiaryPageFragment extends Fragment implements SearchAdapter.OnItemListener, MealAdapter.OnItemListener {
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

    private RecyclerView searchRecyclerView;
    private LocalDate currentDay;
    private SearchView searchView;

    private EndlessScrollListener listener;
    public static final AtomicBoolean shouldReceiveProducts = new AtomicBoolean(true);
    private static final AtomicBoolean performingSearch = new AtomicBoolean(false);

    @SuppressLint("DefaultLocale")
    private void setUpCurrentDay(View view){
        ((TextView) view.findViewById(R.id.currentDay)).setText(String.format("%02d %s %d", currentDay.getDayOfMonth(), getMonthLong(requireActivity(), currentDay.getMonthValue()), currentDay.getYear()));
    }

    @Override // SEARCH RECYCLER
    public void onItemClick(Product product) {
        // TODO change
        if(product == null)
            Toast.makeText(requireActivity(), R.string.product_not_found , Toast.LENGTH_SHORT).show();
        else {
            requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new ProductFragment(product, null, null, null)).addToBackStack(null).commit();
        }
    }

    private void subscribeToObservers(View view){
        APISearch.products.observe(getViewLifecycleOwner(), products -> {
            performingSearch.set(false);
            view.findViewById(R.id.searchBar).setVisibility(View.GONE);
            view.findViewById(R.id.loadMore).setVisibility(View.VISIBLE);
            view.findViewById(R.id.loading).setVisibility(View.INVISIBLE);
            if(shouldReceiveProducts.get()) {
                if (products.isEmpty()) {
                    if (searchRecyclerView.getAdapter() == null)
                        ((TextView) view.findViewById(R.id.loadMore)).setText(requireActivity().getString(R.string.no_results));
                    else
                        ((TextView) view.findViewById(R.id.loadMore)).setText(requireActivity().getString(R.string.no_more_results));
                    listener.noMorePages();
                } else {
                    if (searchRecyclerView.getAdapter() == null)
                        searchRecyclerView.setAdapter(new SearchAdapter(requireActivity(), products, this));
                    else
                        ((SearchAdapter) searchRecyclerView.getAdapter()).addProducts(products);

                    if (products.size() < 24) {
                        ((TextView) view.findViewById(R.id.loadMore)).setText(requireActivity().getString(R.string.no_more_results));
                        listener.noMorePages();
                    } else {
                        ((TextView) view.findViewById(R.id.loadMore)).setText(requireActivity().getString(R.string.swipe_down_for_more));
                        listener.notifyMorePages();
                    }
                }

                view.findViewById(R.id.searchRecyclerLayout).setVisibility(View.VISIBLE);
            } else {
                if(searchRecyclerView.getAdapter() != null)
                    searchRecyclerView.setAdapter(null);

                view.findViewById(R.id.searchRecyclerLayout).setVisibility(View.GONE);
            }
        });

        APISearch.barcodeProduct.observe(getViewLifecycleOwner(), this::onItemClick);
    }

    @SuppressLint("DefaultLocale")
    private void setUpRecyclerViews(View view){
        RecyclerView breakfastRecyclerView = view.findViewById(R.id.breakfastRecyclerView);
        RecyclerView lunchRecyclerView = view.findViewById(R.id.lunchRecyclerView);
        RecyclerView dinnerRecyclerView = view.findViewById(R.id.dinnerRecyclerView);
        RecyclerView snackRecyclerView = view.findViewById(R.id.snackRecyclerView);

        String currDateFormatted = (String) DateFormat.format("yyyyMMdd", Date.from(currentDay.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        ArrayList<Product> products;
        ArrayList<Float> quantities;
        Iterator<Product> it;
        Iterator<Float> it2;

        products = MDBHNutritionTracker.getInstance(requireActivity()).getBreakfastProducts(currDateFormatted);
        quantities =  MDBHNutritionTracker.getInstance(requireActivity()).getBreakfastQuantities(currDateFormatted);

        float totalBreakfastCals = 0f;
        it = products.iterator();
        it2 = quantities.iterator();
        while(it.hasNext() && it2.hasNext()){
            totalBreakfastCals += Math.round(it.next().getEnergy_kcal_100g() * it2.next());
        }

        if(!products.isEmpty()){
            MealAdapter bfMealAdapter = new MealAdapter(requireActivity(), products ,quantities,this, MDBHNutritionTracker.BREAKFAST, currentDay);
            breakfastRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()){
                @Override
                public boolean supportsPredictiveItemAnimations() {
                    return false;
                }
            });
            breakfastRecyclerView.setHasFixedSize(true);
            breakfastRecyclerView.setAdapter(bfMealAdapter);
            breakfastRecyclerView.setVisibility(View.VISIBLE);
            view.findViewById(R.id.breakfastEmpty).setVisibility(View.GONE);
        } else{
            breakfastRecyclerView.setVisibility(View.GONE);
            view.findViewById(R.id.breakfastEmpty).setVisibility(View.VISIBLE);
        }

        products = MDBHNutritionTracker.getInstance(requireActivity()).getLunchProducts(currDateFormatted);
        quantities =  MDBHNutritionTracker.getInstance(requireActivity()).getLunchQuantities(currDateFormatted);

        float totalLunchCals = 0f;
        it = products.iterator();
        it2 = quantities.iterator();
        while(it.hasNext() && it2.hasNext()){
            totalLunchCals += Math.round(it.next().getEnergy_kcal_100g() * it2.next());
        }

        if(!products.isEmpty()){
            MealAdapter luMealAdapter = new MealAdapter(requireActivity(), products ,quantities,this, MDBHNutritionTracker.LUNCH, currentDay);
            lunchRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()){
                @Override
                public boolean supportsPredictiveItemAnimations() {
                    return false;
                }
            });
            lunchRecyclerView.setHasFixedSize(true);
            lunchRecyclerView.setAdapter(luMealAdapter);
            lunchRecyclerView.setVisibility(View.VISIBLE);
            view.findViewById(R.id.lunchEmpty).setVisibility(View.GONE);
        } else{
            lunchRecyclerView.setVisibility(View.GONE);
            view.findViewById(R.id.lunchEmpty).setVisibility(View.VISIBLE);
        }

        products = MDBHNutritionTracker.getInstance(requireActivity()).getDinnerProducts(currDateFormatted);
        quantities =  MDBHNutritionTracker.getInstance(requireActivity()).getDinnerQuantities(currDateFormatted);

        float totalDinnerCals = 0f;
        it = products.iterator();
        it2 = quantities.iterator();
        while(it.hasNext() && it2.hasNext()){
            totalDinnerCals += Math.round(it.next().getEnergy_kcal_100g() * it2.next());
        }

        if(!products.isEmpty()){
            MealAdapter diMealAdapter = new MealAdapter(requireActivity(), products ,quantities,this, MDBHNutritionTracker.DINNER, currentDay);
            dinnerRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()){
                @Override
                public boolean supportsPredictiveItemAnimations() {
                    return false;
                }
            });
            dinnerRecyclerView.setHasFixedSize(true);
            dinnerRecyclerView.setAdapter(diMealAdapter);
            dinnerRecyclerView.setVisibility(View.VISIBLE);
            view.findViewById(R.id.dinnerEmpty).setVisibility(View.GONE);
        } else{
            dinnerRecyclerView.setVisibility(View.GONE);
            view.findViewById(R.id.dinnerEmpty).setVisibility(View.VISIBLE);
        }

        products = MDBHNutritionTracker.getInstance(requireActivity()).getSnackProducts(currDateFormatted);
        quantities =  MDBHNutritionTracker.getInstance(requireActivity()).getSnackQuantities(currDateFormatted);

        float totalSnackCals = 0f;
        it = products.iterator();
        it2 = quantities.iterator();
        while(it.hasNext() && it2.hasNext()){
            totalSnackCals += Math.round(it.next().getEnergy_kcal_100g() * it2.next());
        }

        if(!products.isEmpty()){
            MealAdapter snMealAdapter = new MealAdapter(requireActivity(), products ,quantities,this, MDBHNutritionTracker.SNACK, currentDay);
            snackRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()){
                @Override
                public boolean supportsPredictiveItemAnimations() {
                    return false;
                }
            });
            snackRecyclerView.setHasFixedSize(true);
            snackRecyclerView.setAdapter(snMealAdapter);
            snackRecyclerView.setVisibility(View.VISIBLE);
            view.findViewById(R.id.snackEmpty).setVisibility(View.GONE);
        } else{
            snackRecyclerView.setVisibility(View.GONE);
            view.findViewById(R.id.snackEmpty).setVisibility(View.VISIBLE);
        }

        float caloriesGoal;
        if(currentDay.getYear() == Calendar.getInstance().get(Calendar.YEAR) && currentDay.getDayOfYear() == Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
            caloriesGoal = Math.round(NutritionGoals.getCaloriesGoal(requireActivity()));
        else
            caloriesGoal = MDBHNutritionGoals.getInstance(requireActivity()).readCaloriesForDate(currentDay.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        float intakeCals = totalBreakfastCals + totalLunchCals + totalDinnerCals + totalSnackCals;

        if(UnitPreferenceFragment.getEnergyUnit(requireActivity()).equals(UnitPreferenceFragment.ENERGY_UNIT_KJ)){
            ((TextView) view.findViewById(R.id.unitRemaining)).setText(requireActivity().getString(R.string.kilojoules_remaining));
            ((TextView) view.findViewById(R.id.breakfastCalories)).setText(String.format("%.1f", totalBreakfastCals * 4.184f));
            ((TextView) view.findViewById(R.id.lunchCalories)).setText(String.format("%.1f", totalLunchCals * 4.184f));
            ((TextView) view.findViewById(R.id.dinnerCalories)).setText(String.format("%.1f", totalDinnerCals * 4.184f));
            ((TextView) view.findViewById(R.id.snackCalories)).setText(String.format("%.1f", totalSnackCals * 4.184f));

            if(caloriesGoal != -1) {
                ((TextView) view.findViewById(R.id.goalCalories)).setText(String.format("%.1f", Math.round(caloriesGoal) * 4.184f));
                ((TextView) view.findViewById(R.id.intakeCalories)).setText(String.format("%.1f", Math.round(intakeCals) * 4.184f));
                float remainingCalories = caloriesGoal - intakeCals;
                ((TextView) view.findViewById(R.id.remainingCalories)).setText(String.format("%.1f", Math.round(remainingCalories) * 4.184f));
            } else {
                ((TextView) view.findViewById(R.id.goalCalories)).setText("?");
                ((TextView) view.findViewById(R.id.intakeCalories)).setText("?");
                ((TextView) view.findViewById(R.id.remainingCalories)).setText("?");
            }
        } else{
            ((TextView) view.findViewById(R.id.unitRemaining)).setText(requireActivity().getString(R.string.calories_remaining));
            ((TextView) view.findViewById(R.id.breakfastCalories)).setText(String.format("%d", Math.round(totalBreakfastCals)));
            ((TextView) view.findViewById(R.id.lunchCalories)).setText(String.format("%d", Math.round(totalLunchCals)));
            ((TextView) view.findViewById(R.id.dinnerCalories)).setText(String.format("%d", Math.round(totalDinnerCals)));
            ((TextView) view.findViewById(R.id.snackCalories)).setText(String.format("%d", Math.round(totalSnackCals)));

            if(caloriesGoal != -1) {
                ((TextView) view.findViewById(R.id.goalCalories)).setText(String.format("%d", Math.round(caloriesGoal)));
                ((TextView) view.findViewById(R.id.intakeCalories)).setText(String.format("%d", Math.round(intakeCals)));
                float remainingCalories = caloriesGoal - intakeCals;
                ((TextView) view.findViewById(R.id.remainingCalories)).setText(String.format("%d", Math.round(remainingCalories)));
            } else {
                ((TextView) view.findViewById(R.id.goalCalories)).setText("?");
                ((TextView) view.findViewById(R.id.intakeCalories)).setText("?");
                ((TextView) view.findViewById(R.id.remainingCalories)).setText("?");
            }
        }
    }

    public void putProduct(Product product, float amountChosen, String dateFormatted, int mealType){
        ArrayList<Float> quantities;
        List<Integer> productIDs = new ArrayList<>();
        switch (mealType){
            case MDBHNutritionTracker.BREAKFAST:
                quantities = MDBHNutritionTracker.getInstance(requireActivity()).getBreakfastQuantities(dateFormatted);
                for(Product prod : MDBHNutritionTracker.getInstance(requireActivity()).getBreakfastProducts(dateFormatted))
                    productIDs.add(prod.getId());

                productIDs.add(product.getId());
                quantities.add(amountChosen);

                MDBHNutritionTracker.getInstance(requireActivity()).addOrUpdateMeal(mealType, dateFormatted, productIDs, quantities);
                break;
            case MDBHNutritionTracker.LUNCH:
                quantities = MDBHNutritionTracker.getInstance(requireActivity()).getLunchQuantities(dateFormatted);
                for(Product prod : MDBHNutritionTracker.getInstance(requireActivity()).getLunchProducts(dateFormatted))
                    productIDs.add(prod.getId());

                productIDs.add(product.getId());
                quantities.add(amountChosen);

                MDBHNutritionTracker.getInstance(requireActivity()).addOrUpdateMeal(mealType, dateFormatted, productIDs, quantities);
                break;
            case MDBHNutritionTracker.DINNER:
                quantities = MDBHNutritionTracker.getInstance(requireActivity()).getDinnerQuantities(dateFormatted);
                for(Product prod : MDBHNutritionTracker.getInstance(requireActivity()).getDinnerProducts(dateFormatted))
                    productIDs.add(prod.getId());

                productIDs.add(product.getId());
                quantities.add(amountChosen);

                MDBHNutritionTracker.getInstance(requireActivity()).addOrUpdateMeal(mealType, dateFormatted, productIDs, quantities);
                break;
            case MDBHNutritionTracker.SNACK:
                quantities = MDBHNutritionTracker.getInstance(requireActivity()).getSnackQuantities(dateFormatted);
                for(Product prod : MDBHNutritionTracker.getInstance(requireActivity()).getSnackProducts(dateFormatted))
                    productIDs.add(prod.getId());

                productIDs.add(product.getId());
                quantities.add(amountChosen);

                MDBHNutritionTracker.getInstance(requireActivity()).addOrUpdateMeal(mealType, dateFormatted, productIDs, quantities);
                break;
        }
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
        searchAutoComplete.setTextColor(requireActivity().getColor(R.color.InvertedBackgroundColor));

        // focus listener
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            searchView.setSelected(hasFocus);
            if(!hasFocus) {
                shouldReceiveProducts.set(false);
                if (searchRecyclerView.getAdapter() != null)
                    searchRecyclerView.setAdapter(null);

                view.findViewById(R.id.searchRecyclerLayout).setVisibility(View.GONE);
            }
        });

        // query listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                shouldReceiveProducts.set(false);

                if (searchRecyclerView.getAdapter() != null)
                    searchRecyclerView.setAdapter(null);

                view.findViewById(R.id.searchRecyclerLayout).setVisibility(View.GONE);

                if(!query.isEmpty() && !performingSearch.get()) {
                    shouldReceiveProducts.set(true);
                    performingSearch.set(true);
                    view.findViewById(R.id.searchBar).setVisibility(View.VISIBLE);
                    APISearch.getInstance().searchAPI(query, requireContext(), false, false, 1);
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                shouldReceiveProducts.set(false);

                // Only local API search is available due to API's TOS
                if (searchRecyclerView.getAdapter() != null)
                    searchRecyclerView.setAdapter(null);

                view.findViewById(R.id.searchRecyclerLayout).setVisibility(View.GONE);

                if(!query.isEmpty() && !performingSearch.get()) {
                    shouldReceiveProducts.set(true);
                    performingSearch.set(true);
                    APISearch.getInstance().searchAPI(query, requireContext(), false, true, 1);
                }

                return false;
            }
        });

        view.findViewById(R.id.qrCodeScanner).setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                requireActivity().getSupportFragmentManager().beginTransaction().hide(DiaryPageFragment.this).add(R.id.in_app_container, new BarcodeScanner()).addToBackStack(null).commit();
            else
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
                setUpRecyclerViews(view);
            }
        });

        view.findViewById(R.id.dayAfter).setOnClickListener(v -> {
            if(currentDay.getYear() < Calendar.getInstance().get(Calendar.YEAR) ||
                    (currentDay.getYear() == Calendar.getInstance().get(Calendar.YEAR) && currentDay.getMonthValue() < Calendar.getInstance().get(Calendar.MONTH) + 1) ||
                    (currentDay.getYear() == Calendar.getInstance().get(Calendar.YEAR) && currentDay.getMonthValue() == Calendar.getInstance().get(Calendar.MONTH) + 1) && currentDay.getDayOfMonth() < Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
                currentDay = currentDay.plusDays(1);
                setUpCurrentDay(view);
                setUpRecyclerViews(view);
            }
        });

        view.findViewById(R.id.three_dots).setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new NutritionGoals()).addToBackStack(null).commit());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.diary_screen, container, false);

        setUpOnClickListeners(view);
        subscribeToObservers(view);

        currentDay = LocalDate.now();
        setUpCurrentDay(view);
        setUpRecyclerViews(view);

        listener = new EndlessScrollListener(pageNumber -> {
            view.findViewById(R.id.loadMore).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.loading).setVisibility(View.VISIBLE);
            shouldReceiveProducts.set(true);
            performingSearch.set(true);
            APISearch.getInstance().searchAPI(searchView.getQuery().toString(), requireContext(), false, false, pageNumber);
        });

        searchRecyclerView = view.findViewById(R.id.searchRecycler);

        searchRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()){
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });
        searchRecyclerView.setHasFixedSize(true); // all items have same height (they don't vary or change)
        searchRecyclerView.addOnScrollListener(listener);

        return view;
    }

    public void updateNutritionData(View view){
        if(view == null)
            view = getView();

        if(view != null){
            currentDay = LocalDate.now();
            setUpCurrentDay(view);
            setUpRecyclerViews(view);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getView() != null) {
            setUpCurrentDay(getView());
            setUpRecyclerViews(getView());
        }
    }

    @Override // MEAL RECYCLER
    public void onItemClick(Product product, float quantity, int mealType, LocalDate date) {
        requireActivity().getSupportFragmentManager().beginTransaction().hide(this).add(R.id.in_app_container, new ProductFragment(product, quantity * 100f, mealType, date)).addToBackStack(null).commit();
    }
}