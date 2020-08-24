package com.bokecc.livemodule.live.intro;

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
import com.bokecc.sdk.mobile.live.DWLive;

/**
 * 直播间简介展示控件
 */
public class LiveIntroComponent extends LinearLayout {

    private Context mContext;

    TextView mTitle;
    private WebView webView;
    public LiveIntroComponent(Context context) {
        super(context);
        mContext = context;
        initIntroView();
    }

    public LiveIntroComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initIntroView();
    }

    // 设置直播间标题和简介
    public void initIntroView() {

        LayoutInflater.from(mContext).inflate(R.layout.portrait_intro_layout, this, true);
        mTitle = findViewById(R.id.tv_intro_title);
        webView = findViewById(R.id.intro_webview);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
// 设置支持Javascript
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setTextSize(WebSettings.TextSize.NORMAL);
//设置背景颜色
        webView.setBackgroundColor(0);
        if (DWLive.getInstance() != null && DWLive.getInstance().getRoomInfo() != null) {
            if (!TextUtils.isEmpty(DWLive.getInstance().getRoomInfo().getName()))
                mTitle.setText(DWLive.getInstance().getRoomInfo().getName());
            if (!TextUtils.isEmpty(DWLive.getInstance().getRoomInfo().getDesc())){
                webView.loadDataWithBaseURL(null, "<html><head><meta name='viewport' content=width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0><style type='text/css' >img{height: auto;max-width: 100%;max-height: 100%;}</style></head><body>"+DWLive.getInstance().getRoomInfo().getDesc()+"</body></html>","text/html","utf-8", null);
            }else {
                webView.loadDataWithBaseURL(null, "<html><head><meta name='viewport' content=width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0><style type='text/css' >img{height: auto;max-width: 100%;max-height: 100%;}</style></head><body><p>暂无简介</p></body></html>","text/html","utf-8", null);
            }
        }
    }
}

