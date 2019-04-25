package com.bokecc.livemodule.live.doc;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.sdk.mobile.live.widget.DocView;

/**
 * 直播间文档展示控件
 */
public class LiveDocComponent extends LinearLayout {

    private Context mContext;

    private DocView mDocView;

    public LiveDocComponent(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public LiveDocComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
    }

    private void initViews() {
        mDocView = new DocView(mContext);
        // 设置true：响应文档内容上下滑动，不支持悬浮窗拖动  设置false：支持悬浮窗拖动，不响应文档内容上下滑动
        mDocView.setScrollable(false);
        mDocView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mDocView);

        DWLiveCoreHandler liveCoreHandler = DWLiveCoreHandler.getInstance();
        if (liveCoreHandler != null) {
            liveCoreHandler.setDocView(mDocView);
        }
    }

    // 设置文档区域是否可滑动
    public void setDocScrollable(boolean scrollable) {
        if (mDocView != null) {
            mDocView.setScrollable(scrollable);
        }
    }
}

