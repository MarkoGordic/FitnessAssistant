package com.example.fitnessassistant.uiprefs;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

// class used for applying the theme
public class ColorMode {
    public static final String NONE = "none";
    public static final String DARK_MODE = "dark_mode";
    public static final String LIGHT_MODE = "light_mode";
    public static String ACTIVE_MODE = null;

    // used to apply the given theme/color mode (dark/light)
    public static void applyColorMode(Activity activity, String mode){
        SharedPreferences preferences = activity.getSharedPreferences("ui_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if(ACTIVE_MODE == null){
            mode = preferences.getString("mode", NONE);

            if (mode.equals(NONE)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY);

                switch (activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        mode = DARK_MODE;
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                    case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    default:
                        mode = LIGHT_MODE;
                }
            }

            editor.putString("mode", mode);
            ACTIVE_MODE = mode;
            editor.apply();
        } else if (mode != null && !ACTIVE_MODE.equals(mode)) {
            editor.putString("mode", mode);
            ACTIVE_MODE = mode;
            editor.apply();
        }

        if (ACTIVE_MODE.equals(DARK_MODE))
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
