package com.example.fitnessassistant.util;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Date;

public class TimeFunctional {

    public static String getCurrentDateFormatted(){
        return (String) DateFormat.format("yyyyMMdd", new Date());
    }

    public static String getMonthShort(int numOfMonth){
        switch(numOfMonth){
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Aug";
            case 9:
                return "Sep";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
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
