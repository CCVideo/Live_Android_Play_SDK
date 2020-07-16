package com.bokecc.dwlivedemo.download;

import android.os.Handler;

import com.bokecc.download.HdDownloadListener;
import com.bokecc.download.HdHuodeException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownLoadManager {
    /**
     * 下载的缓存队列 解压的缓存队列
     */
    private LinkedList<DownLoadBean> downLoadQuery, unZipquery;
    /**
     * 正在下载的队列
     */
    private LinkedHashMap<String, HdDwonLoadUtils> downLoaddingQuery;
    /**
     * 数据库帮助类
     */
    private static TasksManagerDBController tasksManagerDBController;
    /**
     * handler 主要作用就是将回调回抛到主线程
     */
    private Handler handler;
    /**
     * 回调
     */
    private DownLoadTaskListener downLoadTaskListener;
    /**
     * 解压器
     */
    private UnZiper unZiper;
    /**
     * 是否正在解压
     */
    private AtomicBoolean isUnZipping = new AtomicBoolean(false);

    public void setDownLoadTaskListener(DownLoadTaskListener downLoadTaskListener) {
        this.downLoadTaskListener = downLoadTaskListener;
    }

    public DownLoadManager() {
        downLoadQuery = new LinkedList<>();
        unZipquery = new LinkedList<>();
        downLoaddingQuery = new LinkedHashMap<>();
        tasksManagerDBController = new TasksManagerDBController();
        handler = new Handler();
    }

    /**
     * 获取数据库所有数据
     */
    public void getAllLocalData() {
        ThreadPoolUtils.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final List<DownLoadBean> allTasks = tasksManagerDBController.getAllTasks();

                for (final DownLoadBean downLoadBean : allTasks) {
                    if (downLoadBean.getTaskStatus() == DownLoadStatus.DOWNLOAD_WAIT || downLoadBean.getTaskStatus() == DownLoadStatus.DOWNLOADING) {
                        //如果是等待下载或者正在下载 直接去下载
                        ThreadPoolUtils.getInstance().execute(new Runnable() {
                            @Override
                            public void run() {
                                startDownload(downLoadBean);
                            }
                        });
                    } else if (downLoadBean.getTaskStatus() == DownLoadStatus.ZIP_WAIT || downLoadBean.getTaskStatus() == DownLoadStatus.ZIPING) {
                        //如果是等待解压或者正在解压 直接去解压
                        startUnzip(downLoadBean);
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (downLoadTaskListener != null) {
                            downLoadTaskListener.getAllDateResult(allTasks);
                        }
                    }
                });
                ThreadPoolUtils.getInstance().cancel(this);
            }
        });
    }

    public void reStart(final DownLoadBean downLoadBean) {
        ThreadPoolUtils.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                startDownload(downLoadBean);
                ThreadPoolUtils.getInstance().cancel(this);
            }
        });
    }

    /**
     * 开始下载
     *
     * @param downLoadBean
     */
    private void startDownload(final DownLoadBean downLoadBean) {
        if (downLoaddingQuery.size() < 5) {
            downLoadBean.setTaskStatus(DownLoadStatus.DOWNLOAD_START);
            tasksManagerDBController.update(downLoadBean);
            statusChange(DownLoadStatus.DOWNLOAD_START, downLoadBean);

            //判断文件是否存在
            File file = new File(downLoadBean.getPath());
            if (!file.exists()) {
                File fileParent = file.getParentFile();//返回的是File类型,可以调用exsit()等方法
                if (!fileParent.exists()) {
                    fileParent.mkdirs();// 能创建多级目录
                }
                if (!file.exists()) {
                    try {
                        file.createNewFile();//有路径才能创建文件
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            HdDwonLoadUtils hdDownloader = new HdDwonLoadUtils(downLoadBean.getUrl(), downLoadBean.getPath(), downLoadBean);
            hdDownloader.setDownloadListener(new HdDownloadListener() {
                @Override
                public void handleProcess(final long start, final long end, final String url) {
                    final DownLoadBean downLoadBean1 = downLoaddingQuery.get(url).getDownLoadBean();
                    if (downLoadBean1 != null) {
                        downLoadBean1.setProgress(start);
                        downLoadBean1.setTotal(end);
                        //更新数据库
                        tasksManagerDBController.update(downLoadBean1);
                    }
                    if (start == end) {
                        downLoadBean1.setTaskStatus(DownLoadStatus.DOWNLOAD_FINISH);
                        tasksManagerDBController.update(downLoadBean1);
                        statusChange(DownLoadStatus.DOWNLOAD_FINISH, downLoadBean1);
                        startUnzip(downLoadBean1);
                        downLoaddingQuery.remove(url);
                        //自动下载下一个
                        if (downLoadQuery.size() > 0) {
                            DownLoadBean downLoadBean2 = downLoadQuery.pollFirst();
                            if (downLoadBean2 != null) {
                                startDownload(downLoadBean2);
                            }
                        }
                    }
                    if (downLoadTaskListener != null)
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                downLoadTaskListener.onProcess(downLoadBean1);
                            }
                        });
                }

                @Override
                public void handleException(HdHuodeException e, final int i, final String url) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (i == 300) {//下载失败
                                HdDwonLoadUtils hdDwonLoadUtils = downLoaddingQuery.get(url);
                                if (hdDwonLoadUtils != null) {
                                    final DownLoadBean downLoadBean1 = hdDwonLoadUtils.getDownLoadBean();
                                    downLoadBean1.setTaskStatus(DownLoadStatus.DOWNLOAD_ERROR);
                                    tasksManagerDBController.update(downLoadBean1);
                                    if (downLoadTaskListener != null)
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                downLoadTaskListener.error(3, url);
                                            }
                                        });
                                }
                            }

                        }
                    });
                }

                @Override
                public void handleStatus(final String url, final int status) {
                    if (status == 200) {//下载中
                        final DownLoadBean downLoadBean1 = downLoaddingQuery.get(url).getDownLoadBean();
                        downLoadBean1.setTaskStatus(DownLoadStatus.DOWNLOADING);
                        tasksManagerDBController.update(downLoadBean1);
                        statusChange(DownLoadStatus.DOWNLOADING, downLoadBean1);
                    }
                }

                @Override
                public void handleCancel(String url) {

                }
            });
            hdDownloader.setReconnectLimit(1);
            hdDownloader.start();
            downLoaddingQuery.put(downLoadBean.getUrl(), hdDownloader);
        } else {
            downLoadBean.setTaskStatus(DownLoadStatus.DOWNLOADING);
            tasksManagerDBController.update(downLoadBean);
            statusChange(DownLoadStatus.DOWNLOAD_WAIT, downLoadBean);
            //加到缓存队列
            downLoadQuery.add(downLoadBean);
        }
    }

    /**
     * 开始解压
     *
     * @param downLoadBean
     */
    private void startUnzip(final DownLoadBean downLoadBean) {
        if (isUnZipping.get()) {
            downLoadBean.setTaskStatus(DownLoadStatus.ZIP_WAIT);
            tasksManagerDBController.update(downLoadBean);
            statusChange(DownLoadStatus.ZIP_WAIT, downLoadBean);
            unZipquery.add(downLoadBean);
        } else {
            downLoadBean.setTaskStatus(DownLoadStatus.ZIPING);
            tasksManagerDBController.update(downLoadBean);
            statusChange(DownLoadStatus.ZIPING, downLoadBean);
            File ccrFile = new File(downLoadBean.getPath());
            unZiper = new UnZiper(new UnZiper.UnZipListener() {
                @Override
                public void onError(int errorCode, String message) {
                    downLoadBean.setTaskStatus(DownLoadStatus.ZIP_ERROR);
                    tasksManagerDBController.update(downLoadBean);
                    isUnZipping.set(false);
                    if (unZipquery.size() > 0) {
                        startUnzip(unZipquery.pollFirst());
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (downLoadTaskListener != null) {
                                downLoadTaskListener.error(5, downLoadBean.getUrl());
                            }
                        }
                    });
                }

                @Override
                public void onUnZipFinish() {
                    downLoadBean.setTaskStatus(DownLoadStatus.ZIP_FINISH);
                    tasksManagerDBController.update(downLoadBean);
                    isUnZipping.set(false);
                    statusChange(DownLoadStatus.ZIP_FINISH, downLoadBean);
                    if (unZipquery.size() > 0) {
                        startUnzip(unZipquery.pollFirst());
                    }
                }
            }, ccrFile, FileUtil.getUnzipDir(ccrFile));
            isUnZipping.set(true);
            unZiper.unZipFile();
        }
    }

    /**
     * 状态改变
     */
    private void statusChange(final int status, final DownLoadBean downLoadBean) {
        if (downLoadTaskListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    downLoadTaskListener.statusChange(status, downLoadBean);
                }
            });
        }
    }

    /**
     * 下载新的数据
     *
     * @param downLoadBean
     */
    public void start(final DownLoadBean downLoadBean) {
        ThreadPoolUtils.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final int b = tasksManagerDBController.addTask(downLoadBean);
                if (b == 0) {
                    if (downLoadTaskListener != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                downLoadTaskListener.addDateSuccess(downLoadBean);
                            }
                        });
                    }
                    startDownload(downLoadBean);
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (downLoadTaskListener != null) {
                                if (b == -1 || b == -3) {
                                    downLoadTaskListener.error(3, downLoadBean.getUrl());
                                } else {
                                    downLoadTaskListener.error(12, downLoadBean.getUrl());
                                }

                            }

                        }
                    });

                }
                ThreadPoolUtils.getInstance().cancel(this);
            }
        });
    }

    public void reDownload(final DownLoadBean downLoadBean) {
        ThreadPoolUtils.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                //重新去下载
//                final int b = tasksManagerDBController.update(downLoadBean);
                startDownload(downLoadBean);
                ThreadPoolUtils.getInstance().cancel(this);
            }
        });
    }

    /**
     * 递归删除文件和文件夹
     *
     * @param file 要删除的根目录
     */
    public void recursionDeleteFile(File file) {
        if (file.getAbsoluteFile().exists()) {
            @SuppressWarnings("unused")
            File[] f = file.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    File currFile = new File(dir, name);
                    if (currFile.isDirectory()) {
                        recursionDeleteFile(currFile.getAbsoluteFile());
                    } else {
                        currFile.delete();
                    }
                    currFile.delete();
                    return false;
                }
            });
        }
        file.delete();
