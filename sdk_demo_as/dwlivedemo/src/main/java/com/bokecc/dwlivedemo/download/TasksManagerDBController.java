package com.bokecc.dwlivedemo.download;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bokecc.dwlivedemo.DWApplication;

import java.util.ArrayList;
import java.util.List;

import static com.bokecc.dwlivedemo.download.DownLoadBean.PROGRESS;
import static com.bokecc.dwlivedemo.download.DownLoadBean.TOTAL;

public class TasksManagerDBController {

    public final static String TABLE_NAME = "download";

    private final SQLiteDatabase db;

    TasksManagerDBController() {
        TasksManagerDBOpenHelper openHelper = new TasksManagerDBOpenHelper(DWApplication.getContext());
        db = openHelper.getWritableDatabase();
    }

    public List<DownLoadBean> getAllTasks() {
        final Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        final List<DownLoadBean> list = new ArrayList<>();
        try {
            if (!c.moveToLast()) {
                return list;
            }
            do {
                DownLoadBean model = new DownLoadBean();
                model.setId(c.getInt(c.getColumnIndex(DownLoadBean.ID)));
                model.setUrl(c.getString(c.getColumnIndex(DownLoadBean.URL)));
                model.setPath(c.getString(c.getColumnIndex(DownLoadBean.PATH)));
                model.setTaskStatus(c.getInt(c.getColumnIndex(DownLoadBean.TASK_STATUS)));
                model.setTotal(c.getLong(c.getColumnIndex(TOTAL)));
                model.setProgress(c.getLong(c.getColumnIndex(PROGRESS)));
                list.add(model);
            } while (c.moveToPrevious());
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return list;
    }

    /**
     *
     * @param downLoadBean
     * @return 0是成功  -1是参数为null   -2是重复  -3是添加数据库失败
     */
    public int addTask(DownLoadBean downLoadBean) {
        if (downLoadBean==null) {
            return -1;
        }
        //先查询是否有相同的
        try{
            DownLoadBean downLoadBeanByUrl = getDownLoadBeanByUrl(downLoadBean.getUrl());
            if (downLoadBeanByUrl!=null){
                return -2;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        boolean succeed = db.insert(TABLE_NAME, null, downLoadBean.toContentValues()) != -1;
        return succeed?0:-3;
    }

    private DownLoadBean getDownLoadBeanByUrl(String url){
        Cursor cursor = db.rawQuery("select * from "+TABLE_NAME+" where url=?", new String[]{url});
        DownLoadBean downLoadBean = null;
        try {
            do {
                if (!cursor.moveToLast()) {
                    return downLoadBean;
                }
                downLoadBean = new DownLoadBean();
                downLoadBean.setId(cursor.getInt(cursor.getColumnIndex(DownLoadBean.ID)));
                downLoadBean.setUrl(cursor.getString(cursor.getColumnIndex(DownLoadBean.URL)));
                downLoadBean.setPath(cursor.getString(cursor.getColumnIndex(DownLoadBean.PATH)));
                downLoadBean.setTaskStatus(cursor.getInt(cursor.getColumnIndex(DownLoadBean.TASK_STATUS)));
                downLoadBean.setTotal(cursor.getLong(cursor.getColumnIndex(TOTAL)));
                downLoadBean.setProgress(cursor.getLong(cursor.getColumnIndex(PROGRESS)));
            } while (cursor.moveToPrevious());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return downLoadBean;
    }

    public int update(DownLoadBean downLoadBean) {
        ContentValues values = downLoadBean.toContentValues();
        int ret = db.update(TABLE_NAME, values, "url=?", new String[]{downLoadBean.getUrl() + ""});
        return ret;
    }

    public int removeTask(String url){

       int ret = db.delete(TABLE_NAME,"url=?", new String[]{url + ""});

       return ret;
    }
    public void onDestroy(){
        if (db!=null){
            db.close();
        }
    }

    public int updateTaskModelProgress(String url, long process, long total) {
        ContentValues cv = new ContentValues();
        cv.put(PROGRESS, process);
        cv.put(TOTAL, total);
        int ret = db.update(TABLE_NAME, cv, "url=?", new String[]{url});
        return ret;
    }
}
