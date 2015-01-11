package com.example.krishnadamarla.sunshine.helpers;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

/**
 * Created by krishnadamarla on 1/10/15.
 */
public class AppTest extends Application {

    private static Context mContext;

    public static Resources getContextResources() {
        return mContext.getResources();
    }

    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }
}