package com.example.fitnessassistant.database;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RealtimeDB {
    public static void registerNewUser(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference currUser = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
            String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            currUser.child("Username").setValue("Test123")
            .addOnCompleteListener(task -> {
                try {
                    if (!task.isSuccessful()) {
                        if (task.getException() != null)
                            throw task.getException();
                    }
                    else{
                        System.out.println("Registration successful!");
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            });
        }
    }

    public static void removeUser(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference currUser = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
            currUser.removeValue();
        }
    }

    public static void updateUsername(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference currUser = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
            currUser.removeValue();
        }
    }

}
