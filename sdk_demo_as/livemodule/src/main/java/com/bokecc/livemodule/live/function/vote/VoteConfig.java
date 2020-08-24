package com.bokecc.livemodule.live.function.vote;

import org.json.JSONObject;

import java.util.List;

/**
 * 答题卡的记录bean
 */
public class VoteConfig {
    /**
     * 总共的选项个数2-5  只有在false的时候才使用
     */
    private int voteCount;
    /**
     * 0表示单选，1表示多选，目前只有单选  只有在false的时候才使用
     */
    private int VoteType;
    /**
     * 针对单选 已选的结果
     */
    private int selectIndex;

    /**
     * 针对多选 已选的结果
     */
    private List<Integer> selectIndexs;

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
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
}
