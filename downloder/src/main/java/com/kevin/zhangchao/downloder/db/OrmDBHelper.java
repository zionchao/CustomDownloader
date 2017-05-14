package com.kevin.zhangchao.downloder.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.kevin.zhangchao.downloder.entity.DownloadEntry;

import java.sql.SQLException;

/**
 * Created by ZhangChao on 2017/5/13.
 */

public class OrmDBHelper extends OrmLiteSqliteOpenHelper {

    public static final String DB_NAME = "kevin_downloader";
    public static final int DB_VERSION = 1;

    public OrmDBHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion) {
        super(context, DB_NAME, factory, DB_VERSION);
    }

    public OrmDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, DownloadEntry.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int arg2, int arg3) {
    }

}