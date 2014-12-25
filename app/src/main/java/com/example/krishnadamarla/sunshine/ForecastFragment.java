package com.example.krishnadamarla.sunshine;

import android.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.krishnadamarla.sunshine.backgroundtasks.FetchForecast;

import java.util.ArrayList;

/**
 * Created by krishnadamarla on 12/22/14.
 */
 public class ForecastFragment extends Fragment {

    private ArrayAdapter arrayAdapter;
    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_refresh)
        {
            FetchForecast fetchForecastTask = new FetchForecast();
            fetchForecastTask.execute(GetWeatherMapAPI("32092"));
            String[] forecastArray = null;
            try
            {
                forecastArray = fetchForecastTask.get();
                arrayAdapter = new ArrayAdapter(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview,forecastArray);

            }
            catch (Exception e)
            {
                Log.e("ForecastFragment","exception getting results from FetchForecast async task", e);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        FetchForecast fetchForecastTask = new FetchForecast();
        fetchForecastTask.execute(GetWeatherMapAPI("60563"));
        String[] forecastArray = null;
        try
        {
           forecastArray = fetchForecastTask.get();

        }
        catch (Exception e)
        {
            Log.e("ForecastFragment","exception getting results from FetchForecast async task", e);
        }

        arrayAdapter = new ArrayAdapter(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview,forecastArray);
        ListView forecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        forecastListView.setAdapter(arrayAdapter);
        return rootView;
    }

    private String GetWeatherMapAPI(String zipCode)
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
        builder.appendQueryParameter("cnt","7");
        return builder.build().toString();
    }
 }

