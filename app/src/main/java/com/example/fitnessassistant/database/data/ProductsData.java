package com.example.fitnessassistant.database.data;

import com.example.fitnessassistant.nutritiontracker.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductsData {
    List<String> data = new ArrayList<>();

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data){
        this.data = data;
    }

    public void setDataDB(List<Product> data) {
        List<String> newData = new ArrayList<>();

        for(int i = 0; i < data.size(); i++) {
            System.out.println(data.get(i).getName() + data.get(i).getId() + " DEKI SMEKI");
            String product =
                    data.get(i).getId() + "#" +
                    data.get(i).getName() + "#" +
                    data.get(i).getBrands() + "#" +
                    data.get(i).getBarcode() + "#" +
                    data.get(i).nutrimentsToDBString();
            System.out.println(product + "DEKISA");
            newData.add(product);
        }

        this.data = newData;
    }
}
