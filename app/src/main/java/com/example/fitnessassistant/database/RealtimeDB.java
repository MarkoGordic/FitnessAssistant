package com.example.fitnessassistant.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.fitnessassistant.database.data.ActivityData;
import com.example.fitnessassistant.database.data.BackupStatus;
import com.example.fitnessassistant.database.data.GoalsData;
import com.example.fitnessassistant.database.data.PedometerData;
import com.example.fitnessassistant.database.data.PreferencesData;
import com.example.fitnessassistant.database.data.SleepData;
import com.example.fitnessassistant.database.mdbh.MDBHActivityTracker;
import com.example.fitnessassistant.database.mdbh.MDBHPedometer;
import com.example.fitnessassistant.database.mdbh.MDBHSleepTracker;
import com.example.fitnessassistant.database.mdbh.MDBHWeight;
import com.example.fitnessassistant.pedometer.StepGoalFragment;
import com.example.fitnessassistant.questions.BirthdayFragment;
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
import java.util.Calendar;
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
                        ServiceFunctional.setSleepTrackerShouldRun(context, false);
                        ServiceFunctional.stopSleepTrackerService(context);
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
    }

    public static void checkBackupStatus(Context context){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users");

            db.child(userID).child("backup").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    BackupStatus data = snapshot.getValue(BackupStatus.class);

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if(data != null){
                        editor.putString("pedometer_backup", data.getPedometer());
                        editor.putString("preferences_backup", data.getUserPreferences());
                        editor.putString("goals_backup", data.getUserGoals());
                        editor.putString("sleep_backup", data.getSleepTracker());
                        editor.putString("activities_backup", data.getActivitiesTracker());
                        // TODO Add other backups here when added
                    }
                    editor.apply();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public static void updateBackupStatus(Context context) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users");

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            BackupStatus backupStatus = new BackupStatus();
            backupStatus.setPedometer(sharedPreferences.getString("pedometer_backup", "n#/"));
            backupStatus.setUserPreferences(sharedPreferences.getString("preferences_backup", "n#/"));
            backupStatus.setUserGoals(sharedPreferences.getString("goals_backup", "n#/"));
            backupStatus.setSleepTracker(sharedPreferences.getString("sleep_backup", "n#/"));
            backupStatus.setActivitiesTracker(sharedPreferences.getString("activities_backup", "n#/"));
            // TODO Add other backups here when added

            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    db.child(userID).child("backup").setValue(backupStatus);
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

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("pedometer_backup", "y#" + Calendar.getInstance().getTimeInMillis());
                    editor.apply();

                    updateBackupStatus(context);
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

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("preferences_backup", "y#" + Calendar.getInstance().getTimeInMillis());
                    editor.apply();

                    updateBackupStatus(context);
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
                        BirthdayFragment.putBirthday(context, data.getBirthday());

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

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("goals_backup", "y#" + Calendar.getInstance().getTimeInMillis());
                    editor.apply();

                    updateBackupStatus(context);
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
                        WeightFragment.putFirstWeightDate(context, data.getFirstDate());

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

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String check = sharedPreferences.getString("sleep_data", "n#/");

            if(check.charAt(0) != 'n'){
                storage.getReference().child(userID).child("/activities").delete().addOnCompleteListener(task -> {
                    List<Bitmap> data = MDBHActivityTracker.getInstance(context).readActivitiesBitmapsDB();
                    List<Integer> ids = MDBHActivityTracker.getInstance(context).readActivitiesIDs();

                    for(int i = 0; i < data.size(); i++){
                        Bitmap bitmap = data.get(i);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        Uri newUri = Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "ProfilePic", null));

                        storage.getReference("users/" + userID + "/activities/" + ids.get(i) + ".jpg").putFile(newUri).addOnFailureListener(e -> { });
                    }
                });
            }else{
                List<Bitmap> data = MDBHActivityTracker.getInstance(context).readActivitiesBitmapsDB();
                List<Integer> ids = MDBHActivityTracker.getInstance(context).readActivitiesIDs();

                for(int i = 0; i < data.size(); i++){
                    Bitmap bitmap = data.get(i);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    Uri newUri = Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "ProfilePic", null));

                    storage.getReference("users/" + userID + "/activities/" + ids.get(i) + ".jpg").putFile(newUri).addOnFailureListener(e -> { });
                }
            }
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
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    db.setValue(activityData);

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("activities_backup", "y#" + Calendar.getInstance().getTimeInMillis());
                    editor.apply();

                    updateBackupStatus(context);
                }

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
                        int j = 0;
                        for(int i : data.getIds()){
                            final int jFinal = j;
                            StorageReference storageRef = storage.getReference("users/" + userID + "/activities/" + i + ".jpg");
                            try {
                                File file = File.createTempFile("Images", "jpg");
                                storageRef.getFile(file).addOnSuccessListener(taskSnapshot -> {
                                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                    MDBHActivityTracker.getInstance(context).addNewActivity(context, data.activities.get(jFinal).getDate(), data.activities.get(jFinal).getAverageSpeed(), data.activities.get(jFinal).getDistance(), data.activities.get(jFinal).getCaloriesBurnt(), bitmap, data.activities.get(jFinal).getActivityType(), data.activities.get(jFinal).getDuration());
                                });
                                j++;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }
    }

    public static void saveUserSleepData(Context context){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(userID).child("data");

            SleepData data = new SleepData();

            data.setData(MDBHSleepTracker.getInstance(context).readSleepDB());

            db.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    db.child("sleep").setValue(data);

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("sleep_backup", "y#" + Calendar.getInstance().getTimeInMillis());
                    editor.apply();

                    updateBackupStatus(context);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
    }

    public static void restoreSleepData(Context context){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference db = FirebaseDatabase.getInstance("https://fitness-assistant-app-default-rtdb.europe-west1.firebasedatabase.app").getReference("users").child(userID).child("data").child("sleep");

            db.get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()){
                    Log.e("Firebase", "Error getting data", task.getException());
                } else {
                    DataSnapshot dataSnapshot = task.getResult();
                    SleepData data = dataSnapshot.getValue(SleepData.class);
                    List<String> sleep;

                    MDBHSleepTracker.getInstance(context).deleteSegmentsDB();

                    // Saving new data
                    if(data != null) {
                        sleep = data.getData();
                        for(String day : sleep){
                            StringTokenizer tokenizer = new StringTokenizer(day, "#");
                            String date = tokenizer.nextToken();
                            long duration = Long.parseLong(tokenizer.nextToken());
                            long startTime = Integer.parseInt(tokenizer.nextToken());
                            long endTime = Long.parseLong(tokenizer.nextToken());
                            int confirmationStatus = Integer.parseInt(tokenizer.nextToken());
                            int quality = Integer.parseInt(tokenizer.nextToken());
                            MDBHSleepTracker.getInstance(context).forceAddNewSleepSegment(startTime, endTime, duration, date, quality, confirmationStatus);
                        }
                    }
                }
            });
        }
    }

}
