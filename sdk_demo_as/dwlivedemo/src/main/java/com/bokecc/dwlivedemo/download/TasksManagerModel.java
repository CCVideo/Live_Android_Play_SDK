package com.bokecc.dwlivedemo.download;

import android.content.ContentValues;

public class TasksManagerModel {

    /**
     * 当任务处于{@code INIT_STATUS}状态时，表示任务在下载列表中，但是并未加入下载队列
     */
    public final static int INIT_STATUS = 10;

    /**
     *当任务处于{@code PENDING}状态时，表示任务已经加入加载队列，等待下载
     */
    public final static int PENDING = 11;

    /**
     * 当任务处于{@code STARTED}状态时，表示下载该任务的线程开始启动访问网络
     */
    public final static int STARTED = 12;

    /**
     * 当任务处于{@code CONNECTED}状态时，表示网络已成功连接到服务端
     */
    public final static int CONNECTED = 13;

    /**
     * 当任务处于{@code PROGRESS}状态时，表示当前任务正在进行下载中
     */
    public final static int PROGRESS = 14;

    /**
     * 当任务处于{@code PAUSED}状态时，表示当前任务被手动暂停
     */
    public final static int PAUSED = 15;

    /**
     * 当任务处于{@code ERROR}状态时，表示当前任务下载出错
     */
    public final static int ERROR = 16;

    /**
     * 当任务处于{@code COMPLETED}状态时，表示当前任务已经下载完成
     */
    public final static int COMPLETED = 17;

    /**
     * 当任务处于{@code ZIP_WAIT}状态时，表示当前任务已经下载完成，等待解压
     */
    public final static int ZIP_WAIT= 18;

    /**
     * 当任务处于{@code ZIP_ING}状态时，表示当前任务正在解压过程中
     */
    public final static int ZIP_ING = 19;

    /**
     * 当任务处于{@code ZIP_FINISH}状态时，表示当前任务解压已经完成
     */
    public final static int ZIP_FINISH = 20;

    /**
     * 当任务处于{@code ZIP_ERROR}状态时，表示当前任务解压出错
     */
    public final static int ZIP_ERROR = 21;



    //--------------------数据库key----------------------------------

    public final static String ID = "id";
    public final static String NAME = "name";
    public final static String URL = "url";
    public final static String PATH = "path";
    public final static String TOTAL = "total";
    /**
     * 用于存储当前任务的执行状态：数据库分两个：解压状态的数据库，下载引擎数据库
     * SDK上层维护解压状态数据库：存储的状态为 INIT_STATUS，ZIP_WAIT ，ZIP_FINISH，ZIP_ERROR
     *下载状态由下载引擎数据库维护
     *
     * 通俗的讲：只要任务没有下载完成 task_status的值始终是INIT_STATUS，任务下载的状态，需要从下载引擎数据库
     * 中获取，当任务下载完成后，task_status才会依次设置成ZIP_WAIT，ZIP_FINISH等状态
     */
    public final static String TASK_STATUS = "task_status";

    private int id;
    private String name;
    private String url;
    private String path;
    private int taskStatus;
    private long total;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public  int getTaskStatus() {
        return taskStatus;
    }

    public  void setTaskStatus(int taskStatus) {
        this.taskStatus = taskStatus;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(ID, id);
        cv.put(NAME, name);
        cv.put(URL, url);
        cv.put(PATH, path);
        cv.put(TASK_STATUS, taskStatus);
        return cv;
    }
}