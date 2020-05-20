package com.bokecc.livemodule.live.function.practice.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.sdk.mobile.live.pojo.PracticeStatisInfo;

import java.util.ArrayList;

/**
 * 随堂测统计柱状图列表适配器
 */
public class PracticeStatisAdapter extends RecyclerView.Adapter<PracticeStatisAdapter.ResultViewHolder> {

    private Context mContext;
    private ArrayList<PracticeStatisInfo.OptionStatis> practiceStatisices;
    private LayoutInflater mInflater;

    private int practiceNumber;
    private int type;

    public PracticeStatisAdapter(Context context) {
        practiceStatisices = new ArrayList<>();
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    public void setAllPracticeNumber(int number) {
        practiceNumber = number;
    }

    /**
     * 添加数据
     */
    public void add(ArrayList<PracticeStatisInfo.OptionStatis> practiceStatisices,int type) {
        this.practiceStatisices = practiceStatisices;
        this.type=type;
        notifyDataSetChanged();
    }

    @Override
    public PracticeStatisAdapter.ResultViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.practice_summary_single, parent, false);
        return new PracticeStatisAdapter.ResultViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PracticeStatisAdapter.ResultViewHolder holder, int position) {

        PracticeStatisInfo.OptionStatis optionSingle = practiceStatisices.get(position);

        holder.mWrongProgressBar.setMax(practiceNumber);
        holder.mRightProgressBar.setMax(practiceNumber);

        if (optionSingle.isCorrect()) {
            holder.mWrongProgressBar.setVisibility(View.GONE);
            holder.mRightProgressBar.setVisibility(View.VISIBLE);
            holder.mRightProgressBar.setProgress(optionSingle.getCount());
        } else {
            holder.mWrongProgressBar.setVisibility(View.VISIBLE);
            holder.mRightProgressBar.setVisibility(View.GONE);
            holder.mWrongProgressBar.setProgress(optionSingle.getCount());
        }
        if (type==0){
            holder.mSummaryOrder.setText(orders2[optionSingle.getIndex()]);
        }else{
            holder.mSummaryOrder.setText(orders[optionSingle.getIndex()]);
        }

        String userCount = optionSingle.getCount() + "人 ";
        String percentText = "";
        if (optionSingle.getPercent().contains(".")){
            String[] split = optionSingle.getPercent().split("\\.");
            if (split.length>=2&&split[1].contains("0")){
                String replace = split[1].replace("0", "");
                percentText = split[0]+replace;
            }else{
                percentText = optionSingle.getPercent();
            }
        }else{
            percentText = optionSingle.getPercent();
        }
        String percent = "(" + percentText + ")";

        String msg = userCount + percent;

        SpannableString ss = new SpannableString(msg);

        ss.setSpan(new ForegroundColorSpan(Color.parseColor("#666666")),
                0,
                userCount.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ss.setSpan(new ForegroundColorSpan(Color.parseColor("#333333")),
                userCount.length(),
                msg.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        holder.mSummaryCount.setText(ss);
    }

    String[] orders = new String[]{"A：", "B：", "C：", "D：", "E：", "F："};
    String[] orders2 = new String[]{"√：","×："};
    @Override
    public int getItemCount() {
        return practiceStatisices == null ? 0 : practiceStatisices.size();
    }

    final class ResultViewHolder extends RecyclerView.ViewHolder {

        TextView mSummaryOrder;

        ProgressBar mRightProgressBar;
        ProgressBar mWrongProgressBar;

        TextView mSummaryCount;

        ResultViewHolder(View itemView) {
            super(itemView);
            mSummaryOrder = itemView.findViewById(R.id.qs_summary_order);
            mRightProgressBar = itemView.findViewById(R.id.right_summary_progressBar);
            mWrongProgressBar = itemView.findViewById(R.id.wrong_summary_progressBar);
            mSummaryCount = itemView.findViewById(R.id.qs_summary_count);
        }
    }
}