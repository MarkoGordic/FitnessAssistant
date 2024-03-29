package com.example.fitnessassistant.nutritiontracker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.fitnessassistant.database.mdbh.MDBHNutritionTracker;
import com.example.fitnessassistant.util.AuthFunctional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class APISearch {
    public static MutableLiveData<ArrayList<Product>> products = new MutableLiveData<>();
    public static MutableLiveData<Product> barcodeProduct = new MutableLiveData<>();
    public static String barcodeDetected = null;

    final static String baseURL = "https://world.openfoodfacts.org/api/v0/product/";
    final static String searchURL = "https://world.openfoodfacts.org/cgi/search.pl?search_terms=";
    final static String searchQuery = "&nocache=1&json=1&page=";

    public static APISearch instance;

    public static APISearch getInstance() {
        if(instance == null)
            instance = new APISearch();

        return instance;
    }

    public void searchAPI(String search, Context context, boolean isBarcode, boolean autocomplete, int page) {
        ArrayList<Product> results;
        // First, we check local DB for results
        if (!isBarcode)
            results = MDBHNutritionTracker.getInstance(context).searchProductsByName(search);
        else
            results = MDBHNutritionTracker.getInstance(context).searchProductsByBarcode(search);

        if(results.size() > 0 && isBarcode){
            products.postValue(results);
            return;
        }

        // Then we will search API for additional results
        if(AuthFunctional.currentlyOnline){
            ArrayList<Product> finalResults = new ArrayList<>(results);
            if (!autocomplete) {
                if(isBarcode){
                    new TaskRunner().executeAsync(new JSONTask(baseURL + search + ".json"), (result) -> {
                        try {
                            JSONObject obj = new JSONObject(result);
                            Product product = null;
                            if(obj.has("status"))
                                if(obj.getInt("status") != 0)
                                    product = new Product(obj, context, false);

                            barcodeProduct.postValue(product);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }else{
                    new TaskRunner().executeAsync(new JSONTask(searchURL + search + searchQuery + page), (result) -> {
                        try {
                            JSONObject obj = new JSONObject(result);
                            JSONArray jsonArray = obj.getJSONArray("products");

                            for (int i = 0; i < jsonArray.length(); i++)
                                finalResults.add(new Product(jsonArray.getJSONObject(i), context, true));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            products.postValue(finalResults);
                        }
                    });
                }
            }
            else
                products.postValue(finalResults);
        }else
            products.postValue(results);
    }

    private static class TaskRunner {
        private interface Callback<x>{
            void onComplete(x Result);
        }

        public<x> void executeAsync(Callable<x> callable, Callback<x> callback){
            Executors.newSingleThreadExecutor().execute(() -> {
                try{
                    final x result = callable.call();

                    new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(result));
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }
    }

    private static class JSONTask implements Callable<String>{
        private final String URL;

        public JSONTask(String... params) {
            this.URL = params[0];
        }

        @Override
        public String call() {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder buffer = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                    Log.d("Response: ", "> " + line);
                }

                return buffer.toString();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
