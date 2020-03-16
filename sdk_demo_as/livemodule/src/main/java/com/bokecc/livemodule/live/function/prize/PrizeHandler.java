package com.bokecc.livemodule.live.function.prize;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;

import com.bokecc.livemodule.live.function.prize.view.PrizeLandPopup;
import com.bokecc.livemodule.live.function.prize.view.PrizePopup;

/**
 * “奖杯”处理机制
 */
public class PrizeHandler {

    private Context mContext;

    private PrizePopup mPrizePopup;  // “奖杯”弹窗
    private PrizeLandPopup mPrizeLandPopup; // “奖杯”弹窗（横屏）

    /**
     * 初始化‘奖杯’功能
     */
    public void initPrize(Context context) {
        mContext = context;
        mPrizePopup = new PrizePopup(context);
        mPrizeLandPopup = new PrizeLandPopup(context);
    }

    /**
     * 展示奖杯
     */
    public void showPrize(View root, String viewerName, String viewerId) {
        if (isPortrait(mContext)) {
            mPrizePopup.show(root);
            mPrizePopup.showPrize(viewerName, viewerId);
        } else {
            mPrizeLandPopup.show(root);
            mPrizeLandPopup.showPrize(viewerName, viewerId);
        }
    }

    // 判断当前屏幕朝向是否为竖屏
    private boolean isPortrait(Context context) {
        int mOrientation = context.getResources().getConfiguration().orientation;
        return mOrientation != Configuration.ORIENTATION_LANDSCAPE;
    }

}
