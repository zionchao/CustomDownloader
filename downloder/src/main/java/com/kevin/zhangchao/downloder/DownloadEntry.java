package com.kevin.zhangchao.downloder;

import java.io.Serializable;

/**
 * Created by zhangchao_a on 2017/5/12.
 */

public class DownloadEntry implements Serializable{

    public String id;
    public String url;
    public String name;

    public enum DownloadStatus{
        wating,downloading,pause,resume,cancel
    }
    public DownloadStatus status;

    public int currentLength;
    public int totalLength;
}
