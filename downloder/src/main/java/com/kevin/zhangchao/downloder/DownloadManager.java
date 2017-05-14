package com.kevin.zhangchao.downloder;

import android.content.Context;
import android.content.Intent;

/**
 * Created by zhangchao_a on 2017/5/12.
 */

public class DownloadManager {

    private static DownloadManager mInstance;

    private Context context;

    private static final int MIN_OPERATE_INTEVAL=1000*1;
    private long mLastOperatedTime=0;

    private DownloadManager(Context context){
        this.context=context;
        context.startService(new Intent(context,DownloadService.class));
    }

    public synchronized static DownloadManager getInstance(Context context) {
        if (mInstance==null)
        {
            mInstance=new DownloadManager(context);
        }
        return mInstance;
    }

    public void add(DownloadEntry entry){
        if(!checkIfExecutable())
            return;
        Intent intent=new Intent(context,DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY,entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,Constants.KEY_DOWNLOAD_ACTION_ADD);
        context.startService(intent);
    }

    public void pause(DownloadEntry entry){
        if(!checkIfExecutable())
            return;
        Intent intent=new Intent(context,DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY,entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,Constants.KEY_DOWNLOAD_ACTION_PAUSE);
        context.startService(intent);
    }
    public void resume(DownloadEntry entry){
        if(!checkIfExecutable())
            return;
        Intent intent=new Intent(context,DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY,entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,Constants.KEY_DOWNLOAD_ACTION_RESUME);
        context.startService(intent);
    }

    public void cancle(DownloadEntry entry){
        if(!checkIfExecutable())
            return;
        Intent intent=new Intent(context,DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY,entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,Constants.KEY_DOWNLOAD_ACTION_CANCEL);
        context.startService(intent);
    }

    public void pauseAll() {
        Intent intent=new Intent(context,DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL);
        context.startService(intent);
    }

    public void recoverAll() {
        Intent intent=new Intent(context,DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL);
        context.startService(intent);
    }

    public void addObserver(DataWatcher watcher){
        DataChanger.getInstance(context).addObserver(watcher);
    }

    public void removeObserver(DataWatcher watcher){
        DataChanger.getInstance(context).deleteObserver(watcher);
    }

    /**
     * 频繁的点击导致页面错误，此方法用于减低错误出现的次数
     * @return
     */
    private boolean checkIfExecutable(){
        long tmp= System.currentTimeMillis();
        if (tmp-mLastOperatedTime>MIN_OPERATE_INTEVAL){
            mLastOperatedTime=tmp;
            return true;
        }
        return false;
    }

    public DownloadEntry queryDownloadEntry(String id){
        return DataChanger.getInstance(context).queryDownloadEntry(id);
    }

}
