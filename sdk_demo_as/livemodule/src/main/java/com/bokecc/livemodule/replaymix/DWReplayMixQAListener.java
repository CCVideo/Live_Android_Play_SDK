package com.bokecc.livemodule.replaymix;


import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQAMsg;

import java.util.TreeSet;

/**
 * 回放问答回调监听
 */
public interface DWReplayMixQAListener {

    /**
     * 提问回答信息
     *
     * @param qaMsgs 问答信息
     */
    void onQuestionAnswer(TreeSet<ReplayQAMsg> qaMsgs);

}
