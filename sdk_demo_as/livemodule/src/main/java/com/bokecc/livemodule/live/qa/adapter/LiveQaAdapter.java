package com.bokecc.livemodule.live.qa.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
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
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class LiveQaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static String TAG = LiveQaAdapter.class.getSimpleName();
    private final static int TYPE_QUESTION = 0;
    private final static int TYPE_ANSWER = 1;


    private ArrayList<String> mAllIdList;        // 所有的问答列表
    private ArrayList<String> mMyIdList;         // 我自己的问答列表
    private ArrayList<String> mPublishedIdList;  // 发布的问答列表
    private ArrayList<String> mIdListCurrent;    // 当前列表

    // 拿空间换时间
    private LinkedHashMap<String, QaInfo> mQaInfoMapCurrent;
    private LinkedHashMap<String, QaInfo> mQaInfoMapAll;
    private LinkedHashMap<String, QaInfo> mQaInfoMapNormal;
    private LinkedHashMap<String, QaInfo> mQaInfoMapSelf;
    private LayoutInflater mInflater;


    // 获取到直播间信息
    private LiveInfo liveInfo;
    // 是否只显示自己的
    private boolean isOnlyShowSelf = false;
    // 数据是否发生改变
    private boolean isDataChange;


    private SimpleDateFormat simpleDateFormat;
    private SimpleDateFormat sdf;
    private Calendar cal = Calendar.getInstance();
    private SpannableStringBuilder ss = new SpannableStringBuilder();

    public LiveQaAdapter(Context context) {
        mQaInfoMapAll = new LinkedHashMap<>();
        mQaInfoMapNormal = new LinkedHashMap<>();
        mQaInfoMapSelf = new LinkedHashMap<>();
        mPublishedIdList = new ArrayList<>();
        mMyIdList = new ArrayList<>();
        mAllIdList = new ArrayList<>();
        mIdListCurrent = mPublishedIdList;
        mQaInfoMapCurrent = mQaInfoMapNormal;
        mInflater = LayoutInflater.from(context);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf = new SimpleDateFormat("HH:mm");
        // 获取直播间信息，用于计算问答时间
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            liveInfo = DWLiveCoreHandler.getInstance().getLiveInfo();
        }
    }

    // 用于回放的问答添加
    public void addReplayQuestionAnswer(LinkedHashMap<String, QaInfo> mQaInfoMap) {
        mQaInfoMapAll.putAll(mQaInfoMap);
        mAllIdList.addAll(mQaInfoMap.keySet());
        mQaInfoMapNormal.putAll(mQaInfoMap);

        isDataChange = true;
        notifyDataSetChanged();
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

        if (mIdListCurrent != null) {
            mAllIdList.clear();
            mMyIdList.clear();
            mPublishedIdList.clear();
        }

        isDataChange = true;
    }

    /**
     * 设置是否只显示自己的问答
     */
    public void setOnlyShowSelf(boolean isOnlyShowSelf) {
        if (isOnlyShowSelf) {
            mQaInfoMapCurrent = mQaInfoMapSelf;
            mIdListCurrent = mMyIdList;
        } else {
            mQaInfoMapCurrent = mQaInfoMapNormal;
            mIdListCurrent = mPublishedIdList;
        }
        this.isOnlyShowSelf = isOnlyShowSelf;
        isDataChange = true;
        notifyDataSetChanged();
    }



    /**
     * 添加一条问题
     */
    public void addQuestion(Question question) {
        if (mQaInfoMapAll.containsKey(question.getId())) {
            return;
        } else {
            mQaInfoMapAll.put(question.getId(), new QaInfo(question));
            mAllIdList.add(question.getId());

            if (question.getQuestionUserId().equals(DWLive.getInstance().getViewer().getId())) {
                // 本人发布的问题，进行展示
                mQaInfoMapNormal.put(question.getId(), new QaInfo(question));
                mQaInfoMapSelf.put(question.getId(), new QaInfo(question));
                mPublishedIdList.add(question.getId());
                mMyIdList.add(question.getId());
            } else if (question.getIsPublish() == 1) {
                // 如果接收到问题的已经发布了，也展示出来
                if (!mPublishedIdList.contains(question.getId())) {
                    mPublishedIdList.add(question.getId());
                }
                if (!mQaInfoMapNormal.containsKey(question.getId())) {
                    mQaInfoMapNormal.put(question.getId(), new QaInfo(question));
                }

            }
        }

        isDataChange = true;

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
        mPublishedIdList.clear();
        for (Map.Entry<String, QaInfo> entry : mQaInfoMapAll.entrySet()) {
            if (entry.getValue().getAnswers().size() > 0) {
                QaInfo qaInfo = entry.getValue();
                QaInfo newQaInfo = new QaInfo(qaInfo.getQuestion());
                newQaInfo.setAnswers((ArrayList<Answer>) qaInfo.getAnswers().clone()); //防止浅拷贝
                mQaInfoMapNormal.put(entry.getKey(), newQaInfo);
                mPublishedIdList.add(entry.getKey());
            } else if (entry.getValue().getQuestion().getQuestionUserId().equals(currentUserId)) {
                Question mQuestion = entry.getValue().getQuestion();
                mQaInfoMapNormal.put(mQuestion.getId(), new QaInfo(mQuestion));
                mPublishedIdList.add(mQuestion.getId());
            } else if (entry.getValue().getAnswers().size() == 0) {
                QaInfo qaInfo = entry.getValue();
                Question question = qaInfo.getQuestion();
                if (question.getIsPublish() == 1) {
                    QaInfo newQaInfo = new QaInfo(question);
                    mQaInfoMapNormal.put(entry.getKey(), newQaInfo);
                    mPublishedIdList.add(entry.getKey());
                }

            }
        }
        isDataChange = true;
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
            // 添加回答
            mQaInfoMapAll.get(answer.getQuestionId()).addAnswer(answer);

            Question question = mQaInfoMapAll.get(answer.getQuestionId()).getQuestion();

            if (mQaInfoMapNormal.containsKey(question.getId())) {
                mQaInfoMapNormal.get(question.getId()).addAnswer(answer);
            } else {
                mQaInfoMapNormal.clear();
                mPublishedIdList.clear();
                for (Map.Entry<String, QaInfo> entry : mQaInfoMapAll.entrySet()) {
                    if (entry.getValue().getAnswers().size() > 0) {

                        QaInfo qaInfo = entry.getValue();
                        QaInfo newQaInfo = new QaInfo(qaInfo.getQuestion());
                        newQaInfo.setAnswers((ArrayList<Answer>) qaInfo.getAnswers().clone()); //防止浅拷贝

                        mQaInfoMapNormal.put(entry.getKey(), newQaInfo);
                        mPublishedIdList.add(entry.getKey());
                    } else if (entry.getValue().getQuestion().getQuestionUserId().equals(currentUserId)) {
                        Question mQuestion = entry.getValue().getQuestion();
                        mQaInfoMapNormal.put(mQuestion.getId(), new QaInfo(mQuestion));
                        mPublishedIdList.add(mQuestion.getId());
                    }
                }
            }

            if (question.getQuestionUserId().equals(currentUserId)) {
                mQaInfoMapSelf.get(answer.getQuestionId()).addAnswer(answer);
            }
            isDataChange = true;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == TYPE_QUESTION) {
            itemView = mInflater.inflate(R.layout.live_pc_qa_single_line, parent, false);
            return new LiveQaAdapter.ChatViewHolder(itemView);
        } else {
            itemView = mInflater.inflate(R.layout.live_pc_qa_answer, parent, false);
            return new LiveQaAdapter.SubViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemViewType = holder.getItemViewType();
        // 问题列表
        if (itemViewType == TYPE_QUESTION) {
            int[] ints = translateToDoubleIndex(position);
            QaInfo info = mQaInfoMapCurrent.get(mIdListCurrent.get(ints[0]));
            if (info != null) {
                Question question = info.getQuestion();
                // 设置提问者名称
                ss.clear();
                ss.append(question.getQuestionUserName());
                ss.setSpan(getQuestionRoleNameColorSpan(question), 0, question.getQuestionUserName().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ((LiveQaAdapter.ChatViewHolder) holder).questionName.setText(ss);
                // 计算问答时间
                try {
                    int sendTime = Integer.valueOf(question.getTime());
                    if (sendTime > 0) {
                        if (liveInfo != null) {
                            Date date = simpleDateFormat.parse(liveInfo.getLiveStartTime());
                            cal.setTime(date);
                            cal.add(Calendar.SECOND, sendTime);
                            date = cal.getTime();
                            ((LiveQaAdapter.ChatViewHolder) holder).questionTime.setText(sdf.format(date));
                        }
                    } else {
                        ((LiveQaAdapter.ChatViewHolder) holder).questionTime.setText(sdf.format(new Date()));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                // 设置问题内容
                ((LiveQaAdapter.ChatViewHolder) holder).questionContent.setText(question.getContent());

                if (info.getAnswers() != null && info.getAnswers().size() > 0) {
                    ((LiveQaAdapter.ChatViewHolder) holder).qaSeparateLine.setVisibility(View.VISIBLE);
                } else {
                    ((LiveQaAdapter.ChatViewHolder) holder).qaSeparateLine.setVisibility(View.GONE);
                }


                if (isOnlyShowSelf) {
                    if (question.getQuestionUserId().equals(DWLive.getInstance().getViewer().getId())) {
                        ((LiveQaAdapter.ChatViewHolder) holder).qaSingleLayout.setVisibility(View.VISIBLE);
                    } else {
                        ((LiveQaAdapter.ChatViewHolder) holder).qaSingleLayout.setVisibility(View.GONE);
                    }
                } else {
                    ((LiveQaAdapter.ChatViewHolder) holder).qaSingleLayout.setVisibility(View.VISIBLE);
                }


            }
        }
        // 回答列表
        else if (itemViewType == TYPE_ANSWER) {
            int[] ints = translateToDoubleIndex(position);
            QaInfo info = mQaInfoMapCurrent.get(mIdListCurrent.get(ints[0]));
            if (info != null) {
                if (isOnlyShowSelf) {
                    if (!info.getQuestion().getQuestionUserId().equals(DWLive.getInstance().getViewer().getId())) {
                        ((LiveQaAdapter.SubViewHolder) holder).qaSingleLayout.setVisibility(View.GONE);
                        return;
                    }
                }
                ((LiveQaAdapter.SubViewHolder) holder).qaSingleLayout.setVisibility(View.VISIBLE);
                Answer answer = info.getAnswers().get(ints[1]);
                String msg = answer.getAnswerUserName() + ": " + answer.getContent();
                ss.clear();
                ss.append(msg);
                ss.setSpan(new ForegroundColorSpan(Color.parseColor("#12ad1a")),
                        0, answer.getAnswerUserName().length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new ForegroundColorSpan(Color.parseColor("#1E1F21")),
                        answer.getAnswerUserName().length() + 1, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ((LiveQaAdapter.SubViewHolder) holder).answerText.setText(ss);


            }

        }


    }

    private int mSize = 0;

    @Override
    public int getItemCount() {
        if (mSize == 0 || isDataChange) {
            int totalSize = 0;

            for (int i = 0; i < mIdListCurrent.size(); i++) {
                QaInfo qaInfo = mQaInfoMapCurrent.get(mIdListCurrent.get(i));
                int size;
                if (qaInfo != null) {
                    ArrayList<Answer> answers = qaInfo.getAnswers();
                    size = answers.size();
                    totalSize += (size + 1);
                }
            }
            mSize = totalSize;
        }
        //Log.d("dds_test", "getItemCount:" + mSize);
        isDataChange = false;
        return mSize;
    }


    @Override
    public int getItemViewType(int position) {
        int currentPosition = -1;
        for (String qId : mIdListCurrent) {
            QaInfo info = mQaInfoMapCurrent.get(qId);
            if (info != null) {
                currentPosition = currentPosition + 1;
                if (currentPosition == position) {
                    return TYPE_QUESTION;
                }
                ArrayList<Answer> answers = info.getAnswers();
                if (answers != null && answers.size() > 0) {
                    int size = answers.size();
                    for (int i = 0; i < size; i++) {
                        currentPosition = currentPosition + 1;
                        if (position == currentPosition) {
                            return TYPE_ANSWER;
                        }
                    }
                }
            }
        }
        return super.getItemViewType(position);
    }

    private int[] translateToDoubleIndex(int adapterPosition) {
        final int[] result = new int[]{-1, -1};
        final int groupCount = mIdListCurrent.size();
        int adaptPositionCursor = 0;
        for (int groupCursor = 0; groupCursor < groupCount; groupCursor++) {
            if (adaptPositionCursor == adapterPosition) {
                result[0] = groupCursor;
                break;
            }
            QaInfo qaInfo = mQaInfoMapCurrent.get(mIdListCurrent.get(groupCursor));
            if (qaInfo != null) {
                ArrayList<Answer> answers = qaInfo.getAnswers();
                int childCount = answers.size();
                final int offset = adapterPosition - adaptPositionCursor;
                if (childCount >= offset) {
                    result[0] = groupCursor;
                    result[1] = offset - 1;
                    break;
                }
                adaptPositionCursor += childCount;

            }
            adaptPositionCursor++;
        }
        return result;
    }

    private ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#ff6633"));
    private ForegroundColorSpan foregroundColorSpan2 = new ForegroundColorSpan(Color.parseColor("#79808b"));

    // 获取问题角色对应的名字的颜色
    private ForegroundColorSpan getQuestionRoleNameColorSpan(Question question) {
        if (question.getQuestionUserId().equalsIgnoreCase(DWLive.getInstance().getViewer().getId())) {
            return foregroundColorSpan;
        }
        return foregroundColorSpan2;
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

    final class SubViewHolder extends RecyclerView.ViewHolder {

        TextView answerText;
        LinearLayout qaSingleLayout;

        public SubViewHolder(@NonNull View itemView) {
            super(itemView);
            answerText = itemView.findViewById(R.id.qa_answer);
            qaSingleLayout = itemView.findViewById(R.id.ll_qa_single_layout);
        }
    }
}
