package com.bokecc.livemodule.live.function.punch.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.utils.PopupAnimUtil;
import com.bokecc.livemodule.view.BasePopupWindow;
import com.bokecc.sdk.mobile.live.BaseCallback;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.pojo.PunchAction;
import com.bokecc.sdk.mobile.live.pojo.PunchCommitRespone;
import com.bokecc.sdk.mobile.live.util.NetworkUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 签到Dialog
 */
public class PunchDialog extends BasePopupWindow {
    private TextView countDownText;
    private int time;
    private Button submitButton;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private String punchId;
    private AtomicBoolean submitting = new AtomicBoolean();
    private TextView tvTip;
    private TextView tvError;
    private PunchListener punchListener;
    public PunchDialog(Context context,PunchListener punchListener) {
        super(context);
        this.punchListener=punchListener;
    }
    private BaseCallback baseCallback = new BaseCallback<PunchCommitRespone>() {
        @Override
        public void onError(String s) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    submitting.set(false);
                    submitButton.setEnabled(true);
                    tvError.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void onSuccess(PunchCommitRespone punchCommitRespone) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mHandler.removeCallbacksAndMessages(null);
                    submitting.set(false);
                    submitButton.setEnabled(true);
                    dismiss();
                    if (punchListener!=null){
                        punchListener.onSuccess();
                    }
                }
            });
        }
    };
    @Override
    protected void onViewCreated() {
        countDownText = findViewById(R.id.id_count_down_time);
        submitButton = findViewById(R.id.id_submit_btn);
        tvTip = findViewById(R.id.tv_punch_tip);
        tvError = findViewById(R.id.punch_error);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (submitting.get()) return;
                if (!NetworkUtils.isNetworkAvailable(mContext)) {
                    baseCallback.onError("");
                }else{
                    submitting.set(true);
                    submitButton.setEnabled(false);
                    DWLive.getInstance().commitPunch(punchId, baseCallback);
                }
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

    public void show(View view, PunchAction punchAction,int time){
        this.punchId = punchAction.getId();
        this.time = time;
        tvTip.setText(punchAction.getTips());
        if (isShowing()&&punchId.equals(this.punchId)){
            return;
        }
        tvError.setVisibility(View.INVISIBLE);
        show(view);
    }
    @Override
    public void show(View view) {
        super.show(view);
        submitting.set(false);
        mHandler.post(new CountRunnable());
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

    public boolean isSame(String punchId) {
        return isShowing()&&punchId.equals(this.punchId);
    }

    private class CountRunnable implements Runnable {
        @Override
        public void run() {
            //ELog.i("Sivin", "time:" + time);
            if (time < 0) {
                dismiss();
                if (punchListener!=null){
                    punchListener.onEnd();
                }
                return;
            }
            countDownText.setText(time + "s");
            time--;
            mHandler.postDelayed(this, 1000);
        }
    }
    public interface PunchListener{
        void onEnd();
        void onSuccess();
    }
}
