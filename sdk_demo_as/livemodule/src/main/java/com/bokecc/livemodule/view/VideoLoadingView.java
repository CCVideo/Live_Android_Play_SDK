package com.bokecc.livemodule.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.utils.net.NetSpeed;
import com.bokecc.livemodule.utils.net.NetSpeedTimer;

import java.util.Locale;

public class VideoLoadingView extends LinearLayout {

    private TextView mNet;
    private TextView mLoadingtext;

    public VideoLoadingView(Context context) {
        super(context);
        initView(context);
    }

    public VideoLoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public VideoLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }
    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.video_loading_view, this);
        mNet = findViewById(R.id.tv_net);
        mLoadingtext = findViewById(R.id.tv_video_loadingtext);
    }

    public void setSpeed(float speed) {
        mNet.setText(getSpeed(speed));
    }
    private String getSpeed(float speed){
        if (speed >= 1000 * 1000) {
            return String.format(Locale.US, "%.2f MB/s", ((float)speed) / 1000 / 1000);
        } else if (speed >= 1000) {
            return String.format(Locale.US, "%.1f KB/s", ((float)speed) / 1000);
        } else {
            return String.format(Locale.US, "%d B/s", (long)speed);
        }
    }

    public void showSpeeed(boolean showSpeed) {
        if (showSpeed){
            mNet.setVisibility(GONE);
            mLoadingtext.setVisibility(GONE);
        }else{
            mNet.setVisibility(VISIBLE);
            mLoadingtext.setVisibility(VISIBLE);
        }
    }
}
