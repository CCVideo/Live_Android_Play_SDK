package com.bokecc.dwlivedemo.download;

public interface DownLoadStatus {
    /**
     * 等待下载
     */
    int DOWNLOAD_WAIT = 0;
    /**
     * 开始下载
     */
    int DOWNLOAD_START =1;
    /**
     * 正在下载
     */
    int DOWNLOADING = 2;
    /**
     * 暂停下载
     */
    int DOWNLOAD_PAUSE = 3;
    /**
     * 下载完成
     */
    int DOWNLOAD_FINISH = 4;
    /**
     * 下载错误
     */
    int DOWNLOAD_ERROR = 5;
    /**
     * 等待解压
     */
    int ZIP_WAIT = 6;
    /**
     * 开始解压
     */
    int ZIP_START = 7;
    /**
     * 正在解压
     */
    int ZIPING = 8;
    /**
     * 暂停解压
     */
    int ZIP_PAUSE = 9;

    /**
     * 解压完成
     */
    int ZIP_FINISH = 10;
    /**
     * 解压错误
     */
    int ZIP_ERROR =11;
    /**
     * 重复下载
     */
    int DOWNLOAD_REPETITION =12;
}
