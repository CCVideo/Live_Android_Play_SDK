package com.bokecc.livemodule.live.morefunction.fab;

import android.support.design.widget.FloatingActionButton;

/*
 * 项目名:    SuspensionFAB
 * 包名       com.azhon.suspensionfab
 * 文件名:    OnFabClickListener
 * 创建者:    ZSY
 * 创建时间:  2017/6/30 on 13:59
 * 描述:     TODO 按钮的点击事件
 */
public interface OnFabClickListener {

    /**
     * @param fab 按钮
     * @param tag 回调之前设置的tag
     */
    void onFabClick(FloatingActionButton fab, Object tag);

    void onFabVisible(boolean visible);
}
