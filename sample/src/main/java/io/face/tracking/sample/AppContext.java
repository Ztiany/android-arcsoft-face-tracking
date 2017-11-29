package io.face.tracking.sample;

import android.app.Application;

/**
 * @author Ztiany
 *         Email: 1169654504@qq.com
 *         Date : 2017-11-29 14:07
 */
public class AppContext extends Application {

    private static AppContext sAppContext;

    @Override
    public void onCreate() {
        sAppContext = this;
        super.onCreate();
    }

    public static AppContext getAppContext() {
        return sAppContext;
    }

}
