package com.bokecc.livemodule.live.function.punch.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.utils.PopupAnimUtil;
import com.bokecc.livemodule.view.BasePopupWindow;
import com.bokecc.sdk.mobile.live.BaseCallback;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.pojo.PunchAction;
import com.bokecc.sdk.mobile.live.pojo.PunchCommitRespone;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 签到Dialog
 */
public class PunchResultDialog extends BasePopupWindow {

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private TextView tvTip;
    private ImageView iVTip;
    private Handler handler = new Handler();
    private EndRunnable runnable;

    public PunchResultDialog(Context context) {
        super(context);
    }
    @Override
    protected void onViewCreated() {
        iVTip = findViewById(R.id.iv_punch_result);
        tvTip = findViewById(R.id.tv_punch_result);
    }

    @Override
    protected int getContentView() {
        return R.layout.layout_punch_result_dialog;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefScaleEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefScaleExitAnim();
    }


    public void show(View view, boolean isEnd){
        if (isEnd){
            iVTip.setVisibility(View.GONE);
            tvTip.setText("打卡结束");
            tvTip.setTextColor(Color.parseColor("#FF6633"));
        }else{

            iVTip.setVisibility(View.VISIBLE);
            tvTip.setText("打卡成功");
            tvTip.setTextColor(Color.parseColor("#1E1F21"));
        }
        show(view);
        runnable = new EndRunnable();
        handler.postDelayed(runnable,2000);
    }
    @Override
    public void show(View view) {
        super.show(view);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mHandler.removeCallbacksAndMessages(null);
    }
    private class EndRunnable implements Runnable {
        @Override
        public void run() {
            dismiss();
        }
    }
}
