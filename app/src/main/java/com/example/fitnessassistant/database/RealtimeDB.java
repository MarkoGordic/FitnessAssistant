package com.example.fitnessassistant.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.fitnessassistant.util.ServiceFunctional;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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

    // saving user preferences to database
    public static void saveUserPreferences(String gender, String height, String weightUnit, String heightUnit, String fluidUnit, String energyUnit, String distanceUnit){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(userID).child("preferences");

            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    db.child("height").setValue(height);
                    db.child("gender").setValue(gender);
                    db.child("weightUnit").setValue(weightUnit);
                    db.child("heightUnit").setValue(heightUnit);
                    db.child("fluidUnit").setValue(fluidUnit);
                    db.child("energyUnit").setValue(energyUnit);
                    db.child("distanceUnit").setValue(distanceUnit);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
    }

    public static void restoreUserPreferences(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(userID);

            db.child("preferences").get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()){
                    Log.e("Firebase", "Error getting data", task.getException());
                } else {
                    System.out.println(task.getResult());
                }
            });
        }
    }
}
