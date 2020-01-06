package com.bokecc.dwlivedemo.download;

/**
 * @author Sivin 2019/4/11
 * Description: 列表点击和长按监听
 */
public interface DownloadItemClickListener {
    /**
     * 任务下载并解压完成，点击回调
     * @param taskId 任务id
     */
    void onFinishTaskClick(int taskId);

    /**
     * 长按列表，监听回调
     * @param taskId 任务id
     */
    void onItemLongClick(int taskId);
}
