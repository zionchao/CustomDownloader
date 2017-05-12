package com.kevin.zhangchao.downloder;

import android.content.Context;
import android.content.Intent;

/**
 * Created by zhangchao_a on 2017/5/12.
 */

public class DownloadManager {

    private static DownloadManager mInstance;

    private Context context;

    private DownloadManager(Context context){
        this.context=context;
    }
    public synchronized static DownloadManager getInstance(Context context) {
        if (mInstance==null)
        {
            mInstance=new DownloadManager(context);
        }
        return mInstance;
    }

    public void add(DownloadEntry entry){
        Intent intent=new Intent(context,DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY,entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,Constants.KEY_DOWNLOAD_ACTION_ADD);
        context.startService(intent);
    }

    public void pause(DownloadEntry entry){
        Intent intent=new Intent(context,DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY,entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,Constants.KEY_DOWNLOAD_ACTION_PAUSE);
        context.startService(intent);
    }
    public void resume(DownloadEntry entry){
        Intent intent=new Intent(context,DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY,entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,Constants.KEY_DOWNLOAD_ACTION_RESUME);
        context.startService(intent);
    }

    public void cancle(DownloadEntry entry){
        Intent intent=new Intent(context,DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY,entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,Constants.KEY_DOWNLOAD_ACTION_CANCEL);
        context.startService(intent);
    }

    public void addObserver(DataWatcher watcher){
        DataChanger.getInstance().addObserver(watcher);
    }

    public void removeObserver(DataWatcher watcher){
        DataChanger.getInstance().deleteObserver(watcher);
    }
}
