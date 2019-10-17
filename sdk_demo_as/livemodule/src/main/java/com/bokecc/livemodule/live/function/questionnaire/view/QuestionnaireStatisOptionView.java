package com.bokecc.livemodule.live.function.questionnaire.view;


import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.sdk.mobile.live.pojo.QuestionnaireStatisInfo;

import java.text.NumberFormat;

/**
 * 问卷统计选项展示控件
 */
public class QuestionnaireStatisOptionView extends LinearLayout {

    private static final char DEFAULT_OPTION_DESC = 'A';

    private Context mContext;
    private TextView mOptionDesc;
    private TextView mOptionContent;
    private ProgressBar mSelectProportion;
    private TextView mSelectCount;
    private TextView mSelectPrecent;
    private TextView mTrueFlag;

    public QuestionnaireStatisOptionView(Context context) {
        super(context);
        mContext = context;
        initViews();
    }

    public QuestionnaireStatisOptionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
    }

    private void initViews() {
        LayoutInflater.from(mContext).inflate(R.layout.questionnaire_statis_option_layout, this, true);
        mOptionDesc = (TextView) findViewById(R.id.option_desc);
        mOptionContent = (TextView) findViewById(R.id.option_content);
        mSelectProportion = (ProgressBar) findViewById(R.id.select_proportion);
        mSelectCount = (TextView) findViewById(R.id.select_count);
        mSelectPrecent = (TextView) findViewById(R.id.select_precent);
        mTrueFlag = (TextView) findViewById(R.id.true_flag);
    }

    public void setOption(QuestionnaireStatisInfo.Option option, int submitAnswerViewerCount, final int position, final int optionIndex) {
        mOptionDesc.setText(String.valueOf((char) (DEFAULT_OPTION_DESC + option.getIndex())) + "： ");
        mOptionContent.setText(option.getContent());
        mSelectProportion.setProgress(option.getSelectedCount());
        mSelectProportion.setMax(submitAnswerViewerCount);
        mSelectCount.setText(option.getSelectedCount() + "人");
        mSelectPrecent.setText("(" + calculationPrecent(option.getSelectedCount(), submitAnswerViewerCount) + "%)");
        mTrueFlag.setVisibility(option.getCorrect() == 1 ? VISIBLE : INVISIBLE);
    }


    /**
     * 计算百分比
     */
    private String calculationPrecent(int selectCount, int all) {
        // 判断分母是否为0
        if (all == 0) {
            return "0";
        }
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后1位
        numberFormat.setMaximumFractionDigits(1);
        return numberFormat.format((float) selectCount / (float) all * 100);
    }

}

