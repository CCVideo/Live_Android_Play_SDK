package com.bokecc.livemodule.live.function;

public abstract class FunctionCallBack {
    public boolean isSmall;
    public boolean isVote;
    /**
     * 缩小化  目前只针对答题卡和随堂测
     * @param isVote 是否是答题卡 true是答题卡  false是随堂测
     */
    public void onMinimize(boolean isVote){
        isSmall = true;
        this.isVote = isVote;
    };

    /**
     * 收到展示结果 或者老师结束随堂测/答题等情况  需要隐藏收起的小按钮
     */
    public void onClose(){
        isSmall = false;
    };
}
