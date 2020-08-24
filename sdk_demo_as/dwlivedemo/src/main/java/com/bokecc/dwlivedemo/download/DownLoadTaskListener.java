package com.bokecc.dwlivedemo.download;

import java.util.List;

public interface DownLoadTaskListener {
    /**
     * 获取数据库的所有数据回调
     * @param date 下载数据
     */
    void getAllDateResult(List<DownLoadBean> date);

    /**
     * 下载的所有异常
     * @param status 1 获取所有数据失败
     *               2 删除失败
     *               3 下载失败
     *               4 暂停失败
     *               5 解压失败
     *               6 更新本地数据库失败
     * @param url 下载的url
     */
    void error(int status,String url);

    /**
     * 下载进度监听
     */
    void onProcess(DownLoadBean downLoadBean);

    /**
     * 下载状态改变
     * @param downLoadStatus
     * @param downLoadBean  下载的数据
     */
    void statusChange(int downLoadStatus, DownLoadBean downLoadBean);
    /**
     * 添加数据成功 显示到界面上
     * @param downLoadBean  下载的数据
     */
    void addDateSuccess( DownLoadBean downLoadBean);
}
