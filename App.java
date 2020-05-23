package com.veyndan.imageloading;

import android.app.Application;

import com.squareup.picasso.Picasso;

import timber.log.Timber;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree() {
            @Override
            protected void log(final int priority, final String tag, final String message,
                               final Throwable t) {
                super.log(priority, "veyndan_" + tag, message, t);
            }
        });

        Picasso.setSingletonInstance(new Picasso.Builder(this)
                .indicatorsEnabled(true)
                .build());
    }
}
