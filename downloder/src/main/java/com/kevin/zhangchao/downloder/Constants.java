package com.kevin.zhangchao.downloder;

/**
 * Created by zhangchao_a on 2017/5/12.
 */

public class Constants {
    public static final String KEY_DOWNLOAD_ENTRY = "key_download_entry";
    public static final String KEY_DOWNLOAD_ACTION ="key_download_action" ;
    public static final int KEY_DOWNLOAD_ACTION_ADD = 1;
    public static final int KEY_DOWNLOAD_ACTION_PAUSE = 2;
    public static final int KEY_DOWNLOAD_ACTION_RESUME = 3;
    public static final int KEY_DOWNLOAD_ACTION_CANCEL = 4;
    public static final int KEY_DOWNLOAD_ACTION_PAUSE_ALL = 5;
    public static final int KEY_DOWNLOAD_ACTION_RECOVER_ALL = 6;

    public static int KEY_DOWNLOAD_MAX_COUNT=3;
    public static final int MAX_DOWNLOAD_TASKS = 3;
    public static final int MAX_DOWNLOAD_THREADS = 3;
    public static final int CONNECT_TIME = 10 * 1000;
    public static final int READ_TIME = 10 * 1000;
}
