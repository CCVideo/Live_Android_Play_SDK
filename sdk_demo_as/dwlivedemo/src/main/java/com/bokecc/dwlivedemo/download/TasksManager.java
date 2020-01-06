package com.bokecc.dwlivedemo.download;

import android.util.SparseArray;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadConnectListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author Sivin 2019/4/9
 * Description:
 */
public class TasksManager {
    public static final int CODE_OK = 0;
    public static final int CODE_TASK_ALREADY_EXIST = 1;
    public static final int CODE_URL_ERROR = 2;
    public static final int INSERT_DATA_BASE_ERROR = 3;
    public class Status {
        int val;
        Status(int val) {
            this.val = val;
        }
        int getVal() {
            return val;
        }
        void setVal(int val) {
            this.val = val;
        }
    }

    private final static class HolderClass {
        private final static TasksManager INSTANCE = new TasksManager();
    }

    public static TasksManager getImpl() {
        return HolderClass.INSTANCE;
    }

    /**
     * 下载任务数据库controller
     */
    private TasksManagerDBController dbController;

    /**
     * 列表任务集合
     */
    private List<TasksManagerModel> modelList;

    /**
     * 正在下载的任务集合
     */
    private SparseArray<BaseDownloadTask> taskSparseArray = new SparseArray<>();

    /**
     * 文件下载连接监听
     */
    private FileDownloadConnectListener listener;


    private TasksManager() {
        dbController = new TasksManagerDBController();
        modelList = dbController.getAllTasks();
    }


    public void onCreate(final WeakReference<DownloadView> activityWeakReference) {
        if (!FileDownloader.getImpl().isServiceConnected()) {
            FileDownloader.getImpl().bindService();
            registerServiceConnectionListener(activityWeakReference);
        }
    }


    /**
     * 添加需要下载的任务
     *
     * @param task task
     */
    public void addDownloadTask(final BaseDownloadTask task) {
        taskSparseArray.put(task.getId(), task);
    }

    /**
     * 移除下载任务
     *
     * @param taskId 任务id
     */
    public void removeDownloadTask(final int taskId) {
        taskSparseArray.remove(taskId);
    }

    public void updateViewHolder(final int taskId, final Object holder) {
        final BaseDownloadTask task = taskSparseArray.get(taskId);
        if (task == null) {
            return;
        }
        task.setTag(holder);
    }

    /**
     * 取消全部已经加入下载队列中的任务
     */
    public void releaseDownloadTask() {
        taskSparseArray.clear();
    }

    private void registerServiceConnectionListener(final WeakReference<DownloadView>
                                                           downloadView) {
        if (listener != null) {
            FileDownloader.getImpl().removeServiceConnectListener(listener);
        }

        listener = new FileDownloadConnectListener() {

            @Override
            public void connected() {
                if (downloadView == null
                        || downloadView.get() == null) {
                    return;
                }
                downloadView.get().postNotifyDataChanged();
            }

            @Override
            public void disconnected() {
                if (downloadView == null
                        || downloadView.get() == null) {
                    return;
                }
                downloadView.get().postNotifyDataChanged();
            }
        };

        FileDownloader.getImpl().addServiceConnectListener(listener);
    }

    private void unregisterServiceConnectionListener() {
        FileDownloader.getImpl().removeServiceConnectListener(listener);
        listener = null;
    }


    public void onDestroy() {
        unregisterServiceConnectionListener();
        releaseDownloadTask();
    }

    public boolean isReady() {
        return FileDownloader.getImpl().isServiceConnected();
    }

    public void removeTask(int taskId) {
        TasksManagerModel task = getById(taskId);
        removeDownloadTask(taskId);
        modelList.remove(task);
        dbController.removeTask(taskId);
        FileDownloader.getImpl().clear(taskId, task.getPath());
        FileUtil.delete(new File(FileUtil.getUnzipDir(new File(task.getPath()))));
    }

    public TasksManagerModel get(final int position) {
        return modelList.get(position);
    }

    public TasksManagerModel getById(final int id) {
        for (TasksManagerModel model : modelList) {
            if (model.getId() == id) {
                return model;
            }
        }
        return null;
    }


    /**
     * @param status Download Status
     * @return 是否已经下载完成
     * @see FileDownloadStatus
     */
    public boolean isDownloaded(final int status) {
        return status == FileDownloadStatus.completed;
    }


    public int getStatus(final int id, String path) {
        return FileDownloader.getImpl().getStatus(id, path);
    }

    /**
     * 获取文件的总大小
     *
     * @param id 任务id
     * @return 下载文件的总大小
     */
    public long getTotal(final int id) {
        return FileDownloader.getImpl().getTotal(id);
    }

    /**
     * 获取当前文件下载多少
     *
     * @param id taskId
     * @return 当前下载的字节数
     */
    public long getSoFar(final int id) {
        return FileDownloader.getImpl().getSoFar(id);
    }

    /**
     * @return 当前的任务数
     */
    public int getTaskCounts() {
        return modelList.size();
    }


    /**
     * 添加一个任务
     *
     * @param name 文件名
     * @param url  文件下载url
     * @param path 文件存放路径
     * @return 0 :任务添加成功，非0任务已存在
     */
    public int addTask(String name, final String url, final String path) {
        Status status = new Status(CODE_OK);
        TasksManagerModel tasksManagerModel = dbController.addTask(name, url, path, status);
        if (tasksManagerModel != null) {
            modelList.add(tasksManagerModel);
        }
        return status.getVal();
    }

    public void updateTaskModelStatus(int taskId, int status) {
        dbController.updateTaskModelStatus(taskId, status);
    }

    public void updateTaskModelTotal(int id, long total) {
        dbController.updateTaskModelTotal(id, total);
    }
}
