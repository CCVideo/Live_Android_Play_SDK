package com.bokecc.livemodule.live.function.punch.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.utils.PopupAnimUtil;
import com.bokecc.livemodule.view.BasePopupWindow;
import com.bokecc.sdk.mobile.live.BaseCallback;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.logging.ELog;
import com.bokecc.sdk.mobile.live.pojo.PunchCommitRespone;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 签到Dialog
 */
public class PunchDialog extends BasePopupWindow {
    private TextView countDownText;
    private int time;
    private SubmitButton submitButton;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private String punchId;
    private AtomicBoolean submitting = new AtomicBoolean();
    public PunchDialog(Context context) {
        super(context);
    }
    @Override
    protected void onViewCreated() {
        countDownText = findViewById(R.id.id_count_down_time);
        submitButton = findViewById(R.id.id_submit_btn);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitButton.startSubmitAnim();
                startPunch();
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.layout_punch_dialog;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefScaleEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefScaleExitAnim();
    }


    public void setPunchId(String punchId) {
        this.punchId = punchId;
    }

    @Override
    public void show(View view) {
        super.show(view);
        submitting.set(false);
        mHandler.post(new CountRunnable());
    }

    /**
     * 开始打开
     */
    private void startPunch() {
        if (submitting.get()) return;
        submitting.set(true);
        submitButton.startSubmitAnim();
        DWLive.getInstance().commitPunch(punchId, new BaseCallback<PunchCommitRespone>() {
            @Override
            public void onError(String s) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        submitting.set(false);
                        submitButton.reset();
                        mHandler.removeCallbacksAndMessages(null);
                        Toast.makeText(mContext,"打卡失败",Toast.LENGTH_LONG).show();
                        dismiss();
                    }
                });
            }

            @Override
            public void onSuccess(PunchCommitRespone punchCommitRespone) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        submitting.set(true);
                        mHandler.removeCallbacksAndMessages(null);
                        submitting.set(false);
                        submitButton.reset();
                        Toast.makeText(mContext,"打卡成功",Toast.LENGTH_LONG).show();
                        dismiss();
                    }
                });
            }
        });
    }


    public void setTime(int time) {
        this.time = time;
    }

    public void destroy( String message) {
        Toast.makeText(mContext,message,Toast.LENGTH_LONG).show();
        dismiss();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mHandler.removeCallbacksAndMessages(null);
    }

    private class CountRunnable implements Runnable {
        @Override
        public void run() {
            countDownText.setText(time + "s");
            ELog.i("Sivin", "time:" + time);
            if (time < 0) {
                dismiss();
                return;
            }
            time--;
            mHandler.postDelayed(this, 1000);
        }
    }



}
