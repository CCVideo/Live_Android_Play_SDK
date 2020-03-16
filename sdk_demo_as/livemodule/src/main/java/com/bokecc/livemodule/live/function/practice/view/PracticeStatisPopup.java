package com.bokecc.livemodule.live.function.practice.view;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.live.function.practice.adapter.PracticeStatisAdapter;
import com.bokecc.livemodule.utils.PopupAnimUtil;
import com.bokecc.livemodule.view.BasePopupWindow;
import com.bokecc.sdk.mobile.live.pojo.PracticeStatisInfo;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * 随堂测统计弹出界面
 */
public class PracticeStatisPopup extends BasePopupWindow {

    String[] orders = new String[]{"A", "B", "C", "D", "E", "F"};

    private ImageView qsClose;

    private TextView mPracticeOverDesc;
    private TextView mPracticeingDesc;

    private TextView mPracticePeopleNum;
    private TextView mPracticeAnswerDesc;

    private RecyclerView mStatisList;

    private PracticeStatisAdapter mStatisAdapter;

    // 构造函数
    public PracticeStatisPopup(Context context) {
        super(context);
    }

    @Override
    protected void onViewCreated() {

        qsClose = findViewById(R.id.qs_close);
        qsClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        mPracticeOverDesc = findViewById(R.id.practiceing_over_desc);
        mPracticeingDesc = findViewById(R.id.practiceing_desc);
        mPracticePeopleNum = findViewById(R.id.practice_people_num);
        mPracticeAnswerDesc = findViewById(R.id.practice_answer_desc);
        mStatisList = findViewById(R.id.statis_list);

        mStatisList.setLayoutManager(new LinearLayoutManager(mContext));
        mStatisAdapter = new PracticeStatisAdapter(mContext);
        mStatisList.setAdapter(mStatisAdapter);
    }

    /**
     * 展示随堂测统计信息
     */
    public void showPracticeStatis(final PracticeStatisInfo info) {
        if (info == null) {
            return;
        }

        if (info.getStatus() == 1) {
            mPracticeingDesc.setVisibility(View.VISIBLE);
            mPracticeOverDesc.setVisibility(View.GONE);
        } else if (info.getStatus() == 2) {
            mPracticeOverDesc.setVisibility(View.VISIBLE);
            mPracticeingDesc.setVisibility(View.GONE);
        }

        mStatisAdapter.setAllPracticeNumber(info.getAnswerPersonNum());
        mPracticePeopleNum.setText("共" + info.getAnswerPersonNum() + "人回答，正确率" + calculationPrecent(info.getCorrectPersonNum(), info.getAnswerPersonNum()) + "%");

        ArrayList<Integer> practiceHistoryResult = DWLiveCoreHandler.getInstance().getPracticeResult(info.getId());

        StringBuilder yourChoose = new StringBuilder();
        StringBuilder corrects = new StringBuilder();

        for (int i = 0; i < info.getOptionStatis().size(); i++) {
            if (info.getOptionStatis().get(i).isCorrect()) {
                corrects.append(orders[i]);
            }
        }

        if (practiceHistoryResult == null) {
            mPracticeAnswerDesc.setVisibility(View.GONE);
        } else {
            for (int i = 0; i < practiceHistoryResult.size(); i++) {
                yourChoose.append(orders[practiceHistoryResult.get(i)]);
            }
            mPracticeAnswerDesc.setVisibility(View.VISIBLE);
        }

        String msg = "您的答案：" + yourChoose.toString() + "     正确答案：" + corrects.toString();

        SpannableString ss = new SpannableString(msg);

        ss.setSpan(new ForegroundColorSpan(Color.parseColor(getMyAnswerColor(yourChoose.toString().equals(corrects.toString())))),
                5,
                5 + yourChoose.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ss.setSpan(new ForegroundColorSpan(Color.parseColor("#12b88f")),
                5 + yourChoose.length() + 10,
                5 + yourChoose.length() + 10 + corrects.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);


        mPracticeAnswerDesc.setText(ss);

        mStatisAdapter.add(info.getOptionStatis());
    }

    // 展示随堂测停止的UI
    public void showPracticeStop() {
        mPracticeOverDesc.setVisibility(View.VISIBLE);
        mPracticeingDesc.setVisibility(View.GONE);
    }

    String wrongTextColor = "#fc512b";
    String rightTextColor = "#12b88f";

    private String getMyAnswerColor(boolean isRight) {
        if (isRight) {
            return rightTextColor;
        } else {
            return wrongTextColor;
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.practice_statis;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefScaleEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefScaleExitAnim();
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