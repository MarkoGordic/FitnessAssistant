package com.example.fitnessassistant.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.util.ActivitySelection;

public class SpinnerAdapter extends ArrayAdapter<ActivitySelection> {
    private final Context context;
    private final ActivitySelection[] activitySelections;

    public SpinnerAdapter(Context context, int textViewResourceId, ActivitySelection[] activitySelections){
        super(context, textViewResourceId, activitySelections);
        this.context = context;
        this.activitySelections = activitySelections;
    }

    @Override
    public int getCount() {
        return activitySelections.length;
    }

    @Nullable
    @Override
    public ActivitySelection getItem(int position) {
        return activitySelections[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView item = (TextView) super.getView(position, convertView, parent);

        item.setText(activitySelections[position].getName());
        item.setCompoundDrawablesWithIntrinsicBounds(activitySelections[position].getDrawable(), null, null, null);

        return item;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        TextView item = (TextView) super.getView(position, convertView, parent);

        item.setBackground(AppCompatResources.getDrawable(context, R.drawable.spinner_dropdown_border));
        item.setText(activitySelections[position].getName());
        item.setCompoundDrawablesWithIntrinsicBounds(activitySelections[position].getDrawable(), null, null, null);

        return item;
    }
}
