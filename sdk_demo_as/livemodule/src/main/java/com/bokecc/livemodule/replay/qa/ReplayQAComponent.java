package com.bokecc.livemodule.replay.qa;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.qa.module.QaInfo;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.livemodule.replay.DWReplayQAListener;
import com.bokecc.livemodule.replay.qa.adapter.ReplayQaAdapter;
import com.bokecc.sdk.mobile.live.pojo.Answer;
import com.bokecc.sdk.mobile.live.pojo.Question;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayAnswerMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQAMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQuestionMsg;

import java.util.LinkedHashMap;
import java.util.TreeSet;

/**
 * 回放问答展示控件
 */
public class ReplayQAComponent extends RelativeLayout implements DWReplayQAListener {

    private Context mContext;
    private RecyclerView mQaList;
    private RelativeLayout mInputLayout;
    private ReplayQaAdapter mQaAdapter;

    private LinkedHashMap<String, QaInfo> mQaInfoMap;
    int mQaInfoLength;

    public ReplayQAComponent(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public ReplayQAComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
    }

    private void initViews() {
        LayoutInflater.from(mContext).inflate(R.layout.live_portrait_qa_layout, this, true);
        mQaList = findViewById(R.id.rv_qa_container);
        mInputLayout = findViewById(R.id.rl_qa_input_layout);
        mQaInfoLength = 0;
        mInputLayout.setVisibility(View.GONE);
        initQaLayout();
    }

    public void initQaLayout() {
        mQaList.setLayoutManager(new LinearLayoutManager(mContext));
        mQaAdapter = new ReplayQaAdapter(mContext);
        mQaList.setAdapter(mQaAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);

        // 设置监听
        DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
        if (dwReplayCoreHandler != null) {
            dwReplayCoreHandler.setReplayQAListener(this);
        }
    }

    public void clearQaInfo() {
        mQaAdapter.resetQaInfos();
    }

    public void addReplayQAInfos(LinkedHashMap<String, QaInfo> replayQaInfos) {
        mQaAdapter.addReplayQuestoinAnswer(replayQaInfos);
    }

    public void addQuestion(Question question) {
        mQaAdapter.addQuestion(question);
    }

    public void addAnswer(Answer answer) {
        mQaAdapter.addAnswer(answer);
    }

    @Override
    public void onQuestionAnswer(final TreeSet<ReplayQAMsg> qaMsgs) {
        LinkedHashMap<String, QaInfo> qaInfoMap = new LinkedHashMap<>();

        for (ReplayQAMsg qaMsg: qaMsgs) {

            ReplayQuestionMsg questionMsg = qaMsg.getReplayQuestionMsg();
            Question question = new Question();
            question.setContent(questionMsg.getContent())
                    .setId(questionMsg.getQuestionId())
                    .setQuestionUserId(questionMsg.getQuestionUserId())
                    .setQuestionUserName(questionMsg.getQuestionUserName())
                    .setTime(String.valueOf(questionMsg.getTime()))
                    .setUserAvatar(questionMsg.getQuestionUserAvatar());

            TreeSet<ReplayAnswerMsg> answerMsgs = qaMsg.getReplayAnswerMsgs();

            // 没有回答
            if (answerMsgs.size() < 1) {
                if (questionMsg.getIsPublish() == 0) {
                    // 未发布的问题
                    continue;
                } else if (questionMsg.getIsPublish() == 1) {
                    // 发布的问题
                    QaInfo qaInfo = new QaInfo(question);
                    qaInfoMap.put(question.getId(), qaInfo);
                    continue;
                }
            }

            // 回答过
            QaInfo qaInfo = new QaInfo(question);
            for (ReplayAnswerMsg answerMsg:answerMsgs) {
                Answer answer = new Answer();
                answer.setUserAvatar(answerMsg.getUserAvatar())
                        .setContent(answerMsg.getContent())
                        .setAnswerUserId(answerMsg.getUserId())
                        .setAnswerUserName(answerMsg.getUserName())
                        .setReceiveTime(String.valueOf(answerMsg.getTime()))
                        .setUserRole(answerMsg.getUserRole());
                qaInfo.addAnswer(answer);
            }

            qaInfoMap.put(question.getId(), qaInfo);
        }

        mQaInfoMap = qaInfoMap;

        mQaList.post(new Runnable() {
            @Override
            public void run() {
                addReplayQAInfos(mQaInfoMap);
            }
        });
    }
}
