package com.example.fitnessassistant.uiprefs;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;

import java.util.Locale;

// class used for changing the in-app language
public class LocaleExt {
    private static final String SYSTEM_LANG = "sys";

    // checks if language is what it should be
    private static boolean isAppLangDiff(Context context, String prefLang) {
        Configuration appConfig = context.getResources().getConfiguration();
        Configuration sysConfig = Resources.getSystem().getConfiguration();
        String appLang = appConfig.getLocales().get(0).getDisplayLanguage();
        String sysLang = sysConfig.getLocales().get(0).getDisplayLanguage();

        if(prefLang.equals(SYSTEM_LANG)){
            return !appLang.equals(sysLang);
        } else{
            return !appLang.equals(prefLang);
        }
    }

    // changes language if it's not what it should be
    public static Context toLangIfDiff(Context context, String lang) {
        if(isAppLangDiff(context, lang)) {
            return toLang(context, lang);
        } else
            return context;
    }

    // changes language
    private static Context toLang(Context context, String toLang) {
        Configuration config = context.getResources().getConfiguration();

        Locale toLocale = langToLocale(toLang);
        Locale.setDefault(toLocale);
        config.setLocale(toLocale);

        LocaleList localeList = new LocaleList(toLocale);
        LocaleList.setDefault(localeList);
        config.setLocales(localeList);

        config.setLayoutDirection(toLocale);

        return context.createConfigurationContext(config);
    }

    // gets locale based on language
    private static Locale langToLocale(String toLang) {
        if(toLang.equals(SYSTEM_LANG))
            return Resources.getSystem().getConfiguration().getLocales().get(0);
        else
            return new Locale(toLang);
    }
}
