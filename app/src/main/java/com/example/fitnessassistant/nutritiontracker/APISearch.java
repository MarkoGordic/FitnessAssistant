package com.example.fitnessassistant.nutritiontracker;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessassistant.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class APISearch extends Fragment {
    public static List<Product> products;
    public TextView JSONResponse;

    final static String searchURL = "https://world.openfoodfacts.org/cgi/search.pl?search_terms=";
    final static String searchQuery = "&nocache=1&json=1";

    // You need to check local DB for barcode, then global API

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.barcodescanner, container, false);
        JSONResponse = view.findViewById(R.id.barcode_text);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private static void searchAPI(String search, Context context){
        new TaskRunner().executeAsync(new JSONTask(searchURL + search + searchQuery), (result) -> {
            try {
                JSONObject obj = new JSONObject(result);
                JSONArray jsonArray = obj.getJSONArray("products");

                for(int i = 0; i < jsonArray.length(); i++)
                    products.add(new Product(jsonArray.getJSONObject(i), context));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
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