//        if (file.isFile()) {
//            file.delete();
//            return;
//        }
//        if (file.isDirectory()) {
//            File[] childFile = file.listFiles();
//            if (childFile == null || childFile.length == 0) {
//                file.delete();
//                return;
//            }
//            for (File f : childFile) {
//                recursionDeleteFile(f);
//            }
//        }
    }

    /**
     * 移除本地的所有数据 ----- 就是彻底删除
     */
    public void delete(final DownLoadBean downLoadBean) {
        ThreadPoolUtils.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final int i = tasksManagerDBController.removeTask(downLoadBean.getUrl());
                if (downLoaddingQuery != null && downLoaddingQuery.get(downLoadBean.getUrl()) != null) {
                    downLoaddingQuery.get(downLoadBean.getUrl()).cancel();
                    downLoaddingQuery.remove(downLoadBean.getUrl());
                }
                File unzipDir = new File(downLoadBean.getPath().replace(".ccr", ""));
                recursionDeleteFile(unzipDir);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (downLoadTaskListener != null) {
                            if (i <= 0) {
                                downLoadTaskListener.error(2, downLoadBean.getUrl());
                            }
                        }
                    }
                });
                ThreadPoolUtils.getInstance().cancel(this);
            }
        });
    }

    /**
     * 暂停
     *
     * @param downLoadBean
     */
    public void pause(DownLoadBean downLoadBean) {
        HdDwonLoadUtils hdDwonLoadUtils = downLoaddingQuery.get(downLoadBean.getUrl());
        if (hdDwonLoadUtils != null) {
            hdDwonLoadUtils.cancel();
            downLoadBean.setTaskStatus(DownLoadStatus.DOWNLOAD_PAUSE);
            statusChange(DownLoadStatus.DOWNLOAD_PAUSE, downLoadBean);
        } else {
            if (downLoadTaskListener != null)
                downLoadTaskListener.error(4, downLoadBean.getUrl());
        }
    }

    /**
     * 释放资源
     */
    public void destroy() {
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
        ThreadPoolUtils.getInstance().destroy();
        tasksManagerDBController.onDestroy();
        if (unZiper != null) {
            unZiper.unzipThread.interrupt();
        }
        if (downLoaddingQuery != null) {
            for (Map.Entry<String, HdDwonLoadUtils> entry : downLoaddingQuery.entrySet()) {
                HdDwonLoadUtils mapValue = entry.getValue();
                mapValue.cancel();
            }
        }
    }
}
