package com.kevin.zhangchao.downloder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

/**
 * Created by zhangchao_a on 2017/5/12.
 */

public class DownloadService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DownloadEntry entry= (DownloadEntry) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
        int action=intent.getIntExtra(Constants.KEY_DOWNLOAD_ACTION,-1);
        doAction(action,entry);

        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(int action, DownloadEntry entry) {
        switch (action){
            case Constants.KEY_DOWNLOAD_ACTION_ADD:
                entry.status= DownloadEntry.DownloadStatus.downloading;
                DataChanger.getInstance().postStatus(entry);
                break;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
