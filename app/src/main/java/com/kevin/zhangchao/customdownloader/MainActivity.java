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

    private DownloadEntry entry;

    public DataWatcher watcher=new DataWatcher() {
        @Override
        public void notifyUpdate(DownloadEntry data) {
            entry=data;
//            if (entry.status== DownloadEntry.DownloadStatus.cancel){
//                entry=null;
//            }
            Trace.e(data.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mDownloadManager=DownloadManager.getInstance(this);
    }

    @OnClick({R.id.mDownloadBtn,R.id.btnResume,R.id.btnPause})
    public void testDownload(Button view){
        if(entry==null){
            entry = new DownloadEntry("http://gdown.baidu.com/data/wisegame/8fe54eabc0905223/aiqiyi_80860.apk");
        }
        switch (view.getId()){
            case R.id.mDownloadBtn:
                mDownloadManager.add(entry);
                break;
            case R.id.btnResume:
//                mDownloadManager.resume(entry);
                mDownloadManager.cancle(entry);
                break;
            case R.id.btnPause:
                if (entry.status== DownloadEntry.DownloadStatus.downloading)
                {
                    mDownloadManager.pause(entry);
                }else if(entry.status== DownloadEntry.DownloadStatus.paused){
                    mDownloadManager.resume(entry);
                }

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

