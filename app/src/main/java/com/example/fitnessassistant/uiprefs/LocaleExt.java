package com.example.fitnessassistant.uiprefs;

import static com.example.fitnessassistant.util.TimeFunctional.getCurrentDateFormatted;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;

import com.example.fitnessassistant.R;
import com.example.fitnessassistant.database.mdbh.MDBHPedometer;
import com.example.fitnessassistant.pedometer.Pedometer;
import com.example.fitnessassistant.pedometer.PedometerWidget;
import com.example.fitnessassistant.pedometer.StepGoalFragment;
import com.example.fitnessassistant.sleeptracker.SleepTracker;
import com.example.fitnessassistant.util.ServiceFunctional;

import java.util.Locale;

// class used for changing the in-app language
public class LocaleExt {
    private static final String SYSTEM_LANG = "sys";

    // checks if language is what it should be
    public static boolean isAppLangDiff(Context context, String prefLang) {
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
    public static Context toLangIfDiff(Context context, String lang, boolean updateWidgets, boolean updateNotifications) {
        if(isAppLangDiff(context, lang)) {
            return toLang(context, lang, updateWidgets, updateNotifications);
        } else
            return context;
    }

    // changes language
    private static Context toLang(Context context, String toLang, boolean updateWidgets, boolean updateNotifications) {
        Configuration config = context.getResources().getConfiguration();

        Locale toLocale = langToLocale(toLang);
        Locale.setDefault(toLocale);
        config.setLocale(toLocale);

        LocaleList localeList = new LocaleList(toLocale);
        LocaleList.setDefault(localeList);
        config.setLocales(localeList);

        config.setLayoutDirection(toLocale);

        Context newContext = context.createConfigurationContext(config);

        if(updateWidgets) {
            for (int id : AppWidgetManager.getInstance(newContext).getAppWidgetIds(new ComponentName(newContext, PedometerWidget.class))) {
                PedometerWidget.updateAppWidget(newContext, AppWidgetManager.getInstance(newContext), id, AppWidgetManager.getInstance(context).getAppWidgetOptions(id).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT));
            }
            Pedometer.updatePedometerWidgetData(newContext ,((int) MDBHPedometer.getInstance(newContext).readPedometerSteps(getCurrentDateFormatted())), null);
        }

        if(updateNotifications) {
            if (ServiceFunctional.getPedometerShouldRun(newContext))
                Pedometer.pushPedometerNotification(newContext, ((int) MDBHPedometer.getInstance(newContext).readPedometerSteps(getCurrentDateFormatted())) + " " + newContext.getString(R.string.steps_small), newContext.getString(R.string.your_today_goal) + " " + StepGoalFragment.getStepGoalForToday(newContext) + ".");
            if (ServiceFunctional.getSleepTrackerShouldRun(newContext))
                SleepTracker.pushSleepTrackerNotification(newContext);
        }
        return newContext;
    }

    // gets locale based on language
    private static Locale langToLocale(String toLang) {
        if(toLang.equals(SYSTEM_LANG))
            return Resources.getSystem().getConfiguration().getLocales().get(0);
        else
            return new Locale(toLang);
    }
}
