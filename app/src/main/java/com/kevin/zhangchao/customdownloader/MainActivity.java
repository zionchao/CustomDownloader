package com.kevin.zhangchao.customdownloader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.kevin.zhangchao.downloder.DataWatcher;
import com.kevin.zhangchao.downloder.DownloadEntry;
import com.kevin.zhangchao.downloder.DownloadManager;
import com.kevin.zhangchao.downloder.Trace;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private DownloadManager mDownloadManager;

    public DataWatcher watcher=new DataWatcher() {
        @Override
        public void notifyUpdate(DownloadEntry data) {
            Trace.e(data.toString());
        }
    };
    private DownloadEntry entry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mDownloadManager=DownloadManager.getInstance(this);
    }

    @OnClick({R.id.mDownloadBtn,R.id.btnResume,R.id.btnPause})
    public void testDownload(Button view){
        switch (view.getId()){
            case R.id.mDownloadBtn:
                entry = new DownloadEntry();
                entry.name = "test.jpg";
                entry.url = "http://api.stay4it.com/uploads/test.jpg";
                entry.id = "1";
                mDownloadManager.add(entry);
                break;
            case R.id.btnResume:
                mDownloadManager.resume(entry);
                break;
            case R.id.btnPause:
                mDownloadManager.pause(entry);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDownloadManager.addObserver(watcher);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDownloadManager.removeObserver(watcher);
    }
}

