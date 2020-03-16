package com.bokecc.livemodule.localplay.intro;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.localplay.DWLocalDWReplayIntroListener;
import com.bokecc.livemodule.localplay.DWLocalReplayCoreHandler;
import com.bokecc.livemodule.replaymix.DWReplayMixCoreHandler;
import com.bokecc.livemodule.replaymix.DWReplayMixIntroListener;
import com.bokecc.livemodule.view.MixedTextView;
import com.bokecc.sdk.mobile.live.pojo.RoomInfo;

/**
 * 回放直播间简介展示控件
 */
public class LocalReplayIntroComponent extends LinearLayout implements DWLocalDWReplayIntroListener {

    private Context mContext;

    TextView mTitle;

    LinearLayout mContent;

    public LocalReplayIntroComponent(Context context) {
        super(context);
        mContext = context;
        initIntroView();
    }

    public LocalReplayIntroComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initIntroView();
    }

    // 设置直播间标题和简介
    public void initIntroView() {
        LayoutInflater.from(mContext).inflate(R.layout.portrait_intro_layout, this, true);
        mTitle = (TextView) findViewById(R.id.tv_intro_title);
        mContent = (LinearLayout) findViewById(R.id.content_layer);
        DWLocalReplayCoreHandler.getInstance().setLocalIntroListener(this);
    }


    /**
     * 更新直播间信息
     */
    @Override
    public void updateRoomInfo(final RoomInfo info) {
        if (info == null) return;
        if (mTitle != null) {
            mTitle.post(new Runnable() {
                @Override
                public void run() {
                    mTitle.setText(info.getName());
                    mContent.removeAllViews();
                    mContent.addView(new MixedTextView(mContext, info.getDesc()));
                }
            });
        }
    }
}