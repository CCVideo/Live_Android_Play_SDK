package com.bokecc.dwlivedemo.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TasksManagerDBOpenHelper extends SQLiteOpenHelper {
    public final static String DATABASE_NAME = "tasksmanager.db";
    public final static int DATABASE_VERSION = 2;

    public TasksManagerDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + TasksManagerDBController.TABLE_NAME
                + String.format(
                "("
                        + "%s INTEGER PRIMARY KEY, " // id, download id
                        + "%s VARCHAR, " // url
                        + "%s VARCHAR, " // path
                        + "%s INTEGER, " // taskStatus
                        + "%s INTEGER, " // PROGRESS
                        + "%s INTEGER " // total
                        + ")"
                , DownLoadBean.ID
                , DownLoadBean.URL
                , DownLoadBean.PATH
                , DownLoadBean.TASK_STATUS
                , DownLoadBean.PROGRESS
                , DownLoadBean.TOTAL
        ));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == 2) {
            db.delete(TasksManagerDBController.TABLE_NAME, null, null);
        }
    }
}
