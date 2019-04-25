package com.bokecc.livemodule.live.qa;

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
import com.bokecc.livemodule.live.qa.adapter.LiveQaAdapter;
import com.bokecc.livemodule.live.qa.util.QaListDividerItemDecoration;
import com.bokecc.livemodule.view.BaseRelativeLayout;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.pojo.Answer;
import com.bokecc.sdk.mobile.live.pojo.Question;

import org.json.JSONException;

/**
 * 直播间问答组件
 */
public class LiveQAComponent extends BaseRelativeLayout implements DWLiveQAListener {

    private RecyclerView mQaList;
    private EditText mQaInput;
    private ImageView mQaVisibleStatus;
    private Button mQaSend;

    private LiveQaAdapter mQaAdapter;

    private InputMethodManager mImm;

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
        mQaList = (RecyclerView) findViewById(R.id.rv_qa_container);
        mQaInput = (EditText) findViewById(R.id.id_qa_input);
        mQaVisibleStatus = (ImageView) findViewById(R.id.self_qa_invisible);
        mQaSend = (Button) findViewById(R.id.id_qa_send);

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
                    toastOnUiThread("显示所有回答");
                    mQaAdapter.setOnlyShowSelf(false);


                } else {
                    mQaVisibleStatus.setSelected(true);
                    toastOnUiThread("只看我的回答");
                    mQaAdapter.setOnlyShowSelf(true);
                }
            }
        });
    }

    public void initQaLayout() {
        mImm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mQaList.setLayoutManager(new LinearLayoutManager(mContext));
        mQaAdapter = new LiveQaAdapter(mContext);
        mQaList.setAdapter(mQaAdapter);
        mQaList.addItemDecoration(new QaListDividerItemDecoration(mContext));
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
    }

    public void addQuestion(Question question) {
        mQaAdapter.addQuestion(question);
        if (mQaAdapter.getItemCount() > 1) {
            mQaList.smoothScrollToPosition(mQaAdapter.getItemCount() - 1);
        }
    }

    public void showQuestion(String questionId) {
        mQaAdapter.showQuestion(questionId);
    }

    public void addAnswer(Answer answer) {
        mQaAdapter.addAnswer(answer);
    }

    //------------------------ 处理直播问答回调信息 ------------------------------------

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
}

