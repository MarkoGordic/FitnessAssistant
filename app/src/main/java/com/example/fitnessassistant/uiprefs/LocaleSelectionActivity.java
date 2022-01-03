package com.example.fitnessassistant.uiprefs;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.profile.SettingsFragment;

import java.util.ArrayList;
import java.util.Locale;

public class LocaleSelectionActivity extends AppCompatActivity {
    // used to change the look of the item selected in LanguageAdapter
    public static int selectedPosition = -1;

    private void setUpOnClickListeners(){
        ((AppCompatImageView) findViewById(R.id.drawableView)).setImageResource(R.drawable.world);
        ((TextView) findViewById(R.id.headerView)).setText(R.string.select_a_language);

        // sets text with emojis
        String serbian = String.format("%s  %s", SettingsFragment.localeToEmoji(new Locale("sr", "RS")), getString(R.string.serbian));
        String english = String.format("%s  %s", SettingsFragment.localeToEmoji(new Locale("en", "GB")), getString(R.string.english));

        ListView listView = findViewById(R.id.languagesList);
        ArrayList<String> languageList = new ArrayList<>();

        languageList.add(serbian);
        languageList.add(english);

        selectedPosition = listView.getFirstVisiblePosition();
        LanguageAdapter languageAdapter = new LanguageAdapter(getApplicationContext(), languageList);

        listView.setAdapter(languageAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedPosition = position;
            languageAdapter.notifyDataSetChanged();
        });

        findViewById(R.id.proceedButton).setOnClickListener(v -> {
            String languageSelected = languageAdapter.getItem(selectedPosition);

            // prepares dialogs based on country/language chosen
            if(languageSelected != null) {
                if (languageSelected.equals(serbian)) {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("langPref", "sr").apply();
                    SettingsFragment.restartApp(getApplicationContext(), 500);
                } else if (languageSelected.equals(english)) {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("langPref", "en").apply();
                    SettingsFragment.restartApp(getApplicationContext(), 500);
                }
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.locale_selection_screen);
        setUpOnClickListeners();
    }
}
