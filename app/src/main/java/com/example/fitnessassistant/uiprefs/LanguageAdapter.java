package com.example.fitnessassistant.uiprefs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.fitnessassistant.R;

import java.util.List;

public class LanguageAdapter extends ArrayAdapter<String> {
    private final List<String> languages;
    private final Context ctx;

    public LanguageAdapter(@NonNull Context context, @NonNull List<String> list) {
        super(context, 0, list);
        ctx = context;
        languages = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = LayoutInflater.from(ctx).inflate(R.layout.language_item, parent, false);

        // used for LocaleSelectionActivity
        if(position == LocaleSelectionActivity.selectedPosition) {
            Drawable d = AppCompatResources.getDrawable(ctx, R.drawable.caret_left);
            if(d != null)
                d.setTint(ctx.getColor(R.color.SpaceCadet));
            ((ImageView) view.findViewById(R.id.drawableItem)).setImageDrawable(d);
        } else
            ((ImageView) view.findViewById(R.id.drawableItem)).setImageDrawable(null);

        String lang = languages.get(position);

        ((TextView) view.findViewById(R.id.languageItem)).setText(lang);

        return view;
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return languages.get(position);
    }
}
