package com.example.fitnessassistant.pedometer;

import static com.example.fitnessassistant.uiprefs.LocaleExt.toLangIfDiff;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

import com.example.fitnessassistant.R;

// TODO: me - fix design, check for signed out user
// TODO: Gordic - stopService if user is signed out
// TODO: me - stopService onButtonClick
//  add step goal
//  add questions...

public class PedometerWidget extends AppWidgetProvider {
    private SharedPreferences sharedPreferences;

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        appWidgetManager.updateAppWidget(appWidgetId, getRemoteViews(context, appWidgetManager.getAppWidgetOptions(appWidgetId).getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public void onEnabled(Context context){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("PedometerWidget", true);
        editor.apply();
    }

    public void onDisabled(Context context){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("PedometerWidget", false);
        editor.apply();
    }

    // handling resize for samsung devices
    private void handleResize(Context context, Intent intent){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        int appWidgetId = intent.getIntExtra("widgetId", 0);
        int widgetSpanX = intent.getIntExtra("widgetspanx",0);
        int widgetSpanY = intent.getIntExtra("widgetspany", 0);

        if(appWidgetId > 0 && widgetSpanX > 0 && widgetSpanY > 0){
            Bundle newOptions = new Bundle();

            newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,widgetSpanX * 74);
            newOptions.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT,widgetSpanY * 74);

            onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        }
    }

    // handling resize for samsung devices
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(intent.getAction().contentEquals("com.sec.android.widgetapp.APPWIDGET_RESIZE")){
            handleResize(context, intent);
        }
    }

    private static void updateWidgetLang(RemoteViews views, Context context, String lang){
        Context newContext = toLangIfDiff(context, lang, false);
        views.setTextViewText(R.id.widgetHeader, newContext.getText(R.string.steps));
        views.setTextViewText(R.id.stepsTV, newContext.getText(R.string.steps_small));
        views.setTextViewText(R.id.weeklyAverageTextView, newContext.getText(R.string.weekly_average));
        views.setTextViewText(R.id.averageStepsTV, newContext.getText(R.string.steps_small));
        views.setTextViewText(R.id.stepStreakTextView, newContext.getText(R.string.step_streak));
        views.setTextViewText(R.id.dayStreakTV, newContext.getText(R.string.days));
    }

    // gettingRemoteView based on changing screen size
    private static RemoteViews getRemoteViews(Context context, int height){
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.pedometer_widget);

        updateWidgetLang(views, context, PreferenceManager.getDefaultSharedPreferences(context).getString("langPref", "sys"));

        views.setImageViewResource(R.id.widgetImageLarge, R.drawable.mail_focused);
        views.setImageViewResource(R.id.streakImageView, R.drawable.gear);
        views.setImageViewResource(R.id.widgetImageSmall, R.drawable.mail_focused);

        if(height >= 220){
            views.setViewVisibility(R.id.weeklyAverageTextView, View.VISIBLE);
            views.setViewVisibility(R.id.horizontalLay, View.VISIBLE);
            views.setViewVisibility(R.id.stepStreakTextView, View.VISIBLE);
            views.setViewVisibility(R.id.horizontalLay2, View.VISIBLE);

            views.setViewVisibility(R.id.widgetImageLarge, View.VISIBLE);
            views.setViewVisibility(R.id.widgetImageSmall, View.GONE);
        } else{
            views.setViewVisibility(R.id.weeklyAverageTextView, View.GONE);
            views.setViewVisibility(R.id.horizontalLay, View.GONE);
            views.setViewVisibility(R.id.stepStreakTextView, View.GONE);
            views.setViewVisibility(R.id.horizontalLay2, View.GONE);

            views.setViewVisibility(R.id.widgetImageLarge, View.GONE);
            views.setViewVisibility(R.id.widgetImageSmall, View.VISIBLE);
        }

        return views;
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        appWidgetManager.updateAppWidget(appWidgetId, getRemoteViews(context, newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)));
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }


}