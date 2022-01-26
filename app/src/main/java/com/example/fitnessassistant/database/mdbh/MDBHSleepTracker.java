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
    private static final String SEGMENTS_CONFIRMATION_STATUS = "status";
    private static final String SEGMENTS_DURATION = "duration";

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
                        SEGMENTS_CONFIRMATION_STATUS + " INTEGER, " +
                        SEGMENTS_DURATION + " REAL, " +
                        SEGMENTS_START_TIME + " REAL, " +
                        SEGMENTS_END_TIME + " REAL);";

        db.execSQL(query);
    }

    public void addNewSleepSegment(Context context, Long startTime, Long endTime, Long duration, String date, Integer quality, Integer confirmationStatus, boolean updateRequest){
        if(date == null)
            return;

        String query = "SELECT * FROM " + SEGMENTS_TABLE_NAME + " WHERE " + SEGMENTS_SLEEP_DATE + " = " + date;
        SQLiteDatabase dbRead = this.getReadableDatabase();
        Cursor cursor = null;
        boolean dataExists = false;
        int confirmation = -1; // indicator if user already confirmed today's sleep

        if(dbRead != null){
            cursor = dbRead.rawQuery(query, null);
        }

        if(cursor != null)
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();

                confirmation = cursor.getInt(cursor.getColumnIndex(SEGMENTS_CONFIRMATION_STATUS));
                cursor.close();
                dataExists = true;
            }

        if(confirmation == 1 && !updateRequest)
            return;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        boolean same = true;

        if(startTime != null && endTime != null) {
            cv.put(SEGMENTS_START_TIME, startTime);
            cv.put(SEGMENTS_END_TIME, endTime);
            cv.put(SEGMENTS_DURATION, duration);
            same = checkSleepSegment(startTime, endTime);
        }

        cv.put(SEGMENTS_SLEEP_DATE, date);
        cv.put(SEGMENTS_SLEEP_QUALITY, quality);
        cv.put(SEGMENTS_CONFIRMATION_STATUS, confirmationStatus);

        long result = -1;

        if(!same && !updateRequest){
            Context updatedContext = LocaleExt.toLangIfDiff(context, PreferenceManager.getDefaultSharedPreferences(context).getString("langPref", "sys"), true, false);
            SleepTracker.pushSleepDetectedNotification(updatedContext, startTime, endTime);
        }

        if(dataExists)
            result = db.update(SEGMENTS_TABLE_NAME, cv, "date = ?", new String[]{date});
        else if(endTime != null && startTime != null)
            result = db.insert(SEGMENTS_TABLE_NAME, null, cv);

        if(result == -1)
            System.out.println("Fail! DATABASE");
        else
            System.out.println("Success! DATABASE");
    }

    public void removeSleepSegment(String date){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + SEGMENTS_TABLE_NAME+ " WHERE "+ SEGMENTS_SLEEP_DATE +"='"+date+"'");
    }

    public void forceAddNewSleepSegment(Long startTime, Long endTime, Long duration, String date, Integer quality, Integer confirmationStatus){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(SEGMENTS_START_TIME, startTime);
        cv.put(SEGMENTS_END_TIME, endTime);
        cv.put(SEGMENTS_DURATION, duration);
        cv.put(SEGMENTS_SLEEP_DATE, date);
        cv.put(SEGMENTS_SLEEP_QUALITY, quality);
        cv.put(SEGMENTS_CONFIRMATION_STATUS, confirmationStatus);

        long result = db.insert(SEGMENTS_TABLE_NAME, null, cv);

        if(result == -1)
            System.out.println("Fail! DATABASE");
        else
            System.out.println("Success! DATABASE");
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

    public SleepSegment getSleepSegmentForDateFromDB(String date){
        String query = "SELECT * FROM " + SEGMENTS_TABLE_NAME + " WHERE " + SEGMENTS_SLEEP_DATE + " = " + date;
        SQLiteDatabase db = this.getReadableDatabase();

        SleepSegment data = null;

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);

            if(cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    data = new SleepSegment();
                    data.setStartTime(cursor.getLong(cursor.getColumnIndex(SEGMENTS_START_TIME)));
                    data.setEndTime(cursor.getLong(cursor.getColumnIndex(SEGMENTS_END_TIME)));
                    data.setDuration(cursor.getLong(cursor.getColumnIndex(SEGMENTS_DURATION)));
                    data.setQuality(cursor.getInt(cursor.getColumnIndex(SEGMENTS_SLEEP_QUALITY)));
                    data.setConfirmationStatus(cursor.getInt(cursor.getColumnIndex(SEGMENTS_CONFIRMATION_STATUS)));
                }
                cursor.close();
            }
        }

        return data;
    }

    public List<String> readSleepDB(){
        String query = "SELECT * FROM " + SEGMENTS_TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        List<String> data = new ArrayList<>();
        if(db != null){
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                do{
                    data.add(cursor.getString(cursor.getColumnIndex(SEGMENTS_SLEEP_DATE)) + "#" + cursor.getLong(cursor.getColumnIndex(SEGMENTS_DURATION)) + "#" + cursor.getInt(cursor.getColumnIndex(SEGMENTS_START_TIME)) + "#" + cursor.getInt(cursor.getColumnIndex(SEGMENTS_END_TIME)) + "#" + cursor.getString(cursor.getColumnIndex(SEGMENTS_CONFIRMATION_STATUS)) + "#" + cursor.getInt(cursor.getColumnIndex(SEGMENTS_SLEEP_QUALITY)));
                }while(cursor.moveToNext());
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
