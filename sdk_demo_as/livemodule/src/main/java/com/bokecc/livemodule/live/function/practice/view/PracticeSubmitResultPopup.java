package com.bokecc.livemodule.live.function.practice.view;

import android.content.Context;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.utils.PopupAnimUtil;
import com.bokecc.livemodule.view.BasePopupWindow;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.pojo.PracticeSubmitResultInfo;

/**
 * 随堂测提交结果弹出界面
 */
public class PracticeSubmitResultPopup extends BasePopupWindow {

    private ImageView mSubmitResultEmoji;
    private TextView mSubmitResultDesc;

    // 构造函数
    public PracticeSubmitResultPopup(Context context) {
        super(context);
    }

    @Override
    protected void onViewCreated() {
        mSubmitResultEmoji = findViewById(R.id.practice_result_emoji);
        mSubmitResultDesc = findViewById(R.id.practice_result_desc);
    }

    public void showPracticeSubmitResult(final PracticeSubmitResultInfo info) {
        // 展示答题结果 正确与否
        showPracticeResult(info);
        // 请求一次随堂测统计信息
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    if (isShowing()) {
                        DWLive.getInstance().getPracticeStatis(info.getId());
                        if (mSubmitResultEmoji != null) {
                            mSubmitResultEmoji.post(new Runnable() {
                                @Override
                                public void run() {
                                    dismiss();
                                }
                            });
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 展示答题结果
    public void showPracticeResult(PracticeSubmitResultInfo info) {
        mSubmitResultEmoji.setImageResource(info.getAnswerResult() == 1 ? R.drawable.practice_right : R.drawable.practice_wrong);
        mSubmitResultDesc.setText(info.getAnswerResult() == 1 ? "恭喜，答对啦！" : "哎呀，答错了，下次继续努力！");
    }

    @Override
    protected int getContentView() {
        return R.layout.practice_submit_result_layout;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefScaleEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefScaleExitAnim();
    }


}