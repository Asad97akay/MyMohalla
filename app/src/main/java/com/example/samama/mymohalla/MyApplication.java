package com.example.samama.mymohalla;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by samam on 2/4/2017.
 */

public class MyApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}