package com.example.krishnadamarla.sunshine;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.krishnadamarla.sunshine.helpers.WeatherJsonParser;

import org.json.JSONException;

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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        arrayAdapter = new ArrayAdapter(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview);
        new FetchForecast().execute(GetWeatherMapAPI("60563"));
        ListView forecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        forecastListView.setAdapter(arrayAdapter);
        forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentItemForecast = (String) arrayAdapter.getItem(position);
                //Toast.makeText(getActivity(), currentItemForecast, Toast.LENGTH_LONG).show();
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                detailIntent.putExtra(Intent.EXTRA_TEXT, currentItemForecast);
                startActivity(detailIntent);
            }
        });
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

        @Override
        protected void onPostExecute(String[] strings) {
            try{
                if (strings == null) return;
                arrayAdapter.clear();
                arrayAdapter.addAll(strings);
            }
            catch (Exception e)
            {
                Log.e("OnPostExecute","binding results", e);
            }

        }
    }
 }

