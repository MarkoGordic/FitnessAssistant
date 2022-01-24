package com.example.fitnessassistant.database.mdbh;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.fitnessassistant.database.data.Meal;
import com.example.fitnessassistant.nutritiontracker.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class MDBHNutritionTracker extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "NutritionTracker.db";
    private static final int DATABASE_VERSION = 1;

    private static final String PRODUCTS_TABLE_NAME = "products";
    private static final String PRODUCTS_COLUMN_ID = "_id";
    private static final String PRODUCTS_COLUMN_NAME = "name";
    private static final String PRODUCTS_COLUMN_NUTRIMENTS = "nutriments";
    private static final String PRODUCTS_COLUMN_BARCODE = "barcode";
    private static final String PRODUCTS_COLUMN_BRANDS = "brands";

    private static final String MEALS_TABLE_NAME = "meals";
    private static final String MEALS_COLUMN_ID = "_id";
    private static final String MEALS_COLUMN_TYPE = "type";
    private static final String MEALS_COLUMN_DATE = "date";
    private static final String MEALS_COLUMN_PRODUCTS = "product";
    private static final String MEALS_COLUMN_QUANTITY = "quantity";

    private static MDBHNutritionTracker instance;

    public static MDBHNutritionTracker getInstance(Context context) {
        if(instance == null)
            instance = new MDBHNutritionTracker(context);

        return instance;
    }

    public MDBHNutritionTracker(@Nullable Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void addNewProduct(String name, String nutriments, String barcode, String brands){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(PRODUCTS_COLUMN_NAME, name);
        cv.put(PRODUCTS_COLUMN_NUTRIMENTS, nutriments);
        cv.put(PRODUCTS_COLUMN_BARCODE, barcode);
        cv.put(PRODUCTS_COLUMN_BRANDS, brands);

        long result = db.insert(PRODUCTS_TABLE_NAME, null, cv);

        if(result == -1)
            System.out.println("Fail! DATABASE");
        else
            System.out.println("Success! DATABASE");
    }

    public void addNewMeal(int type, long date, List<Integer> product_ids, float quantity){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        StringBuilder products = new StringBuilder();
        for(int i = 0; i < product_ids.size(); i++) {
            products.append(product_ids.get(i));
            if(i != product_ids.size() - 1)
                products.append('#');
        }

        cv.put(MEALS_COLUMN_TYPE, type);
        cv.put(MEALS_COLUMN_DATE, date);
        cv.put(MEALS_COLUMN_PRODUCTS, String.valueOf(products));
        cv.put(MEALS_COLUMN_QUANTITY, quantity);

        long result = db.insert(MEALS_TABLE_NAME, null, cv);

        if(result == -1)
            System.out.println("Fail! DATABASE");
        else
            System.out.println("Success! DATABASE");
    }

    public ArrayList<Product> searchProductsByName(String searchTerm){
        String query = "SELECT * FROM " + PRODUCTS_TABLE_NAME + " WHERE " + PRODUCTS_COLUMN_NAME + " LIKE " + "?";
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Product> data = new ArrayList<>();

        if(db != null) {
            Cursor cursor = db.rawQuery(query, new String[]{searchTerm});
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do{
                    Product product = new Product();
                    product.setName(cursor.getString(cursor.getColumnIndex(PRODUCTS_COLUMN_NAME)));
                    product.setBarcode(cursor.getString(cursor.getColumnIndex(PRODUCTS_COLUMN_BARCODE)));
                    product.setBrands(cursor.getString(cursor.getColumnIndex(PRODUCTS_COLUMN_BRANDS)));
                    String nutriments = cursor.getString(cursor.getColumnIndex(PRODUCTS_COLUMN_NUTRIMENTS));
                    StringTokenizer stringTokenizer = new StringTokenizer(nutriments, "#");
                    product.setCalcium_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setChloride_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setFluoride_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setMagnesium_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setPotassium_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setCholesterol_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setSalt_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setZinc_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setSodium_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setBiotin_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setCarbohydrates_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setEnergy_kcal_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setFiber_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setFat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setSaturated_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setTrans_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setMonounsaturated_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setPolyunsaturated_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setOmega_3_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setOmega_6_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setOmega_9_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setProteins_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setIron_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setCopper_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setManganese_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setIodine_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setCaffeine_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setTaurine_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setSugars_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setSucrose_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setGlucose_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setFructose_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setLactose_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setMaltose_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setStarch_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setCasein_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setAlcohol_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_a_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_b1_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_b2_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_b6_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_b9_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_b12_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_c_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_d_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_e_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_k_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_pp_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    data.add(product);
                }while(cursor.moveToNext());
                cursor.close();
            }
        }

        return data;
    }

    public ArrayList<Product> searchProductsByBarcode(String searchTerm){
        String query = "SELECT * FROM " + PRODUCTS_TABLE_NAME + " WHERE " + PRODUCTS_COLUMN_BARCODE + "= ?";
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<Product> data = new ArrayList<>();

        if(db != null) {
            Cursor cursor = db.rawQuery(query, new String[]{searchTerm});
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do{
                    Product product = new Product();
                    product.setName(cursor.getString(cursor.getColumnIndex(PRODUCTS_COLUMN_NAME)));
                    product.setBarcode(cursor.getString(cursor.getColumnIndex(PRODUCTS_COLUMN_BARCODE)));
                    product.setBrands(cursor.getString(cursor.getColumnIndex(PRODUCTS_COLUMN_BRANDS)));
                    String nutriments = cursor.getString(cursor.getColumnIndex(PRODUCTS_COLUMN_NUTRIMENTS));
                    StringTokenizer stringTokenizer = new StringTokenizer(nutriments, "#");
                    product.setCalcium_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setChloride_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setFluoride_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setMagnesium_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setPotassium_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setCholesterol_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setSalt_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setZinc_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setSodium_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setBiotin_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setCarbohydrates_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setEnergy_kcal_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setFiber_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setFat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setSaturated_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setTrans_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setMonounsaturated_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setPolyunsaturated_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setOmega_3_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setOmega_6_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setOmega_9_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setProteins_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setIron_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setCopper_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setManganese_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setIodine_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setCaffeine_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setTaurine_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setSugars_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setSucrose_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setGlucose_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setFructose_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setLactose_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setMaltose_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setStarch_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setCasein_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setAlcohol_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_a_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_b1_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_b2_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_b6_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_b9_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_b12_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_c_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_d_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_e_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_k_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_pp_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    data.add(product);
                }while(cursor.moveToNext());
                cursor.close();
            }
        }

        return data;
    }

    public Product getProductFromDB(int id){
        String query = "SELECT * FROM " + PRODUCTS_TABLE_NAME + " WHERE " + PRODUCTS_COLUMN_ID + "= ?";
        SQLiteDatabase db = this.getReadableDatabase();

        Product data = null;

        if(db != null) {
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do{
                    Product product = new Product();
                    product.setName(cursor.getString(cursor.getColumnIndex(PRODUCTS_COLUMN_NAME)));
                    product.setBarcode(cursor.getString(cursor.getColumnIndex(PRODUCTS_COLUMN_BARCODE)));
                    product.setBrands(cursor.getString(cursor.getColumnIndex(PRODUCTS_COLUMN_BRANDS)));
                    String nutriments = cursor.getString(cursor.getColumnIndex(PRODUCTS_COLUMN_NUTRIMENTS));
                    StringTokenizer stringTokenizer = new StringTokenizer(nutriments, "#");
                    product.setCalcium_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setChloride_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setFluoride_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setMagnesium_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setPotassium_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setCholesterol_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setSalt_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setZinc_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setSodium_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setBiotin_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setCarbohydrates_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setEnergy_kcal_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setFiber_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setFat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setSaturated_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setTrans_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setMonounsaturated_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setPolyunsaturated_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setOmega_3_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setOmega_6_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setOmega_9_fat_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setProteins_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setIron_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setCopper_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setManganese_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setIodine_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setCaffeine_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setTaurine_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setSugars_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setSucrose_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setGlucose_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setFructose_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setLactose_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setMaltose_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setStarch_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setCasein_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setAlcohol_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_a_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_b1_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_b2_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_b6_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_b9_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_b12_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_c_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_d_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_e_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_k_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    product.setVitamin_pp_100g(Float.parseFloat(stringTokenizer.nextToken()));
                    data = product;
                }while(cursor.moveToNext());
                cursor.close();
            }
        }

        return data;
    }

    // returns false if targeted product does not exist in DB
    public boolean editProductFromDB(Product product){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(PRODUCTS_COLUMN_ID, product.getId());
        cv.put(PRODUCTS_COLUMN_NAME, product.getName());
        cv.put(PRODUCTS_COLUMN_BARCODE, product.getBarcode());
        cv.put(PRODUCTS_COLUMN_BRANDS, product.getBrands());
        cv.put(PRODUCTS_COLUMN_NUTRIMENTS, product.nutrimentsToDBString());

        // now we need to determine does old data exists
        String query = "SELECT * FROM " + PRODUCTS_TABLE_NAME + " WHERE " + PRODUCTS_COLUMN_ID + " = " + product.getId();
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

        if(dataExists) {
            db.update(PRODUCTS_TABLE_NAME, cv, "id = ?", new String[]{String.valueOf(product.getId())});
            return true;
        }
        else
            return false;

    }

    public void removeProductFromDB(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + PRODUCTS_TABLE_NAME+ " WHERE "+ PRODUCTS_COLUMN_ID +"='"+id+"'");
    }

    public void removeMealFromDB(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + MEALS_TABLE_NAME + " WHERE "+ MEALS_COLUMN_ID +"='"+id+"'");
    }

    public boolean editMealFromDB(Meal meal){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        List<Integer> product_ids = meal.getProductIDs();
        StringBuilder products = new StringBuilder();
        for(int i = 0; i < product_ids.size(); i++) {
            products.append(product_ids.get(i));
            if(i != product_ids.size() - 1)
                products.append('#');
        }

        cv.put(MEALS_COLUMN_ID, meal.getId());
        cv.put(MEALS_COLUMN_PRODUCTS, String.valueOf(products));
        cv.put(MEALS_COLUMN_TYPE, meal.getType());
        cv.put(MEALS_COLUMN_DATE, meal.getDate());
        cv.put(MEALS_COLUMN_QUANTITY, meal.getQuantity());

        // now we need to determine does old data exists
        String query = "SELECT * FROM " + PRODUCTS_TABLE_NAME + " WHERE " + PRODUCTS_COLUMN_ID + " = " + meal.getId();
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

        if(dataExists) {
            db.update(MEALS_TABLE_NAME, cv, "id = ?", new String[]{String.valueOf(meal.getId())});
            return true;
        }
        else
            return false;
    }

    public List<Meal> getAllMealsFromDB(){
        String query = "SELECT * FROM " + MEALS_TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        List<Meal> data = new ArrayList<>();

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                do{
                    Meal meal = new Meal();
                    meal.setId(cursor.getInt(cursor.getColumnIndex(MEALS_COLUMN_ID)));
                    meal.setDate(cursor.getLong(cursor.getColumnIndex(MEALS_COLUMN_DATE)));
                    meal.setType(cursor.getInt(cursor.getColumnIndex(MEALS_COLUMN_TYPE)));
                    String products = cursor.getString(cursor.getColumnIndex(MEALS_COLUMN_PRODUCTS));
                    StringTokenizer stringTokenizer = new StringTokenizer(products,"#");
                    List<Integer> product_ids = new ArrayList<>();
                    while (stringTokenizer.hasMoreElements())
                        product_ids.add(Integer.parseInt(stringTokenizer.nextToken()));
                    meal.setProductIDs(product_ids);
                    meal.setQuantity(cursor.getFloat(cursor.getColumnIndex(MEALS_COLUMN_QUANTITY)));
                    data.add(meal);
                }while(cursor.moveToNext());
                cursor.close();
            }
        }

        return data;
    }

    public int getLastProductID(){
        String query = "SELECT * FROM " + PRODUCTS_TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        int id = -1;

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                do{
                    id = cursor.getInt(cursor.getColumnIndex(PRODUCTS_COLUMN_ID));
                }while(cursor.moveToNext());
                cursor.close();
            }
        }

        return id;
    }

    public int getLastMealID(){
        String query = "SELECT * FROM " + MEALS_TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        int id = -1;

        if(db != null){
            Cursor cursor = db.rawQuery(query, null);
            if(cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                do{
                    id = cursor.getInt(cursor.getColumnIndex(MEALS_COLUMN_ID));
                }while(cursor.moveToNext());
                cursor.close();
            }
        }

        return id;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + PRODUCTS_TABLE_NAME +
                        " (" + PRODUCTS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        PRODUCTS_COLUMN_NAME + " TEXT, " +
                        PRODUCTS_COLUMN_NUTRIMENTS + " TEXT, " +
                        PRODUCTS_COLUMN_BRANDS + " TEXT, " +
                        PRODUCTS_COLUMN_BARCODE + " TEXT);";

        db.execSQL(query);

        query =
                "CREATE TABLE " + MEALS_TABLE_NAME +
                        " (" + MEALS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        MEALS_COLUMN_TYPE + " INTEGER, " +
                        MEALS_COLUMN_DATE + " TEXT, " +
                        MEALS_COLUMN_QUANTITY + " REAL, " +
                        MEALS_COLUMN_PRODUCTS + " TEXT);";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PRODUCTS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MEALS_TABLE_NAME);
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
