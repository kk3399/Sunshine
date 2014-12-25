package com.example.krishnadamarla.sunshine.backgroundtasks;

import android.os.AsyncTask;
import android.util.Log;

import com.example.krishnadamarla.sunshine.WeatherAPIClient;
import com.example.krishnadamarla.sunshine.helpers.WeatherJsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by krishnadamarla on 12/23/14.
 */
public class FetchForecast extends AsyncTask<String, Void, String[]>
{
    @Override
    protected String[] doInBackground(String... params) {
        String weatherJson = WeatherAPIClient.GetForecastForAWeek(params[0]);
        try
        {
            return WeatherJsonParser.getWeatherDataFromJson(weatherJson, 7);
        }
        catch(JSONException je)
        { Log.e("JSON Exception","exception while converting results to json", je);}
        return  null;
    }
}