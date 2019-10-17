package com.bokecc.livemodule.replay.intro;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.view.MixedTextView;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;

/**
 * 回放直播间简介展示控件
 */
public class ReplayIntroComponent extends LinearLayout {

    private Context mContext;

    TextView mTitle;

    LinearLayout mContent;

    public ReplayIntroComponent(Context context) {
        super(context);
        mContext = context;
        initIntroView();
    }

    public ReplayIntroComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initIntroView();
    }

    // 设置直播间标题和简介
    public void initIntroView() {
        LayoutInflater.from(mContext).inflate(R.layout.portrait_intro_layout, this, true);
        mTitle = (TextView) findViewById(R.id.tv_intro_title);
        mContent = (LinearLayout) findViewById(R.id.content_layer);

        if (DWLiveReplay.getInstance() != null && DWLiveReplay.getInstance().getRoomInfo() != null) {
            mTitle.setText(DWLiveReplay.getInstance().getRoomInfo().getName());
            mContent.removeAllViews();
            mContent.addView(new MixedTextView(mContext, DWLiveReplay.getInstance().getRoomInfo().getDesc()));
        }
    }
}