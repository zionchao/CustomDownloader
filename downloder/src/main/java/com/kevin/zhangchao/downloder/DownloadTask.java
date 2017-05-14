package com.kevin.zhangchao.downloder;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * Created by zhangchao_a on 2017/5/12.
 */

public class DownloadTask implements ConnectThread.ConnectListener, DownloadThread.DownloadListener {

    private final Handler mHandler;
    private final ExecutorService mExecutorService;
    private DownloadEntry entry;

    //TODO read volatile explain
    private volatile boolean isPaused;
    private volatile boolean isCancelled;
    private ConnectThread mConnectThread;

    private DownloadThread[] mDownloadThreads;
    private int tmpDivide;

    public DownloadTask(DownloadEntry entry, Handler mHandler, ExecutorService pExecutorService) {
        this.entry=entry;
        this.mHandler=mHandler;
        this.mExecutorService=pExecutorService;
    }

    public void start() {
        if (entry.isSupportRange){
            startDownload();
        }else{
            entry.status= DownloadEntry.DownloadStatus.connecting;
            notifyUpdate(entry,DownloadService.NOTIFY_CONNECTING);
            mConnectThread=new ConnectThread(entry.url,this);
            mExecutorService.execute(mConnectThread);
        }
    }

    private void startDownload() {
        if (entry.isSupportRange){
            startMutiDownload();
        }else{
            startSingleDownload();
        }
    }

    private void notifyUpdate(DownloadEntry entry,int what){
        Message msg=mHandler.obtainMessage();
        msg.obj=entry;
        msg.what=what;
        mHandler.sendMessage(msg);
    }

    public void pause() {
        Trace.d("download pause");
        isPaused=true;
        if (mConnectThread != null && mConnectThread.isRunning()){
            mConnectThread.cancel();
        }
        if (mDownloadThreads!=null&&mDownloadThreads.length>0){
            for (int i=0;i<mDownloadThreads.length;i++){
                if (mDownloadThreads[i]!=null&&mDownloadThreads[i].isRunning()){
                    if (entry.isSupportRange)
                        mDownloadThreads[i].pause();
                    else
                        mDownloadThreads[i].cancel();
                }
            }
        }
    }

    public void cancel() {
        Trace.d("download cancel");
        isCancelled=true;
        if (mConnectThread != null && mConnectThread.isRunning()){
            mConnectThread.cancel();
        }

        if (mDownloadThreads!=null&&mDownloadThreads.length>0){
            for (int i=0;i<mDownloadThreads.length;i++){
                if (mDownloadThreads[i]!=null&&mDownloadThreads[i].isRunning()){
                    mDownloadThreads[i].cancel();
                }
            }
        }
    }


    private void startMutiDownload() {
        entry.status= DownloadEntry.DownloadStatus.downloading;
        notifyUpdate(entry,DownloadService.NOTIFY_DOWNLOADING);
        int block=entry.totalLength/Constants.MAX_DOWNLOAD_THREADS;
        int startPos=0;
        int endPos=0;
        //第一次进来,断点信息为0
        if (entry.ranges==null){
            entry.ranges=new HashMap<>();
            for (int i=0;i<Constants.MAX_DOWNLOAD_THREADS;i++){
                entry.ranges.put(i,0);
            }
        }
        mDownloadThreads=new DownloadThread[Constants.MAX_DOWNLOAD_THREADS];
        for(int i=0;i<Constants.MAX_DOWNLOAD_THREADS;i++){
            startPos=i*block+entry.ranges.get(i);
            if (i==Constants.MAX_DOWNLOAD_THREADS-1){
                endPos=entry.totalLength;
            }else{
                endPos=(i+1)*block-1;
            }
            if (startPos<endPos){
                mDownloadThreads[i]=new DownloadThread(entry.url,i,startPos,endPos,this);
                mExecutorService.execute(mDownloadThreads[i]);
            }
        }
    }

    private void startSingleDownload() {
        entry.status= DownloadEntry.DownloadStatus.downloading;
        notifyUpdate(entry,DownloadService.NOTIFY_DOWNLOADING);
        mDownloadThreads=new DownloadThread[1];
        mDownloadThreads[0]=new DownloadThread(entry.url,0,0,0,this);
        mExecutorService.execute(mDownloadThreads[0]);
    }


