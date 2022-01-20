package com.example.fitnessassistant.util;

import android.content.Context;
import android.text.format.DateFormat;

import com.example.fitnessassistant.R;

import java.util.Calendar;
import java.util.Date;

public class TimeFunctional {

    public static String getCurrentDateFormatted(){
        return (String) DateFormat.format("yyyyMMdd", new Date());
    }

    public static String getMonthShort(Context context, int numOfMonth){
        switch(numOfMonth){
            case 1:
                return context.getString(R.string.jan);
            case 2:
                return context.getString(R.string.feb);
            case 3:
                return context.getString(R.string.mar);
            case 4:
                return context.getString(R.string.apr);
            case 5:
                return context.getString(R.string.may);
            case 6:
                return context.getString(R.string.jun);
            case 7:
                return context.getString(R.string.jul);
            case 8:
                return context.getString(R.string.aug);
            case 9:
                return context.getString(R.string.sep);
            case 10:
                return context.getString(R.string.oct);
            case 11:
                return context.getString(R.string.nov);
            case 12:
                return context.getString(R.string.dec);
            default:
                return null;
        }
    }

    public static String getMonthLong(Context context, int numOfMonth){
        switch(numOfMonth){
            case 1:
                return context.getString(R.string.jan_long);
            case 2:
                return context.getString(R.string.feb_long);
            case 3:
                return context.getString(R.string.mar_long);
            case 4:
                return context.getString(R.string.apr_long);
            case 5:
                return context.getString(R.string.may_long);
            case 6:
                return context.getString(R.string.jun_long);
            case 7:
                return context.getString(R.string.jul_long);
            case 8:
                return context.getString(R.string.aug_long);
            case 9:
                return context.getString(R.string.sep_long);
            case 10:
                return context.getString(R.string.oct_long);
            case 11:
                return context.getString(R.string.nov_long);
            case 12:
                return context.getString(R.string.dec_long);
            default:
                return null;
        }
    }

    public static String[] getPastDaysInTheWeek(){
        Calendar cal = Calendar.getInstance();
        String[] last7dates;
        int numOfDays;

        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:
                numOfDays = 7;
                break;
            case Calendar.SATURDAY:
            case Calendar.FRIDAY:
            case Calendar.THURSDAY:
            case Calendar.WEDNESDAY:
            case Calendar.MONDAY:
                numOfDays = cal.get(Calendar.DAY_OF_WEEK) - 1;
                break;
            default:
                numOfDays = 0;
        }

        last7dates = new String[numOfDays];
        cal.add(Calendar.DAY_OF_YEAR, 1 - numOfDays);

        for(int i = 0; i < numOfDays; i++){
            last7dates[i] = (String) DateFormat.format("yyyyMMdd", cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        return last7dates;
    }
}
