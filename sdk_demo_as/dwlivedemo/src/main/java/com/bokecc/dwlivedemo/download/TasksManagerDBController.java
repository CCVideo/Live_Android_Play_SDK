package com.bokecc.dwlivedemo.download;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.bokecc.dwlivedemo.DWApplication;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.util.ArrayList;
import java.util.List;

public class TasksManagerDBController {

    public final static String TABLE_NAME = "tasksmanger";

    private final SQLiteDatabase db;

    TasksManagerDBController() {
        TasksManagerDBOpenHelper openHelper = new TasksManagerDBOpenHelper(DWApplication.getContext());
        db = openHelper.getWritableDatabase();
    }

    public List<TasksManagerModel> getAllTasks() {
        final Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        final List<TasksManagerModel> list = new ArrayList<>();
        try {
            if (!c.moveToLast()) {
                return list;
            }
            do {
                TasksManagerModel model = new TasksManagerModel();
                model.setId(c.getInt(c.getColumnIndex(TasksManagerModel.ID)));
                model.setName(c.getString(c.getColumnIndex(TasksManagerModel.NAME)));
                model.setUrl(c.getString(c.getColumnIndex(TasksManagerModel.URL)));
                model.setPath(c.getString(c.getColumnIndex(TasksManagerModel.PATH)));
                model.setTaskStatus(c.getInt(c.getColumnIndex(TasksManagerModel.TASK_STATUS)));
                model.setTotal(c.getLong(c.getColumnIndex(TasksManagerModel.TOTAL)));
                list.add(model);
            } while (c.moveToPrevious());
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return list;
    }

    public TasksManagerModel addTask(String name, final String url, final String path,TasksManager.Status status) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(path)) {
            status.setVal(TasksManager.CODE_URL_ERROR);
            return null;
        }
        // 必须用FileDownloadUtils.generateId去关联TasksManagerModel和FileDownloader
        final int id = FileDownloadUtils.generateId(url, path + "/" + name);
        TasksManagerModel model = TasksManager.getImpl().getById(id);
        if (model != null) { //任务已存在
            status.setVal(TasksManager.CODE_TASK_ALREADY_EXIST);
            return null;
        }
        model = new TasksManagerModel();
        model.setId(id);
        model.setName(name);
        model.setUrl(url);
        model.setPath(path + "/" + name);
        model.setTaskStatus(TasksManagerModel.INIT_STATUS);
        final boolean succeed = db.insert(TABLE_NAME, null, model.toContentValues()) != -1;
        if(!succeed){
            status.setVal(TasksManager.INSERT_DATA_BASE_ERROR);
        }
        return succeed ? model : null;
    }


    public int updateTaskModelStatus(int taskId, int status) {
        ContentValues values = new ContentValues();
        values.put(TasksManagerModel.TASK_STATUS, status);
        int ret = db.update(TABLE_NAME, values, "id=?", new String[]{taskId + ""});
        return ret;
    }

    public int updateTaskModelTotal(int taskId, long total) {
        ContentValues values = new ContentValues();
        values.put(TasksManagerModel.TOTAL, total);
        int ret = db.update(TABLE_NAME, values, "id=?", new String[]{taskId + ""});
        return ret;
    }

    public int removeTask(int taskId){

       int ret = db.delete(TABLE_NAME,"id=?", new String[]{taskId + ""});

       return ret;
    }
}
