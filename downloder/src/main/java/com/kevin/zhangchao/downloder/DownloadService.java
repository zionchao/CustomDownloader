package com.kevin.zhangchao.downloder;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.LogRecord;

/**
 * Created by zhangchao_a on 2017/5/12.
 */

public class DownloadService extends Service {

    private HashMap<String,DownloadTask> mDownloadingTasks=new HashMap<>();
    private ExecutorService mExecutorService;

    private Handler mHandler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DataChanger.getInstance().postStatus((DownloadEntry) msg.obj);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mExecutorService= Executors.newCachedThreadPool();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent!=null){
            DownloadEntry entry= (DownloadEntry) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
            int action=intent.getIntExtra(Constants.KEY_DOWNLOAD_ACTION,-1);
            doAction(action,entry);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(int action, DownloadEntry entry) {
        switch (action){
            case Constants.KEY_DOWNLOAD_ACTION_ADD:
                startDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_PAUSE:
                pauseDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_RESUME:
                resumeDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_CANCEL:
                cancelDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL:
                pauseAll(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL:
                recoverAll(entry);
                break;
        }
    }

    private void startDownload(DownloadEntry entry) {
        DownloadTask task=new DownloadTask(entry,mHandler);
//        task.start();
        mDownloadingTasks.put(entry.id,task);
        mExecutorService.execute(task);
    }

    private void pauseDownload(DownloadEntry entry) {
        DownloadTask task=mDownloadingTasks.remove(entry.id);
        if (task!=null)
            task.pause();
    }

    private void cancelDownload(DownloadEntry entry) {
        DownloadTask task=mDownloadingTasks.remove(entry.id);
        if (task!=null)
            task.cancel();
    }

    private void resumeDownload(DownloadEntry entry) {
        startDownload(entry);
    }

    private void pauseAll(DownloadEntry entry) {
    }

    private void recoverAll(DownloadEntry entry) {
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
