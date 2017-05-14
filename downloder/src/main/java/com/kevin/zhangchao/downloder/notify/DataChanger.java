package com.kevin.zhangchao.downloder.notify;

import android.content.Context;

import com.kevin.zhangchao.downloder.db.DBController;
import com.kevin.zhangchao.downloder.entity.DownloadEntry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;

/**
 * Created by zhangchao_a on 2017/5/12.
 */

public class DataChanger extends Observable {

    private static DataChanger mInstance;
    private final Context context;

    private LinkedHashMap<String ,DownloadEntry> mOperatedEntries;

    private DataChanger(Context context){
        mOperatedEntries=new LinkedHashMap<>();
        this.context=context;
    }

    public synchronized static DataChanger getInstance(Context context) {
        if (mInstance==null)
        {
            mInstance=new DataChanger(context);
        }
        return mInstance;
    }

    public void postStatus(DownloadEntry entry){
        mOperatedEntries.put(entry.id,entry);
        DBController.getInstance(context).newOrUpdate(entry);
        setChanged();
        notifyObservers(entry);
    }

    public ArrayList<DownloadEntry> queryAllRecoverableEntries(){
        ArrayList<DownloadEntry> mRecoverabeEntries=null;
        for (Map.Entry<String,DownloadEntry> entry:mOperatedEntries.entrySet()){
            if (entry.getValue().status== DownloadEntry.DownloadStatus.paused){
                if (mRecoverabeEntries==null){
                    mRecoverabeEntries=new ArrayList<>();
                }
                mRecoverabeEntries.add(entry.getValue());
            }
        }
        return mRecoverabeEntries;
    }

    public DownloadEntry queryDownloadEntry(String id){
        return mOperatedEntries.get(id);
    }

    public void addToOperatedEntryMap(String id, DownloadEntry entry) {
        mOperatedEntries.put(id,entry);
    }

    public boolean containsEntry(String id){
        return mOperatedEntries.containsKey(id);
    }

    public void deleteDownloadEntry(String id) {
        mOperatedEntries.remove(id);
        DBController.getInstance(context).deleteById(id);
    }
}
