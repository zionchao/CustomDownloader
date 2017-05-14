package com.kevin.zhangchao.downloder.utils;

import android.util.Log;

/**
 * Created by zhangchao_a on 2017/5/12.
 */

public class Trace {

    public static final String TAG = "kevinzhang";
    private static final boolean DEBUG = true;

    public static void d(String msg) {
        if (DEBUG)
            Log.d(TAG, msg);
    }

    public static void e(String msg) {
        if (DEBUG)
            Log.e(TAG, msg);
    }

}
