package com.example.krishnadamarla.sunshine.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.krishnadamarla.sunshine.R;
import com.example.krishnadamarla.sunshine.data.WeatherContract;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by krishnadamarla on 12/27/14.
 */
public class Utility {
    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_general_location_key),
                context.getString(R.string.pref_general_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_general_temp_units_key),
                context.getString(R.string.pref_general_temp_units_metric))
                .equals(context.getString(R.string.pref_general_temp_units_metric));
    }

    public static String formatTemperature(double temperature, boolean isMetric) {
        double temp;
        if ( !isMetric ) {
            temp = 9*temperature/5+32;
        } else {
            temp = temperature;
        }
        return String.format("%.0f", temp);
    }

    public static String formatDate(String dateString) {
        Date date = WeatherContract.getDateFromDb(dateString);
        return DateFormat.getDateInstance().format(date);
    }
}
