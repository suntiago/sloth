package com.suntiago.dblibDemo;

import android.content.Context;

import com.suntiago.lockpattern.PatternManager;
import com.suntiago.sloth.SlothApplication;
import com.suntiago.sloth.account.AccountManager;
import com.suntiago.sloth.utils.FileUtils;
import com.suntiago.sloth.utils.file.StorageManagerHelper;
import com.suntiago.sloth.utils.log.CrashHandler;
import com.suntiago.sloth.utils.log.Slog;


/**
 * Created by Jeremy on 2018/11/20.
 */

public class DemoApp extends SlothApplication {
    static final String COM = "suntiago";
    static final String appNAme = "demo";

    @Override
    public void onCreate() {
        super.onCreate();
        Context ct = this;
        StorageManagerHelper.getStorageHelper().initPath(COM, appNAme);

        FileUtils.initPath(COM, appNAme);
        AccountManager.init(ct);
        PatternManager.init(ct);
        Slog.init(ct);
        Slog.setDebug(true, true);
        Slog.enableSaveLog(false);

        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());

    /*com.suntiago.network.network.utils.Slog.setDebug(logEnable, logEnable);
        com.suntiago.network.network.utils.Slog.setLogCallback(
                new com.suntiago.network.network.utils.Slog.ILog() {
                    @Override
                    public void i(String tag, String msg) {
                        Slog.i(tag, msg);
                    }

                    @Override
                    public void v(String tag, String msg) {
                        Slog.v(tag, msg);
                    }

                    @Override
                    public void d(String tag, String msg) {
                        Slog.d(tag, msg);
                    }

                    @Override
                    public void e(String tag, String msg) {
                        Slog.e(tag, msg);
                    }

                    @Override
                    public void state(String packName, String state) {
                        Slog.state(packName, state);
                    }
                });*/
    }
}
