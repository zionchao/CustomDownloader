package com.kevin.zhangchao.customdownloader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.kevin.zhangchao.downloder.DataWatcher;
import com.kevin.zhangchao.downloder.DownloadEntry;
import com.kevin.zhangchao.downloder.DownloadManager;

public class MainActivity extends AppCompatActivity {

    public DataWatcher watcher=new DataWatcher() {
        @Override
        public void notifyUpdate(DownloadEntry data) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DownloadManager.getInstance(this).addObserver(watcher);
    }

    @Override
    protected void onStop() {
        super.onStop();
        DownloadManager.getInstance(this).removeObserver(watcher);
    }
}