    @Override
    public synchronized void onConnected(boolean isSupportRange, int totalLength) {
        entry.isSupportRange=isSupportRange;
        entry.totalLength=totalLength;
        startDownload();
    }

    @Override
    public synchronized void onConnectError(String message) {
        if (isPaused||isCancelled){
            entry.status = isPaused ? DownloadEntry.DownloadStatus.paused : DownloadEntry.DownloadStatus.cancel;
            notifyUpdate(entry,DownloadService.NOTIFY_PAUSED_OR_CANCELLED);
        }else{
            entry.status= DownloadEntry.DownloadStatus.error;
            notifyUpdate(entry,DownloadService.NOTIFY_ERROR);
        }
    }

    @Override
    public  synchronized void onProgressChanged(int index, int progress) {
        if (entry.isSupportRange){
            int range=entry.ranges.get(index)+progress;
            entry.ranges.put(index,range);
        }
        entry.currentLength+=progress;
        if (entry.totalLength>0){
            int percent= (int) (entry.currentLength*100l/entry.totalLength);
            if(percent>entry.percent){
                entry.percent=percent;
                notifyUpdate(entry,DownloadService.NOTIFY_UPDATING);
            }
        }else{
            int divide=entry.currentLength/(50*1024);
            if (divide>tmpDivide){
                tmpDivide=divide;
                notifyUpdate(entry,DownloadService.NOTIFY_UPDATING);
            }
        }
    }

    @Override
    public synchronized void onDownloadCompleted(int index) {
        for (int i=0;i<mDownloadThreads.length;i++){
            if (mDownloadThreads[i]!=null){
                if (!mDownloadThreads[i].isCompleted()){
                   return;
                }
            }
        }

        if (entry.totalLength>0&&entry.currentLength!=entry.totalLength){
            entry.reset();
            String path = Environment.getExternalStorageDirectory() + File.separator +
                    "rmp" + File.separator + entry.url.substring(entry.url.lastIndexOf("/") + 1);
            File file = new File(path);
            if (file.exists())
                file.delete();
            entry.status = DownloadEntry.DownloadStatus.error;
            notifyUpdate(entry, DownloadService.NOTIFY_ERROR);
        }else{
            entry.status= DownloadEntry.DownloadStatus.completed;
            notifyUpdate(entry,DownloadService.NOTIFY_COMPLETED);
        }
    }

    @Override
    public synchronized void onDownloadError(int index,String message) {
        boolean isAllError=true;
        for (int i=0;i<mDownloadThreads.length;i++){
            if (mDownloadThreads[i]!=null){
                if (!mDownloadThreads[i].isError()){
                    isAllError=false;
                    mDownloadThreads[i].cancelByError();
                }
            }
        }
        if (isAllError){
            entry.status = DownloadEntry.DownloadStatus.error;
            notifyUpdate(entry, DownloadService.NOTIFY_ERROR);
        }
    }

    @Override
    public synchronized void onDownloadPaused(int index) {
        for (int i=0;i<mDownloadThreads.length;i++){
            if (mDownloadThreads[i]!=null){
                if (!mDownloadThreads[i].isPaused()){
                    return;
                }
            }
        }
        entry.status= DownloadEntry.DownloadStatus.paused;
        notifyUpdate(entry,DownloadService.NOTIFY_PAUSED_OR_CANCELLED);

    }

    @Override
    public synchronized void onDownloadCancelled(int index) {
        for (int i=0;i<mDownloadThreads.length;i++){
            if (mDownloadThreads[i]!=null){
                if (!mDownloadThreads[i].isCancelled()){
                    return;
                }
            }
        }
        entry.status= DownloadEntry.DownloadStatus.cancel;
        entry.reset();
        String path = Environment.getExternalStorageDirectory() + File.separator +
                "rmp" + File.separator + entry.url.substring(entry.url.lastIndexOf("/") + 1);
        File file = new File(path);
        if (file.exists())
            file.delete();
        notifyUpdate(entry,DownloadService.NOTIFY_PAUSED_OR_CANCELLED);
    }
}
