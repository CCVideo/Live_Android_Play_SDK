package com.bokecc.livemodule.live.function.lottery;

import android.content.Context;
import android.view.View;

import com.bokecc.livemodule.live.function.lottery.view.LotteryPopup;
import com.bokecc.livemodule.live.function.lottery.view.LotteryStartPopup;

/**
 * '抽奖' 处理机制
 */
public class LotteryHandler {


    private LotteryPopup mLotteryPopup;
    private LotteryStartPopup mLotteryStartPopup;

    private boolean isLotteryWin = false;

    private int lotteryDelay = 3 * 1000;

    /**
     * 初始化抽奖功能
     */
    public void initLottery(Context context) {
        mLotteryStartPopup = new LotteryStartPopup(context);
        mLotteryStartPopup.setKeyBackCancel(true);
        mLotteryPopup = new LotteryPopup(context);
        mLotteryPopup.setKeyBackCancel(true);
    }

    /** 开始抽奖 */
    public void startLottery(View root, String lotteryId) {
        isLotteryWin = false;
        mLotteryStartPopup.show(root);
        mLotteryStartPopup.startLottery();
    }

    /** 展示抽奖结果 */
    public void showLotteryResult(View root, final boolean isWin, final String lotteryCode, final String lotteryId, final String winnerName) {
        // 如果已经中奖了，而且中奖界面没有关闭，则不做后续的界面处理
        if (isLotteryWin && mLotteryPopup.isShowing()) {
            return;
        }

        this.isLotteryWin = isWin;
        mLotteryPopup.show(root);
        mLotteryPopup.onLotteryResult(isWin, lotteryCode, winnerName);

        if (!isLotteryWin) {
            root.postDelayed(dismissLottery, lotteryDelay);
        }
    }

    /** 停止抽奖 */
    public void stopLottery(View root, String lotteryId) {
        if (mLotteryStartPopup != null && mLotteryStartPopup.isShowing()) {
            mLotteryStartPopup.dismiss();
        }
        if (!isLotteryWin) {
            root.postDelayed(dismissLottery, lotteryDelay);
        }
    }


    // 隐藏抽奖页
    Runnable dismissLottery = new Runnable() {
        @Override
        public void run() {
            if (mLotteryPopup != null && mLotteryPopup.isShowing()) {
                mLotteryPopup.dismiss();
            }
        }
    };



}
