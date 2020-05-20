package com.bokecc.livemodule.live.qa;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.live.DWLiveQAListener;
import com.bokecc.livemodule.live.chat.KeyboardHeightObserver;
import com.bokecc.livemodule.live.qa.adapter.LiveQaAdapter;
import com.bokecc.livemodule.live.qa.module.QaInfo;
import com.bokecc.livemodule.view.BaseRelativeLayout;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.pojo.Answer;
import com.bokecc.sdk.mobile.live.pojo.Question;

import org.json.JSONException;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 直播间问答组件
 */
public class LiveQAComponent extends BaseRelativeLayout implements DWLiveQAListener, KeyboardHeightObserver {

    // 列表
    private RecyclerView mQaList;
    private LiveQaAdapter mQaAdapter;
    private InputMethodManager mImm;

    // 底部输入框
    private View mChatLayout;
    private EditText mQaInput;
    private ImageView mQaVisibleStatus;

    private LinkedHashMap<String, QaInfo> mQaInfoMap;
    int mQaInfoLength;

    public LiveQAComponent(Context context) {
        super(context);
        initQaLayout();
    }

    public LiveQAComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initQaLayout();
    }

    public void initViews() {
        LayoutInflater.from(mContext).inflate(R.layout.live_portrait_qa_layout, this, true);
        mQaList = findViewById(R.id.rv_qa_container);
        mQaInput = findViewById(R.id.id_qa_input);
        mQaVisibleStatus = findViewById(R.id.self_qa_invisible);
        Button mQaSend = findViewById(R.id.id_qa_send);
        mQaInfoLength = 0;
        mChatLayout = findViewById(R.id.rl_qa_input_layout);

        // 发送问题
        mQaSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 判断如果直播未开始，则告诉用户，无法提问
                if (DWLive.getInstance().getPlayStatus() == DWLive.PlayStatus.PREPARING) {
                    toastOnUiThread("直播未开始，无法提问");
                    return;
                }

                // 直播中，提问判断内容是否符合要求，符合要求，进行提问
                String questionMsg = mQaInput.getText().toString().trim();
                if (TextUtils.isEmpty(questionMsg)) {
                    toastOnUiThread("输入信息不能为空");
                } else {
                    try {
                        DWLive.getInstance().sendQuestionMsg(questionMsg);
                        mQaInput.setText("");
                        mImm.hideSoftInputFromWindow(mQaInput.getWindowToken(), 0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // 切换所有问答和我的问答
        mQaVisibleStatus.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQaVisibleStatus.isSelected()) {
                    mQaVisibleStatus.setSelected(false);
                    toastOnUiThread("显示所有问答");
                    mQaAdapter.setOnlyShowSelf(false);


                } else {
                    mQaVisibleStatus.setSelected(true);
                    toastOnUiThread("只看我的问答");
                    mQaAdapter.setOnlyShowSelf(true);
                }
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initQaLayout() {
        mImm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mQaList.setLayoutManager(new LinearLayoutManager(mContext));
        mQaAdapter = new LiveQaAdapter(mContext);
        mQaList.setAdapter(mQaAdapter);
        // mQaList.addItemDecoration(new QaListDividerItemDecoration(mContext));
        mQaList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mImm.hideSoftInputFromWindow(mQaInput.getWindowToken(), 0);
                return false;
            }


        });

        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            dwLiveCoreHandler.setDwLiveQAListener(this);
        }
    }


    public void clearQaInfo() {
        mQaAdapter.resetQaInfos();
        mQaAdapter.notifyDataSetChanged();
    }

    public void addQuestion(Question question) {
        mQaAdapter.addQuestion(question);
        mQaAdapter.notifyDataSetChanged();
        if (mQaAdapter.getItemCount() > 1) {
            mQaList.scrollToPosition(mQaAdapter.getItemCount() - 1);
        }
    }

    public void showQuestion(String questionId) {
        mQaAdapter.showQuestion(questionId);
        mQaAdapter.notifyDataSetChanged();
    }

    public void addAnswer(Answer answer) {
        mQaAdapter.addAnswer(answer);
        mQaAdapter.notifyDataSetChanged();
    }

    //------------------------ 处理直播问答回调信息 ------------------------------------


    @Override
    public void onHistoryQuestionAnswer(final List<Question> questions, final List<Answer> answers) {

        mQaList.post(new Runnable() {
            @Override
            public void run() {
                for (Question question : questions) {
                    mQaAdapter.addQuestion(question);
                }

                for (Answer answer : answers) {
                    mQaAdapter.addAnswer(answer);
                }
                mQaAdapter.notifyDataSetChanged();
                if (mQaAdapter.getItemCount() > 1) {
                    mQaList.scrollToPosition(mQaAdapter.getItemCount() - 1);
                }
            }
        });
    }

    @Override
    public void onQuestion(final Question question) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addQuestion(question);
            }
        });
    }

    @Override
    public void onPublishQuestion(final String questionId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showQuestion(questionId);
            }
        });
    }

    @Override
    public void onAnswer(final Answer answer) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addAnswer(answer);
            }
        });
    }

    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {
        if (height > 10) {
            mChatLayout.setTranslationY(-height);
        } else {
            mChatLayout.setTranslationY(0);
        }
    }
}

