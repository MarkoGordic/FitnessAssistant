package com.example.fitnessassistant.database;

import android.content.Context;

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
                    FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(task -> ServiceFunctional.stopPedometerService(context));
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
}
