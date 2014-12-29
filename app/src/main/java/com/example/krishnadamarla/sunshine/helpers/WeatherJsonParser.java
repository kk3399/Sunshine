package com.example.krishnadamarla.sunshine.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.krishnadamarla.sunshine.FetchWeatherTask;
import com.example.krishnadamarla.sunshine.R;
import com.example.krishnadamarla.sunshine.data.WeatherContract;

import org.apache.http.conn.scheme.PlainSocketFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Created by krishnadamarla on 12/25/14.
 */
public final class WeatherJsonParser {

    public static final String LOG_TAG = WeatherJsonParser.class.getSimpleName();

    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    public static String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    public static String formatHighLows(double high, double low, String tempUnits, String defaultTempUnits) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);
        if (tempUnits != null && !tempUnits.isEmpty() && !tempUnits.equals(defaultTempUnits))
        {
            roundedHigh = (9 * roundedHigh)/5 + 32;
            roundedLow = (9 * roundedLow)/5 + 32;
        }

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    public static void getWeatherDataFromJson(String forecastJsonStr, String locationSetting, Context context)
            throws JSONException {

        String defaultTempUnits = context.getString(R.string.pref_general_temp_units_metric);
        String tempUnits = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_general_temp_units_key), defaultTempUnits);


        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";
        final String OWM_COORD_LAT = "lat";
        final String OWM_COORD_LONG = "lon";

        // Weather information.  Each day's forecast info is an element of the "list" array.
        final String OWM_LIST = "list";

        final String OWM_DATETIME = "dt";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        // All temperatures are children of the "temp" object.
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        String cityName = cityJson.getString(OWM_CITY_NAME);
        JSONObject coordJSON = cityJson.getJSONObject(OWM_COORD);
        double cityLatitude = coordJSON.getLong(OWM_COORD_LAT);
        double cityLongitude = coordJSON.getLong(OWM_COORD_LONG);


        long dateTime;
        double pressure;
        int humidity;
        double windSpeed;
        double windDirection;

        double high;
        double low;

        String description;
        int weatherId;

        long locationID = FetchWeatherTask.addLocation(context, locationSetting, cityName, cityLatitude, cityLongitude);
        Vector<ContentValues> contentValuesVector = new Vector<ContentValues>();
        for(int i = 0; i < weatherArray.length(); i++) {


            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            dateTime = dayForecast.getLong(OWM_DATETIME);

            pressure = dayForecast.getDouble(OWM_PRESSURE);
            humidity = dayForecast.getInt(OWM_HUMIDITY);
            windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

            // Description is in a child array called "weather", which is 1 element long.
            // That element also contains a weather code.
            JSONObject weatherObject =
                    dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = temperatureObject.getDouble(OWM_MAX);
            low = temperatureObject.getDouble(OWM_MIN);

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationID);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                    WeatherContract.getDbDateString(new Date(dateTime * 1000L)));
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);
            contentValuesVector.add(weatherValues);
        }

        Log.v(LOG_TAG,"Vector size:" + contentValuesVector.size());

        if(contentValuesVector.size() > 0) {
            ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
            contentValuesVector.toArray(contentValuesArray);
            int bulkInsertRowCount;
            if ((bulkInsertRowCount = FetchWeatherTask.addWeather(context, contentValuesArray)) > 0)
            {
                Log.v(LOG_TAG, "Bulk insert row count: "+ bulkInsertRowCount);
            }
            else
                Log.e(LOG_TAG, "Bulk insert failed");
        }
    }


}
