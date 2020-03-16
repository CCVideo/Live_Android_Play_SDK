package com.bokecc.livemodule.live.chat.adapter;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bokecc.livemodule.live.chat.util.ViewHolder;

public abstract class CommonArrayAdapter<T> extends BaseAdapter {

    protected Context mContext;

    protected T []datas;

    public CommonArrayAdapter(Context context) {
        mContext = context;
    }

    public void bindData(T []datas) {
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas == null ? 0 : datas.length;
    }

    @Override
    public Object getItem(int position) {
        return datas == null ? null : datas[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = ViewHolder.get(mContext, convertView, parent, getItemViewId());
        onBindViewHolder(viewHolder, position);
        return viewHolder.getConvertView();
    }

    protected abstract void onBindViewHolder(ViewHolder viewHolder, int position);

    protected abstract int getItemViewId();

}

