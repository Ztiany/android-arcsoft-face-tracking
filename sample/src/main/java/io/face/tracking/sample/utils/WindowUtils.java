package io.face.tracking.sample.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import io.face.tracking.sample.AppContext;

/**
 * @author Ztiany
 *         Email: 1169654504@qq.com
 *         Date : 2017-11-20 11:07
 */
public class WindowUtils {

    public static int getScreenWidth() {
        WindowManager wm = (WindowManager) getAppContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    public static int getScreenHeight() {
        WindowManager wm = (WindowManager) getAppContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    private static Context getAppContext() {
        return AppContext.getAppContext();
    }
}
