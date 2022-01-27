package com.example.fitnessassistant.nutritiontracker;

import android.content.Context;

import com.example.fitnessassistant.database.mdbh.MDBHNutritionTracker;

import org.json.JSONException;
import org.json.JSONObject;

public class Product {
    int id;
    private String name;
    private String brands;
    private String barcode;
    private float calcium_100g;
    private float chloride_100g;
    private float fluoride_100g;
    private float magnesium_100g;
    private float potassium_100g;
    private float cholesterol_100g;
    private float salt_100g;
    private float zinc_100g;
    private float sodium_100g;
    private float biotin_100g;
    private float carbohydrates_100g;
    private float energy_kcal_100g; // -
    private float fiber_100g;
    private float fat_100g;
    private float saturated_fat_100g; // -
    private float trans_fat_100g; // -
    private float monounsaturated_fat_100g; // -
    private float polyunsaturated_fat_100g; // -
    private float omega_3_fat_100g; // -, -
    private float omega_6_fat_100g; // -, -
    private float omega_9_fat_100g; // -, -
    private float proteins_100g;
    private float iron_100g;
    private float copper_100g;
    private float manganese_100g;
    private float iodine_100g;
    private float caffeine_100g;
    private float taurine_100g;
    private float sugars_100g;
    private float sucrose_100g;
    private float glucose_100g;
    private float fructose_100g;
    private float lactose_100g;
    private float maltose_100g;
    private float starch_100g;
    private float casein_100g;
    private float alcohol_100g;
    private float vitamin_a_100g; // -
    private float vitamin_b1_100g; // -
    private float vitamin_b2_100g; // -
    private float vitamin_b6_100g; // -
    private float vitamin_b9_100g; // -
    private float vitamin_b12_100g; // -
    private float vitamin_c_100g; // -
    private float vitamin_d_100g; // -
    private float vitamin_e_100g; // -
    private float vitamin_k_100g; // -
    private float vitamin_pp_100g; // -  / B3

    public Product(){}

