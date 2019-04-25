package com.bokecc.livemodule.live.function.questionnaire.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.sdk.mobile.live.pojo.QuestionnaireInfo;

/*
 * 问卷选项展示控件
 * Created by renhui on 2017/8/15.
 */
public class QuestionnaireOptionView extends LinearLayout implements CompoundButton.OnCheckedChangeListener {

    private static final char DEFAULT_OPTION_DESC = 'A';

    private CheckedChangeListener mCheckedChangeListener;
    private int mPosition;
    private int mOptionIndex;
    private boolean mIsRadio;

    private Context mContext;
    private TextView mOptionDesc;
    private TextView mOptionContent;
    private RadioButton mOptionRadio;
    private CheckBox mOptionCheckbox;
    private TextView mTrueFlagView;  // 正确标识（问卷展示专用）

    public QuestionnaireOptionView(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public QuestionnaireOptionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
    }

    private void initViews() {
        LayoutInflater.from(mContext).inflate(R.layout.questionnaire_option_layout, this, true);
        mOptionDesc = (TextView) findViewById(R.id.option_desc);
        mOptionContent = (TextView) findViewById(R.id.option_content);
        mOptionRadio = (RadioButton) findViewById(R.id.option_radio);
        mOptionCheckbox = (CheckBox) findViewById(R.id.option_checkbox);
        mTrueFlagView = (TextView) findViewById(R.id.true_flag);
    }

    public void setOption(CheckedChangeListener listener, QuestionnaireInfo.Option option, boolean isRadio, final int position, final int optionIndex) {
        mCheckedChangeListener = listener;
        mPosition = position;
        mOptionIndex = optionIndex;
        mIsRadio = isRadio;
        mOptionDesc.setText(String.valueOf((char) (DEFAULT_OPTION_DESC + option.getIndex())) + "： ");
        mOptionContent.setText(option.getContent());

        if (mIsRadio) {
            mOptionRadio.setVisibility(VISIBLE);
            mOptionCheckbox.setVisibility(GONE);
            mOptionRadio.setOnCheckedChangeListener(this);
            mOptionCheckbox.setOnCheckedChangeListener(null);
        } else {
            mOptionRadio.setVisibility(GONE);
            mOptionCheckbox.setVisibility(VISIBLE);
            mOptionRadio.setOnCheckedChangeListener(null);
            mOptionCheckbox.setOnCheckedChangeListener(this);
        }

        // 点击选项内容触发修改选中状态
        mOptionContent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRadio) {
                    mOptionRadio.setChecked(!mOptionRadio.isChecked());
                } else {
                    mOptionCheckbox.setChecked(!mOptionCheckbox.isChecked());
                }
            }
        });

        // 点击选项说明触发修改选中状态
        mOptionDesc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRadio) {
                    mOptionRadio.setChecked(!mOptionRadio.isChecked());
                } else {
                    mOptionCheckbox.setChecked(!mOptionCheckbox.isChecked());
                }
            }
        });
    }

    /**
     * 展示'正确'的标识
     */
    public void showTrueFlagView() {
        if (mTrueFlagView != null) {
            mTrueFlagView.setVisibility(VISIBLE);
        }
    }

    /**
     * 禁用选项的按钮
     */
    public void disableOptionView() {
        if (mOptionRadio != null) {
            mOptionRadio.setEnabled(false);
        }
        if (mOptionCheckbox != null) {
            mOptionCheckbox.setEnabled(false);
        }
    }

    public void setCheckedStatus(boolean isChecked) {
        mOptionRadio.setOnCheckedChangeListener(null);
        mOptionCheckbox.setOnCheckedChangeListener(null);

        if (mIsRadio) {
            mOptionRadio.setChecked(isChecked);
            mOptionRadio.setOnCheckedChangeListener(this);
        } else {
            mOptionCheckbox.setChecked(isChecked);
            mOptionCheckbox.setOnCheckedChangeListener(this);
        }
    }

    /* 此界面展示的选项是否被选中了 */
    public boolean isChecked() {
        if (mIsRadio) {
            return mOptionRadio.isChecked();
        } else {
            return mOptionCheckbox.isChecked();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mCheckedChangeListener != null) {
            mCheckedChangeListener.onCheckedChanged(mPosition, mOptionIndex, isChecked);
        }
    }

    public interface CheckedChangeListener {
        void onCheckedChanged(int position, int optionIndex, boolean isChecked);
    }
}

