package com.example.fitnessassistant.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

// TODO : Close DB when Pedometer DB is no longer in use (if even needed)

public class MDBHPedometer extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Pedometer.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "steps";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_STEPS = "totalSteps";
    private static final String COLUMN_STEP_GOAL = "stepGoal";

    private static MDBHPedometer instance;

    public static MDBHPedometer getInstance(Context context) {
        if(instance == null)
            instance = new MDBHPedometer(context);

        return instance;
    }

    public MDBHPedometer(@Nullable Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + TABLE_NAME +
                        " (" + COLUMN_DATE + " INTEGER PRIMARY KEY, " +
                        COLUMN_STEPS + " REAL, " +
                        COLUMN_STEP_GOAL + " INTEGER);";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void putPedometerData(Context context, String date, Float steps, Integer stepGoal){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        if(steps == null){
            cv.put(COLUMN_DATE, date);
            cv.put(COLUMN_STEP_GOAL, stepGoal);
        }else if(stepGoal == null){
            cv.put(COLUMN_DATE, date);
            cv.put(COLUMN_STEPS, steps);
        }else{
            cv.put(COLUMN_DATE, date);
            cv.put(COLUMN_STEPS, steps);
            cv.put(COLUMN_STEP_GOAL, stepGoal);
        }

        // now we need to determine do we need to update old data or insert new
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_DATE + " = " + date;
        SQLiteDatabase dbRead = this.getReadableDatabase();
        Cursor cursor = null;
        boolean dataExists = false;

        if(dbRead != null){
            cursor = dbRead.rawQuery(query, null);
        }

        if(cursor != null)
            if(cursor.getCount() > 0) {
                cursor.close();
                //dbRead.close();
                dataExists = true;
            }

        long result;
        if(dataExists)
            result = db.update(TABLE_NAME, cv, "date = ?", new String[]{String.valueOf(date)});
        else
            result = db.insert(TABLE_NAME, null, cv);

        context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit().putBoolean("pedometerDataChanged", !context.getSharedPreferences("pedometer", Context.MODE_PRIVATE).getBoolean("pedometerDataChanged", false)).apply();

        if(result == -1)
            System.out.println("Fail! DATABASE");
        else
            System.out.println("Success! DATABASE");
    }

    public List<String> readPedometerDB(){
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        List<String> data = new ArrayList<>();
        if(db != null){
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null){
                cursor.moveToFirst();
                do{
                    data.add(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)) + "#" + cursor.getFloat(cursor.getColumnIndex(COLUMN_STEPS)) + "#" + cursor.getInt(cursor.getColumnIndex(COLUMN_STEP_GOAL)));
                }while(cursor.moveToNext());
                cursor.close();
            }
        }

        //db.close();
        return data;
    }

    public int readPedometerStepGoal(String date){
        String query = "SELECT " + COLUMN_STEP_GOAL + " FROM " + TABLE_NAME + " WHERE " + COLUMN_DATE + " = " + date;
        SQLiteDatabase db = this.getReadableDatabase();
        int data = 10000;

        if(db != null ){
            Cursor cursor = db.rawQuery(query, null);

            if(cursor.moveToFirst()){
                data = cursor.getInt(cursor.getColumnIndex(COLUMN_STEP_GOAL));
            }
            cursor.close();
        }

        return data;
    }

    public float readPedometerSteps(String date){
        String query = "SELECT " + COLUMN_STEPS + " FROM " + TABLE_NAME + " WHERE " + COLUMN_DATE + " = " + date;
        SQLiteDatabase db = this.getReadableDatabase();
        float data = 0f;

        if(db != null ){
            Cursor cursor = db.rawQuery(query, null);

            if(cursor.moveToFirst()){
                data = cursor.getFloat(cursor.getColumnIndex(COLUMN_STEPS));
            }
            cursor.close();
        }

        return data;
    }

    public boolean checkIfRecordExists(String date){
        String query = "SELECT " + COLUMN_STEPS + " FROM " + TABLE_NAME + " WHERE " + COLUMN_DATE + " = " + date;
        SQLiteDatabase dbRead = this.getReadableDatabase();
        Cursor cursor = null;

        if(dbRead != null){
            cursor = dbRead.rawQuery(query, null);
        }

        if(cursor != null)
            if(cursor.getCount() > 0) {
                cursor.close();
                return true;
            }

        return false;
    }

}
