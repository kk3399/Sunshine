package com.example.krishnadamarla.sunshine;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
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
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.krishnadamarla.sunshine.data.WeatherContract;
import com.example.krishnadamarla.sunshine.helpers.Utility;
import com.example.krishnadamarla.sunshine.sync.SunshineSyncAdapter;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by krishnadamarla on 12/22/14.
 */
 public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ForecastAdapter forecastAdapter;

    private String _Location;
    public static final int FORECAST_LOADER = 0;
    private int _Position = 0;
    private ListView _ListView;
    public static final String SELECTED_POSITION = "selectedPosition";
    private boolean mUseTodayLayout = false;

    public static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    //these indices are tied to FORECAST_COLUMNS. If FORECAST_COLUMNS change these should change
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MIN_TEMP = 3;
    public static final int COL_WEATHER_MAX_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_ID_API = 6;

    public ForecastFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER,null,this);
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

    public void setUseTodayLayout(boolean useTodayLayout)
    {
        mUseTodayLayout = useTodayLayout;
        if (forecastAdapter != null)
            forecastAdapter.setUseTodayLayout(useTodayLayout);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_refresh)
        {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if(_Position != ListView.INVALID_POSITION)
        {
            outState.putInt(SELECTED_POSITION, _Position);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        forecastAdapter = new ForecastAdapter(getActivity(),null, 0);
        forecastAdapter.setUseTodayLayout(mUseTodayLayout);

        _ListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        _ListView.setAdapter(forecastAdapter);
        _ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                _Position = position;
                Cursor cursor = ((CursorAdapter) parent.getAdapter()).getCursor();
                if (cursor != null && cursor.moveToPosition(position))
                {
//                    String currentItemForecast = String.format("%s - %s - %s/%s",
//                            Utility.formatDate(cursor.getString(COL_WEATHER_DATE)),
//                            cursor.getString(COL_WEATHER_DESC),
//                            Utility.formatTemperature(cursor.getDouble(COL_WEATHER_MAX_TEMP), Utility.isMetric(getActivity())),
//                            Utility.formatTemperature(cursor.getDouble(COL_WEATHER_MIN_TEMP),Utility.isMetric(getActivity())));
                    String currentItemForecast = cursor.getString(COL_WEATHER_DATE);
                    //Toast.makeText(getActivity(), currentItemForecast, Toast.LENGTH_LONG).show();


                    ((Callback) getActivity()).onItemSelected(currentItemForecast);


                }
            }
        });

        if (savedInstanceState != null  && savedInstanceState.containsKey(SELECTED_POSITION))
        {
            _Position =  savedInstanceState.getInt(SELECTED_POSITION);
        }
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(_Location !=null && !_Location.equals(Utility.getPreferredLocation(getActivity())))
        {
            getLoaderManager().restartLoader(FORECAST_LOADER, null,this);
        }
    }

    private void updateWeather()
    {
        String location = Utility.getPreferredLocation(getActivity());
        Toast.makeText(getActivity(),location, Toast.LENGTH_SHORT).show();
        //new FetchWeatherTask(getActivity()).execute(location);

//        Intent serviceIntent = new Intent(getActivity(), SunshineService.class);
//        serviceIntent.putExtra(SunshineService.LOCATION_STR_INPUT, location);
//        getActivity().startService(serviceIntent);
//
//        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
//        Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
//        alarmIntent.putExtra(SunshineService.LOCATION_STR_INPUT, location);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
//        alarmManager.set(AlarmManager.RTC_WAKEUP,
//                System.currentTimeMillis() + 3000, pendingIntent);

        SunshineSyncAdapter.syncImmediately(getActivity());
    }




    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String startDate = WeatherContract.getDbDateString(new Date());
        _Location = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";

        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(_Location, startDate);

        return new CursorLoader(getActivity(),
                                weatherForLocationUri,
                                FORECAST_COLUMNS,
                                null,
                                null,
                                sortOrder
                                );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        forecastAdapter.swapCursor(data);
        if (_Position != ListView.INVALID_POSITION)
        {
            _ListView.smoothScrollToPosition(_Position);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        forecastAdapter.swapCursor(null);

    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String date);
    }
}

