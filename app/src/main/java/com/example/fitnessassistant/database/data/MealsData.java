package com.example.fitnessassistant.database.data;

import java.util.ArrayList;
import java.util.List;

public class MealsData {
    List<String> data = new ArrayList<>();

    public void setData(List<String> data){
        this.data = data;
    }

    public void setDataDB(List<Meal> data) {
        List<String> newData = new ArrayList<>();

        for(int i = 0; i < data.size(); i++){
            StringBuilder meal = new StringBuilder(data.get(i).getDate() + "#");
            meal.append(data.get(i).getType());
            meal.append("#");
            List<Integer> products = data.get(i).getProductIDs();
            List<Float> quantities = data.get(i).getQuantity();
            for(int j = 0; j < products.size(); j++){
                meal.append(products.get(j).toString());
                meal.append("#");
                meal.append(quantities.get(j).toString());
                if(j != products.size() - 1)
                    meal.append("#");
            }
            newData.add(String.valueOf(meal));
        }

        this.data = newData;
    }

    public List<String> getData() {
        return data;
    }
}
