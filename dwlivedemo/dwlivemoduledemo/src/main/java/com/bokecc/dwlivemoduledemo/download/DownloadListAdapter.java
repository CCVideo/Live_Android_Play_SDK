package com.bokecc.dwlivemoduledemo.download;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bokecc.dwlivemoduledemo.R;
import com.bokecc.sdk.mobile.live.util.LogUtil;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import java.io.File;
import java.util.Locale;


/**
 * Created by Sivin on 2019/4/13.
 */

public class DownloadListAdapter extends RecyclerView.Adapter<DownloadListAdapter.DownloadViewHolder> {

    private static final String TAG = DownloadListAdapter.class.getSimpleName();
    /**
     * 上下文
     */
    private Context context;

    /**
     * 文件解压任务队列
     */
    private SparseArray<UnZiper> unZipTaskQueue;

    /**
     * 列表点击和长按监听器
     */
    private DownloadItemClickListener mItemClickListener;

    public DownloadListAdapter(Context context) {
        this.context = context;
        unZipTaskQueue = new SparseArray<>();
    }

    public void setItemClickListener(DownloadItemClickListener listener) {
        mItemClickListener = listener;
    }

    //触发下载或暂停的动作
    private View.OnClickListener taskActionOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getTag() == null) {
                return;
            }

            DownloadViewHolder holder = (DownloadViewHolder) v.getTag();
            int taskId = holder.getId();

            TasksManagerModel tasksModel = TasksManager.getImpl().getById(taskId);
            if (tasksModel.getTaskStatus() == TasksManagerModel.INIT_STATUS && TasksManager.getImpl().isReady()) {
                final int status = TasksManager.getImpl().getStatus(taskId, tasksModel.getPath());
                //任务状态
                switch (status) {
                    case FileDownloadStatus.paused: //任务处于暂停，点击开始加入下载队列
                    case FileDownloadStatus.error://任务出错，点击从新加入下载队列
                        startDownloadTask(holder, tasksModel);
                        break;
                    case FileDownloadStatus.pending:
                    case FileDownloadStatus.connected:
                    case FileDownloadStatus.started:
                    case FileDownloadStatus.progress:
                        FileDownloader.getImpl().pause(taskId); //暂停任务
                        break;
                }
            }

            if (tasksModel.getTaskStatus() == TasksManagerModel.ZIP_FINISH) {
                if (mItemClickListener != null) {
                    mItemClickListener.onFinishTaskClick(holder.getId());
                }
            }
        }
    };

    private void startDownloadTask(DownloadViewHolder holder, TasksManagerModel tasksModel) {
        final BaseDownloadTask downloadTask = FileDownloader.getImpl().create(tasksModel.getUrl())
                .setPath(tasksModel.getPath())
                .setAutoRetryTimes(3)
                .setCallbackProgressTimes(100)
                .setListener(taskDownloadListener);
        TasksManager.getImpl()
                .addDownloadTask(downloadTask);
        TasksManager.getImpl()
                .updateViewHolder(holder.getId(), holder);
        downloadTask.start();
    }


    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.download_single_line, parent, false);
        view.setOnClickListener(taskActionOnClickListener);
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mItemClickListener != null) {
                    DownloadViewHolder holder = (DownloadViewHolder) v.getTag();
                    if (holder == null) return false;
                    int taskId = holder.getId();
                    TasksManagerModel task = TasksManager.getImpl().getById(taskId);
                    if (task != null && task.getTaskStatus() != TasksManagerModel.ZIP_WAIT) {
                        mItemClickListener.onItemLongClick(taskId);
                    }
                }
                return false;
            }
        });
        return new DownloadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DownloadViewHolder holder, int position) {
        final TasksManagerModel tasksModel = TasksManager.getImpl().get(position);
        if (tasksModel == null) return;

        //此处为了点击该item时能获取到holder里的数据
        holder.itemView.setTag(holder);
        holder.fileName.setText(tasksModel.getName());

        //将taskId和position更新到Holder中
        holder.update(tasksModel.getId());
        //将该task与holder关联起来
        TasksManager.getImpl().updateViewHolder(tasksModel.getId(), holder);

        final int taskStatus = tasksModel.getTaskStatus();

        if (taskStatus == TasksManagerModel.ZIP_WAIT) {  //任务状态为等待解压，则开始解压
            UnZiper unZiper = unZipTaskQueue.get(tasksModel.getId());
            // 防止对同一任务多次进行解压
            if (unZiper != null) return;
            startUnzip(holder, tasksModel.getId());
            holder.updateUnZipStatus(TasksManagerModel.ZIP_WAIT, tasksModel.getTotal());
            return;
        }

        if (taskStatus == TasksManagerModel.ZIP_ERROR) {
            holder.updateUnZipStatus(TasksManagerModel.ZIP_ERROR, tasksModel.getTotal());
            return;
        }

        if (taskStatus == TasksManagerModel.ZIP_FINISH && TasksManager.getImpl().isReady()) {
            holder.updateUnZipStatus(TasksManagerModel.ZIP_FINISH, tasksModel.getTotal());
            return;
        }

        //该状态为，任务处于未下载完成的状态
        if (taskStatus == TasksManagerModel.INIT_STATUS && TasksManager.getImpl().isReady()) {

            final int status = TasksManager.getImpl().getStatus(tasksModel.getId(), tasksModel.getPath());

            if (status == FileDownloadStatus.INVALID_STATUS) {
                startDownloadTask(holder, tasksModel);
                holder.updateNotDownloaded(TasksManagerModel.PENDING, 0, 0);

            } else if (status == FileDownloadStatus.pending || status == FileDownloadStatus.started ||
                    status == FileDownloadStatus.connected) {
                // 任务开始但文件还没有创建
                holder.updateDownloading(status, TasksManager.getImpl().getSoFar(tasksModel.getId())
                        , TasksManager.getImpl().getTotal(tasksModel.getId()));

            } else if (!new File(tasksModel.getPath()).exists() &&
                    !new File(FileDownloadUtils.getTempPath(tasksModel.getPath())).exists()) {
                // 本地文件不存在，缓存也不存在,文件出错
                holder.updateNotDownloaded(TasksManagerModel.ERROR, 0, 0);

            } else if (TasksManager.getImpl().isDownloaded(status)) {
                // 任务文件已经下载完成
                holder.updateDownloaded(TasksManager.getImpl().getTotal(tasksModel.getId()));

            } else if (status == FileDownloadStatus.progress) {
                //文件正在下载中
                holder.updateDownloading(TasksManagerModel.PROGRESS, TasksManager.getImpl().getSoFar(tasksModel.getId())
                        , TasksManager.getImpl().getTotal(tasksModel.getId()));

            } else {
                // 任务处于暂停中
                holder.updateNotDownloaded(TasksManagerModel.PAUSED, TasksManager.getImpl().getSoFar(tasksModel.getId())
                        , TasksManager.getImpl().getTotal(tasksModel.getId()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return TasksManager.getImpl().getTaskCounts();
    }


    private void startUnzip(@NonNull final DownloadViewHolder holder, final int taskId) {

        final TasksManagerModel tasksModel = TasksManager.getImpl().getById(taskId);
        if (tasksModel == null) return;

        File ccrFile = new File(tasksModel.getPath());
        UnZiper unZiper = new UnZiper(new UnZiper.UnZipListener() {
            @Override
            public void onError(int errorCode, String message) {
                holder.fileName.post(new Runnable() {
                    @Override
                    public void run() {
                        unZipTaskQueue.remove(tasksModel.getId());
                        tasksModel.setTaskStatus(TasksManagerModel.ZIP_ERROR);
                        holder.updateUnZipStatus(TasksManagerModel.ZIP_ERROR, tasksModel.getTotal());
                        //更新数据库
                        TasksManager.getImpl().updateTaskModelStatus(tasksModel.getId(), TasksManagerModel.ZIP_ERROR);
                    }
                });
            }

            @Override
            public void onUnZipFinish() {
                holder.fileName.post(new Runnable() {
                    @Override
                    public void run() {
                        unZipTaskQueue.remove(tasksModel.getId());
                        holder.updateUnZipStatus(TasksManagerModel.ZIP_FINISH, tasksModel.getTotal());
                        tasksModel.setTaskStatus(TasksManagerModel.ZIP_FINISH);

                        //更新数据库
                        TasksManager.getImpl().updateTaskModelStatus(tasksModel.getId(), TasksManagerModel.ZIP_FINISH);

                    }
                });
            }
        }, ccrFile, FileUtil.getUnzipDir(ccrFile));

        unZipTaskQueue.append(taskId, unZiper);
        unZiper.unZipFile();
    }


    /**
     * 任务下载监听器
     */
    private FileDownloadListener taskDownloadListener = new FileDownloadSampleListener() {

        private DownloadViewHolder checkCurrentHolder(final BaseDownloadTask task) {
            final DownloadViewHolder holder = (DownloadViewHolder) task.getTag();
            if (holder.getId() != task.getId()) {
                return null;
            }
            return holder;
        }

        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.pending(task, soFarBytes, totalBytes);
            final DownloadViewHolder holder = checkCurrentHolder(task);
            if (holder == null) {
                return;
            }
            holder.updateDownloading(TasksManagerModel.PENDING, soFarBytes, totalBytes);
        }

        @Override
        protected void started(BaseDownloadTask task) {
            super.started(task);
            final DownloadViewHolder holder = checkCurrentHolder(task);
            if (holder == null) {
                return;
            }
            holder.updateDownloading(TasksManagerModel.STARTED, 0, 0);
        }

        @Override
        protected void connected(BaseDownloadTask task, String eTag, boolean isContinue, int soFarBytes, int totalBytes) {
            super.connected(task, eTag, isContinue, soFarBytes, totalBytes);
            final DownloadViewHolder holder = checkCurrentHolder(task);
            if (holder == null) {
                return;
            }
            holder.updateDownloading(TasksManagerModel.CONNECTED, soFarBytes, totalBytes);
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.progress(task, soFarBytes, totalBytes);
            final DownloadViewHolder holder = checkCurrentHolder(task);
            if (holder == null) {
                return;
            }
            holder.updateDownloading(TasksManagerModel.PROGRESS, soFarBytes, totalBytes, task.getSpeed());
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            super.error(task, e);
            LogUtil.e(TAG,e.getMessage());
            final DownloadViewHolder holder = checkCurrentHolder(task);
            if (holder == null) {
                return;
            }
            holder.updateNotDownloaded(TasksManagerModel.ERROR, 0, 0);
            TasksManager.getImpl().removeDownloadTask(task.getId());
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.paused(task, soFarBytes, totalBytes);
            final DownloadListAdapter.DownloadViewHolder tag = checkCurrentHolder(task);
            if (tag == null) {
                return;
            }
            tag.updateNotDownloaded(TasksManagerModel.PAUSED, soFarBytes, totalBytes);
            TasksManager.getImpl().removeDownloadTask(task.getId());
        }

        @Override
        protected void completed(final BaseDownloadTask task) {
            super.completed(task);
            final DownloadViewHolder holder = checkCurrentHolder(task);
            if (holder == null) {
                return;
            }

            holder.updateDownloaded(task.getLargeFileTotalBytes());
            TasksManager.getImpl().updateTaskModelTotal(task.getId(), task.getLargeFileTotalBytes());
            TasksManager.getImpl().getById(task.getId()).setTotal(task.getLargeFileTotalBytes());

            //下载完成移除任务
            TasksManager.getImpl().removeDownloadTask(task.getId());
            //等待解压
            holder.updateUnZipStatus(TasksManagerModel.ZIP_WAIT, task.getLargeFileTotalBytes());
            startUnzip(holder, task.getId());
        }
    };


    public class DownloadViewHolder extends RecyclerView.ViewHolder {

        private int id; //持有当前任务的id
        private TextView fileName;
        private TextView downloadTv;
        private ProgressBar downloadPb;
        private ImageView downloadIcon;
        private TextView downloadSpeedTv;

        void update(final int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        DownloadViewHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.id_file_name);
            downloadTv = itemView.findViewById(R.id.id_download_progress_numberic);
            downloadPb = itemView.findViewById(R.id.id_download_progressbar);
            downloadIcon = itemView.findViewById(R.id.id_download_icon);
            downloadSpeedTv = itemView.findViewById(R.id.id_download_speed);
        }


        void updateNotDownloaded(final int status, final long soFar, final long total) {
            setDownloadProgressViewStyle(status);
            String statusStr = parseStatus(status);
            downloadSpeedTv.setText(statusStr);
            float percent = 0.0f;
            if (total > 0) {
                percent = soFar * 1.0f / total;
            }
            if (status >= TasksManagerModel.COMPLETED) {
                percent = 100.0f;
            }
            String progress = Formatter.formatFileSize(context, soFar) + "/" + Formatter.formatFileSize(context, total) + "(" + (int) (percent * 100) + "%)";
            downloadTv.setText(progress);
            downloadPb.setMax(100);
            downloadPb.setProgress((int) (percent * 100));
        }


        void updateDownloading(final int status, long soFarBytes, long totalBytes) {
            updateDownloading(status, soFarBytes, totalBytes, 0);
        }

        void updateDownloading(final int status, long soFarBytes, long totalBytes, int downloadSpeed) {
            setDownloadProgressViewStyle(status);
            String statusStr = parseStatus(status);
            if (downloadSpeed != 0) {
                statusStr = String.format(Locale.CHINA, "下载中 %dkB/S", downloadSpeed);
            }
            downloadSpeedTv.setText(statusStr);
            float percent = 0.0f;
            if (totalBytes > 0) {
                percent = soFarBytes * 1.0f / totalBytes;
            }
            if (status >= TasksManagerModel.COMPLETED) {
                percent = 100.0f;
            }
            String progress = Formatter.formatFileSize(context, soFarBytes) + "/" + Formatter.formatFileSize(context, totalBytes) + "(" + (int) (percent * 100) + "%)";
            downloadTv.setText(progress);
            downloadPb.setMax(100);
            downloadPb.setProgress((int) (percent * 100));
        }


        void updateDownloaded(long total) {
            setDownloadProgressViewStyle(TasksManagerModel.ZIP_WAIT);
            String progress = Formatter.formatFileSize(context, total) + "/" + Formatter.formatFileSize(context, total) + "(" + (int) (1.0f * 100) + "%)";
            downloadTv.setText(progress);
            downloadPb.setMax(100);
            downloadPb.setProgress(100);
            downloadSpeedTv.setText("下载完成  等待解压");
        }

        void updateUnZipStatus(final int status, long total) {
            String statusStr = parseStatus(status);
            downloadSpeedTv.setText(statusStr);
            setDownloadProgressViewStyle(status);
            String progress = Formatter.formatFileSize(context, total) + "/" + Formatter.formatFileSize(context, total) + "(" + (int) (1.0f * 100) + "%)";
            downloadTv.setText(progress);
            downloadPb.setMax(100);
            downloadPb.setProgress(100);
        }

        private String parseStatus(int status) {
            switch (status) {
                case TasksManagerModel.PENDING:
                    return "等待中";
                case TasksManagerModel.STARTED:
                    return "连接中";
                case TasksManagerModel.CONNECTED:
                    return "资源已连接";
                case TasksManagerModel.PROGRESS:
                    return "下载中";
                case TasksManagerModel.PAUSED:
                    return "已暂停";
                case TasksManagerModel.COMPLETED:
                    return "已完成";
                case TasksManagerModel.ZIP_WAIT:
                    return "下载完成  等待解压";
                case TasksManagerModel.ZIP_ING:
                    return "下载完成  处理中";
                case TasksManagerModel.ZIP_FINISH:
                    return "下载完成  解压完成";
                case TasksManagerModel.ZIP_ERROR:
                    return "下载完成  解压失败";
                default:
                    return "下载失败";
            }
        }


        private void setDownloadProgressViewStyle(int status) {
            switch (status) {
                case TasksManagerModel.PAUSED:
                    downloadIcon.setImageDrawable(context.getResources().getDrawable(R.mipmap.download_wait));
                    downloadPb.setProgressDrawable(context.getResources().getDrawable(R.drawable.download_progress_finish_bg));
                    break;
                case TasksManagerModel.PENDING:
                case TasksManagerModel.PROGRESS:
                    downloadIcon.setImageDrawable(context.getResources().getDrawable(R.mipmap.download_ing));
                    downloadPb.setProgressDrawable(context.getResources().getDrawable(R.drawable.download_progress_ing_bg));
                    break;
                case TasksManagerModel.ERROR:
                    downloadIcon.setImageDrawable(context.getResources().getDrawable(R.mipmap.download_fail));
                    downloadPb.setProgressDrawable(context.getResources().getDrawable(R.drawable.download_progress_fail_bg));
                    break;
                default:
                    downloadIcon.setImageDrawable(context.getResources().getDrawable(R.mipmap.download_success));
                    downloadPb.setProgressDrawable(context.getResources().getDrawable(R.drawable.download_progress_finish_bg));
                    break;
            }
        }


    }
}
