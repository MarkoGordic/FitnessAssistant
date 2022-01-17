package com.example.fitnessassistant.database.mdbh;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MDBHNutritionTracker extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "NutritionTracker.db";
    private static final int DATABASE_VERSION = 1;

    private static final String PRODUCTS_TABLE_NAME = "products";
    private static final String PRODUCTS_COLUMN_ID = "_id";
    private static final String PRODUCTS_COLUMN_NAME = "name";
    private static final String PRODUCTS_COLUMN_NUTRIMENTS = "nutriments";
    private static final String PRODUCTS_COLUMN_BARCODE = "barcode";

    private static final String MEALS_TABLE_NAME = "meals";
    private static final String MEALS_COLUMN_ID = "_id";
    private static final String MEALS_COLUMN_TYPE = "type";
    private static final String MEALS_COLUMN_DATE = "date";
    private static final String MEALS_COLUMN_PRODUCT = "product";

    private static MDBHNutritionTracker instance;

    public static MDBHNutritionTracker getInstance(Context context) {
        if(instance == null)
            instance = new MDBHNutritionTracker(context);

        return instance;
    }

    public MDBHNutritionTracker(@Nullable Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void addNewProduct(String name, String nutriments, String barcode){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(PRODUCTS_COLUMN_NAME, name);
        cv.put(PRODUCTS_COLUMN_NUTRIMENTS, nutriments);
        cv.put(PRODUCTS_COLUMN_BARCODE, barcode);

        long result = db.insert(PRODUCTS_TABLE_NAME, null, cv);

        if(result == -1)
            System.out.println("Fail! DATABASE");
        else
            System.out.println("Success! DATABASE");
    }

    public void addNewMeal(int type, long date, int product_id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(MEALS_COLUMN_TYPE, type);
        cv.put(MEALS_COLUMN_DATE, date);
        cv.put(MEALS_COLUMN_PRODUCT, product_id);

        long result = db.insert(MEALS_TABLE_NAME, null, cv);

        if(result == -1)
            System.out.println("Fail! DATABASE");
        else
            System.out.println("Success! DATABASE");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + PRODUCTS_TABLE_NAME +
                        " (" + PRODUCTS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        PRODUCTS_COLUMN_NAME + " TEXT, " +
                        PRODUCTS_COLUMN_NUTRIMENTS + " TEXT, " +
                        PRODUCTS_COLUMN_BARCODE + " TEXT);";

        db.execSQL(query);

        query =
                "CREATE TABLE " + MEALS_TABLE_NAME +
                        " (" + MEALS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MEALS_COLUMN_TYPE + " INTEGER, " +
                        MEALS_COLUMN_DATE + " REAL, " +
                        MEALS_COLUMN_PRODUCT + " INTEGER);";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PRODUCTS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MEALS_COLUMN_PRODUCT);
        onCreate(db);
    }

    public void removeProductsTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + PRODUCTS_TABLE_NAME);
    }

    public void removeMealsTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + MEALS_TABLE_NAME);
    }

    public void deleteDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(PRODUCTS_TABLE_NAME, null, null);
        db.delete(MEALS_TABLE_NAME, null, null);
    }
}
