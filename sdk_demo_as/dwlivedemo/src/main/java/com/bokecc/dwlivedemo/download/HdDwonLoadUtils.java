package com.bokecc.dwlivedemo.download;

import com.bokecc.download.HdDownloader;

public class HdDwonLoadUtils extends HdDownloader {
    private DownLoadBean downLoadBean;
    public HdDwonLoadUtils(String downloadUrl, String savePath,DownLoadBean downLoadBean) {
        super(downloadUrl, savePath);
        this.downLoadBean=downLoadBean;
    }

    public DownLoadBean getDownLoadBean() {
        return downLoadBean;
    }
}
