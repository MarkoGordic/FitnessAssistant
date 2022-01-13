package com.example.fitnessassistant.database.mdbh;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MDBHWeight extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Weight.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "weight";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_WEIGHT = "weight";
    private static final String COLUMN_RECORDS = "records";

    private static MDBHWeight instance;

    public static MDBHWeight getInstance(Context context) {
        if(instance == null)
            instance = new MDBHWeight(context);

        return instance;
    }

    public MDBHWeight(@Nullable Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + TABLE_NAME +
                        " (" + COLUMN_DATE + " TEXT PRIMARY KEY, " +
                        COLUMN_RECORDS + " INTEGER, " +
                        COLUMN_WEIGHT + " REAL);";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void putWeightData(String date, int records, float totalWeight){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_RECORDS, records);
        cv.put(COLUMN_WEIGHT, totalWeight);

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
                dataExists = true;
            }

        long result;
        if(dataExists)
            result = db.update(TABLE_NAME, cv, "date = ?", new String[]{String.valueOf(date)});
        else
            result = db.insert(TABLE_NAME, null, cv);

        if(result == -1)
            System.out.println("Fail! DATABASE");
        else
            System.out.println("Success! DATABASE");
    }

    public String readWeightData(String date){
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_DATE + " = " + date;
        SQLiteDatabase db = this.getReadableDatabase();

        String data = null;
        if(db != null){
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null){
                cursor.moveToFirst();
                data = cursor.getFloat(cursor.getColumnIndex(COLUMN_WEIGHT)) + "#" + cursor.getInt(cursor.getColumnIndex(COLUMN_RECORDS));
                cursor.close();
            }
        }

        return data;
    }

    public int readWeightRecords(String date){
        String query = "SELECT " + COLUMN_RECORDS + " FROM " + TABLE_NAME + " WHERE " + COLUMN_DATE + " = " + date;
        SQLiteDatabase db = this.getReadableDatabase();
        int data = 0;

        if(db != null ){
            Cursor cursor = db.rawQuery(query, null);

            if(cursor.moveToFirst()){
                data = cursor.getInt(cursor.getColumnIndex(COLUMN_RECORDS));
            }
            cursor.close();
        }

        return data;
    }

    public float readWeightTotalWeight(String date){
        String query = "SELECT " + COLUMN_WEIGHT + " FROM " + TABLE_NAME + " WHERE " + COLUMN_DATE + " = " + date;
        SQLiteDatabase db = this.getReadableDatabase();
        float data = 0f;

        if(db != null ){
            Cursor cursor = db.rawQuery(query, null);

            if(cursor.moveToFirst()){
                data = cursor.getFloat(cursor.getColumnIndex(COLUMN_WEIGHT));
            }
            cursor.close();
        }

        return data;
    }

    public void deleteDayDB(String date){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME+ " WHERE "+ COLUMN_DATE +"='"+date+"'");
    }

    public void deleteDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    public List<String> readWeightDB(){
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        List<String> data = new ArrayList<>();

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null){
                cursor.moveToFirst();
                do{
                    data.add(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)) + "#" + cursor.getFloat(cursor.getColumnIndex(COLUMN_WEIGHT)) + "#" + cursor.getInt(cursor.getColumnIndex(COLUMN_RECORDS)));
                }while(cursor.moveToNext());
                cursor.close();
            }
        }

        return data;
    }
}
