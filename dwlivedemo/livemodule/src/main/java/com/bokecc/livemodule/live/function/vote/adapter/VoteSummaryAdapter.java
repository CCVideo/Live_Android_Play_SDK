package com.bokecc.livemodule.live.function.vote.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.function.vote.view.VotePopup;

import java.util.ArrayList;

public class VoteSummaryAdapter extends RecyclerView.Adapter<VoteSummaryAdapter.VoteViewHolder> {

    private Context mContext;
    private ArrayList<VotePopup.VoteSingleStatisics> voteStatisices;
    private LayoutInflater mInflater;

    public VoteSummaryAdapter(Context context) {
        voteStatisices = new ArrayList<>();
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    /**
     * 添加数据
     */
    public void add(ArrayList<VotePopup.VoteSingleStatisics> voteStatisices) {
        this.voteStatisices = voteStatisices;
        notifyDataSetChanged();
    }

    public ArrayList<VotePopup.VoteSingleStatisics> getVoteStatisices() {
        return voteStatisices;
    }

    @Override
    public VoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.qs_summary_single, parent, false);
        return new VoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(VoteViewHolder holder, int position) {
        VotePopup.VoteSingleStatisics voteSingle = voteStatisices.get(position);

        holder.mProgressBar.setMax(100);
        holder.mProgressBar.setProgress((int)(Float.parseFloat(voteSingle.getPercent())));

        if (voteStatisices.size() > 2) {
            holder.mSummaryOrder.setText(orders[voteSingle.getOption()]);
        } else {
            holder.mSummaryOrder.setText(ordersRightWrong[voteSingle.getOption()]);
        }

        String userCount = voteSingle.getCount() + "人 ";

        String percent = "(" + voteSingle.getPercent() + "%)";

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

    String[] orders = new String[]{"A：", "B：", "C：", "D：", "E："};
    String[] ordersRightWrong = new String[]{"√：", "X："};
    @Override
    public int getItemCount() {
        return voteStatisices == null ? 0 : voteStatisices.size();
    }

    final class VoteViewHolder extends RecyclerView.ViewHolder {

        TextView mSummaryOrder;

        ProgressBar mProgressBar;

        TextView mSummaryCount;

        VoteViewHolder(View itemView) {
            super(itemView);
            mSummaryOrder = itemView.findViewById(R.id.qs_summary_order);
            mProgressBar = itemView.findViewById(R.id.qs_summary_progressBar);
            mSummaryCount = itemView.findViewById(R.id.qs_summary_count);
        }
    }
}
