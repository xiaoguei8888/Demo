package com.boss.weather;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by bojia on 2017-09-02.
 */

public class WeatherApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // timber
        Timber.plant(new Timber.DebugTree());
    }
}
