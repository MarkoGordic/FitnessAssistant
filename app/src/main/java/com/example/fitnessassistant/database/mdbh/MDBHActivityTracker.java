package com.example.fitnessassistant.database.mdbh;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;

import com.example.fitnessassistant.database.data.Activity;
import com.example.fitnessassistant.database.data.ActivityRecycler;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MDBHActivityTracker extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SavedActivities.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "activities";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_DATE = "activity_date";
    private static final String COLUMN_AVERAGE_SPEED = "activity_average_speed";
    private static final String COLUMN_CALORIES_BURNT = "activity_calories_burnt";
    private static final String COLUMN_DISTANCE = "activity_distance";
    private static final String COLUMN_ACTIVITY_DURATION = "activity_duration";
    private static final String COLUMN_ACTIVITY_TYPE = "activity_type";
    private static final String COLUMN_IMAGE = "activity_image";

    private static MDBHActivityTracker instance;

    public static MDBHActivityTracker getInstance(Context context) {
        if(instance == null)
            instance = new MDBHActivityTracker(context);

        return instance;
    }

    public MDBHActivityTracker(@Nullable Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + TABLE_NAME +
                        " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_DATE + " REAL, " +
                        COLUMN_AVERAGE_SPEED + " REAL, " +
                        COLUMN_CALORIES_BURNT + " REAL, " +
                        COLUMN_DISTANCE + " REAL, " +
                        COLUMN_ACTIVITY_DURATION + " TEXT, " +
                        COLUMN_ACTIVITY_TYPE + " INTEGER, " +
                        COLUMN_IMAGE + " BLOB);";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addNewActivity(long date, float averageSpeed, double distance, float calories, Bitmap image, int activityType, String duration){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] activityImage = outputStream.toByteArray();

        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_AVERAGE_SPEED, averageSpeed);
        cv.put(COLUMN_DISTANCE, distance);
        cv.put(COLUMN_CALORIES_BURNT, calories);
        cv.put(COLUMN_IMAGE, activityImage);
        cv.put(COLUMN_ACTIVITY_TYPE, activityType);
        cv.put(COLUMN_ACTIVITY_DURATION, duration);

        long result = db.insert(TABLE_NAME, null, cv);

        if(result != -1){
            int latestID = readLatestID();
        }
    }

    public void removeActivityFromDB(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME+ " WHERE "+ COLUMN_ID +"='"+id+"'");
    }

    public List<Activity> readActivitiesDataDB() {
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        List<Activity> activities = new ArrayList<>();

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                do{
                    Activity activity = new Activity();
                    activity.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    activity.setDuration(cursor.getString(cursor.getColumnIndex(COLUMN_ACTIVITY_DURATION)));
                    activity.setDate(cursor.getLong(cursor.getColumnIndex(COLUMN_DATE)));
                    activity.setAverageSpeed(cursor.getFloat(cursor.getColumnIndex(COLUMN_AVERAGE_SPEED)));
                    activity.setDistance(cursor.getFloat(cursor.getColumnIndex(COLUMN_DISTANCE)));
                    activity.setCaloriesBurnt(cursor.getFloat(cursor.getColumnIndex(COLUMN_CALORIES_BURNT)));
                    activity.setActivityType(cursor.getInt(cursor.getColumnIndex(COLUMN_ACTIVITY_TYPE)));
                    activities.add(activity);
                }while(cursor.moveToNext());
                cursor.close();
            }
        }

        return activities;
    }

    public List<Bitmap> readActivitiesBitmapsDB() {
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        List<Bitmap> images = new ArrayList<>();

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                do{
                    byte[] bytes = cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE));
                    Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    images.add(image);
                }while(cursor.moveToNext());
                cursor.close();
            }
        }

        return images;
    }

    public List<Integer> readActivitiesIDs() {
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        List<Integer> ids = new ArrayList<>();

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                do{
                    ids.add(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                }while(cursor.moveToNext());
                cursor.close();
            }
        }

        return ids;
    }

    public int readLatestID(){
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        int id = -1;

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                do{
                    id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                }while(cursor.moveToNext());
                cursor.close();
            }
        }

        return id;
    }

    public List<ActivityRecycler> readActivitiesDataForRecyclerDB() {
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        List<ActivityRecycler> activities = new ArrayList<>();

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                do{
                    ActivityRecycler activityRecycler = new ActivityRecycler();
                    activityRecycler.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                    activityRecycler.setDuration(cursor.getString(cursor.getColumnIndex(COLUMN_ACTIVITY_DURATION)));
                    activityRecycler.setDate(cursor.getLong(cursor.getColumnIndex(COLUMN_DATE)));
                    activityRecycler.setAverageSpeed(cursor.getFloat(cursor.getColumnIndex(COLUMN_AVERAGE_SPEED)));
                    activityRecycler.setDistance(cursor.getFloat(cursor.getColumnIndex(COLUMN_DISTANCE)));
                    activityRecycler.setCaloriesBurnt(cursor.getFloat(cursor.getColumnIndex(COLUMN_CALORIES_BURNT)));
                    activityRecycler.setActivityType(cursor.getInt(cursor.getColumnIndex(COLUMN_ACTIVITY_TYPE)));
                    activityRecycler.setImage(BitmapFactory.decodeByteArray(cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE)), 0, cursor.getBlob(cursor.getColumnIndex(COLUMN_IMAGE)).length));
                    activities.add(activityRecycler);
                }while(cursor.moveToNext());
                cursor.close();
            }
        }

        return activities;
    }

    public float[] getTotals(){
        float[] totals = new float[4];
        totals[0] = 0f;
        totals[1] = 0f;
        totals[2] = 0f;
        totals[3] = 0f;

        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                do{
                    totals[0] += cursor.getInt(cursor.getColumnIndex(COLUMN_CALORIES_BURNT));

                    switch (cursor.getInt(cursor.getColumnIndex(COLUMN_ACTIVITY_TYPE))){
                        case ActivityRecycler.ACTIVITY_RUNNING:
                            totals[1] += cursor.getFloat(cursor.getColumnIndex(COLUMN_DISTANCE));
                            break;
                        case ActivityRecycler.ACTIVITY_WALKING:
                            totals[2] += cursor.getFloat(cursor.getColumnIndex(COLUMN_DISTANCE));
                            break;
                        case ActivityRecycler.ACTIVITY_CYCLING:
                            totals[3] += cursor.getFloat(cursor.getColumnIndex(COLUMN_DISTANCE));
                    }
                }while(cursor.moveToNext());
                cursor.close();
            }
        }

        return totals;
    }

    public List<String> getPersonalBests(){
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        List<String> data = new ArrayList<>();

        float maxDistanceWalk = 0f;
        String maxDateWalk = null;
        float maxDistanceRun = 0f;
        String maxDateRun = null;
        float maxDistanceBicycle = 0f;
        String maxDateBicycle = null;
        int maxCalories = 0;
        String maxDateCalories = null;
        String maxDuration = "00:00:00";
        String maxDateDuration = null;

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                do{
                    if(maxCalories < cursor.getInt(cursor.getColumnIndex(COLUMN_CALORIES_BURNT))){
                        maxCalories = cursor.getInt(cursor.getColumnIndex(COLUMN_CALORIES_BURNT));
                        maxDateCalories = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                    }

                    String tempDuration = cursor.getString(cursor.getColumnIndex(COLUMN_ACTIVITY_DURATION));
                    StringTokenizer tokenizer1 = new StringTokenizer(maxDuration, ":");
                    StringTokenizer tokenizer2 = new StringTokenizer(tempDuration, ":");
                    List<Integer> duration1 = new ArrayList<>();
                    List<Integer> duration2 = new ArrayList<>();
                    while(tokenizer1.hasMoreElements()){
                        duration1.add(Integer.parseInt(tokenizer1.nextToken()));
                        duration2.add(Integer.parseInt(tokenizer2.nextToken()));
                    }

                    if(duration2.get(0) > duration1.get(0)){
                        maxDuration = tempDuration;
                        maxDateDuration = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                    }else if(duration2.get(0).equals(duration1.get(0))){
                        if(duration2.get(1) > duration1.get(1)){
                            maxDuration = tempDuration;
                            maxDateDuration = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                        }
                        else if(duration2.get(1).equals(duration1.get(1))){
                            if(duration2.get(2) > duration1.get(2)){
                                maxDuration = tempDuration;
                                maxDateDuration = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                            }
                        }
                    }

                    switch (cursor.getInt(cursor.getColumnIndex(COLUMN_ACTIVITY_TYPE))){
                        case ActivityRecycler.ACTIVITY_RUNNING:
                            if(maxDistanceRun < cursor.getFloat(cursor.getColumnIndex(COLUMN_DISTANCE))){
                                maxDistanceRun = cursor.getFloat(cursor.getColumnIndex(COLUMN_DISTANCE));
                                maxDateRun = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                            }
                            break;
                        case ActivityRecycler.ACTIVITY_WALKING:
                            if(maxDistanceWalk < cursor.getFloat(cursor.getColumnIndex(COLUMN_DISTANCE))){
                                maxDistanceWalk = cursor.getFloat(cursor.getColumnIndex(COLUMN_DISTANCE));
                                maxDateWalk = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                            }
                            break;
                        case ActivityRecycler.ACTIVITY_CYCLING:
                            if(maxDistanceBicycle < cursor.getFloat(cursor.getColumnIndex(COLUMN_DISTANCE))){
                                maxDistanceBicycle = cursor.getFloat(cursor.getColumnIndex(COLUMN_DISTANCE));
                                maxDateBicycle = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                            }
                    }
                }while(cursor.moveToNext());
                cursor.close();

                data.add(String.valueOf(maxDistanceRun));
                data.add(maxDateRun);
                data.add(String.valueOf(maxDistanceWalk));
                data.add(maxDateWalk);
                data.add(String.valueOf(maxDistanceBicycle));
                data.add(maxDateBicycle);
                data.add(String.valueOf(maxCalories));
                data.add(maxDateCalories);
                data.add(maxDuration);
                data.add(maxDateDuration);
            }
        }

        return data;
    }

    public void deleteDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }
}
