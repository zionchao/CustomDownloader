package com.kevin.zhangchao.customdownloader;

import android.app.Application;

import com.kevin.zhangchao.downloder.DownloadManager;

/**
 * Created by ZhangChao on 2017/5/14.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DownloadManager.getInstance(this);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
