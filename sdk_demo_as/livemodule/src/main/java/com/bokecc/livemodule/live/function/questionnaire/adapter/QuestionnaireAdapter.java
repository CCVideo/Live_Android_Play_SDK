package com.bokecc.livemodule.live.function.questionnaire.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.function.questionnaire.view.QuestionnaireEditView;
import com.bokecc.livemodule.live.function.questionnaire.view.QuestionnaireOptionView;
import com.bokecc.sdk.mobile.live.pojo.QuestionnaireInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
 * 问卷调查题目适配器
 * Created by renhui on 2017/8/15.
 */
public class QuestionnaireAdapter extends RecyclerView.Adapter<QuestionnaireAdapter.QuestionnaireViewHolder> implements QuestionnaireOptionView.CheckedChangeListener {

    private final static int RADIO_TYPE = 0; // 单选题
    private final static int CHECKBOX_TYPE = 1; // 多选题
    private final static int QA_TYPE = 2;  // 问答题

    private boolean hasQAQuestionnaire; // 问卷是否有问答题
    private boolean hasChoiceQuestionnaire; // 是否有单选题

    private SparseArray<SparseArray<QuestionnaireOptionView>> mOptionViews;
    private SparseArray<QuestionnaireEditView> mEditViews;

    private SparseArray<SparseArray<Boolean>> mOptionContents;
    private SparseArray<String> mEditContents;


    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<QuestionnaireInfo.Subject> mSubject;
    private String mQuestionnaireTitle;


    public QuestionnaireAdapter(Context context, QuestionnaireInfo info) {
        mContext = context;
        mSubject = info.getSubjects();
        mQuestionnaireTitle = info.getTitle();
        mInflater = LayoutInflater.from(mContext);

        hasQAQuestionnaire = false;
        hasChoiceQuestionnaire = false;
    }

