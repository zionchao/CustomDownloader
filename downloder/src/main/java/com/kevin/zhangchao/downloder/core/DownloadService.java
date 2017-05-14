package com.kevin.zhangchao.downloder.core;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.kevin.zhangchao.downloder.DownloadConfig;
import com.kevin.zhangchao.downloder.utils.Constants;
import com.kevin.zhangchao.downloder.db.DBController;
import com.kevin.zhangchao.downloder.notify.DataChanger;
import com.kevin.zhangchao.downloder.entity.DownloadEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by zhangchao_a on 2017/5/12.
 */

public class DownloadService extends Service {

    public static final int NOTIFY_DOWNLOADING = 1;
    public static final int NOTIFY_UPDATING = 2;
    public static final int NOTIFY_PAUSED_OR_CANCELLED = 3;
    public static final int NOTIFY_COMPLETED = 4;
    public static final int NOTIFY_CONNECTING = 5;
    public static final int NOTIFY_ERROR= 5;


    private HashMap<String,DownloadTask> mDownloadingTasks=new HashMap<>();
    private ExecutorService mExecutorService;
    private DBController mDBController;

    private LinkedBlockingDeque<DownloadEntry> mWaitingQueue=new LinkedBlockingDeque<>();

    private Handler mHandler=new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case NOTIFY_PAUSED_OR_CANCELLED:
                case NOTIFY_COMPLETED:
                case NOTIFY_ERROR:
                    checkNext((DownloadEntry) msg.obj);
                    break;

            }
            DownloadEntry entry=(DownloadEntry) msg.obj;
            DataChanger.getInstance(getApplicationContext()).postStatus(entry);
        }

    };


    @Override
    public void onCreate() {
        super.onCreate();
        mExecutorService= Executors.newCachedThreadPool();
        mDBController=DBController.getInstance(getApplicationContext());
        initDownload();
    }

    private void initDownload() {
        ArrayList<DownloadEntry> mDownloadEntries=mDBController.queryAll();
        if (mDBController!=null){
            for (DownloadEntry entry:mDownloadEntries)
            {
                if (entry.status== DownloadEntry.DownloadStatus.downloading||entry.status== DownloadEntry.DownloadStatus.wating){
                  if (DownloadConfig.getConfig().isRecoverDownloadWhenStart()){
                      if (entry.isSupportRange){
                          entry.status= DownloadEntry.DownloadStatus.paused;
                      }else{
                          entry.status= DownloadEntry.DownloadStatus.idle;
                          entry.reset();
                      }
                      addDownload(entry);
                  }else{
                      if (entry.isSupportRange){
                          entry.status= DownloadEntry.DownloadStatus.paused;
                      }else{
                          entry.status= DownloadEntry.DownloadStatus.idle;
                          entry.reset();
                      }
                  }
                }
                DataChanger.getInstance(getApplicationContext()).addToOperatedEntryMap(entry.id,entry);
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent!=null){
            DownloadEntry entry= (DownloadEntry) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
            if (entry!=null&&DataChanger.getInstance(getApplicationContext()).containsEntry(entry.id)){
                entry=DataChanger.getInstance(getApplicationContext()).queryDownloadEntry(entry.id);
            }
            int action=intent.getIntExtra(Constants.KEY_DOWNLOAD_ACTION,-1);
            doAction(action,entry);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void checkNext(DownloadEntry entry) {
        mDownloadingTasks.remove(entry);
        DownloadEntry newEntry=mWaitingQueue.poll();
        if (newEntry!=null){
            startDownload(newEntry);
        }
    }

    private void doAction(int action, DownloadEntry entry) {
        switch (action){
            case Constants.KEY_DOWNLOAD_ACTION_ADD:
                addDownload(entry);
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
                pauseAll();
                break;
            case Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL:
                recoverAll();
                break;
        }
    }

    private void addDownload(DownloadEntry entry){
        if (mDownloadingTasks.size()>=DownloadConfig.getConfig().getMaxDownloadTasks()){
            mWaitingQueue.offer(entry);
            entry.status= DownloadEntry.DownloadStatus.wating;
            DataChanger.getInstance(getApplicationContext()).postStatus(entry);
        }else{
            startDownload(entry);
        }
    }
    private void startDownload(DownloadEntry entry) {
        DownloadTask task=new DownloadTask(entry,mHandler,mExecutorService);
        task.start();
        mDownloadingTasks.put(entry.id,task);
//        mExecutorService.execute(task);
    }

    private void pauseDownload(DownloadEntry entry) {
        DownloadTask task=mDownloadingTasks.remove(entry.id);
        if (task!=null)
            task.pause();
        else
        {
            mWaitingQueue.remove(entry);
            entry.status= DownloadEntry.DownloadStatus.paused;
            DataChanger.getInstance(getApplicationContext()).postStatus(entry);
        }
    }

    private void cancelDownload(DownloadEntry entry) {
        DownloadTask task=mDownloadingTasks.remove(entry.id);
        if (task!=null)
            task.cancel();
        else{
            mWaitingQueue.remove(entry);
            entry.status= DownloadEntry.DownloadStatus.cancel;
            DataChanger.getInstance(getApplicationContext()).postStatus(entry);
        }
    }

    private void resumeDownload(DownloadEntry entry) {
        addDownload(entry);
    }

    private void pauseAll() {
        while(mWaitingQueue.iterator().hasNext()){
            DownloadEntry entry=mWaitingQueue.poll();
            entry.status= DownloadEntry.DownloadStatus.paused;
            DataChanger.getInstance(getApplicationContext()).postStatus(entry);
        }

        for (Map.Entry<String,DownloadTask> entry: mDownloadingTasks.entrySet()){
            entry.getValue().pause();
        }
        mDownloadingTasks.clear();

    }

    private void recoverAll() {
        ArrayList<DownloadEntry> mRecoverableEntries=DataChanger.getInstance(getApplicationContext()).queryAllRecoverableEntries();
        if (mRecoverableEntries!=null){
            for (DownloadEntry entry:mRecoverableEntries){
                addDownload(entry);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
