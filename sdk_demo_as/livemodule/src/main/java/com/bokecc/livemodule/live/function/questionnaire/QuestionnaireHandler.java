package com.bokecc.livemodule.live.function.questionnaire;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;

import com.bokecc.livemodule.live.function.questionnaire.view.ExeternalQuestionnairePopup;
import com.bokecc.livemodule.live.function.questionnaire.view.QuestionnairePopup;
import com.bokecc.livemodule.live.function.questionnaire.view.QuestionnaireStatisPopup;
import com.bokecc.livemodule.live.function.questionnaire.view.QuestionnaireStopPopup;
import com.bokecc.sdk.mobile.live.pojo.QuestionnaireInfo;
import com.bokecc.sdk.mobile.live.pojo.QuestionnaireStatisInfo;

/**
 * '问卷' 处理机制
 */
public class QuestionnaireHandler {

    private QuestionnairePopup mQuestionnairePopup;  // 问卷弹出界面
    private QuestionnaireStopPopup mQuestionnaireStopPopup; // 问卷结束弹出界面
    private ExeternalQuestionnairePopup mExeternalQuestionnairePopup; // 第三方问卷弹出界面
    private QuestionnaireStatisPopup mQuestionnaireStatisPopup; // 问卷统计界面

    /** 初始化问卷的弹出界面 */
    public void initQuestionnaire(Context context) {
        mQuestionnairePopup = new QuestionnairePopup(context);
        mExeternalQuestionnairePopup = new ExeternalQuestionnairePopup(context);

        mQuestionnaireStopPopup = new QuestionnaireStopPopup(context);
        mQuestionnaireStatisPopup = new QuestionnaireStatisPopup(context);
    }


    /** 开始问卷答题 */
    public void startQuestionnaire(View rootView, final QuestionnaireInfo info) {
        if (mQuestionnaireStopPopup!=null&&mQuestionnaireStopPopup.isShowing())
            mQuestionnaireStopPopup.dismiss();
        if (mQuestionnaireStatisPopup!=null&&mQuestionnaireStatisPopup.isShowing()){
            mQuestionnaireStatisPopup.dismiss();
        }
        mQuestionnairePopup.setQuestionnaireInfo(info);
        if (!mQuestionnairePopup.isShowing()){
            mQuestionnairePopup.show(rootView);
        }
    }

    /** 停止问卷答题 */
    public void stopQuestionnaire(View rootView) {
        if (mQuestionnairePopup != null && mQuestionnairePopup.isShowing()) {
            if (!mQuestionnairePopup.hasSubmitedQuestionnaire()) {
                mQuestionnairePopup.dismiss();
                mQuestionnaireStopPopup.show(rootView);
            }
        }
    }

    /** 展示问卷统计信息 */
    public void showQuestionnaireStatis(View rootView, final QuestionnaireStatisInfo info) {
        mQuestionnaireStatisPopup.setQuestionnaireStatisInfo(info);
        mQuestionnaireStatisPopup.show(rootView);
    }

    /** 展示第三方问卷 */
    public void showExeternalQuestionnaire(View rootView, final String title, final String externalUrl) {
        if (mExeternalQuestionnairePopup != null) {
            mExeternalQuestionnairePopup.setQuestionnaireInfo(title, externalUrl);
            mExeternalQuestionnairePopup.show(rootView);
        }
    }

}