    @Override
    public QuestionnaireViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.questionnaire_item, parent, false);
        return new QuestionnaireViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(QuestionnaireViewHolder holder, int position) {
        QuestionnaireInfo.Subject subject = mSubject.get(position);
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
            if (mEditViews == null) {
                mEditViews = new SparseArray<>();
            }
            QuestionnaireEditView editView = new QuestionnaireEditView(mContext);
            editView.setPosition(position);
            editView.setEditTextChangeListener(new QuestionnaireEditView.EditTextChangeListener() {
                @Override
                public void onEditTextChanged(int position, String content) {
                    if (mEditContents == null) {
                        mEditContents = new SparseArray<>();
                    }
                    mEditContents.put(position, content);
                }
            });

            if (mEditContents != null && !TextUtils.isEmpty(mEditContents.get(position))) {
                editView.setContent(mEditContents.get(position));
            }

            mEditViews.put(position, editView);

            holder.option_container.addView(editView);
            hasQAQuestionnaire = true;
        } else {
            if (mOptionViews == null) {
                mOptionViews = new SparseArray<>();
            }
            if (mOptionContents == null) {
                mOptionContents = new SparseArray<>();
            }
            SparseArray<QuestionnaireOptionView> questionnaireOptionViews = mOptionViews.get(position);
            SparseArray<Boolean> questionnaireOptions = mOptionContents.get(position);
            for (int i = 0; i < subject.getOptions().size(); i++) {
                QuestionnaireOptionView optionView = new QuestionnaireOptionView(mContext);
                optionView.setOption(this, subject.getOptions().get(i), subject.getType() == RADIO_TYPE, position, i);
                holder.option_container.addView(optionView);
                if (questionnaireOptionViews == null) {
                    questionnaireOptionViews = new SparseArray<>();
                }
                if (questionnaireOptions == null) {
                    questionnaireOptions = new SparseArray<>();
                }
                if (questionnaireOptions.get(i) == null) {
                    questionnaireOptions.put(i, false);
                    optionView.setCheckedStatus(false);
                } else {
                    optionView.setCheckedStatus(questionnaireOptions.get(i));
                }
                mOptionContents.put(position, questionnaireOptions);
                questionnaireOptionViews.put(i, optionView);

                mOptionViews.put(position, questionnaireOptionViews);
            }
            hasChoiceQuestionnaire = true;
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

    @Override
    public void onCheckedChanged(int position, int optionIndex, boolean isChecked) {

        if (mOptionContents != null && mOptionContents.get(position) != null) {
            mOptionContents.get(position).put(optionIndex, isChecked);
        }

        // 多选不需要修改其他的选中状态，所以此处只需要修改单选的其他选项的状态
        if (mSubject.get(position).getType() == RADIO_TYPE) {
            SparseArray<QuestionnaireOptionView> views = mOptionViews.get(position);
            if (views != null && views.size() >= 0) {
                for (int i = 0; i < views.size(); i++) {
                    // 除了选中的radio，其他的都设定为未选中
                    if (optionIndex != i) {
                        mOptionViews.get(position).get(i).setCheckedStatus(false);
                    }
                }
            }
        }
    }

    /* 判断当前问卷是否完成了 */
    public boolean isQuestionnaireComplete() {
        boolean isQAComplete = true;
        if (hasQAQuestionnaire) {
            // 判断是否所有的问答题是否完成
            for (int i = 0; i < mEditViews.size(); i++) {
                if (!mEditViews.valueAt(i).isAnswered()) {
                    isQAComplete = false;
                    break;
                }
            }
        }

        boolean isChoiceComplete = true;
        if (hasChoiceQuestionnaire) {
            // 判断是否所有的选择题是否完成
            for (int i = 0; i < mOptionViews.size(); i++) {
                boolean flag = false;
                for (int j = 0; j < mOptionViews.valueAt(i).size(); j++) {
                    if (mOptionViews.valueAt(i).valueAt(j).isChecked()) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    isChoiceComplete = false;
                    break;
                }
            }
        }

        return isQAComplete && isChoiceComplete;
    }

    /* 获取问卷的答案*/
    public String getQuestionnaireAnswer() throws JSONException {

        JSONArray answerArray = new JSONArray();  // 所有问卷答案的容器

        if (hasQAQuestionnaire) {
            // 整理问答题的回答
            for (int i = 0; i < mEditViews.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("subjectId", mSubject.get(mEditViews.keyAt(i)).getId());
                jsonObject.put("answerContent", mEditViews.valueAt(i).getAnswer());
                answerArray.put(jsonObject);
            }
        }

        if (hasChoiceQuestionnaire) {
            // 整理选择题的答案
            for (int i = 0; i < mOptionViews.size(); i++) {
                if (mSubject.get(mOptionViews.keyAt(i)).getType() == RADIO_TYPE) {
                    // 单选答案
                    String checkedOptionId = "";
                    for (int j = 0; j < mOptionViews.valueAt(i).size(); j++) {
                        if (mOptionViews.valueAt(i).valueAt(j).isChecked()) {
                            checkedOptionId = mSubject.get(mOptionViews.keyAt(i)).getOptions().get(j).getId();
                            break;
                        }
                    }
                    if (!checkedOptionId.isEmpty()) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("subjectId", mSubject.get(mOptionViews.keyAt(i)).getId());
                        jsonObject.put("selectedOptionId", checkedOptionId);
                        answerArray.put(jsonObject);
                    }
                } else {
                    // 多选答案
                    ArrayList<String> checkedOptionIds = new ArrayList<>();
                    for (int j = 0; j < mOptionViews.valueAt(i).size(); j++) {
                        if (mOptionViews.valueAt(i).valueAt(j).isChecked()) {
                            checkedOptionIds.add(mSubject.get(mOptionViews.keyAt(i)).getOptions().get(j).getId());
                        }
                    }
                    if (checkedOptionIds.size() > 0) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("subjectId", mSubject.get(mOptionViews.keyAt(i)).getId());
                        jsonObject.put("selectedOptionIds", checkedOptionIds.toString().substring(1, checkedOptionIds.toString().length() - 1));
                        answerArray.put(jsonObject);
                    }
                }
            }
        }
        JSONObject answerObject = new JSONObject();
        answerObject.put("subjectsAnswer", answerArray);
        return answerObject.toString();
    }

    /**
     * 展示正确答案
     */
    public void showRight() {

        // 整理选择题的答案
        if (mOptionViews != null && mOptionViews.size() > 0) {
            for (int i = 0; i < mOptionViews.size(); i++) {
                for (int j = 0; j < mOptionViews.valueAt(i).size(); j++) {
                    mOptionViews.valueAt(i).valueAt(j).disableOptionView();
                    if (mSubject.get(mOptionViews.keyAt(i)).getOptions().get(j).getCorrect() == 1) {
                        mOptionViews.valueAt(i).valueAt(j).showTrueFlagView();
                    }
                }
            }
        }

        // 整理问答题的回答
        if (mEditViews != null && mEditViews.size() > 0) {
            for (int i = 0; i < mEditViews.size(); i++) {
                mEditViews.valueAt(i).disableEditView();
            }
        }
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

