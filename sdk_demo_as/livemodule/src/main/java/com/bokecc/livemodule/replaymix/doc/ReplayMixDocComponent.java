package com.bokecc.livemodule.replaymix.doc;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bokecc.livemodule.replaymix.DWReplayMixCoreHandler;
import com.bokecc.sdk.mobile.live.widget.DocView;

/**
 * 回放直播间文档展示控件
 */
public class ReplayMixDocComponent extends LinearLayout {

    private Context mContext;

    private DocView mDocView;

    public ReplayMixDocComponent(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public ReplayMixDocComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
    }

    private void initViews() {
        mDocView = new DocView(mContext);
        // 为了保证悬浮窗能正常处理触摸事件，必须将文档WebView响应滑动禁用调，且小窗模式建议使用文档的适应窗口模式
        mDocView.setScrollable(false);
        mDocView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mDocView);

        DWReplayMixCoreHandler replayCoreHandler = DWReplayMixCoreHandler.getInstance();
        if (replayCoreHandler != null) {
            replayCoreHandler.setDocView(mDocView);
        }
    }
}
