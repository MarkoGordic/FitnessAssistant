package com.example.fitnessassistant.diary;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.fitnessassistant.R;

public class StringSpinnerAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] selections;

    public StringSpinnerAdapter(Context context, int textViewResourceId, String[] selections){
        super(context, textViewResourceId, selections);
        this.context = context;
        this.selections = selections;
    }

    @Override
    public int getCount() {
        return selections.length;
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return selections[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView item = (TextView) super.getView(position, convertView, parent);
        item.setText(selections[position]);
        return item;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView item = (TextView) super.getView(position, convertView, parent);

        item.setBackground(AppCompatResources.getDrawable(context, R.drawable.meal_spinner_dropdown_border));
        item.setText(selections[position]);

        return item;
    }
}
