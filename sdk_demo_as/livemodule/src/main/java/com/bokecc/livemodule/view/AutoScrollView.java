package com.bokecc.livemodule.view;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.bokecc.livemodule.R;
import com.bokecc.sdk.mobile.live.pojo.UserRedminAction;

public class AutoScrollView extends LinearLayout {
    private Handler handler = new Handler();
    private AutoScrollTextView textSwitcher;
    AutoScrollCallBack autoScrollCallBack = new AutoScrollCallBack() {
        @Override
        public void onEnd() {
            setVisibility(GONE);
//            AutoScrollView.this.setAnimation(AnimationUtils.loadAnimation(AutoScrollView.this.getContext(),R.anim.punch_out));

        }

        @Override
        public void onStart() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    setVisibility(VISIBLE);
//                    AutoScrollView.this.setAnimation(AnimationUtils.loadAnimation(AutoScrollView.this.getContext(),R.anim.punch_in));
                }
            });

        }
    };
    public AutoScrollView(Context context) {
        super(context);
        initView(context);
    }

    public AutoScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public AutoScrollView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        inflate(context, R.layout.auto_scroll_view,this);
        textSwitcher = findViewById(R.id.text_switcher);
        textSwitcher.setListener(autoScrollCallBack);
    }

    /**
     * 设置数据
     */
    public void addDate( UserRedminAction userJoinExitAction) {
        textSwitcher.addDate(userJoinExitAction);
    }

    public interface AutoScrollCallBack{
        void onEnd();
        void onStart();
    }
}
