package com.kevin.zhangchao.downloder;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by zhangchao_a on 2017/5/12.
 */

public abstract class DataWatcher implements Observer {
    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof DownloadEntry){
            notifyUpdate((DownloadEntry)data);
        }
    }

    public abstract void notifyUpdate(DownloadEntry data);
}
