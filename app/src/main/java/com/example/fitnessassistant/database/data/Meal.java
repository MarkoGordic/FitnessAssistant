package com.example.fitnessassistant.database.data;

public class Meal {
    int id;
    String name;
    int type;
    long date;
    int productID;
    float quantity;

    public float getQuantity() {
        return quantity;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getProductID() {
        return productID;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }
}
