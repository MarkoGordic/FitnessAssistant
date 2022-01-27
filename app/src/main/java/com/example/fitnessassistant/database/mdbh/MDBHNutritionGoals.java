package com.example.fitnessassistant.database.mdbh;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MDBHNutritionGoals extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "NutritionGoals.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "goals";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_CALORIES = "calories";
    private static final String COLUMN_PROTEIN = "protein";
    private static final String COLUMN_FAT = "fat";
    private static final String COLUMN_CARBS = "carbs";

    private static MDBHNutritionGoals instance;

    public static MDBHNutritionGoals getInstance(Context context) {
        if(instance == null)
            instance = new MDBHNutritionGoals(context);

        return instance;
    }

    public MDBHNutritionGoals(@Nullable Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + TABLE_NAME +
                        " (" + COLUMN_DATE + " TEXT PRIMARY KEY, " +
                        COLUMN_CALORIES + " REAL, " +
                        COLUMN_CARBS + " REAL, " +
                        COLUMN_FAT + " REAL, " +
                        COLUMN_PROTEIN + " REAL);";

        db.execSQL(query);
    }

    public void putNutritionGoalsData(String date, Float calories, Float carbs, Float fat, Float protein){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_CALORIES, calories);
        cv.put(COLUMN_CARBS, carbs);
        cv.put(COLUMN_FAT, fat);
        cv.put(COLUMN_PROTEIN, protein);

        long result = db.insert(TABLE_NAME, null, cv);

        if(result == -1)
            System.out.println("Fail! DATABASE");
        else
            System.out.println("Success! DATABASE");
    }

    public String findLatestDayInDB(){
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        String data = null;
        if(db != null){
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null){
                if(cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        if(cursor.isNull(cursor.getColumnIndex(COLUMN_CALORIES))){
                            data = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                            break;
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        }

        return data;
    }

    public float readCaloriesForDate(String date){
        String query = "SELECT " + COLUMN_CALORIES + " FROM " + TABLE_NAME + " WHERE " + COLUMN_DATE + " = " + date;
        SQLiteDatabase db = this.getReadableDatabase();
        float data = -1;

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);

            if(cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    data = cursor.getFloat(cursor.getColumnIndex(COLUMN_CALORIES));
                }
                cursor.close();
            }
        }

        return data;
    }

    public float readFatForDate(String date){
        String query = "SELECT " + COLUMN_FAT + " FROM " + TABLE_NAME + " WHERE " + COLUMN_DATE + " = " + date;
        SQLiteDatabase db = this.getReadableDatabase();
        float data = -1;

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);

            if(cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    data = cursor.getFloat(cursor.getColumnIndex(COLUMN_FAT));
                }
                cursor.close();
            }
        }

        return data;
    }

    public float readProteinForDate(String date){
        String query = "SELECT " + COLUMN_PROTEIN + " FROM " + TABLE_NAME + " WHERE " + COLUMN_DATE + " = " + date;
        SQLiteDatabase db = this.getReadableDatabase();
        float data = -1;

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);

            if(cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    data = cursor.getFloat(cursor.getColumnIndex(COLUMN_PROTEIN));
                }
                cursor.close();
            }
        }

        return data;
    }

    public float readCarbsForDate(String date){
        String query = "SELECT " + COLUMN_CARBS + " FROM " + TABLE_NAME + " WHERE " + COLUMN_DATE + " = " + date;
        SQLiteDatabase db = this.getReadableDatabase();
        float data = -1;

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);

            if(cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    data = cursor.getFloat(cursor.getColumnIndex(COLUMN_CARBS));
                }
                cursor.close();
            }
        }

        return data;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void deleteDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }
}
