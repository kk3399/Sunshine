package com.example.krishnadamarla.sunshine;

import android.app.Activity;
import android.app.ActionBar;
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
import android.os.Build;
import android.widget.Adapter;
import android.widget.ShareActionProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.example.krishnadamarla.sunshine.data.WeatherContract;
import com.example.krishnadamarla.sunshine.helpers.Utility;


public class DetailActivity extends Activity {


    public static final int FORECAST_LOADER = 0;
    public static final String[] FORECAST_COLUMNS = {
        WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP};


    public static final int COL_WEATHER_DATE = 0;
    public static final int COL_WEATHER_DESC = 1;
    public static final int COL_WEATHER_MIN_TEMP = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;

    private String _StartDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT))
        {

            _StartDate =  intent.getStringExtra(Intent.EXTRA_TEXT);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            getLoaderManager().initLoader(FORECAST_LOADER, null,this);
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
            mShareActionProvider = (ShareActionProvider) item.getActionProvider();
            mShareActionProvider.setShareIntent(getSharableIntent());
        }

        private Intent getSharableIntent() {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            String forecast = getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, forecast + "#sunshine");
            return intent;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getApplicationContext(),
                    WeatherContract.WeatherEntry.buildWeatherLocationWithDate(Utility.getPreferredLocation(getApplicationContext()), _StartDate),
                    FORECAST_COLUMNS, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if(data.moveToNext())
            {
                TextView dateTextView = (TextView) findViewById(R.id.detail_item_date_textview);
                dateTextView.setText(data.getString(COL_WEATHER_DATE));

                TextView descriptionTextView = (TextView) findViewById(R.id.detail_item_forecast_textview);
                descriptionTextView.setText(data.getString(COL_WEATHER_DESC));

                TextView minTempTextView = (TextView) findViewById(R.id.detail_item_low_textview);
                minTempTextView.setText(data.getString(COL_WEATHER_MIN_TEMP));

                TextView maxTempTextView = (TextView) findViewById(R.id.detail_item_high_textview);
                maxTempTextView.setText(data.getString(COL_WEATHER_MAX_TEMP));
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

            TextView dateTextView = (TextView) findViewById(R.id.detail_item_date_textview);
            dateTextView.setText("");

            TextView descriptionTextView = (TextView) findViewById(R.id.detail_item_forecast_textview);
            dateTextView.setText("");

            TextView minTempTextView = (TextView) findViewById(R.id.detail_item_low_textview);
            dateTextView.setText("");

            TextView maxTempTextView = (TextView) findViewById(R.id.detail_item_high_textview);
            dateTextView.setText("");
        }
    }
}
