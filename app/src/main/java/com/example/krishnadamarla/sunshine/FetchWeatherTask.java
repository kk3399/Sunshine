package com.example.krishnadamarla.sunshine;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.krishnadamarla.sunshine.data.WeatherContract;
import com.example.krishnadamarla.sunshine.helpers.WeatherJsonParser;

import org.json.JSONException;

/**
 * Created by krishnadamarla on 12/27/14.
 */
public class FetchWeatherTask extends AsyncTask<String, Void, Void>
{
    private Context _context;
    public static final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    public FetchWeatherTask(Context context)
    {
        _context = context;
    }

    public static long addLocation(Context context, String locationSetting, String cityName, double lat, double lon)
    {
        long locationId;
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(WeatherContract.LocationEntry.CONTENT_URI,
                    new String[]{WeatherContract.LocationEntry._ID},
                    WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                    new String[]{locationSetting},
                    null);
        if (cursor.moveToNext())
        {
            Log.v(LOG_TAG, "location already exists " + locationSetting);
            locationId = cursor.getLong(0);
            cursor.close();
        }
        else
        {
            cursor.close();
            ContentValues values = new ContentValues();
            values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            values.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, lat);
            values.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, lon);
            Uri locationUri =  resolver.insert(WeatherContract.LocationEntry.CONTENT_URI, values);
            cursor.close();
            locationId = ContentUris.parseId(locationUri);
        }
        return locationId;
    }

    public static int addWeather(Context context, ContentValues[] values)
    {
        Log.v(LOG_TAG,"addWeather is getting called");
        ContentResolver resolver = context.getContentResolver();
        return resolver.bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, values);
    }

    @Override
    protected Void doInBackground(String... params) {
        if(params.length == 0)
            return null;

        String weatherJson = WeatherAPIClient.GetForecastForAWeek(getWeatherMapAPI(params[0], 14));
        if (weatherJson == null || weatherJson.isEmpty())
            return null;

        try
        {
            WeatherJsonParser.getWeatherDataFromJson(weatherJson, params[0], _context);
        }
        catch(JSONException je)
        { Log.e("JSON Exception", "exception while converting results to json", je);}
        return  null;
    }

    private String getWeatherMapAPI(String zipCode, int numOfDays)
    {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http");
        builder.authority("api.openweathermap.org");
        builder.appendPath("data");
        builder.appendPath("2.5");
        builder.appendPath("forecast");
        builder.appendPath("daily");
        builder.appendQueryParameter("q", zipCode);
        builder.appendQueryParameter("mode", "json");
        builder.appendQueryParameter("units", "metric");
        builder.appendQueryParameter("cnt",Integer.toString(numOfDays));
        return builder.build().toString();
    }


}
