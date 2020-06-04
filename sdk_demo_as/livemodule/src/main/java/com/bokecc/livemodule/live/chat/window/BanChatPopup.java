package com.bokecc.livemodule.live.chat.window;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.utils.PopupAnimUtil;
import com.bokecc.livemodule.view.BasePopupWindow;

/**
 * 弹出框
 */
public class BanChatPopup extends BasePopupWindow {

    public BanChatPopup(Context context) {
        super(context);
    }

    TextView tvBanchatContent;

    Button btnBanchatOk;

    @Override
    protected void onViewCreated() {

        tvBanchatContent = findViewById(R.id.tv_banchat_content);
        btnBanchatOk = findViewById(R.id.btn_banchat_ok);

        btnBanchatOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDestroy();
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.banchat_layout;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefScaleEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefScaleExitAnim();
    }

    /**
     *
     * @param content  弹框显示的文本
     */
    public void banChat(String content) {
        if (!TextUtils.isEmpty(content))
            tvBanchatContent.setText(content);
    }

    public void onDestroy() {
        dismiss();
    }

}