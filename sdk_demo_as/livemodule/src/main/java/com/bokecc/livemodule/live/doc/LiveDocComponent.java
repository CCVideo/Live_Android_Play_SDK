package com.bokecc.livemodule.live.doc;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.widget.DocView;

/**
 * 直播间文档展示控件
 */
public class LiveDocComponent extends LinearLayout implements LiveDocSizeChangeListener {


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
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        // params.gravity = Gravity.CENTER;
        mDocView.setLayoutParams(params);
        addView(mDocView);

        mDocView.changeBackgroundColor("#ffffff");

        DWLiveCoreHandler liveCoreHandler = DWLiveCoreHandler.getInstance();
        if (liveCoreHandler != null) {
            liveCoreHandler.setDocView(mDocView);
            liveCoreHandler.setDocSizeChangeListener(this);
        }
    }

    // 设置文档区域是否可滑动
    public void setDocScrollable(boolean scrollable) {
        if (mDocView != null) {
            mDocView.setScrollable(scrollable);
        }
    }

    // 设置文档的拉伸模式
    public void setScaleType(int type) {
        // 如果不是自适应宽度不会起作用
        if (mDocView.isDocFitWidth()) return;
        if (0 == type) {
            DWLive.getInstance().setDocScaleType(DocView.ScaleType.CENTER_INSIDE);
        } else if (1 == type) {
            DWLive.getInstance().setDocScaleType(DocView.ScaleType.FIT_XY);
        } else if (2 == type) {
            DWLive.getInstance().setDocScaleType(DocView.ScaleType.CROP_CENTER);
        }
    }

    public void updateSize(int srcWidth, int srcHeight) {

    }

}

