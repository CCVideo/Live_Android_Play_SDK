package com.bokecc.livemodule.replaymix.qa.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.live.qa.module.QaInfo;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.pojo.Answer;
import com.bokecc.sdk.mobile.live.pojo.LiveInfo;
import com.bokecc.sdk.mobile.live.pojo.Question;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 回放问答适配器
 */
public class ReplayMixQaAdapter extends RecyclerView.Adapter<ReplayMixQaAdapter.ChatViewHolder> {

    private final static String TAG = ReplayMixQaAdapter.class.getSimpleName();

    private Context mContext;

    private ArrayList<String> mPublishedIdList;  // 已经发布的问题Id列表

    // 拿空间换时间
    private LinkedHashMap<String, QaInfo> mQaInfoMapCurrent;
    private LinkedHashMap<String, QaInfo> mQaInfoMapAll;
    private LinkedHashMap<String, QaInfo> mQaInfoMapNormal;
    private LinkedHashMap<String, QaInfo> mQaInfoMapSelf;
    private LayoutInflater mInflater;

    // 获取到直播间信息
    private LiveInfo liveInfo;

    public ReplayMixQaAdapter(Context context) {
        mQaInfoMapAll = new LinkedHashMap<>();
        mQaInfoMapNormal = new LinkedHashMap<>();
        mQaInfoMapSelf = new LinkedHashMap<>();
        mPublishedIdList = new ArrayList<>();
        mQaInfoMapCurrent = mQaInfoMapNormal;
        mContext = context;
        mInflater = LayoutInflater.from(context);

        // 获取直播间信息，用于计算问答时间
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            liveInfo = DWLiveCoreHandler.getInstance().getLiveInfo();
        }
    }

    /**
     * 重连的时候，需要重置
     */
    public void resetQaInfos() {
        if (mQaInfoMapCurrent != null) {
            mQaInfoMapAll.clear();
            mQaInfoMapNormal.clear();
            mQaInfoMapSelf.clear();
        }
    }

    private boolean isOnlyShowSelf = false;

    public void setOnlyShowSelf(boolean isOnlyShowSelf) {
        if (isOnlyShowSelf) {
            mQaInfoMapCurrent = mQaInfoMapSelf;
        } else {
            mQaInfoMapCurrent = mQaInfoMapNormal;
        }

        notifyDataSetChanged();
    }

    // 用于回放的问答添加
    public void addReplayQuestoinAnswer(LinkedHashMap<String, QaInfo> mQaInfoMap) {
        this.mQaInfoMapCurrent = mQaInfoMap;
        notifyDataSetChanged();
    }

    public void addQuestion(Question question) {
        if (mQaInfoMapAll.containsKey(question.getId())) {
            return;
        } else {
            mQaInfoMapAll.put(question.getId(), new QaInfo(question));

            if (question.getQuestionUserId().equals(DWLive.getInstance().getViewer().getId())) {
                // 本人发布的问题，进行展示
                mQaInfoMapNormal.put(question.getId(), new QaInfo(question));
                mQaInfoMapSelf.put(question.getId(), new QaInfo(question));
            } else if (question.getIsPublish() == 1) {
                // 如果接收到问题的已经发布了，也展示出来
                if (!mPublishedIdList.contains(question.getId())) {
                    mPublishedIdList.add(question.getId());
                }
                mQaInfoMapNormal.put(question.getId(), new QaInfo(question));
            }
        }

        notifyDataSetChanged();
    }

    /**
     * 收到客户端发布的questionId，将问题展示出来
     */
    public void showQuestion(String questionId) {

        // 如果当前 QaInfoMapAll 没有存储此id，不做任何处理
        if (!mQaInfoMapAll.containsKey(questionId)) {
            return;
        }

        String currentUserId = DWLive.getInstance().getViewer().getId();
        mQaInfoMapNormal.clear();

        if (!mPublishedIdList.contains(questionId)) {
            mPublishedIdList.add(questionId);
        }

        for (Map.Entry<String, QaInfo> entry : mQaInfoMapAll.entrySet()) {
            if (entry.getValue().getAnswers().size() > 0) {
                QaInfo qaInfo = entry.getValue();
                QaInfo newQaInfo = new QaInfo(qaInfo.getQuestion());
                newQaInfo.setAnswers((ArrayList<Answer>) qaInfo.getAnswers().clone()); //防止浅拷贝
                mQaInfoMapNormal.put(entry.getKey(), newQaInfo);
            } else if (entry.getValue().getQuestion().getQuestionUserId().equals(currentUserId)) {
                Question mQuestion = entry.getValue().getQuestion();
                mQaInfoMapNormal.put(mQuestion.getId(), new QaInfo(mQuestion));
            } else if (entry.getValue().getAnswers().size() == 0 && mPublishedIdList.contains(entry.getValue().getQuestion().getId())) {
                QaInfo qaInfo = entry.getValue();
                QaInfo newQaInfo = new QaInfo(qaInfo.getQuestion());
                mQaInfoMapNormal.put(entry.getKey(), newQaInfo);
            }
        }

        notifyDataSetChanged();
    }

    public void addAnswer(Answer answer) {
        if (mQaInfoMapAll.containsKey(answer.getQuestionId())) {

            // 检测要答案是否已经存在，如果已经存在了，就不执行添加了操作
            ArrayList<Answer> answers = mQaInfoMapAll.get(answer.getQuestionId()).getAnswers();
            if (answers.size() > 0) {
                for (Answer mapAnswer : answers) {
                    if (mapAnswer.equals(answer)) {
                        Log.e(TAG, "now map has contain this answer, not to add");
                        return;
                    }
                }
            }

            String currentUserId = DWLive.getInstance().getViewer().getId();

            mQaInfoMapAll.get(answer.getQuestionId()).addAnswer(answer);

            Question question = mQaInfoMapAll.get(answer.getQuestionId()).getQuestion();

            if (mQaInfoMapNormal.containsKey(question.getId())) {
                mQaInfoMapNormal.get(question.getId()).addAnswer(answer);
            } else {
                mQaInfoMapNormal.clear();

                for (Map.Entry<String, QaInfo> entry : mQaInfoMapAll.entrySet()) {
                    if (entry.getValue().getAnswers().size() > 0) {

                        QaInfo qaInfo = entry.getValue();
                        QaInfo newQaInfo = new QaInfo(qaInfo.getQuestion());
                        newQaInfo.setAnswers((ArrayList<Answer>) qaInfo.getAnswers().clone()); //防止浅拷贝

                        mQaInfoMapNormal.put(entry.getKey(), newQaInfo);
                    } else if (entry.getValue().getQuestion().getQuestionUserId().equals(currentUserId)) {
                        Question mQuestion = entry.getValue().getQuestion();
                        mQaInfoMapNormal.put(mQuestion.getId(), new QaInfo(mQuestion));
                    }
                }
            }

            if (question.getQuestionUserId().equals(currentUserId)) {
                mQaInfoMapSelf.get(answer.getQuestionId()).addAnswer(answer);
            }

            notifyDataSetChanged();
        }
    }

    public LinkedHashMap<String, QaInfo> getQaInfos() {
        return mQaInfoMapCurrent;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.live_pc_qa_single_line, parent, false);
        return new ChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        ArrayList<String> list = new ArrayList<>(mQaInfoMapCurrent.keySet());
        QaInfo info = mQaInfoMapCurrent.get(list.get(position));

        Question question = info.getQuestion();
        ArrayList<Answer> answers = info.getAnswers();

        holder.questionName.setText(question.getQuestionUserName());

        // 计算问答时间
        try {
            int sendTime = Integer.valueOf(question.getTime());
            if (sendTime > 0) {
                if (liveInfo != null) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = simpleDateFormat.parse(liveInfo.getLiveStartTime());
                    Date dateForShow = new Date(date.getTime() + sendTime * 1000);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    holder.questionTime.setText(sdf.format(dateForShow));
                }
            } else {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                holder.questionTime.setText(simpleDateFormat.format(new Date()));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.questionContent.setText(question.getContent());

        holder.answerContainer.removeAllViews();

        if (answers != null && answers.size() > 0) {
            for (Answer answer : answers) {
                String msg = answer.getAnswerUserName() + ": " + answer.getContent();
                SpannableString ss = new SpannableString(msg);
                ss.setSpan(new ForegroundColorSpan(Color.parseColor("#12ad1a")),
                        0, answer.getAnswerUserName().length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new ForegroundColorSpan(Color.parseColor("#1E1F21")),
                        answer.getAnswerUserName().length() + 1, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                TextView textView = new TextView(mContext);
                textView.setText(ss);
                textView.setLineSpacing(0, 1.1f);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.pc_live_qa_answer));
                textView.setGravity(Gravity.CENTER_VERTICAL);
                holder.answerContainer.addView(textView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
        } else {
            holder.qaSeparateLine.setVisibility(View.GONE);
        }

        if (isOnlyShowSelf) {
            if (question.getQuestionUserId().equals(DWLive.getInstance().getViewer().getId())) {
                holder.qaSingleLayout.setVisibility(View.VISIBLE);
            } else {
                holder.qaSingleLayout.setVisibility(View.GONE);
            }
        } else {
            holder.qaSingleLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mQaInfoMapCurrent == null ? 0 : mQaInfoMapCurrent.size();
    }

    final class ChatViewHolder extends RecyclerView.ViewHolder {

        ImageView questionHeadView;
        TextView questionName;
        TextView questionTime;
        TextView questionContent;
        LinearLayout answerContainer;
        View qaSingleLayout;
        View qaSeparateLine;

        public ChatViewHolder(View itemView) {
            super(itemView);
            questionHeadView = itemView.findViewById(R.id.user_head_view);
            questionName = itemView.findViewById(R.id.tv_question_name);
            questionTime = itemView.findViewById(R.id.tv_question_time);
            questionContent = itemView.findViewById(R.id.tv_question);
            answerContainer = itemView.findViewById(R.id.ll_answer);
            qaSingleLayout = itemView.findViewById(R.id.ll_qa_single_layout);
            qaSeparateLine = itemView.findViewById(R.id.qa_separate_line);
        }
    }
}
