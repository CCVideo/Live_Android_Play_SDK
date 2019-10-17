package com.bokecc.livemodule.live.function.questionnaire.view;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.utils.PopupAnimUtil;
import com.bokecc.livemodule.view.BasePopupWindow;

public class ExeternalQuestionnairePopup extends BasePopupWindow {

    private Context mContext;

    private ImageView mCloseView; // 关闭问卷弹窗

    private TextView mTitleView; // 问卷标题（提示语）展示

    private Button mGoButton; // 第三方问卷打开按钮


    public ExeternalQuestionnairePopup(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onViewCreated() {
        mGoButton = findViewById(R.id.btn_go);
        mTitleView = findViewById(R.id.title);
        mCloseView = findViewById(R.id.close);
    }

    @Override
    protected int getContentView() {
        return R.layout.exeternal_questionnaire_layout;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefScaleEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefScaleExitAnim();
    }

    public void setQuestionnaireInfo(String title, final String externalUrl) {

        mTitleView.setText(title);
        mGoButton.setEnabled(true);

        mCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到网页界面，完成第三方问卷
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(externalUrl));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                dismiss();
            }
        });
    }
}
