package com.bokecc.dwlivedemo.download;

import android.support.v7.util.DiffUtil;

import java.util.List;

public class DownLoadDiffCallback extends DiffUtil.Callback {
    private List<DownLoadBean> oldList;
    private List<DownLoadBean> newList;

    public DownLoadDiffCallback(List<DownLoadBean> oldList, List<DownLoadBean> newList){
        this.oldList=oldList;
        this.newList=newList;
    }
    @Override
    public int getOldListSize() {
        return oldList==null?0:oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList==null?0:newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getUrl().equals(newList.get(newItemPosition).getUrl());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return false;
    }
}
