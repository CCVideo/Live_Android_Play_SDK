package com.bokecc.dwlivedemo.download;

import com.bokecc.sdk.mobile.live.util.SupZipTool;

import java.io.File;

/**
 * 解压缩核心类
 */
public class UnZiper {

    public interface UnZipListener {

        /**
         * 解压失败
         * @param errorCode 错误码
         * @param message   错误内容
         */
        void onError(int errorCode, String message);

        /**
         * 解压完成
         */
        void onUnZipFinish();
    }


    public static int ZIP_WAIT = 10;
    public static int ZIP_ING = 11;
    public static int ZIP_FINISH = 12;
    public static int ZIP_ERROR = 13;

    Thread unzipThread;
    UnZipListener listener;
    File oriFile;
    String dir;

    int status = ZIP_WAIT;

    /**
     * 构造函数
     * @param listener 监听器
     * @param oriFile 解压原始文件
     * @param dir 解压到的文件夹
     */
    public UnZiper(UnZipListener listener, File oriFile, String dir) {
        this.listener = listener;
        this.oriFile = oriFile;
        this.dir = dir;
    }

    /**
     * 开始解压
     */
    public void unZipFile() {
        if (unzipThread != null && unzipThread.isAlive()) {
            return;
        } else {
            unzipThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    status = ZIP_ING;

                    // 调用解压方法
                    int resultCode = SupZipTool.decompressZipDec(oriFile.getAbsolutePath(), dir);

                    if (resultCode != 0) {
                        status = ZIP_ERROR;
                        if (listener != null) {
                            listener.onError(resultCode, SupZipTool.getResultMessage(resultCode));
                        }
                    } else {
                        oriFile.delete();
                        status = ZIP_FINISH;
                        if (listener != null) {
                            listener.onUnZipFinish();
                        }
                    }
                }
            });
            unzipThread.start();
        }
    }

    /**
     * 获取解压状态
     * @return
     */
    public int getStatus() {
        return status;
    }

    /**
     * 设置解压状态
     * @param status
     * @return
     */
    public UnZiper setStatus(int status) {
        this.status = status;
        return this;
    }

}
