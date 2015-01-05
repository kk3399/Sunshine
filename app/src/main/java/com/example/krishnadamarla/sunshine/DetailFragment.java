package com.example.krishnadamarla.sunshine;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.example.krishnadamarla.sunshine.data.WeatherContract;
import com.example.krishnadamarla.sunshine.helpers.Utility;

/**
 * Created by krishnadamarla on 1/3/15.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int FORECAST_LOADER = 0;
    public static final String DATE_ARGUMENT = "dateArgument";

    public static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_DEGREES};


    public static final int COL_WEATHER_DATE = 0;
    public static final int COL_WEATHER_DESC = 1;
    public static final int COL_WEATHER_MIN_TEMP = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_HUMIDITY = 4;
    public static final int COL_WEATHER_PRESSURE = 5;
    public static final int COL_WEATHER_WIND = 6;
    public static final int COL_WEATHER_ID_API = 7;
    public static final int COL_WEATHER_DEGREES = 8;

    private String _StartDate = "";

    public static DetailFragment newInstance(String date) {
        DetailFragment f = new DetailFragment();

        f._StartDate = date;
        Bundle args = new Bundle();
        args.putString(DATE_ARGUMENT, date);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle args = getArguments();
        if (args != null && args.containsKey(DATE_ARGUMENT)) {
            getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args != null && args.containsKey(DATE_ARGUMENT)) {
            getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        }
    }

    private ShareActionProvider mShareActionProvider;
    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //inflater.inflate(R.menu.menu_detail, menu);
        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);
        // Fetch and store ShareActionProvider
        //mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        //mShareActionProvider.setShareIntent(getSharableIntent());
    }

    private Intent getSharableIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        Bundle args = getArguments();
        if (args != null && args.containsKey(DATE_ARGUMENT)) {
            String forecast = args.getString(DATE_ARGUMENT);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, forecast + "#sunshine");
            return intent;
        }
        return null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        _StartDate =  getArguments().getString(DATE_ARGUMENT);
        return new CursorLoader(getActivity(),
            WeatherContract.WeatherEntry.buildWeatherLocationWithDate(Utility.getPreferredLocation(getActivity()), _StartDate),
            FORECAST_COLUMNS, null, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToNext())
        {
            ImageView imageView = (ImageView) getActivity().findViewById(R.id.detail_item_art_imageview);
            imageView.setImageResource(Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_ID_API)));

            TextView dateTextView = (TextView) getActivity().findViewById(R.id.detail_item_date_textview);
            dateTextView.setText(Utility.getFormattedMonthDay(getActivity(), data.getString(COL_WEATHER_DATE)));

            TextView dayTextView = (TextView) getActivity().findViewById(R.id.detail_item_day_textview);
            dayTextView.setText(Utility.getFriendlyDayString(getActivity(), data.getString(COL_WEATHER_DATE)));

            TextView descriptionTextView = (TextView) getActivity().findViewById(R.id.detail_item_forecast_textview);
            descriptionTextView.setText(data.getString(COL_WEATHER_DESC));

            boolean isMetric = Utility.isMetric(getActivity());

            TextView minTempTextView = (TextView) getActivity().findViewById(R.id.detail_item_low_textview);
            minTempTextView.setText(Utility.formatTemperature(getActivity(), data.getFloat(COL_WEATHER_MIN_TEMP),isMetric));

            TextView maxTempTextView = (TextView) getActivity().findViewById(R.id.detail_item_high_textview);
            maxTempTextView.setText(Utility.formatTemperature(getActivity(), data.getFloat(COL_WEATHER_MAX_TEMP), isMetric));

            float windSpeedStr = data.getFloat(COL_WEATHER_WIND);
            float windDirStr = data.getFloat(COL_WEATHER_DEGREES);
            TextView windTextView = (TextView) getActivity().findViewById(R.id.detail_item_wind_textview);
            windTextView.setText(Utility.getFormattedWind(getActivity(), windSpeedStr, windDirStr));

            float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
            TextView humidityTextView = (TextView) getActivity().findViewById(R.id.detail_item_humidity_textview);
            humidityTextView.setText(getActivity().getString(R.string.format_humidity, humidity));

            TextView pressureTextView = (TextView) getActivity().findViewById(R.id.detail_item_pressure_textview);
            pressureTextView.setText(getActivity().getString(R.string.format_pressure, data.getFloat(COL_WEATHER_PRESSURE)));
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

//            TextView dateTextView = (TextView) findViewById(R.id.detail_item_date_textview);
//            dateTextView.setText("");
//
//            TextView descriptionTextView = (TextView) findViewById(R.id.detail_item_forecast_textview);
//            dateTextView.setText("");
//
//            TextView minTempTextView = (TextView) findViewById(R.id.detail_item_low_textview);
//            dateTextView.setText("");
//
//            TextView maxTempTextView = (TextView) findViewById(R.id.detail_item_high_textview);
//            dateTextView.setText("");
    }
}