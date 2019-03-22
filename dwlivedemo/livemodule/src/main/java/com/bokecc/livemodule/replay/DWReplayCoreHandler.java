package com.bokecc.livemodule.replay;

import android.view.Surface;

import com.bokecc.sdk.mobile.live.DWLiveEngine;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplayListener;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayBroadCastMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayChatMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayPageInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQAMsg;
import com.bokecc.sdk.mobile.live.widget.DocView;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * 回放相关逻辑核心处理机制
 */
public class DWReplayCoreHandler {

    private static DWReplayCoreHandler dwReplayCoreHandler = new DWReplayCoreHandler();

    /**
     * 获取DWReplayCoreHandler单例的实例
     */
    public static DWReplayCoreHandler getInstance() {
        return dwReplayCoreHandler;
    }

    /**
     * 私有构造函数
     */
    private DWReplayCoreHandler() {

    }

    /******************************* 各类监听相关 ***************************************/

    /**
     * 回放聊天监听
     */
    private DWReplayChatListener replayChatListener;

    /**
     * 设置回放聊天监听
     * @param replayChatListener 回放聊天监听
     */
    public void setReplayChatListener(DWReplayChatListener replayChatListener) {
        this.replayChatListener = replayChatListener;
    }

    /**
     * 回放问答监听
     */
    private DWReplayQAListener replayQAListener;

    /**
     * 设置回放问答监听
     * @param replayQAListener 回放问答监听
     */
    public void setReplayQAListener(DWReplayQAListener replayQAListener) {
        this.replayQAListener = replayQAListener;
    }

    // 直播间信息监听
    private DWReplayRoomListener replayRoomListener;

    /**
     * 设置直播间信息监听
     * @param listener 直播间信息监听
     */
    public void setReplayRoomListener(DWReplayRoomListener listener) {
        this.replayRoomListener = listener;
    }

    /******************************* 设置"播放"组件/控件相关 ***************************************/

    private DWReplayPlayer player;

    private DocView docView;

    /**
     * 设置播放器
     *
     * @param player 播放器
     */
    public void setPlayer(DWReplayPlayer player) {
        this.player = player;
        setDWLivePlayParams();
    }

    /***
     * 获取当前的播放器
     */
    public DWReplayPlayer getPlayer() {
        return this.player;
    }
    /**
     * 设置文档展示控件
     *
     * @param docView 文档展示控件
     */
    public void setDocView(DocView docView) {
        this.docView = docView;
        setDWLivePlayParams();
    }

    /**
     * 设置播放的参数
     */
    private void setDWLivePlayParams() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            dwLiveReplay.setReplayParams(dwLiveReplayListener, DWLiveEngine.getInstance().getContext(), player, docView);
        }
    }

    /******************************* 直播间模版相关 ***************************************/

    private final static String ViEW_VISIBLE_TAG = "1";

    /**
     * 当前直播间是否有'文档'
     */
    public boolean hasPdfView() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null &&  dwLiveReplay.getTemplateInfo() != null && dwLiveReplay.getTemplateInfo().getPdfView() != null) {
            return ViEW_VISIBLE_TAG.equals(dwLiveReplay.getTemplateInfo().getPdfView());
        }
        return false;
    }

    /**
     * 当前直播间是否有'聊天'
     */
    public boolean hasChatView() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null && dwLiveReplay.getTemplateInfo() != null && dwLiveReplay.getTemplateInfo().getPdfView() != null) {
            return ViEW_VISIBLE_TAG.equals(dwLiveReplay.getTemplateInfo().getChatView());
        }
        return false;
    }

    /**
     * 当前直播间是否有'问答'
     */
    public boolean hasQaView() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null && dwLiveReplay.getTemplateInfo() != null &&dwLiveReplay.getTemplateInfo().getPdfView() != null) {
            return ViEW_VISIBLE_TAG.equals(dwLiveReplay.getTemplateInfo().getQaView());
        }
        return false;
    }

    /******************************* 视频播放相关 ***************************************/

    private Surface surface;

    /**
     * 开始播放
     */
    public void start(Surface surface) {
        this.surface = surface;
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            dwLiveReplay.start(surface);
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            dwLiveReplay.stop();
        }
    }

    /**
     * 释放资源
     */
    public void destory() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            dwLiveReplay.onDestroy();
        }
    }

    /**
     * 更新当前缓冲进度
     */
    public void updateBufferPercent(int percent) {
        if (replayRoomListener != null) {
            replayRoomListener.updateBufferPercent(percent);
        }
    }

    /**
     * 回放视频准备好了
     */
    public void replayVideoPrepared() {
        replayRoomListener.showVideoDuration(player.getDuration());
        replayRoomListener.videoPrepared();
    }

    /******************************* 实现 DWLiveListener 定义的方法 ***************************************/

    private DWLiveReplayListener dwLiveReplayListener = new DWLiveReplayListener() {

        /**
         * 提问回答信息
         *
         * @param qaMsgs 问答信息
         */
        @Override
        public void onQuestionAnswer(TreeSet<ReplayQAMsg> qaMsgs) {
            if (replayQAListener != null) {
                replayQAListener.onQuestionAnswer(qaMsgs);
            }
        }

        /**
         * 聊天信息
         *
         * @param replayChatMsgs 聊天信息
         */
        @Override
        public void onChatMessage(TreeSet<ReplayChatMsg> replayChatMsgs) {
            if (replayChatListener != null) {
                replayChatListener.onChatMessage(replayChatMsgs);
            }
        }

        @Override
        public void onBroadCastMessage(ArrayList<ReplayBroadCastMsg> broadCastMsgList) {

        }

        @Override
        public void onPageInfoList(ArrayList<ReplayPageInfo> infoList) {

        }

        @Override
        public void onPageChange(String docId, String docName, int pageNum, int docTotalPage) {

        }

        @Override
        public void onException(DWLiveException exception) {

        }

        @Override
        public void onInitFinished() {

        }
    };
}
