package com.bokecc.livemodule.live.function.questionnaire.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.function.questionnaire.view.QuestionnaireStatisOptionView;
import com.bokecc.sdk.mobile.live.pojo.QuestionnaireStatisInfo;

import java.util.ArrayList;

/**
 * 问卷统计题目适配器
 */
public class QuestionnaireStatisAdapter extends RecyclerView.Adapter<QuestionnaireStatisAdapter.QuestionnaireViewHolder> {

    private final static int RADIO_TYPE = 0; // 单选题
    private final static int CHECKBOX_TYPE = 1; // 多选题
    private final static int QA_TYPE = 2;  // 问答题

    private int mSubmitAnswerViewerCount; // 提交的答题总人数

    private SparseArray<SparseArray<QuestionnaireStatisOptionView>> mOptionViews;

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<QuestionnaireStatisInfo.Subject> mSubject;
    private String mQuestionnaireTitle;


    public QuestionnaireStatisAdapter(Context context, QuestionnaireStatisInfo info) {
        mContext = context;
        mSubject = info.getSubjects();
        mQuestionnaireTitle = info.getTitle();
        mSubmitAnswerViewerCount = info.getSubmitAnswerViewerCount();
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public QuestionnaireViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.questionnaire_statis_item, parent, false);
        return new QuestionnaireViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(QuestionnaireViewHolder holder, int position) {
        QuestionnaireStatisInfo.Subject subject = mSubject.get(position);
        // 题号
        holder.subject_index.setText((position + 1) + ".");
        // 题目主干
        holder.subject_content.setText(subject.getContent());
        // 题目类型
        if (subject.getType() == RADIO_TYPE) {
            holder.subject_type.setText("单选");
        } else if (subject.getType() == CHECKBOX_TYPE) {
            holder.subject_type.setText("多选");
        } else if (subject.getType() == QA_TYPE) {
            holder.subject_type.setText("问答");
        }

        if (position == 0) {
            holder.questionnaire_title.setVisibility(View.VISIBLE);
            holder.questionnaire_title.setText(mQuestionnaireTitle);
            holder.blank_layer.setVisibility(View.GONE);
        } else {
            holder.questionnaire_title.setVisibility(View.GONE);
            holder.blank_layer.setVisibility(View.VISIBLE);
        }

        holder.option_container.removeAllViews();

        if (subject.getType() == QA_TYPE) {
            // 问卷统计 -- 问答题只显示题目
        } else {
            if (mOptionViews == null) {
                mOptionViews = new SparseArray<>();
            }
            SparseArray<QuestionnaireStatisOptionView> questionnaireOptionViews = mOptionViews.get(position);
            for (int i = 0; i < subject.getOptions().size(); i++) {
                QuestionnaireStatisOptionView optionView = new QuestionnaireStatisOptionView(mContext);
                optionView.setOption(subject.getOptions().get(i), mSubmitAnswerViewerCount, position, i);
                holder.option_container.addView(optionView);
                if (questionnaireOptionViews == null) {
                    questionnaireOptionViews = new SparseArray<>();
                }
                questionnaireOptionViews.put(i, optionView);
                mOptionViews.put(position, questionnaireOptionViews);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mSubject.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mSubject.get(position).getType();
    }


    final class QuestionnaireViewHolder extends RecyclerView.ViewHolder {

        TextView subject_content;  // 题目标题

        TextView subject_index;  // 题目编号

        TextView subject_type;  // 题目类型

        LinearLayout option_container;  // 选项容器

        TextView questionnaire_title;

        View blank_layer;

        QuestionnaireViewHolder(View itemView) {
            super(itemView);
            subject_content = itemView.findViewById(R.id.subject_content);
            subject_index = itemView.findViewById(R.id.subject_index);
            subject_type = itemView.findViewById(R.id.subject_type);
            option_container = itemView.findViewById(R.id.option_container);
            questionnaire_title = itemView.findViewById(R.id.questionnaire_title);
            blank_layer = itemView.findViewById(R.id.blank_layer);
        }
    }
}
