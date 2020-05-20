package com.bokecc.livemodule.live.qa.module;

import com.bokecc.sdk.mobile.live.pojo.Answer;
import com.bokecc.sdk.mobile.live.pojo.Question;

import java.util.ArrayList;

/**
 * 问答信息封装类
 */
public class QaInfo {

    private Question mQuestion;
    private ArrayList<Answer> answers;

    public QaInfo(Question question) {
        answers = new ArrayList<>();
        this.mQuestion = question;
    }

    public void addAnswer(Answer answer) {
        answers.add(answer);
    }

    public Question getQuestion() {
        return mQuestion;
    }

    public ArrayList<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(ArrayList<Answer> answers) {
        this.answers = answers;
    }
}