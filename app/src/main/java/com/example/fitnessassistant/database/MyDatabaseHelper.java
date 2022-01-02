package com.example.fitnessassistant.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SavedActivities.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "activities";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_DATE = "activity_date";
    private static final String COLUMN_AVERAGE_SPEED = "activity_average_speed";
    private static final String COLUMN_CALORIES_BURNT = "activity_calories_burnt";
    private static final String COLUMN_DISTANCE = "activity_distance";
    private static final String COLUMN_IMAGE = "activity_image";

    public MyDatabaseHelper(@Nullable Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + TABLE_NAME +
                        " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_DATE + " REAL, " +
                        COLUMN_AVERAGE_SPEED + " REAL, " +
                        COLUMN_CALORIES_BURNT + " INTEGER, " +
                        COLUMN_DISTANCE + " REAL, " +
                        COLUMN_IMAGE + " BLOB);";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addNewActivity(long date, float averageSpeed, double distance, float calories, Bitmap image){
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

        long result = db.insert(TABLE_NAME, null, cv);
        if(result == -1)
            System.out.println("Fail! DATABASE");
        else
            System.out.println("Success! DATABASE");
    }

    Cursor readAllData() {
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if(db != null){
            cursor = db.rawQuery(query, null);
        }

        return cursor;
    }
}
