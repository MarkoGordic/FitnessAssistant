package com.example.fitnessassistant.database;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.fitnessassistant.database.data.ActivityData;
import com.example.fitnessassistant.database.data.GoalsData;
import com.example.fitnessassistant.database.data.PedometerData;
import com.example.fitnessassistant.database.data.PreferencesData;
import com.example.fitnessassistant.database.mdbh.MDBHActivityTracker;
import com.example.fitnessassistant.database.mdbh.MDBHPedometer;
import com.example.fitnessassistant.database.mdbh.MDBHWeight;
import com.example.fitnessassistant.pedometer.StepGoalFragment;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
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

            data.setData(MDBHPedometer.getInstance(context).readPedometerDB());

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

                    MDBHPedometer.getInstance(context).deleteDB();

                    // Saving new data
                    if(data != null) {
                        pedometer = data.getData();
                        for(String day : pedometer){
                            StringTokenizer tokenizer = new StringTokenizer(day, "#");
                            String date = tokenizer.nextToken();
                            float steps = Float.parseFloat(tokenizer.nextToken());
                            int stepGoal = Integer.parseInt(tokenizer.nextToken());
                            MDBHPedometer.getInstance(context).putPedometerData(context, date, steps, stepGoal);
                        }
                    }
                }
            });
        }
    }

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

                        MDBHWeight.getInstance(context).deleteDB();
                        WeightFragment.putPreviousWeights(context, data.getWeights());
                    }
                }
            });
        }
    }

    public static void saveUserGoals(Context context){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(userID).child("data").child("goals");

            GoalsData goalsData = new GoalsData();
            goalsData.setData(context);

            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    db.setValue(goalsData);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
    }

    public static void restoreUserGoals(Context context) {
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(userID).child("data").child("goals");

            db.get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()){
                    Log.e("Firebase", "Error getting data", task.getException());
                } else {
                    DataSnapshot dataSnapshot = task.getResult();
                    GoalsData data = dataSnapshot.getValue(GoalsData.class);

                    if(data != null) {
                        WeightFragment.putGoalWeight(context, data.getTargetWeight());
                        WeightFragment.putFirstWeight(context, data.getFirstWeight());
                        WeightFragment.putFirstWeightDate(context, String.valueOf(data.getFirstDate()));

                        StepGoalFragment.putMondayStepGoal(context, data.getWeeklySteps().get(0));
                        StepGoalFragment.putTuesdayStepGoal(context, data.getWeeklySteps().get(1));
                        StepGoalFragment.putWednesdayStepGoal(context, data.getWeeklySteps().get(2));
                        StepGoalFragment.putThursdayStepGoal(context, data.getWeeklySteps().get(3));
                        StepGoalFragment.putFridayStepGoal(context, data.getWeeklySteps().get(4));
                        StepGoalFragment.putSaturdayStepGoal(context, data.getWeeklySteps().get(5));
                        StepGoalFragment.putSundayStepGoal(context, data.getWeeklySteps().get(6));
                    }
                }
            });
        }
    }

    public static void saveActivityImages(Context context){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

            storage.getReference().child(userID).child("/activities").delete().addOnCompleteListener(task -> {
                List<Bitmap> data = MDBHActivityTracker.getInstance(context).readActivitiesBitmapsDB();
                List<Integer> ids = MDBHActivityTracker.getInstance(context).readActivitiesIDs();

                for(int i = 0; i < data.size(); i++){
                    Bitmap bitmap = data.get(i);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    Uri newUri = Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "ProfilePic", null));

                    storage.getReference("users/" + userID + "/activities/" + ids.get(i) + ".jpg").putFile(newUri).addOnFailureListener(e -> {

                    });
                }
            });
        }
    }

    public static void saveUserActivities(Context context) {
        saveActivityImages(context);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(userID).child("data").child("activities");

            ActivityData activityData = new ActivityData(context);

            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) { db.setValue(activityData); }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
    }

    public static void restoreUserActivities(Context context) {
        MDBHActivityTracker.getInstance(context).deleteDB();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(userID).child("data").child("activities");
            FirebaseStorage storage = FirebaseStorage.getInstance();

            db.get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.e("Firebase", "Error getting data", task.getException());
                } else {
                    DataSnapshot dataSnapshot = task.getResult();
                    ActivityData data = dataSnapshot.getValue(ActivityData.class);

                    if(data != null){
                        for(int i : data.getIds()){
                            StorageReference storageRef = storage.getReference("users/" + userID + "/activities/" + i + ".jpg");
                            try {
                                File file = File.createTempFile("Images", "jpg");
                                storageRef.getFile(file).addOnSuccessListener(taskSnapshot -> {
                                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                    MDBHActivityTracker.getInstance(context).addNewActivity(data.activities.get(i).getDate(), data.activities.get(i).getAverageSpeed(), data.activities.get(i).getDistance(), data.activities.get(i).getCaloriesBurnt(), bitmap, data.activities.get(i).getActivityType(), data.activities.get(i).getDuration());
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }

}
