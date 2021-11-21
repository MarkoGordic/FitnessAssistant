package com.example.fitnessassistant.firestore;

import android.annotation.SuppressLint;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


public class Firestore {
    public static AtomicBoolean success = new AtomicBoolean(false);

    public static void createNewUser(String userID){
        Map<String, Object> user = new HashMap<>();
        user.put("id", userID);

        FirebaseFirestore.getInstance().collection("users")
                .add(user)
                .addOnSuccessListener(documentReference -> success.set(true))
                .addOnFailureListener(e -> Log.w("Error while adding document!", e));
    }

    @SuppressLint("SimpleDateFormat")
    public static void createPedometerData(String userID){
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String dateText = dateFormat.format(Calendar.getInstance().getTime());

        Map<String, Object> pedometerData = new HashMap<>();
        pedometerData.put("date", dateText);
        pedometerData.put("steps", 0);

        DocumentReference userData = FirebaseFirestore.getInstance().collection("users").document(userID).collection("pedometer").document(dateText);
        Task<DocumentSnapshot> document = userData.get();
        if(document.getResult().exists()){
            userData.update(pedometerData)
                    .addOnFailureListener(e -> Log.w("Error while adding document!", e));
        }
        else{
            FirebaseFirestore.getInstance().collection("users").document(userID).collection("pedometer").add(pedometerData);
        }
    }
}