package com.example.fitnessassistant.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.fitnessassistant.questions.GenderFragment;
import com.example.fitnessassistant.questions.HeightFragment;
import com.example.fitnessassistant.questions.UnitPreferenceFragment;
import com.example.fitnessassistant.questions.WeightFragment;
import com.example.fitnessassistant.util.ServiceFunctional;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class RealtimeDB {

    // method for adding new user to database
    public static void registerNewUser(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users");
            String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    db.child(userID).child("username").setValue(username);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
    }

    // method for removing user's data from database
    public static void removeUser(Context context){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users");
            Query query = db.child(userID);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    db.child(userID).removeValue();
                    FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(task -> {
                        // stopping pedometer service
                        ServiceFunctional.setPedometerShouldRun(context, false);
                        ServiceFunctional.stopPedometerService(context);
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
    }

    // method for saving user's data on start
    public static void updateUsername(String username){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users");

            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    db.child(userID).child("username").setValue(username);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
    }

    public static void savePedometerData(Context context){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(userID).child("data");

            PedometerData data = new PedometerData();

            ArrayList<String> dates = new ArrayList<>();
            ArrayList<Float> steps = new ArrayList<>();

            SharedPreferences prefs = context.getSharedPreferences("pedometer", Context.MODE_PRIVATE);
            Map<String, ?> allPedometerData = prefs.getAll();

            for (Map.Entry<String, ?> pairs : allPedometerData.entrySet()) {
                dates.add(pairs.getKey());
                steps.add((float) allPedometerData.get(pairs.getKey()));
            }

            data.setData(dates, steps);

            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    db.child("pedometer").setValue(data);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
    }

    public static void restorePedometerData(Context context){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(userID).child("data").child("pedometer");

            db.get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()){
                    Log.e("Firebase", "Error getting data", task.getException());
                } else {
                    DataSnapshot dataSnapshot = task.getResult();
                    PedometerData data = dataSnapshot.getValue(PedometerData.class);
                    List<String> pedometer;

                    SharedPreferences prefs = context.getSharedPreferences("pedometer", Context.MODE_PRIVATE);

                    // Clearing old data
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear().apply();

                    // Saving new data
                    if(data != null) {
                        pedometer = data.getData();
                        for(String day : pedometer){
                            StringTokenizer tokenizer = new StringTokenizer(day, "#");
                            String date = tokenizer.nextToken();
                            float steps = Float.parseFloat(tokenizer.nextToken());
                            editor.putFloat(date, steps);
                        }
                    }

                    editor.apply();
                }
            });
        }
    }

    // saving user preferences to database
    public static void saveUserPreferences(Context context){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(userID).child("data").child("preferences");

            PreferencesData preferencesData = new PreferencesData();
            preferencesData.setData(context);

            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    db.setValue(preferencesData);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
    }

    public static void restoreUserPreferences(Context context){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(userID).child("data").child("preferences");

            db.get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()){
                    Log.e("Firebase", "Error getting data", task.getException());
                } else {
                    DataSnapshot dataSnapshot = task.getResult();
                    PreferencesData data = dataSnapshot.getValue(PreferencesData.class);

                    if(data != null) {
                        GenderFragment.putGender(context, data.getGender());
                        HeightFragment.putHeight(context, data.getHeight());

                        List<String> units = data.getUnits();
                        UnitPreferenceFragment.putHeightUnit(context, units.get(0));
                        UnitPreferenceFragment.putWeightUnit(context, units.get(1));
                        UnitPreferenceFragment.putDistanceUnit(context, units.get(2));
                        UnitPreferenceFragment.putFluidUnit(context, units.get(3));
                        UnitPreferenceFragment.putEnergyUnit(context, units.get(4));

                        WeightFragment.putPreviousWeights(context, data.getWeights());
                    }
                }
            });
        }
    }

}
