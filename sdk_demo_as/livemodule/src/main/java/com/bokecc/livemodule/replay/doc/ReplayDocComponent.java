package com.bokecc.livemodule.replay.doc;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.widget.DocView;

/**
 * 回放直播间文档展示控件
 */
public class ReplayDocComponent extends LinearLayout implements ReplayDocSizeChangeListener, DocActionListener {

    private final int SCALE_CENTER_INSIDE = 0;
    private final int SCALE_FIT_XY = 1;
    private final int SCALE_CROP_CENTER = 2;

    private int mCurrentScaleType = SCALE_CENTER_INSIDE;

    private Context mContext;

    private DocView mDocView;


    public ReplayDocComponent(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public ReplayDocComponent(Context context, @Nullable AttributeSet attrs) {
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
        DWReplayCoreHandler replayCoreHandler = DWReplayCoreHandler.getInstance();
        if (replayCoreHandler != null) {
            replayCoreHandler.setDocSizeChangeListener(this);
            replayCoreHandler.setDocActionListener(this);
            replayCoreHandler.setDocView(mDocView);
        }
    }
    public void setDocScrollable(boolean isDocScrollable){
        if (mDocView!=null){
            mDocView.setScrollable(isDocScrollable);
        }
    }
    public void setScaleType(int type) {
        mCurrentScaleType = type;
        if (SCALE_CENTER_INSIDE == mCurrentScaleType) {
            DWLiveReplay.getInstance().setDocScaleType(DocView.ScaleType.CENTER_INSIDE);
        } else if (SCALE_FIT_XY == mCurrentScaleType) {
            DWLiveReplay.getInstance().setDocScaleType(DocView.ScaleType.FIT_XY);
        } else if (SCALE_CROP_CENTER == mCurrentScaleType) {
            DWLiveReplay.getInstance().setDocScaleType(DocView.ScaleType.CROP_CENTER);
        }
    }

    public void updateSize(final int srcWidth, final int srcHeight) {

    }

    @Override
    public void onDocLoadFailed() {

    }
}
