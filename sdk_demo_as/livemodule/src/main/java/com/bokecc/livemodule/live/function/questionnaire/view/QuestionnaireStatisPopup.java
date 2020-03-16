package com.bokecc.livemodule.live.function.questionnaire.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.function.questionnaire.adapter.QuestionnaireStatisAdapter;
import com.bokecc.livemodule.utils.PopupAnimUtil;
import com.bokecc.livemodule.view.BasePopupWindow;
import com.bokecc.sdk.mobile.live.pojo.QuestionnaireStatisInfo;

/**
 * 问卷统计弹出界面
 */
public class QuestionnaireStatisPopup extends BasePopupWindow {

    private Context mContext;
    private QuestionnaireStatisInfo mInfo;

    private RecyclerView rv_questionnaire_list; // 问卷题目
    private QuestionnaireStatisAdapter questionnaireStatisAdapter; // 问卷内容适配器
    private ImageView iv_close; // 关闭问卷弹窗

    public QuestionnaireStatisPopup(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onViewCreated() {
        rv_questionnaire_list = findViewById(R.id.questionnaire_list);
        iv_close = findViewById(R.id.close);
    }

    @Override
    protected int getContentView() {
        return R.layout.questionnaire_statis_layout;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefScaleEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefScaleExitAnim();
    }

    public void setQuestionnaireStatisInfo(QuestionnaireStatisInfo info) {
        mInfo = info;

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void show(View view) {
        super.show(view);
        questionnaireStatisAdapter = new QuestionnaireStatisAdapter(mContext, mInfo);
        rv_questionnaire_list.setLayoutManager(new LinearLayoutManager(mContext));
        rv_questionnaire_list.setAdapter(questionnaireStatisAdapter);
    }


}