package com.bokecc.livemodule.live.function.vote;

import android.content.Context;
import android.view.View;

import com.bokecc.livemodule.live.function.vote.view.VotePopup;

import org.json.JSONObject;

import java.util.List;

/**
 * '投票' 处理机制
 */
public class VoteHandler {

    private VotePopup mVotePopup;

    private boolean isVoteResultShow = false;

    /**
     * 初始化投票功能
     */
    public void initVote(Context context) {
        mVotePopup = new VotePopup(context);
    }

    /** 开始投票 */
    public void startVote(View root, final int voteCount, final int VoteType, VoteListener minimizeListener) {
        isVoteResultShow = false;
        mVotePopup.setMinimizeListener(minimizeListener);
        mVotePopup.startVote(voteCount, VoteType);
        mVotePopup.show(root);
    }

    /** 结束投票 */
    public void stopVote() {

    }

    /** 展示投票结果统计 */
    public void showVoteResult(View root, final JSONObject jsonObject) {
        isVoteResultShow = true;
        mVotePopup.onVoteResult(jsonObject);
        mVotePopup.show(root);
    }

    public void startVote(View rootView, int voteCount, int voteType, int selectIndex, List<Integer> selectIndexs, VoteListener minimizeListener) {
        isVoteResultShow = false;
        mVotePopup.setMinimizeListener(minimizeListener);
        mVotePopup.startVote(voteCount, voteType,selectIndex,selectIndexs);
        mVotePopup.show(rootView);
    }
}
