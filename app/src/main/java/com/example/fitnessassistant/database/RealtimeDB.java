package com.example.fitnessassistant.database;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RealtimeDB {

    // method for adding new using to database
    // !! CURRENTLY DISABLED, but working
    public static void registerNewUser(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users");
            String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

            db.addValueEventListener(new ValueEventListener() {
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
