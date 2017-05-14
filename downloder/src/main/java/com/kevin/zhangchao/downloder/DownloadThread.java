package com.kevin.zhangchao.downloder;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zhangchao_a on 2017/5/12.
 */

public class DownloadThread implements Runnable{

    private final String url;
    private final int index;
    private final int startPos;
    private final int endPos;
    private final String path;
    private final DownloadListener listener;

    private DownloadEntry.DownloadStatus mStatus;
    private volatile boolean isPaused;
    private volatile boolean isCancelled;
    private volatile boolean isError;
    private volatile boolean isSupportRange;


    public DownloadThread(String url, int index, int startPos, int endPos,DownloadListener listener) {
        this.url=url;
        this.index=index;
        this.startPos=startPos;
        this.endPos=endPos;
        if (startPos==0&&endPos==0)
            isSupportRange=false;
        else
            isSupportRange=true;
        this.path = Environment.getExternalStorageDirectory() + File.separator +
                "rmp" + File.separator + url.substring(url.lastIndexOf("/") + 1);
        this.listener = listener;
    }

    @Override
    public void run() {
        mStatus= DownloadEntry.DownloadStatus.downloading;
        HttpURLConnection connection=null;
        try {
            connection= (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            if (isSupportRange)
                connection.setRequestProperty("Range","bytes="+startPos+"-"+endPos);
            connection.setConnectTimeout(Constants.CONNECT_TIME);
            connection.setReadTimeout(Constants.READ_TIME);
            int responseCode=connection.getResponseCode();
            int contentLenght=connection.getContentLength();
            File file=new File(path);
            RandomAccessFile raf=null;
            InputStream is=null;
            FileOutputStream fos=null;
            if (responseCode==HttpURLConnection.HTTP_PARTIAL){
                raf=new RandomAccessFile(file,"rw");
                raf.seek(startPos);
                is=connection.getInputStream();
                byte[] buffer=new byte[2048];
                int len=-1;
                while ((len=is.read(buffer))!=-1){
                    if (isPaused||isCancelled||isError)
                        break;
                    raf.write(buffer);
                    listener.onProgressChanged(index,len);
                }
                raf.close();
                is.close();
            }else if (responseCode==HttpURLConnection.HTTP_OK){
                fos=new FileOutputStream(file);
                is=connection.getInputStream();
                byte[]buffer=new byte[2048];
                int len=-1;
                while ((len=is.read(buffer))!=-1){
                    if (isError||isCancelled||isPaused)
                        break;
                    fos.write(buffer);
                    synchronized (listener){
                        listener.onProgressChanged(0,len);
                    }
                }
                fos.close();
                is.close();
            }else{
                synchronized (listener){
                    mStatus= DownloadEntry.DownloadStatus.error;
                    listener.onDownloadError(index,"server error:"+responseCode);
                }
                return ;
            }
            synchronized (listener){
                if (isPaused){
                    mStatus= DownloadEntry.DownloadStatus.paused;
                    listener.onDownloadPaused(index);
                }else if(isCancelled){
                    mStatus= DownloadEntry.DownloadStatus.cancel;
                    listener.onDownloadCancelled(index);
                }else if(isError){
                    mStatus= DownloadEntry.DownloadStatus.error;
                    listener.onDownloadError(index,"cancel manually by error");
                }else{
                    mStatus= DownloadEntry.DownloadStatus.completed;
                    listener.onDownloadCompleted(index);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            synchronized (listener){
                if (isPaused) {
                    mStatus = DownloadEntry.DownloadStatus.paused;
                    listener.onDownloadPaused(index);
                } else if (isCancelled) {
                    mStatus = DownloadEntry.DownloadStatus.cancel;
                    listener.onDownloadCancelled(index);
                } else {
                    mStatus = DownloadEntry.DownloadStatus.error;
                    listener.onDownloadError(index,e.getMessage());
                }
            }
        }finally {
            if (connection!=null){
                connection.disconnect();
            }
        }

    }

    public boolean isRunning() {
        return mStatus== DownloadEntry.DownloadStatus.downloading;
    }

    public void pause() {
        isPaused=true;
        Thread.currentThread().interrupt();
    }

    public boolean isPaused(){
        return mStatus== DownloadEntry.DownloadStatus.paused||mStatus == DownloadEntry.DownloadStatus.completed;
    }

    public void cancel() {
        isCancelled=true;
        Thread.currentThread().interrupt();
    }

    public boolean isCancelled() {
        return mStatus== DownloadEntry.DownloadStatus.cancel||mStatus == DownloadEntry.DownloadStatus.completed;
    }

    public boolean isError() {
        return mStatus== DownloadEntry.DownloadStatus.error;
    }

    public void cancelByError() {
        isError=true;
        Thread.currentThread().interrupt();
    }

    public boolean isCompleted() {
        return mStatus== DownloadEntry.DownloadStatus.completed;
    }

    interface DownloadListener{
        void onProgressChanged(int index,int progress);
        void onDownloadCompleted(int index);
        void onDownloadError(int index,String message);

        void onDownloadPaused(int index);
        void onDownloadCancelled(int index);
    }
}
