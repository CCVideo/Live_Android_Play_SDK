package com.bokecc.livemodule.replaymix.intro;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.replaymix.DWReplayMixCoreHandler;
import com.bokecc.livemodule.replaymix.DWReplayMixIntroListener;
import com.bokecc.sdk.mobile.live.pojo.RoomInfo;

/**
 * 回放直播间简介展示控件
 */
public class ReplayMixIntroComponent extends LinearLayout implements DWReplayMixIntroListener {

    private Context mContext;

    TextView mTitle;

    WebView webView;

    public ReplayMixIntroComponent(Context context) {
        super(context);
        mContext = context;
        initIntroView();
    }

    public ReplayMixIntroComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initIntroView();
    }

    // 设置直播间标题和简介
    public void initIntroView() {
        LayoutInflater.from(mContext).inflate(R.layout.portrait_intro_layout, this, true);
        mTitle = (TextView) findViewById(R.id.tv_intro_title);
        webView = (WebView) findViewById(R.id.intro_webview);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
// 设置支持Javascript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setTextSize(WebSettings.TextSize.NORMAL);
//设置背景颜色
        webView.setBackgroundColor(0);
        DWReplayMixCoreHandler.getInstance().setMixIntroListener(this);
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
                    if (!TextUtils.isEmpty(info.getName()))
                        mTitle.setText(info.getName());
                    if (!TextUtils.isEmpty(info.getDesc())){
                        webView.loadDataWithBaseURL(null, "<html><head><meta name='viewport' content=width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0><style type='text/css' >img{height: auto;max-width: 100%;max-height: 100%;}</style></head><body>"+ info.getDesc()+"</body></html>","text/html","utf-8", null);

                    }else {
                        webView.loadDataWithBaseURL(null, "<html><head><meta name='viewport' content=width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0><style type='text/css' >img{height: auto;max-width: 100%;max-height: 100%;}</style></head><body><p>暂无简介</p></body></html>","text/html","utf-8", null);
                    }
                }
            });
        }
    }
}