package com.bokecc.livemodule.live.chat.util;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewHolder {

    private final SparseArray<View> mViews;
    private View mConvertView;

    private ViewHolder(Context context, ViewGroup parent, int layoutId) {
        this.mViews = new SparseArray<>();
        mConvertView = LayoutInflater.from(context).inflate(layoutId, parent,
                false);
        //setTag
        mConvertView.setTag(this);
    }

    public static ViewHolder get(Context context, View convertView,
                                 ViewGroup parent, int layoutId) {

        if (convertView == null) {
            return new ViewHolder(context, parent, layoutId);
        }
        return (ViewHolder) convertView.getTag();
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    public View getConvertView() {
        return mConvertView;
    }
}
