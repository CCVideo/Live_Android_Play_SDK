package com.bokecc.dwlivedemo.download;

/**
 * @author Sivin 2019/4/11
 * Description: 列表点击和长按监听
 */
public interface DownloadItemClickListener {
    /**
     * 任务下载并解压完成，点击回调
     * @param path 文件路径
     */
    void onFinishTaskClick(String path);

    /**
     * 长按列表，监听回调
     * @param url 下载地址
     */
    void onItemLongClick(DownLoadBean url);

    /**
     * 开始下载
     * @param downLoadBean
     */
    void onDownload(DownLoadBean downLoadBean);

    /**
     * 暂停下载
     * @param downLoadBean
     */
    void onPause(DownLoadBean downLoadBean);

    /**
     * 重新下载
     * @param downLoadBean
     */
    void reDownLoad(DownLoadBean downLoadBean);
}
