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

    public static String[] getLast7DatesFromEnd(){
        String[] last7dates = new String[7];

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -7);

        for(int i = 1; i <= 7; i++){
            cal.add(Calendar.DAY_OF_YEAR, 1);
            last7dates[7 - i] = (String) DateFormat.format("yyyyMMdd", cal.getTime());
        }

        return last7dates;
    }
}
