package com.example.krishnadamarla.sunshine.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.krishnadamarla.sunshine.R;
import com.example.krishnadamarla.sunshine.WeatherAPIClient;
import com.example.krishnadamarla.sunshine.data.WeatherContract;
import com.example.krishnadamarla.sunshine.helpers.Utility;
import com.example.krishnadamarla.sunshine.helpers.WeatherJsonParser;

import org.json.JSONException;

/**
 * Created by krishnadamarla on 1/10/15.
 */
public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {
    public final static String LOG_TAG = SunshineSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the weather, in milliseconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 10; // 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    public SunshineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
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
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        Log.i(LOG_TAG, "Code reached onPerformSync");
        String zipCode = Utility.getPreferredLocation(getContext());

        String weatherJson = WeatherAPIClient.GetForecastForAWeek(getWeatherMapAPI(zipCode, 14));
        if (weatherJson == null || weatherJson.isEmpty())
            return;

        try
        {
            WeatherJsonParser.getWeatherDataFromJson(weatherJson, zipCode, getContext());

        }
        catch(JSONException je)
        { Log.e("JSON Exception", "exception while converting results to json", je);}
    }


   /*
    * Helper method to schedule the sync adapter periodic execution
    */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            Log.i(LOG_TAG, "old sync process used");
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */


        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        ContentResolver.setIsSyncable(newAccount, context.getString(R.string.content_authority), 1);

        SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            //ContentResolver.setIsSyncable(newAccount, context.getString(R.string.content_authority), 1);


            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }
}
