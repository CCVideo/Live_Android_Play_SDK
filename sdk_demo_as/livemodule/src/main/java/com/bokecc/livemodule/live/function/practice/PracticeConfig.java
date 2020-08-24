package com.bokecc.livemodule.live.function.practice;

import com.bokecc.sdk.mobile.live.pojo.PracticeInfo;

import java.util.List;

public class PracticeConfig {
    private PracticeInfo practiceInfo;
    private int VoteType;
    private int selectIndex;
    private List<Integer> selectIndexs;

    public PracticeInfo getPracticeInfo() {
        return practiceInfo;
    }

    public void setPracticeInfo(PracticeInfo practiceInfo) {
        this.practiceInfo = practiceInfo;
    }

    public int getVoteType() {
        return VoteType;
    }

    public void setVoteType(int voteType) {
        VoteType = voteType;
    }

    public int getSelectIndex() {
        return selectIndex;
    }

    public void setSelectIndex(int selectIndex) {
        this.selectIndex = selectIndex;
    }

    public List<Integer> getSelectIndexs() {
        return selectIndexs;
    }

    public void setSelectIndexs(List<Integer> selectIndexs) {
        this.selectIndexs = selectIndexs;
    }

    @Override
    public String toString() {
        return "PracticeConfig{" +
                "practiceInfo=" + practiceInfo +
                ", VoteType=" + VoteType +
                ", selectIndex=" + selectIndex +
                ", selectIndexs=" + selectIndexs +
                '}';
    }
}
