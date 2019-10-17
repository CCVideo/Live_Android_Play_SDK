package com.bokecc.livemodule.live.function.prize.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.utils.PopupAnimUtil;
import com.bokecc.livemodule.view.BasePopupWindow;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.pojo.Viewer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 奖杯弹窗
 */
public class PrizePopup  extends BasePopupWindow {

    private final static int SHOW_TIME = 2;  // 奖杯弹窗持续时间（单位：s）

    TextView selfPrize;
    LinearLayout otherPrize;
    TextView prizeUserName;

    public PrizePopup(Context context) {
        super(context);
    }

    @Override
    protected void onViewCreated() {
        otherPrize = findViewById(R.id.other_prize);
        prizeUserName = findViewById(R.id.prize_viewer_name);
        selfPrize = findViewById(R.id.self_prize);
    }

    @Override
    protected int getContentView() {
        return R.layout.prize_layout;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefScaleEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefScaleExitAnim();
    }

    private int duration;

    public void showPrize(String viewerName, String viewerId) {

        Viewer viewer =  DWLive.getInstance().getViewer();
        if (viewer == null) {
            return;
        }

        if (viewerId.equals(viewer.getId())) {
            selfPrize.setVisibility(View.VISIBLE);
            otherPrize.setVisibility(View.GONE);
            duration = SHOW_TIME;
            startTimerTask();
        } else {
            selfPrize.setVisibility(View.GONE);
            otherPrize.setVisibility(View.VISIBLE);
            if (viewerName.length() > 8) {
                prizeUserName.setText(viewerName.substring(0, 8) + "...");
            } else {
                prizeUserName.setText(viewerName);
            }
            duration = SHOW_TIME;
            startTimerTask();
        }
    }

    Timer timer = new Timer();
    TimerTask timerTask;

    Handler handler = new Handler(Looper.getMainLooper());

    private void startTimerTask() {
        stopTimerTask();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (duration <= 0) {
                            onDestroy();
                            return;
                        }
                        duration--;
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 1 * 1000);
    }

    private void stopTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
        }
    }

    public void onDestroy() {
        dismiss();
        stopTimerTask();
    }
}