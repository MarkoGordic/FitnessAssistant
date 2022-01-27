package com.example.fitnessassistant.database.data;

import com.example.fitnessassistant.nutritiontracker.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductsData {
    List<String> data = new ArrayList<>();

    public List<String> getData() {
        return data;
    }

    public void setData(List<Product> data) {
        List<String> newData = new ArrayList<>();

        for(int i = 0; i < data.size(); i++) {
            StringBuilder product = new StringBuilder();
            product.append(data.get(i).getId() + '#');
            product.append(data.get(i).getName()).append('#');
            product.append(data.get(i).getBrands()).append('#');
            product.append(data.get(i).getBarcode()).append('#');
            product.append(data.get(i).nutrimentsToDBString());
        }

        this.data = newData;
    }
}
