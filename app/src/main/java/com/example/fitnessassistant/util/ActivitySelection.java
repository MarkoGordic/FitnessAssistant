package com.example.fitnessassistant.util;

import android.graphics.drawable.Drawable;

public class ActivitySelection {
    private String name;
    private Drawable drawable;

    public ActivitySelection(String name, Drawable drawable) {
        this.name = name;
        this.drawable = drawable;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
