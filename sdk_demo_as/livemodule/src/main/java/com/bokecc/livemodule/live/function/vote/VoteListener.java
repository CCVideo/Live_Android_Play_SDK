package com.bokecc.livemodule.live.function.vote;

import org.json.JSONObject;

import java.util.List;

public interface VoteListener {
    void onMinimize(int voteCount, int VoteType, int selectIndex, List<Integer> selectIndexs);
}
