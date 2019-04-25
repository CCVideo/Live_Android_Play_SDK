package com.bokecc.livemodule.replay.doc;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.sdk.mobile.live.widget.DocView;

/**
 * 回放直播间文档展示控件
 */
public class ReplayDocComponent extends LinearLayout {

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
            replayCoreHandler.setDocView(mDocView);
        }
    }
}
