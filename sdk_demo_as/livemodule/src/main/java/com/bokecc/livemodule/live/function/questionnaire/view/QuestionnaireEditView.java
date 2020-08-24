package com.bokecc.livemodule.live.function.questionnaire.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.bokecc.livemodule.R;

/**
 * 问卷问答题编辑控件
 */
public class QuestionnaireEditView extends LinearLayout {

    private Context mContext;
    private EditText mSubjectEdit;

    private EditTextChangeListener editTextChangeListener;
    private int position;

    public QuestionnaireEditView(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public QuestionnaireEditView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setEditTextChangeListener(EditTextChangeListener editTextChangeListener) {
        this.editTextChangeListener = editTextChangeListener;
    }

    public void setContent(String content) {
        this.mSubjectEdit.setText(content);
    }

    private void initViews() {
        LayoutInflater.from(mContext).inflate(R.layout.questionnaire_edit_layout, this, true);
        mSubjectEdit = (EditText) findViewById(R.id.subject_edit);

        mSubjectEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mSubjectEdit != null && editTextChangeListener != null) {
                    editTextChangeListener.onEditTextChanged(position, mSubjectEdit.getText().toString());
                }
            }
        });
    }

    /**
     * 问答题是否做答了
     */
    public boolean isAnswered() {
        return !TextUtils.isEmpty(getAnswer().trim());
    }

    /**
     * 获取问答内容
     */
    public String getAnswer() {
        return mSubjectEdit.getText().toString();
    }

    /**
     * 清除焦点
     */
    public void removeFocus() {
        mSubjectEdit.clearFocus();
    }

    /**
     * 禁用编辑框
     */
    public void disableEditView() {
        mSubjectEdit.setEnabled(false);
    }

    public interface EditTextChangeListener {

        /**
         * 输出框内容变化
         */
        void onEditTextChanged(int position, String content);
    }

}
