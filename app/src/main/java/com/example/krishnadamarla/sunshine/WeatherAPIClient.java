package com.example.krishnadamarla.sunshine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.util.Log;

/**
 * Created by krishnadamarla on 12/21/14.
 */
public final class WeatherAPIClient {
    private static String LOG_TAG = WeatherAPIClient.class.getSimpleName();

    public static String GetForecastForAWeek(String weekForecastEndPoint){
        String forecastJsonStr = null;

        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;

        try
        {
            URL url = new URL(weekForecastEndPoint);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();

            InputStream inputStream = httpURLConnection.getInputStream();
            if (inputStream == null)
             forecastJsonStr = null;
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer stringBuffer = new StringBuffer();
            String line = null;
            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuffer.append(line + "\n");
            }
            if(stringBuffer.length() == 0)
                forecastJsonStr = null;
            forecastJsonStr = stringBuffer.toString();
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG, "Error ", e);
            forecastJsonStr = null;
        }
        finally {
            if (httpURLConnection != null)
            {
                httpURLConnection.disconnect();
                httpURLConnection = null;
            }

            if (bufferedReader != null)
            {
                try{
                    bufferedReader.close();
                }
                catch (Exception e)
                {
                    Log.e("WeatherAPIClient", "Error closing stream", e);
                }
            }
        }

        return forecastJsonStr;
    }
}
