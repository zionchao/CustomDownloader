package com.kevin.zhangchao.downloder;

import java.util.Observable;

/**
 * Created by zhangchao_a on 2017/5/12.
 */

public class DataChanger extends Observable {

    private static DataChanger mInstance;

    private DataChanger(){

    }

    public synchronized static DataChanger getInstance() {
        if (mInstance==null)
        {
            mInstance=new DataChanger();
        }
        return mInstance;
    }

    public void postStatus(DownloadEntry entry){
        setChanged();
        notifyObservers(entry);
    }
}
