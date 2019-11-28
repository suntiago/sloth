package com.suntiago.sloth;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.suntiago.sloth.utils.log.Slog;

/**
 * Created by Jeremy on 2018/4/25.
 */

public class SlothApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        initLogs();
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void initLogs() {
        boolean logEnable = true;
        Slog.setDebug(logEnable, logEnable);
    }
}
