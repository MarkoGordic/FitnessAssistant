package com.example.fitnessassistant.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MDBHSleepTracker extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SleepData.db";
    private static final int DATABASE_VERSION = 1;

    // sleepSegments table
    private static final String SEGMENTS_TABLE_NAME = "sleepSegments";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_CONFIDENCE = "activity_date";
    private static final String COLUMN_LIGHT = "activity_average_speed";
    private static final String COLUMN_MOTION = "activity_calories_burnt";
    private static final String COLUMN_TIME = "activity_distance";

    // sleepEvents table
    private static final String EVENTS_TABLE_NAME = "sleepSegments";
    private static final String EVENTS_COLUMN_ID = "_id";
    private static final String EVENT_START_TIME = "start_time";
    private static final String EVENT_END_TIME = "end_time";

    public MDBHSleepTracker(@Nullable Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + SEGMENTS_TABLE_NAME +
                        " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_CONFIDENCE + " INTEGER, " +
                        COLUMN_LIGHT + " INTEGER, " +
                        COLUMN_MOTION + " INTEGER, " +
                        COLUMN_TIME + " REAL);";

        db.execSQL(query);

        query =
                "CREATE TABLE " + EVENTS_TABLE_NAME +
                        " (" + EVENTS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        EVENT_START_TIME + " REAL, " +
                        EVENT_END_TIME + " REAL);";

        db.execSQL(query);
    }

    public void addNewSleepSegment(int confidence, int light, int motion, long time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_CONFIDENCE, confidence);
        cv.put(COLUMN_LIGHT, light);
        cv.put(COLUMN_MOTION, motion);
        cv.put(COLUMN_TIME, time);

        long result = db.insert(SEGMENTS_TABLE_NAME, null, cv);
        if(result == -1)
            System.out.println("Fail! DATABASE");
        else
            System.out.println("Success! DATABASE");
    }

    public void addNewSleepEvent(long startTime, long endTime){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(EVENT_START_TIME, startTime);
        cv.put(EVENT_END_TIME, endTime);

        long result = db.insert(EVENTS_TABLE_NAME, null, cv);
        if(result == -1)
            System.out.println("Fail! DATABASE");
        else
            System.out.println("Success! DATABASE");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SEGMENTS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + EVENTS_TABLE_NAME);
        onCreate(db);
    }
}
