package com.example.fitnessassistant.database.data;

public class ProductSearch {
    int id;
    String name;
    String brands;

    public ProductSearch(){}

    public ProductSearch(int id, String name, String brands){
        this.id = id;
        this.name = name;
        this.brands = brands;
    }

    public int getId() {
        return id;
    }

    public String getBrands() {
        return brands;
    }

    public String getName() {
        return name;
    }

    public void setBrands(String brands) {
        this.brands = brands;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
