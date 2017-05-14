package com.kevin.zhangchao.downloder.entity;

import android.os.Environment;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.kevin.zhangchao.downloder.DownloadConfig;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by zhangchao_a on 2017/5/12.
 */

@DatabaseTable(tableName = "downloadentry")
public class DownloadEntry implements Serializable{
    @DatabaseField(id = true)
    public String id;
    @DatabaseField
    public String url;
    @DatabaseField
    public String name;
    @DatabaseField
    public DownloadStatus status;
    @DatabaseField
    public int currentLength;
    @DatabaseField
    public int totalLength;
    @DatabaseField
    public boolean isSupportRange=false;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    public HashMap<Integer,Integer> ranges;
    @DatabaseField
    public int percent;

    public DownloadEntry(){

    }

    public DownloadEntry(String url) {
        this.url = url;
        this.id=url;
        this.name=url.substring(url.lastIndexOf("/")+1);
        this.status=DownloadStatus.idle;
    }

    public void reset() {
        this.percent=0;
        this.currentLength=0;
        this.ranges=null;
        File file = DownloadConfig.getConfig().getDownloadFile(url);
        if (file.exists())
            file.delete();
    }


    public enum DownloadStatus{
        wating,downloading,connecting,paused,resume,cancel,completed,idle,error
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode()==this.hashCode();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "DownloadEntry: " + url + " is " + status.name() + " with " + currentLength + "/" + totalLength;
    }
}
