package com.kevin.zhangchao.downloder;

import android.os.Handler;
import android.os.Message;

/**
 * Created by zhangchao_a on 2017/5/12.
 */

public class DownloadTask implements Runnable {

    private final Handler mHandler;
    private DownloadEntry entry;

    private boolean isPaused;
    private boolean isCancelled;

    public DownloadTask(DownloadEntry entry, Handler mHandler) {
        this.entry=entry;
        this.mHandler=mHandler;
    }

    public void start() {
        entry.status= DownloadEntry.DownloadStatus.downloading;
//        DataChanger.getInstance().postStatus(entry);
        postMessage(entry);

        entry.totalLength=1024*100;
        for (int i=0;i<entry.totalLength;){
            try {
                Thread.sleep(1000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            if (isPaused||isCancelled){
                entry.status=isPaused? DownloadEntry.DownloadStatus.paused: DownloadEntry.DownloadStatus.cancel;
//                DataChanger.getInstance().postStatus(entry);
                postMessage(entry);
                return;
            }
            i+=1024;
            entry.currentLength+=i;
            entry.status=DownloadEntry.DownloadStatus.downloading;
//            DataChanger.getInstance().postStatus(entry);
            postMessage(entry);
        }

        entry.status=DownloadEntry.DownloadStatus.completed;
        DataChanger.getInstance().postStatus(entry);
    }

    private void postMessage(DownloadEntry entry){
        Message msg=mHandler.obtainMessage();
        msg.obj=entry;
        mHandler.sendMessage(msg);
    }

    public void pause() {
        Trace.d("download pause");
        isPaused=true;
    }

    public void cancel() {
        Trace.d("download cancel");
        isCancelled=true;
    }

    @Override
    public void run() {
        start();
    }
}
