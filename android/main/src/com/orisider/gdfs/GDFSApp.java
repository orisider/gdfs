package com.orisider.gdfs;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

public class GDFSApp extends Application {

    public static Context ctx;
    public static Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();

        ctx = getApplicationContext();
        handler = new Handler();
    }
}
