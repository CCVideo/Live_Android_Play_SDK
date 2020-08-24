package com.bokecc.livemodule.live;

import com.bokecc.sdk.mobile.live.pojo.Answer;
import com.bokecc.sdk.mobile.live.pojo.Question;

import java.util.List;

/**
 * 直播问答回调监听
 */
public interface DWLiveQAListener {


    /**
     * 历史问答
     */
    void onHistoryQuestionAnswer(List<Question> questions, List<Answer> answers);

    /**
     * 提问
     *
     * @param question 问题信息
     */
    void onQuestion(Question question);

    /**
     * 收到客户端发布的问题的编号
     *
     * @param questionId 问题id
     */
    void onPublishQuestion(String questionId);

    /**
     * 回答
     *
     * @param answer 回答信息
     */
    void onAnswer(Answer answer);

}
