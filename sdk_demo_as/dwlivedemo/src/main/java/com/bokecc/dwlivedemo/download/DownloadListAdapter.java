package com.bokecc.dwlivedemo.download;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bokecc.dwlivedemo.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Sivin on 2019/4/13.
 */

public class DownloadListAdapter extends RecyclerView.Adapter<DownloadListAdapter.DownloadViewHolder> {

    private static final String TAG = DownloadListAdapter.class.getSimpleName();
    /**
     * 上下文
     */
    private Context context;
    private List<DownLoadBean> dates;
    /**
     * 列表点击和长按监听器
     */
    private DownloadItemClickListener mItemClickListener;

    public DownloadListAdapter(Context context) {
        this.context = context;
    }

    public void setItemClickListener(DownloadItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void setDates(List<DownLoadBean> dates) {
        if (this.dates==null){
            this.dates = new ArrayList<>();
        }
        if (dates.size()<this.dates.size()||dates.size()==0){
            //删除的话 就需要刷新了
            this.dates.clear();
            this.dates.addAll(dates);
            notifyDataSetChanged();
        }else{
            DownLoadDiffCallback diffCallback =new  DownLoadDiffCallback(this.dates, dates);
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
            this.dates.clear();
            this.dates.addAll(dates);
            diffResult.dispatchUpdatesTo(this);
        }
    }

    public List<DownLoadBean> getDates() {
        if (this.dates==null){
            this.dates = new ArrayList<>();
        }
        return dates;
    }

    @NonNull
    @Override
    public DownloadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.download_single_line, parent, false);
        return new DownloadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DownloadViewHolder holder, int position) {
        final DownLoadBean downLoadBean = dates.get(position);
        if (downLoadBean == null) return;
        //设置名称
        String fileName = downLoadBean.getUrl().substring(downLoadBean.getUrl().lastIndexOf("/") + 1);
        holder.fileName.setText(fileName);
        //设置进度
        float percent = 0.0f;
        if (downLoadBean.getTotal() > 0) {
            percent = downLoadBean.getProgress() * 1.0f / downLoadBean.getTotal();
        }
        if (downLoadBean.getTaskStatus() >= DownLoadStatus.DOWNLOAD_FINISH) {
            percent = 100.0f;
        }
        String progress;
        if (downLoadBean.getTaskStatus()>=DownLoadStatus.DOWNLOAD_FINISH){
            progress = Formatter.formatFileSize(context, downLoadBean.getTotal()) + "/" + Formatter.formatFileSize(context, downLoadBean.getTotal()) + "(" + (int) (percent ) + "%)";
        }else{
            progress = Formatter.formatFileSize(context, downLoadBean.getProgress()) + "/" + Formatter.formatFileSize(context, downLoadBean.getTotal()) + "(" + (int) (percent*100) + "%)";
        }
        holder.downloadTv.setText(progress);
        holder.downloadPb.setMax(100);
        holder.downloadPb.setProgress((int) (percent * 100));
        //设置下载状态
        holder.downloadSpeedTv.setText(holder.parseStatus(downLoadBean.getTaskStatus()));
        //设置图片
        holder.setDownloadProgressViewStyle(downLoadBean.getTaskStatus());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downLoadBean.getTaskStatus() == DownLoadStatus.ZIP_FINISH) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onFinishTaskClick(downLoadBean.getPath());
                    }
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemLongClick(downLoadBean);
                }
                return false;
            }
        });
        holder.downloadIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downLoadBean.getTaskStatus()==DownLoadStatus.DOWNLOAD_PAUSE){
                    //如果是暂停下载就去下载
                    mItemClickListener.onDownload(downLoadBean);
                }else if (downLoadBean.getTaskStatus()==DownLoadStatus.DOWNLOADING){
                    //如果是下载状态就去暂停
                    mItemClickListener.onPause(downLoadBean);
                }else if ( downLoadBean.getTaskStatus()==DownLoadStatus.DOWNLOAD_ERROR){
                    mItemClickListener.reDownLoad(downLoadBean);
                }
                //设置下载状态
                holder.downloadSpeedTv.setText(holder.parseStatus(downLoadBean.getTaskStatus()));
                //设置图片
                holder.setDownloadProgressViewStyle(downLoadBean.getTaskStatus());
            }
        });
    }

    @Override
    public int getItemCount() {
        return dates==null?0:dates.size();
    }

    public class DownloadViewHolder extends RecyclerView.ViewHolder {

        private TextView fileName;
        private TextView downloadTv;
        private ProgressBar downloadPb;
        private ImageView downloadIcon;
        private TextView downloadSpeedTv;
        DownloadViewHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.id_file_name);
            downloadTv = itemView.findViewById(R.id.id_download_progress_numberic);
            downloadPb = itemView.findViewById(R.id.id_download_progressbar);
            downloadIcon = itemView.findViewById(R.id.id_download_icon);
            downloadSpeedTv = itemView.findViewById(R.id.id_download_speed);
        }
        private void setDownloadProgressViewStyle(int status) {
            switch (status) {
                case DownLoadStatus.DOWNLOAD_PAUSE:
                    downloadIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.download_wait));
                    downloadPb.setProgressDrawable(context.getResources().getDrawable(R.drawable.download_progress_finish_bg));
                    break;
                case DownLoadStatus.DOWNLOADING:
                    downloadIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.download_ing));
                    downloadPb.setProgressDrawable(context.getResources().getDrawable(R.drawable.download_progress_ing_bg));
                    break;
                case DownLoadStatus.DOWNLOAD_ERROR:
                    downloadIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.download_fail));
                    downloadPb.setProgressDrawable(context.getResources().getDrawable(R.drawable.download_progress_fail_bg));
                    break;
                default:
                    downloadIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.download_success));
                    downloadPb.setProgressDrawable(context.getResources().getDrawable(R.drawable.download_progress_finish_bg));
                    break;
            }
        }

        private String parseStatus(int status) {
            switch (status) {
                case DownLoadStatus.DOWNLOAD_WAIT:
                    return "等待中";
                case DownLoadStatus.DOWNLOAD_START:
                    return "开始下载";
                case DownLoadStatus.DOWNLOADING:
                    return "下载中";
                case DownLoadStatus.DOWNLOAD_PAUSE:
                    return "暂停";
                case DownLoadStatus.DOWNLOAD_FINISH:
                    return "下载完成";
                case DownLoadStatus.DOWNLOAD_ERROR:
                    return "下载失败";
                case DownLoadStatus.ZIP_WAIT:
                    return "下载完成  等待解压";
                case DownLoadStatus.ZIP_START:
                    return "下载完成  开始解压";
                case DownLoadStatus.ZIPING:
                    return "下载完成  正在解压";
                case DownLoadStatus.ZIP_PAUSE:
                    return "下载完成  暂停解压";
                case DownLoadStatus.ZIP_FINISH:
                    return "下载完成  解压完成";
                case DownLoadStatus.ZIP_ERROR:
                    return "下载完成  解压失败";
                default:
                    return "下载失败";
            }
        }

    }
}
