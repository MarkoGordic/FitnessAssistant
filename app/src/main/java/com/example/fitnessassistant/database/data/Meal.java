package com.example.fitnessassistant.database.data;

import java.util.List;

public class Meal {
    int id;
    int type;
    long date;
    List<Integer> productIDs;
    List<Float> quantity;

    public List<Float> getQuantity() {
        return quantity;
    }

    public int getId() {
        return id;
    }

    public List<Integer> getProductIDs() {
        return productIDs;
    }

    public int getType() {
        return type;
    }

    public long getDate() {
        return date;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setProductIDs(List<Integer> productIDs) {
        this.productIDs = productIDs;
    }

    public void setQuantity(List<Float> quantity) {
        this.quantity = quantity;
    }
}
