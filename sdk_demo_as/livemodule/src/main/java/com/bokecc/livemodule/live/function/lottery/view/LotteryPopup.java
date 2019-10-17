package com.bokecc.livemodule.live.function.lottery.view;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.utils.PopupAnimUtil;
import com.bokecc.livemodule.view.BasePopupWindow;
import com.bumptech.glide.Glide;

/**
 *  中奖弹出框
 */
public class LotteryPopup extends BasePopupWindow {

    public LotteryPopup(Context context) {
        super(context);
    }

    TextView navTips;
    ImageView closeBtn;

    ImageView loadingIcon;

    LinearLayout winnerLayout;
    TextView tvLotteryCode;

    LinearLayout loserLayout;
    TextView winnerName;

    @Override
    protected void onViewCreated() {
        navTips = findViewById(R.id.lottery_nav_tips);
        closeBtn = findViewById(R.id.lottery_close);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        loadingIcon = findViewById(R.id.iv_lottery_loading);

        Glide.with(mContext).load(R.drawable.lottery_loading_gif).asGif().thumbnail( 0.1f ).into(loadingIcon);

        winnerLayout = findViewById(R.id.ll_lottery_win);
        tvLotteryCode = findViewById(R.id.lottery_code);

        loserLayout = findViewById(R.id.ll_lottery_lose);
        winnerName = findViewById(R.id.lottery_winnner_name);
    }

    @Override
    protected int getContentView() {
        return R.layout.lottery_layout;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefScaleEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefScaleExitAnim();
    }

    public void onLotteryResult(boolean isWin, String lotteryCode, String winnerName) {
        hideAll();
        if (isWin) {
            winnerLayout.setVisibility(View.VISIBLE);
            tvLotteryCode.setText(lotteryCode);
            navTips.setText("恭喜您中奖啦");
        } else {
            loserLayout.setVisibility(View.VISIBLE);
            this.winnerName.setText(winnerName);
            navTips.setText("哎呀，就差一点");
        }
    }

    private void hideAll() {
        loadingIcon.setVisibility(View.GONE);
        winnerLayout.setVisibility(View.GONE);
        loserLayout.setVisibility(View.GONE);
    }
}
