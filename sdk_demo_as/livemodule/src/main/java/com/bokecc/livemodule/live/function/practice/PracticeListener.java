package com.bokecc.livemodule.live.function.practice;

import com.bokecc.sdk.mobile.live.pojo.PracticeInfo;

import java.util.List;

public interface PracticeListener {
    void onMinimize(PracticeInfo practiceInfo, int VoteType, int selectIndex, List<Integer> selectIndexs);
}
