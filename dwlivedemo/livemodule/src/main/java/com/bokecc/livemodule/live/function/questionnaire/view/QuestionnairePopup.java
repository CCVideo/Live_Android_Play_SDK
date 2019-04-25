package com.bokecc.livemodule.live.function.questionnaire.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.function.questionnaire.adapter.QuestionnaireAdapter;
import com.bokecc.livemodule.utils.PopupAnimUtil;
import com.bokecc.livemodule.view.BasePopupWindow;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.pojo.QuestionnaireInfo;
import com.bokecc.sdk.mobile.live.socket.SocketQuestionnaireHandler;
import com.bokecc.sdk.mobile.live.util.NetworkUtils;

import org.json.JSONException;

public class QuestionnairePopup extends BasePopupWindow implements SocketQuestionnaireHandler.QuestionnaireListener {

    private final static String NOT_COMPLETE = "您尚有部分题目未回答，请检查。"; // 未完成提示语
    private final static String NO_NETWORK = "网络异常，提交失败，请重试。";
    private final static String SUBMIT_SUCCESS = "答卷提交成功!"; // 提交成功提示语

    private boolean mHasSubmited;

    private int mSubmitedaction;  // 提交后的操作，非必须，默认为0，0：关闭，1：提交后显示答案

    private Context mContext;
    private QuestionnaireInfo mInfo;

    private RecyclerView rv_questionnaire_list; // 问卷题目
    private QuestionnaireAdapter questionnaireAdapter; // 问卷内容适配器
    private TextView tv_tip; // 问卷提交按钮点击时提示语展示
    private Button btn_submit; // 问卷提交按钮
    private ImageView iv_close; // 关闭问卷弹窗

    public QuestionnairePopup(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onViewCreated() {
        rv_questionnaire_list = findViewById(R.id.questionnaire_list);
        btn_submit = findViewById(R.id.btn_submit);
        tv_tip = findViewById(R.id.tip);
        iv_close = findViewById(R.id.close);
    }

    @Override
    protected int getContentView() {
        return R.layout.questionnaire_layout;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefScaleEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefScaleExitAnim();
    }

    public void setQuestionnaireInfo(QuestionnaireInfo info) {
        mHasSubmited = false;
        mInfo = info;
        mSubmitedaction = mInfo.getSubmitedAction();
        tv_tip.setVisibility(View.INVISIBLE);
        btn_submit.setEnabled(true);
        questionnaireAdapter = new QuestionnaireAdapter(mContext, mInfo);
        rv_questionnaire_list.setLayoutManager(new LinearLayoutManager(mContext));
        rv_questionnaire_list.setAdapter(questionnaireAdapter);

        // 获取问卷信息的强制问卷字段，如果为0,就是非强制，则展示关闭按钮，否则隐藏关闭按钮
        iv_close.setVisibility(info.getForcibly() == 0 ? View.VISIBLE : View.INVISIBLE);

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 隐藏输入法
                hideKeyboard();
                btn_submit.setEnabled(false);

                // 如果问卷完成了，则生成答案数据
                if (questionnaireAdapter.isQuestionnaireComplete()) {
                    try {
                        // 判断当前是否有网络
                        if (NetworkUtils.isNetworkAvailable(mContext)) {
                            tv_tip.setVisibility(View.INVISIBLE);
                            DWLive.getInstance().sendQuestionnaireAnswer(QuestionnairePopup.this, mInfo.getId(), questionnaireAdapter.getQuestionnaireAnswer());
                        } else {
                            btn_submit.setEnabled(true);
                            tv_tip.setVisibility(View.VISIBLE);
                            tv_tip.setText(NO_NETWORK);
                            tv_tip.setTextColor(0xffe03a3a);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    btn_submit.setEnabled(true);
                    tv_tip.setVisibility(View.VISIBLE);
                    tv_tip.setText(NOT_COMPLETE);
                    tv_tip.setTextColor(0xffe03a3a);
                }
            }
        });
    }

    /** 是否已经提交了问卷 */
    public boolean hasSubmitedQuestionnaire() {
        return mHasSubmited;
    }

    /* 隐藏输入法 */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rv_questionnaire_list.getWindowToken(), 0);
    }

    @Override
    public void onSubmitResult(final boolean isSucceed, final String msg) {
        tv_tip.post(new Runnable() {
            @Override
            public void run() {
                if (isSucceed) {
                    btn_submit.setEnabled(false);
                    tv_tip.setVisibility(View.VISIBLE);
                    tv_tip.setText(SUBMIT_SUCCESS);
                    tv_tip.setTextColor(0xff17bc2f);
                } else {
                    tv_tip.setVisibility(View.VISIBLE);
                    btn_submit.setEnabled(true);
                    tv_tip.setTextColor(0xffe03a3a);
                    tv_tip.setText(msg);
                }
            }
        });

        // 提交成功后关闭对话框
        if (isSucceed) {
            mHasSubmited = true;
            //
            if (mSubmitedaction == 1) {
                rv_questionnaire_list.post(new Runnable() {
                    @Override
                    public void run() {
                        iv_close.setVisibility(View.VISIBLE);
                        questionnaireAdapter.showRight();
                    }
                });
            } else {
                // 如果不需要提交后显示答案，就3秒后消失
                tv_tip.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                }, 3000);
            }
        }
    }
}

