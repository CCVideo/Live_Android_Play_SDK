package com.bokecc.livemodule.live.morefunction.announce;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.sdk.mobile.live.DWLive;

/**
 * 公告界面
 */
public class AnnounceLayout extends LinearLayout {

    private Context mContext;

    private TextView mAnnounce;

    private ImageView mCloseAnnounce;

    private View mRoot;

    public AnnounceLayout(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public AnnounceLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public AnnounceLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView() {
        mRoot = LayoutInflater.from(mContext).inflate(R.layout.live_portrait_announce, this, true);
        mAnnounce = findViewById(R.id.announce);
        mCloseAnnounce = findViewById(R.id.close_announce);
        mCloseAnnounce.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mRoot.setVisibility(GONE);
            }
        });

       if (DWLive.getInstance() != null && !TextUtils.isEmpty(DWLive.getInstance().getAnnouncement())) {
           mAnnounce.setText(DWLive.getInstance().getAnnouncement());
       }
    }

    /**
     * 设置公告
     *
     * @param announce 公告内容
     */
    public void setAnnounce(final String announce) {
        if (mAnnounce != null) {
            mAnnounce.post(new Runnable() {
                @Override
                public void run() {
                    mAnnounce.setText(announce);
                }
            });
        }
    }

    /**
     * 删除公告
     */
    public void removeAnnounce() {
        if (mAnnounce != null) {
            mAnnounce.post(new Runnable() {
                @Override
                public void run() {
                    mAnnounce.setText("暂无公告");
                }
            });
        }
    }
}
