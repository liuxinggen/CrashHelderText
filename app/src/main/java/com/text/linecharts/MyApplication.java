package com.text.linecharts;

import android.app.Application;

/**
 * 类名：com.text.linecharts
 * 时间：2017/11/24 20:26
 * 描述：
 * 修改人：
 * 修改时间：
 * 修改备注：
 *
 * @author Liu_xg
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getsInstance();
        crashHandler.init(this);
    }
}
