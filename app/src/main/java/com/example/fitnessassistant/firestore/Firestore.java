package com.example.fitnessassistant.firestore;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Firestore {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    AtomicBoolean success = new AtomicBoolean(false);

    public void createNewUser(String userID){
        Map<String, Object> user = new HashMap<>();
        user.put("id", userID);

        db.collection("users")
                .add(user)
                .addOnSuccessListener(documentReference -> success.set(true))
                .addOnFailureListener(e -> Log.w("Error while adding document!", e));
    }
}
