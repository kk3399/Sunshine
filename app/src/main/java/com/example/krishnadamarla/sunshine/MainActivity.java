package com.example.krishnadamarla.sunshine;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ShareActionProvider;

import java.util.ArrayList;


public class MainActivity extends Activity implements ForecastFragment.Callback{

    private boolean _isTwoPane = false;

    @Override
    public void onItemSelected(String date) {

        if(_isTwoPane)
        {
            getFragmentManager().beginTransaction().replace(R.id.weather_detail_container, DetailFragment.newInstance(date)).commit();
        }
        else
        {
            Intent detailIntent = new Intent(getApplicationContext(), DetailActivity.class);
            detailIntent.putExtra(Intent.EXTRA_TEXT, date);
            startActivity(detailIntent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.weather_detail_container) != null)
        {
            _isTwoPane=true;
            if (savedInstanceState == null)
                getFragmentManager().beginTransaction().add(R.id.weather_detail_container, new DetailFragment()).commit();
        }
        else
        {
            _isTwoPane = false;
        }

        ForecastFragment fragment = ( (ForecastFragment)getFragmentManager().findFragmentById(R.id.fragment_forecast));
        fragment.setUseTodayLayout(!_isTwoPane);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
        else if(id == R.id.action_map_location)
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String zipCode = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getString(R.string.pref_general_location_key), getString(R.string.pref_general_location_default));
            Uri geoLocation =  Uri.parse("geo:0,0?q=" + zipCode);
            intent.setData(geoLocation);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }

        return super.onOptionsItemSelected(item);
    }



}
