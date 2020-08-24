package com.bokecc.dwlivedemo.download;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

/**
 * Created by bokecc on 2017/2/17.
 */

public class FileUtil {

    private FileUtil() {
    }

    /**
     * 删除文件夹和文件夹下的所有文件
     *
     * @param file 目标文件
     */
    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }
            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }

    /**
     * 获取文件的解压目录
     *
     * @param oriFile 源文件
     * @return 文件解压后的绝对路径
     */
    public static String getUnzipDir(File oriFile) {
        String fileName = oriFile.getName();
        StringBuilder sb = new StringBuilder();
        sb.append(oriFile.getParent());
        sb.append("/");
        int index = fileName.indexOf(".");
        if (index == -1) {
            sb.append(fileName);
        } else {
            sb.append(fileName.substring(0, index));
        }
        return sb.toString();
    }

    public static String getUnzipDir(String path) {
        File oriFile = new File(path);
        StringBuilder sb = new StringBuilder();
        sb.append(oriFile.getParent());
        sb.append("/");
        int index = path.indexOf(".");
        if (index == -1) {
            sb.append(path);
        } else {
            sb.append(path.substring(0, index));
        }
        return sb.toString();
    }

    /**
     * 获取文件的解压目录名称
     *
     * @param path 源文件
     * @return 文件解压后的绝对路径
     */
    public static String getUnzipFileName(String path) {
        File file = new File(path);
        return file.getName();
    }

    // 获取文件下载路径，兼容10.0
    public static String getCCDownLoadPath(Context context) {
        return getDownloadDirectory(context).getAbsolutePath() + "/CCDownload";
    }

    // 获取SD卡下载路径 兼容10.0
    public static File getDownloadDirectory(Context context) {
        if (Build.VERSION.SDK_INT >= 29) {
            File file = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            if (file != null) {
                return file;
            }
            return context.getFilesDir();
        } else {
            return Environment.getExternalStorageDirectory();

        }
    }
}
