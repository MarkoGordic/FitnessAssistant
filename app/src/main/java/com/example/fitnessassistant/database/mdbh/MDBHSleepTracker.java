package com.example.fitnessassistant.database.mdbh;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import com.example.fitnessassistant.database.data.SleepSegment;
import com.example.fitnessassistant.sleeptracker.SleepTracker;
import com.example.fitnessassistant.uiprefs.LocaleExt;

import java.util.ArrayList;
import java.util.List;

public class MDBHSleepTracker extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SleepData.db";
    private static final int DATABASE_VERSION = 1;

    // sleepEvents table
    private static final String SEGMENTS_TABLE_NAME = "sleepSegments";
    private static final String SEGMENTS_COLUMN_ID = "_id";
    private static final String SEGMENTS_START_TIME = "start_time";
    private static final String SEGMENTS_END_TIME = "end_time";
    private static final String SEGMENTS_SLEEP_QUALITY = "quality";
    private static final String SEGMENTS_SLEEP_DATE = "date";

    private static MDBHSleepTracker instance;

    public static MDBHSleepTracker getInstance(Context context) {
        if(instance == null)
            instance = new MDBHSleepTracker(context);

        return instance;
    }

    public MDBHSleepTracker(@Nullable Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + SEGMENTS_TABLE_NAME +
                        " (" + SEGMENTS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        SEGMENTS_SLEEP_DATE + " TEXT, " +
                        SEGMENTS_SLEEP_QUALITY + " INTEGER, " +
                        SEGMENTS_START_TIME + " REAL, " +
                        SEGMENTS_END_TIME + " REAL);";

        db.execSQL(query);
    }

    public void addNewSleepSegment(Context context, long startTime, long endTime, String date){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(SEGMENTS_START_TIME, startTime);
        cv.put(SEGMENTS_END_TIME, endTime);
        cv.put(SEGMENTS_SLEEP_DATE, date);
        cv.put(SEGMENTS_SLEEP_QUALITY, -1);

        boolean same = checkSleepSegment(startTime, endTime);
        long result;

        if(!same){
            result = db.insert(SEGMENTS_TABLE_NAME, null, cv);

            Context updatedContext = LocaleExt.toLangIfDiff(context, PreferenceManager.getDefaultSharedPreferences(context).getString("langPref", "sys"), true, false);
            SleepTracker.pushSleepDetectedNotification(updatedContext, startTime, endTime);

            if(result == -1)
                System.out.println("Fail! DATABASE");
            else
                System.out.println("Success! DATABASE");
        }
    }

    public boolean checkSleepSegment(long startTime, long endTime){
        String query = "SELECT * FROM " + SEGMENTS_TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        long[] values = new long[2];

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null && cursor.getCount() > 0){
                cursor.moveToLast();

                values[0] = cursor.getLong(cursor.getColumnIndex(SEGMENTS_START_TIME));
                values[1] = cursor.getLong(cursor.getColumnIndex(SEGMENTS_END_TIME));

                cursor.close();
            }
        }

        return values[0] == startTime && values[1] == endTime;
    }

    public List<SleepSegment> getSleepSegmentsForDateFromDB(String date){
        String query = "SELECT * FROM " + SEGMENTS_TABLE_NAME + " WHERE " + SEGMENTS_SLEEP_DATE + " = " + date;
        SQLiteDatabase db = this.getReadableDatabase();

        List<SleepSegment> data = null;

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);

            if(cursor != null && cursor.getCount() > 0) {
                data = new ArrayList<>();
                if (cursor.moveToFirst()) {
                    SleepSegment sleepSegment = new SleepSegment();
                    sleepSegment.setStartTime(cursor.getLong(cursor.getColumnIndex(SEGMENTS_START_TIME)));
                    sleepSegment.setEndTime(cursor.getLong(cursor.getColumnIndex(SEGMENTS_END_TIME)));
                    sleepSegment.setQuality(cursor.getInt(cursor.getColumnIndex(SEGMENTS_SLEEP_QUALITY)));
                    data.add(sleepSegment);
                }
                cursor.close();
            }
        }

        return data;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SEGMENTS_TABLE_NAME);
        onCreate(db);
    }

    public void deleteSegmentsDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(SEGMENTS_TABLE_NAME, null, null);
    }
}
