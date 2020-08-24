package com.bokecc.dwlivedemo.download;

import android.content.ContentValues;

public class DownLoadBean {
    public final static String ID = "id";//自增id
    public final static String URL = "url";//下载地址
    public final static String PATH = "path";//保存路径
    public final static String TOTAL = "total";//总进度
    public final static String PROGRESS = "progress";//当前进度
    /**
     * 用于存储和解压当前任务的执行状态：
     *存储的状态为 DOWNLOAD_START,DOWNLOADING,DOWNLOAD_FINISH,ZIP_START，ZIP_WAIT ，ZIP_FINISH，ZIP_ERROR
     */
    public final static String TASK_STATUS = "task_status";
    private int id;
    private String url;
    //路径
    private String path;
    //下载状态
    private int taskStatus;
    //下载总进度
    private long total;
    //已下载的进度
    private long progress;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(int taskStatus) {
        this.taskStatus = taskStatus;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }
    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(URL, url);
        cv.put(PATH, path);
        cv.put(TASK_STATUS, taskStatus);
        cv.put(PROGRESS, progress);
        cv.put(TOTAL, total);
        return cv;
    }
    public DownLoadBean copy(){
        DownLoadBean downLoadBean= new DownLoadBean();
        downLoadBean.setId(id);
        downLoadBean.setUrl(url);
        downLoadBean.setPath(path);
        downLoadBean.setTotal(total);
        downLoadBean.setProgress(progress);
        downLoadBean.setTaskStatus(taskStatus);
        return downLoadBean;
    }

    @Override
    public String toString() {
        return "DownLoadBean{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", path='" + path + '\'' +
                ", taskStatus=" + taskStatus +
                ", total=" + total +
                ", progress=" + progress +
                '}';
    }
}