    public Product(JSONObject object, Context context, boolean search){
        if(object != null)
            try {
                id = MDBHNutritionTracker.getInstance(context).getLastProductID() + 1;
                if(!search) {
                    if (object.has("product")) {
                        JSONObject product = object.getJSONObject("product");

                        if (object.has("_id"))
                            setBarcode(product.getString("_id"));
                        else
                            setBarcode(null);

                        if (product.has("product_name"))
                            setName(product.getString("product_name"));
                        else
                            if(object.has("generic_name"))
                                setName(object.getString("generic_name"));
                            else
                                setName(null);

                        if (product.has("brands"))
                            setBrands(product.getString("brands"));
                        else
                            setBrands(null);

                        JSONObject nutriments = product.getJSONObject("nutriments");

                        if (nutriments.has("getBiotin_100g"))
                            setBiotin_100g((float) nutriments.getDouble("getBiotin_100g"));
                        else
                            setBiotin_100g(0f);

                        if (nutriments.has("calcium_100g"))
                            setCalcium_100g((float) nutriments.getDouble("calcium_100g"));
                        else
                            setCalcium_100g(0f);

                        if (nutriments.has("fluoride_100g"))
                            setFluoride_100g((float) nutriments.getDouble("fluoride_100g"));
                        else
                            setFluoride_100g(0f);

                        if (nutriments.has("chloride_100g"))
                            setChloride_100g((float) nutriments.getDouble("chloride_100g"));
                        else
                            setChloride_100g(0f);

                        if (nutriments.has("magnesium_100g"))
                            setMagnesium_100g((float) nutriments.getDouble("magnesium_100g"));
                        else
                            setMagnesium_100g(0f);

                        if (nutriments.has("potassium_100g"))
                            setPotassium_100g((float) nutriments.getDouble("potassium_100g"));
                        else
                            setPotassium_100g(0f);

                        if (nutriments.has("cholesterol_100g"))
                            setCholesterol_100g((float) nutriments.getDouble("cholesterol_100g"));
                        else
                            setCholesterol_100g(0f);

                        if (nutriments.has("salt_100g"))
                            setSalt_100g((float) nutriments.getDouble("salt_100g"));
                        else
                            setSalt_100g(0f);

                        if (nutriments.has("sodium_100g"))
                            setSodium_100g((float) nutriments.getDouble("sodium_100g"));
                        else
                            setSodium_100g(0f);

                        if (nutriments.has("carbohydrates_100g"))
                            setCarbohydrates_100g((float) nutriments.getDouble("carbohydrates_100g"));
                        else
                            setCarbohydrates_100g(0f);

                        if (nutriments.has("energy-kcal_100g"))
                            setEnergy_kcal_100g((float) nutriments.getDouble("energy-kcal_100g"));
                        else
                            setEnergy_kcal_100g(0f);

                        if (nutriments.has("fiber_100g"))
                            setFiber_100g((float) nutriments.getDouble("fiber_100g"));
                        else
                            setFiber_100g(0f);

                        if (nutriments.has("fat_100g"))
                            setFat_100g((float) nutriments.getDouble("fat_100g"));
                        else
                            setFat_100g(0f);

                        if (nutriments.has("saturated-fat_100g"))
                            setSaturated_fat_100g((float) nutriments.getDouble("saturated-fat_100g"));
                        else
                            setSaturated_fat_100g(0f);

                        if (nutriments.has("trans-fat_100g"))
                            setTrans_fat_100g((float) nutriments.getDouble("trans-fat_100g"));
                        else
                            setTrans_fat_100g(0f);

                        if (nutriments.has("monounsaturated-fat_100g"))
                            setMonounsaturated_fat_100g((float) nutriments.getDouble("monounsaturated-fat_100g"));
                        else
                            setMonounsaturated_fat_100g(0f);

                        if (nutriments.has("polyunsaturated-fat_100g"))
                            setPolyunsaturated_fat_100g((float) nutriments.getDouble("polyunsaturated-fat_100g"));
                        else
                            setPolyunsaturated_fat_100g(0f);

                        if (nutriments.has("omega-3-fat_100g"))
                            setOmega_3_fat_100g((float) nutriments.getDouble("omega-3-fat_100g"));
                        else
                            setOmega_3_fat_100g(0f);

                        if (nutriments.has("omega-6-fat_100g"))
                            setOmega_6_fat_100g((float) nutriments.getDouble("omega-6-fat_100g"));
                        else
                            setOmega_6_fat_100g(0f);

                        if (nutriments.has("omega-9-fat_100g"))
                            setOmega_9_fat_100g((float) nutriments.getDouble("omega-9-fat_100g"));
                        else
                            setOmega_9_fat_100g(0f);

                        if (nutriments.has("caffeine_100g"))
                            setCaffeine_100g((float) nutriments.getDouble("caffeine_100g"));
                        else
                            setCaffeine_100g(0f);

                        if (nutriments.has("copper_100g"))
                            setCopper_100g((float) nutriments.getDouble("copper_100g"));
                        else
                            setCopper_100g(0f);

                        if (nutriments.has("iodine_100g"))
                            setIodine_100g((float) nutriments.getDouble("iodine_100g"));
                        else
                            setIodine_100g(0f);

                        if (nutriments.has("manganese_100g"))
                            setManganese_100g((float) nutriments.getDouble("manganese_100g"));
                        else
                            setManganese_100g(0f);

                        if (nutriments.has("glucose_100g"))
                            setGlucose_100g((float) nutriments.getDouble("glucose_100g"));
                        else
                            setGlucose_100g(0f);

                        if (nutriments.has("fructose_100g"))
                            setFructose_100g((float) nutriments.getDouble("fructose_100g"));
                        else
                            setFructose_100g(0f);

                        if (nutriments.has("lactose_100g"))
                            setLactose_100g((float) nutriments.getDouble("lactose_100g"));
                        else
                            setLactose_100g(0f);

                        if (nutriments.has("lactose_100g"))
                            setLactose_100g((float) nutriments.getDouble("lactose_100g"));
                        else
                            setLactose_100g(0f);

                        if (nutriments.has("sucrose_100g"))
                            setSucrose_100g((float) nutriments.getDouble("sucrose_100g"));
                        else
                            setSucrose_100g(0f);

                        if (nutriments.has("alcohol_100g"))
                            setAlcohol_100g((float) nutriments.getDouble("alcohol_100g"));
                        else
                            setAlcohol_100g(0f);

                        if (nutriments.has("casein_100g"))
                            setCasein_100g((float) nutriments.getDouble("casein_100g"));
                        else
                            setCasein_100g(0f);

                        if (nutriments.has("maltose_100g"))
                            setMaltose_100g((float) nutriments.getDouble("maltose_100g"));
                        else
                            setMaltose_100g(0f);

                        if (nutriments.has("starch_100g"))
                            setStarch_100g((float) nutriments.getDouble("starch_100g"));
                        else
                            setStarch_100g(0f);

                        if (nutriments.has("taurine_100g"))
                            setTaurine_100g((float) nutriments.getDouble("taurine_100g"));
                        else
                            setTaurine_100g(0f);

                        if (nutriments.has("zinc_100g"))
                            setZinc_100g((float) nutriments.getDouble("zinc_100g"));
                        else
                            setZinc_100g(0f);

                        if (nutriments.has("proteins_100g"))
                            setProteins_100g((float) nutriments.getDouble("proteins_100g"));
                        else
                            setProteins_100g(0f);

                        if (nutriments.has("iron_100g"))
                            setIron_100g((float) nutriments.getDouble("iron_100g"));
                        else
                            setIron_100g(0f);

                        if (nutriments.has("sugars_100g"))
                            setSugars_100g((float) nutriments.getDouble("sugars_100g"));
                        else
                            setSugars_100g(0f);

                        if (nutriments.has("vitamin-a_100g"))
                            setVitamin_a_100g((float) nutriments.getDouble("vitamin-a_100g"));
                        else
                            setVitamin_a_100g(0f);

                        if (nutriments.has("vitamin-b1_100g"))
                            setVitamin_b1_100g((float) nutriments.getDouble("vitamin-b1_100g"));
                        else
                            setVitamin_b1_100g(0f);

                        if (nutriments.has("vitamin-b2_100g"))
                            setVitamin_b2_100g((float) nutriments.getDouble("vitamin-b2_100g"));
                        else
                            setVitamin_b2_100g(0f);

                        if (nutriments.has("vitamin-pp_100g"))
                            setVitamin_pp_100g((float) nutriments.getDouble("vitamin-pp_100g"));
                        else
                            setVitamin_pp_100g(0f);

                        if (nutriments.has("vitamin-b6_100g"))
                            setVitamin_b6_100g((float) nutriments.getDouble("vitamin-b6_100g"));
                        else
                            setVitamin_b6_100g(0f);

                        if (nutriments.has("vitamin-b9_100g"))
                            setVitamin_b9_100g((float) nutriments.getDouble("vitamin-b9_100g"));
                        else
                            setVitamin_b9_100g(0f);

                        if (nutriments.has("vitamin-b12_100g"))
                            setVitamin_b12_100g((float) nutriments.getDouble("vitamin-b12_100g"));
                        else
                            setVitamin_b12_100g(0f);

                        if (nutriments.has("vitamin-c_100g"))
                            setVitamin_c_100g((float) nutriments.getDouble("vitamin-c_100g"));
                        else
                            setVitamin_c_100g(0f);

                        if (nutriments.has("vitamin-d_100g"))
                            setVitamin_d_100g((float) nutriments.getDouble("vitamin-d_100g"));
                        else
                            setVitamin_d_100g(0f);

                        if (nutriments.has("vitamin-e_100g"))
                            setVitamin_e_100g((float) nutriments.getDouble("vitamin-e_100g"));
                        else
                            setVitamin_e_100g(0f);

                        if (nutriments.has("vitamin-k_100g"))
                            setVitamin_k_100g((float) nutriments.getDouble("vitamin-k_100g"));
                        else
                            setVitamin_k_100g(0f);
                    }
                }
                else{
                    if(object.has("product_name"))
                        setName(object.getString("product_name"));
                    else
                        if(object.has("generic_name"))
                            setName(object.getString("generic_name"));
                        else
                            setName(null);

                    if (object.has("_id"))
                        setBarcode(object.getString("_id"));
                    else
                        setBarcode(null);

                    if(object.has("brands"))
                        setBrands(object.getString("brands"));
                    else
                        setBrands(null);

                    JSONObject nutriments = object.getJSONObject("nutriments");
                    if(nutriments.has("getBiotin_100g"))
                        setBiotin_100g((float)nutriments.getDouble("getBiotin_100g"));
                    else
                        setBiotin_100g(0f);

                    if(nutriments.has("calcium_100g"))
                        setCalcium_100g((float)nutriments.getDouble("calcium_100g"));
                    else
                        setCalcium_100g(0f);

                    if(nutriments.has("fluoride_100g"))
                        setFluoride_100g((float)nutriments.getDouble("fluoride_100g"));
                    else
                        setFluoride_100g(0f);

                    if(nutriments.has("chloride_100g"))
                        setChloride_100g((float)nutriments.getDouble("chloride_100g"));
                    else
                        setChloride_100g(0f);

                    if(nutriments.has("magnesium_100g"))
                        setMagnesium_100g((float)nutriments.getDouble("magnesium_100g"));
                    else
                        setMagnesium_100g(0f);

                    if(nutriments.has("potassium_100g"))
                        setPotassium_100g((float)nutriments.getDouble("potassium_100g"));
                    else
                        setPotassium_100g(0f);

                    if(nutriments.has("cholesterol_100g"))
                        setCholesterol_100g((float)nutriments.getDouble("cholesterol_100g"));
                    else
                        setCholesterol_100g(0f);

                    if(nutriments.has("salt_100g"))
                        setSalt_100g((float)nutriments.getDouble("salt_100g"));
                    else
                        setSalt_100g(0f);

                    if(nutriments.has("sodium_100g"))
                        setSodium_100g((float)nutriments.getDouble("sodium_100g"));
                    else
                        setSodium_100g(0f);

                    if(nutriments.has("carbohydrates_100g"))
                        setCarbohydrates_100g((float)nutriments.getDouble("carbohydrates_100g"));
                    else
                        setCarbohydrates_100g(0f);

                    if(nutriments.has("energy-kcal_100g"))
                        setEnergy_kcal_100g((float)nutriments.getDouble("energy-kcal_100g"));
                    else
                        setEnergy_kcal_100g(0f);

                    if(nutriments.has("fiber_100g"))
                        setFiber_100g((float)nutriments.getDouble("fiber_100g"));
                    else
                        setFiber_100g(0f);

                        if(nutriments.has("fat_100g"))
                            setFat_100g((float)nutriments.getDouble("fat_100g"));
                        else
                            setFat_100g(0f);

                        if(nutriments.has("saturated-fat_100g"))
                            setSaturated_fat_100g((float)nutriments.getDouble("saturated-fat_100g"));
                        else
                            setSaturated_fat_100g(0f);

                        if(nutriments.has("trans-fat_100g"))
                            setTrans_fat_100g((float)nutriments.getDouble("trans-fat_100g"));
                        else
                            setTrans_fat_100g(0f);

                        if(nutriments.has("monounsaturated-fat_100g"))
                            setMonounsaturated_fat_100g((float)nutriments.getDouble("monounsaturated-fat_100g"));
                        else
                            setMonounsaturated_fat_100g(0f);

                        if(nutriments.has("polyunsaturated-fat_100g"))
                            setPolyunsaturated_fat_100g((float)nutriments.getDouble("polyunsaturated-fat_100g"));
                        else
                            setPolyunsaturated_fat_100g(0f);

                        if(nutriments.has("omega-3-fat_100g"))
                            setOmega_3_fat_100g((float)nutriments.getDouble("omega-3-fat_100g"));
                        else
                            setOmega_3_fat_100g(0f);

                        if(nutriments.has("omega-6-fat_100g"))
                            setOmega_6_fat_100g((float)nutriments.getDouble("omega-6-fat_100g"));
                        else
                            setOmega_6_fat_100g(0f);

                        if(nutriments.has("omega-9-fat_100g"))
                            setOmega_9_fat_100g((float)nutriments.getDouble("omega-9-fat_100g"));
                        else
                            setOmega_9_fat_100g(0f);

                        if(nutriments.has("caffeine_100g"))
                            setCaffeine_100g((float)nutriments.getDouble("caffeine_100g"));
                        else
                            setCaffeine_100g(0f);

                        if(nutriments.has("copper_100g"))
                            setCopper_100g((float)nutriments.getDouble("copper_100g"));
                        else
                            setCopper_100g(0f);

                        if(nutriments.has("iodine_100g"))
                            setIodine_100g((float)nutriments.getDouble("iodine_100g"));
                        else
                            setIodine_100g(0f);

                        if(nutriments.has("manganese_100g"))
                            setManganese_100g((float)nutriments.getDouble("manganese_100g"));
                        else
                            setManganese_100g(0f);

                        if(nutriments.has("glucose_100g"))
                            setGlucose_100g((float)nutriments.getDouble("glucose_100g"));
                        else
                            setGlucose_100g(0f);

                        if(nutriments.has("fructose_100g"))
                            setFructose_100g((float)nutriments.getDouble("fructose_100g"));
                        else
                            setFructose_100g(0f);

                        if(nutriments.has("lactose_100g"))
                            setLactose_100g((float)nutriments.getDouble("lactose_100g"));
                        else
                            setLactose_100g(0f);

                        if(nutriments.has("lactose_100g"))
                            setLactose_100g((float)nutriments.getDouble("lactose_100g"));
                        else
                            setLactose_100g(0f);

                        if(nutriments.has("sucrose_100g"))
                            setSucrose_100g((float)nutriments.getDouble("sucrose_100g"));
                        else
                            setSucrose_100g(0f);

                        if(nutriments.has("alcohol_100g"))
                            setAlcohol_100g((float)nutriments.getDouble("alcohol_100g"));
                        else
                            setAlcohol_100g(0f);

                        if(nutriments.has("casein_100g"))
                            setCasein_100g((float)nutriments.getDouble("casein_100g"));
                        else
                            setCasein_100g(0f);

                        if(nutriments.has("maltose_100g"))
                            setMaltose_100g((float)nutriments.getDouble("maltose_100g"));
                        else
                            setMaltose_100g(0f);

                        if(nutriments.has("starch_100g"))
                            setStarch_100g((float)nutriments.getDouble("starch_100g"));
                        else
                            setStarch_100g(0f);

                        if(nutriments.has("taurine_100g"))
                            setTaurine_100g((float)nutriments.getDouble("taurine_100g"));
                        else
                            setTaurine_100g(0f);

                        if(nutriments.has("zinc_100g"))
                            setZinc_100g((float)nutriments.getDouble("zinc_100g"));
                        else
                            setZinc_100g(0f);

                        if(nutriments.has("proteins_100g"))
                            setProteins_100g((float)nutriments.getDouble("proteins_100g"));
                        else
                            setProteins_100g(0f);

                        if(nutriments.has("iron_100g"))
                            setIron_100g((float)nutriments.getDouble("iron_100g"));
                        else
                            setIron_100g(0f);

                        if(nutriments.has("sugars_100g"))
                            setSugars_100g((float)nutriments.getDouble("sugars_100g"));
                        else
                            setSugars_100g(0f);

                        if(nutriments.has("vitamin-a_100g"))
                            setVitamin_a_100g((float)nutriments.getDouble("vitamin-a_100g"));
                        else
                            setVitamin_a_100g(0f);

                        if(nutriments.has("vitamin-b1_100g"))
                            setVitamin_b1_100g((float)nutriments.getDouble("vitamin-b1_100g"));
                        else
                            setVitamin_b1_100g(0f);

                        if(nutriments.has("vitamin-b2_100g"))
                            setVitamin_b2_100g((float)nutriments.getDouble("vitamin-b2_100g"));
                        else
                            setVitamin_b2_100g(0f);

                        if(nutriments.has("vitamin-pp_100g"))
                            setVitamin_pp_100g((float)nutriments.getDouble("vitamin-pp_100g"));
                        else
                            setVitamin_pp_100g(0f);

                        if(nutriments.has("vitamin-b6_100g"))
                            setVitamin_b6_100g((float)nutriments.getDouble("vitamin-b6_100g"));
                        else
                            setVitamin_b6_100g(0f);

                        if(nutriments.has("vitamin-b9_100g"))
                            setVitamin_b9_100g((float)nutriments.getDouble("vitamin-b9_100g"));
                        else
                            setVitamin_b9_100g(0f);

                        if(nutriments.has("vitamin-b12_100g"))
                            setVitamin_b12_100g((float)nutriments.getDouble("vitamin-b12_100g"));
                        else
                            setVitamin_b12_100g(0f);

                        if(nutriments.has("vitamin-c_100g"))
                            setVitamin_c_100g((float)nutriments.getDouble("vitamin-c_100g"));
                        else
                            setVitamin_c_100g(0f);

                        if(nutriments.has("vitamin-d_100g"))
                            setVitamin_d_100g((float)nutriments.getDouble("vitamin-d_100g"));
                        else
                            setVitamin_d_100g(0f);

                        if(nutriments.has("vitamin-e_100g"))
                            setVitamin_e_100g((float)nutriments.getDouble("vitamin-e_100g"));
                        else
                            setVitamin_e_100g(0f);

                        if(nutriments.has("vitamin-k_100g"))
                            setVitamin_k_100g((float)nutriments.getDouble("vitamin-k_100g"));
                        else
                            setVitamin_k_100g(0f);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }

    public String nutrimentsToDBString(){
        String product = getCalcium_100g() + "#";
        product += getChloride_100g() + "#";
        product += getFluoride_100g() + "#";
        product += getMagnesium_100g() + "#";
        product += getPotassium_100g() + "#";
        product += getCholesterol_100g() + "#";
        product += getSalt_100g() + "#";
        product += getZinc_100g() + "#";
        product += getSodium_100g() + "#";
        product += getBiotin_100g() + "#";
        product += getCarbohydrates_100g() + "#";
        product += getEnergy_kcal_100g() + "#";
        product += getFiber_100g() + "#";
        product += getFat_100g() + "#";
        product += getSaturated_fat_100g() + "#";
        product += getTrans_fat_100g() + "#";
        product += getMonounsaturated_fat_100g() + "#";
        product += getPolyunsaturated_fat_100g() + "#";
        product += getOmega_3_fat_100g() + "#";
        product += getOmega_6_fat_100g() + "#";
        product += getOmega_9_fat_100g() + "#";
        product += getProteins_100g() + "#";
        product += getIron_100g() + "#";
        product += getCopper_100g() + "#";
        product += getManganese_100g() + "#";
        product += getIodine_100g() + "#";
        product += getCaffeine_100g() + "#";
        product += getTaurine_100g() + "#";
        product += getSugars_100g() + "#";
        product += getSucrose_100g() + "#";
        product += getGlucose_100g() + "#";
        product += getFructose_100g() + "#";
        product += getLactose_100g() + "#";
        product += getMaltose_100g() + "#";
        product += getStarch_100g() + "#";
        product += getCasein_100g() + "#";
        product += getAlcohol_100g() + "#";
        product += getVitamin_a_100g() + "#";
        product += getVitamin_b1_100g() + "#";
        product += getVitamin_b2_100g() + "#";
        product += getVitamin_b6_100g() + "#";
        product += getVitamin_b9_100g() + "#";
        product += getVitamin_b12_100g() + "#";
        product += getVitamin_c_100g() + "#";
        product += getVitamin_d_100g() + "#";
        product += getVitamin_e_100g() + "#";
        product += getVitamin_k_100g() + "#";
        product += getVitamin_pp_100g();

        return product;
    }

    public int getId() {
        return id;
    }

    public float getBiotin_100g() {
        return biotin_100g;
    }

    public float getCalcium_100g() {
        return calcium_100g;
    }

    public float getCarbohydrates_100g() {
        return carbohydrates_100g;
    }

    public float getChloride_100g() {
        return chloride_100g;
    }

    public float getCholesterol_100g() {
        return cholesterol_100g;
    }

    public float getEnergy_kcal_100g() {
        return energy_kcal_100g;
    }

    public float getFat_100g() {
        return fat_100g;
    }

    public float getFiber_100g() {
        return fiber_100g;
    }

    public float getFluoride_100g() {
        return fluoride_100g;
    }

    public float getIron_100g() {
        return iron_100g;
    }

    public float getMagnesium_100g() {
        return magnesium_100g;
    }

    public float getPotassium_100g() {
        return potassium_100g;
    }

    public float getProteins_100g() {
        return proteins_100g;
    }

    public float getSalt_100g() {
        return salt_100g;
    }

    public float getSaturated_fat_100g() {
        return saturated_fat_100g;
    }

    public float getSodium_100g() {
        return sodium_100g;
    }

    public float getSugars_100g() {
        return sugars_100g;
    }

    public float getTrans_fat_100g() {
        return trans_fat_100g;
    }

    public float getVitamin_a_100g() {
        return vitamin_a_100g;
    }

    public float getVitamin_b1_100g() {
        return vitamin_b1_100g;
    }

    public float getVitamin_b2_100g() {
        return vitamin_b2_100g;
    }

    public float getVitamin_b6_100g() {
        return vitamin_b6_100g;
    }

    public float getVitamin_b9_100g() {
        return vitamin_b9_100g;
    }

    public float getVitamin_b12_100g() {
        return vitamin_b12_100g;
    }

    public float getVitamin_c_100g() {
        return vitamin_c_100g;
    }

    public float getVitamin_d_100g() {
        return vitamin_d_100g;
    }

    public float getVitamin_e_100g() {
        return vitamin_e_100g;
    }

    public float getVitamin_pp_100g() {
        return vitamin_pp_100g;
    }

    public float getMonounsaturated_fat_100g() {
        return monounsaturated_fat_100g;
    }

    public float getCaffeine_100g() {
        return caffeine_100g;
    }

    public float getCopper_100g() {
        return copper_100g;
    }

    public float getIodine_100g() {
        return iodine_100g;
    }

    public float getManganese_100g() {
        return manganese_100g;
    }

    public float getOmega_3_fat_100g() {
        return omega_3_fat_100g;
    }

    public float getOmega_6_fat_100g() {
        return omega_6_fat_100g;
    }

    public float getOmega_9_fat_100g() {
        return omega_9_fat_100g;
    }

    public float getPolyunsaturated_fat_100g() {
        return polyunsaturated_fat_100g;
    }

    public float getTaurine_100g() {
        return taurine_100g;
    }

    public float getFructose_100g() {
        return fructose_100g;
    }

    public float getZinc_100g() {
        return zinc_100g;
    }

    public float getGlucose_100g() {
        return glucose_100g;
    }

    public float getSucrose_100g() {
        return sucrose_100g;
    }

    public float getAlcohol_100g() {
        return alcohol_100g;
    }

    public float getCasein_100g() {
        return casein_100g;
    }

    public float getLactose_100g() {
        return lactose_100g;
    }

    public float getMaltose_100g() {
        return maltose_100g;
    }

    public float getStarch_100g() {
        return starch_100g;
    }

    public float getVitamin_k_100g() {
        return vitamin_k_100g;
    }

    public String getName() {
        return name;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getBrands() {
        return brands;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBiotin_100g(float biotin_100g) {
        this.biotin_100g = biotin_100g;
    }

    public void setCalcium_100g(float calcium_100g) {
        this.calcium_100g = calcium_100g;
    }

    public void setCarbohydrates_100g(float carbohydrates_100g) {
        this.carbohydrates_100g = carbohydrates_100g;
    }

    public void setChloride_100g(float chloride_100g) {
        this.chloride_100g = chloride_100g;
    }

    public void setCholesterol_100g(float cholesterol_100g) {
        this.cholesterol_100g = cholesterol_100g;
    }

    public void setEnergy_kcal_100g(float energy_kcal_100g) {
        this.energy_kcal_100g = energy_kcal_100g;
    }

    public void setFat_100g(float fat_100g) {
        this.fat_100g = fat_100g;
    }

    public void setFiber_100g(float fiber_100g) {
        this.fiber_100g = fiber_100g;
    }

    public void setFluoride_100g(float fluoride_100g) {
        this.fluoride_100g = fluoride_100g;
    }

    public void setIron_100g(float iron_100g) {
        this.iron_100g = iron_100g;
    }

    public void setMagnesium_100g(float magnesium_100g) {
        this.magnesium_100g = magnesium_100g;
    }

    public void setPotassium_100g(float potassium_100g) {
        this.potassium_100g = potassium_100g;
    }

    public void setProteins_100g(float proteins_100g) {
        this.proteins_100g = proteins_100g;
    }

    public void setSalt_100g(float salt_100g) {
        this.salt_100g = salt_100g;
    }

    public void setSaturated_fat_100g(float saturated_fat_100g) {
        this.saturated_fat_100g = saturated_fat_100g;
    }

    public void setSodium_100g(float sodium_100g) {
        this.sodium_100g = sodium_100g;
    }

    public void setSugars_100g(float sugars_100g) {
        this.sugars_100g = sugars_100g;
    }

    public void setTrans_fat_100g(float trans_fat_100g) {
        this.trans_fat_100g = trans_fat_100g;
    }

    public void setCaffeine_100g(float caffeine_100g) {
        this.caffeine_100g = caffeine_100g;
    }

    public void setCopper_100g(float copper_100g) {
        this.copper_100g = copper_100g;
    }

    public void setIodine_100g(float iodine_100g) {
        this.iodine_100g = iodine_100g;
    }

    public void setManganese_100g(float manganese_100g) {
        this.manganese_100g = manganese_100g;
    }

    public void setGlucose_100g(float glucose_100g) {
        this.glucose_100g = glucose_100g;
    }

    public void setMonounsaturated_fat_100g(float monounsaturated_fat_100g) {
        this.monounsaturated_fat_100g = monounsaturated_fat_100g;
    }

    public void setFructose_100g(float fructose_100g) {
        this.fructose_100g = fructose_100g;
    }

    public void setOmega_3_fat_100g(float omega_3_fat_100g) {
        this.omega_3_fat_100g = omega_3_fat_100g;
    }

    public void setOmega_6_fat_100g(float omega_6_fat_100g) {
        this.omega_6_fat_100g = omega_6_fat_100g;
    }

    public void setOmega_9_fat_100g(float omega_9_fat_100g) {
        this.omega_9_fat_100g = omega_9_fat_100g;
    }

    public void setLactose_100g(float lactose_100g) {
        this.lactose_100g = lactose_100g;
    }

    public void setPolyunsaturated_fat_100g(float polyunsaturated_fat_100g) {
        this.polyunsaturated_fat_100g = polyunsaturated_fat_100g;
    }

    public void setSucrose_100g(float sucrose_100g) {
        this.sucrose_100g = sucrose_100g;
    }

    public void setAlcohol_100g(float alcohol_100g) {
        this.alcohol_100g = alcohol_100g;
    }

    public void setCasein_100g(float casein_100g) {
        this.casein_100g = casein_100g;
    }

    public void setMaltose_100g(float maltose_100g) {
        this.maltose_100g = maltose_100g;
    }

    public void setStarch_100g(float starch_100g) {
        this.starch_100g = starch_100g;
    }

    public void setTaurine_100g(float taurine_100g) {
        this.taurine_100g = taurine_100g;
    }

    public void setVitamin_k_100g(float vitamin_k_100g) {
        this.vitamin_k_100g = vitamin_k_100g;
    }

    public void setZinc_100g(float zinc_100g) {
        this.zinc_100g = zinc_100g;
    }

    public void setVitamin_a_100g(float vitamin_a_100g) {
        this.vitamin_a_100g = vitamin_a_100g;
    }

    public void setVitamin_b1_100g(float vitamin_b1_100g) {
        this.vitamin_b1_100g = vitamin_b1_100g;
    }

    public void setVitamin_b2_100g(float vitamin_b2_100g) {
        this.vitamin_b2_100g = vitamin_b2_100g;
    }

    public void setVitamin_b6_100g(float vitamin_b6_100g) {
        this.vitamin_b6_100g = vitamin_b6_100g;
    }

    public void setVitamin_b9_100g(float vitamin_b9_100g) {
        this.vitamin_b9_100g = vitamin_b9_100g;
    }

    public void setVitamin_b12_100g(float vitamin_b12_100g) {
        this.vitamin_b12_100g = vitamin_b12_100g;
    }

    public void setVitamin_c_100g(float vitamin_c_100g) {
        this.vitamin_c_100g = vitamin_c_100g;
    }

    public void setVitamin_d_100g(float vitamin_d_100g) {
        this.vitamin_d_100g = vitamin_d_100g;
    }

    public void setVitamin_e_100g(float vitamin_e_100g) {
        this.vitamin_e_100g = vitamin_e_100g;
    }

    public void setVitamin_pp_100g(float vitamin_pp_100g) {
        this.vitamin_pp_100g = vitamin_pp_100g;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setBrands(String brands) {
        this.brands = brands;
    }
}
