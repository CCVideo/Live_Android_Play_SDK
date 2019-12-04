package com.bokecc.livemodule.replay;

import android.util.Log;
import android.view.Surface;

import com.bokecc.livemodule.replay.doc.DocActionListener;
import com.bokecc.livemodule.replay.doc.ReplayDocSizeChangeListener;
import com.bokecc.sdk.mobile.live.DWLiveEngine;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.Exception.ErrorCode;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplayListener;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;
import com.bokecc.sdk.mobile.live.replay.ReplayErrorListener;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayBroadCastMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayChatMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayLiveInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayPageInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQAMsg;
import com.bokecc.sdk.mobile.live.widget.DocView;

import java.util.ArrayList;
import java.util.TreeSet;

import static com.bokecc.sdk.mobile.live.Exception.ErrorCode.DOC_LOAD_FAILED;

/**
 * 回放相关逻辑核心处理机制
 */
public class DWReplayCoreHandler {

    private static final String TAG = "DWReplayCoreHandler";

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
     *
     * @param replayChatListener 回放聊天监听
     */
    public void setReplayChatListener(DWReplayChatListener replayChatListener) {
        this.replayChatListener = replayChatListener;
    }

    private ReplayDocSizeChangeListener mDocSizeListener;

    public void setDocSizeChangeListener(ReplayDocSizeChangeListener listener) {
        mDocSizeListener = listener;
    }

    private DocActionListener mDocActionListener;

    public void setDocActionListener(DocActionListener docActionListener) {
        mDocActionListener = docActionListener;
    }

    /**
     * 回放问答监听
     */
    private DWReplayQAListener replayQAListener;

    /**
     * 设置回放问答监听
     *
     * @param replayQAListener 回放问答监听
     */
    public void setReplayQAListener(DWReplayQAListener replayQAListener) {
        this.replayQAListener = replayQAListener;
    }

    // 直播间信息监听
    private DWReplayRoomListener replayRoomListener;

    /**
     * 设置直播间信息监听
     *
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
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            dwLiveReplay.setReplayPlayer(this.player);
        }
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
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            dwLiveReplay.setReplayErrorListener(mErrorListener);
            dwLiveReplay.setReplayParams(dwLiveReplayListener, DWLiveEngine.getInstance().getContext());
            dwLiveReplay.setReplayDocView(this.docView);
        }
    }

    /******************************* 直播间模版相关 ***************************************/

    private final static String ViEW_VISIBLE_TAG = "1";

    /**
     * 当前直播间是否有'文档'
     */
    public boolean hasPdfView() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null && dwLiveReplay.getTemplateInfo() != null && dwLiveReplay.getTemplateInfo().getPdfView() != null) {
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
        if (dwLiveReplay != null && dwLiveReplay.getTemplateInfo() != null && dwLiveReplay.getTemplateInfo().getPdfView() != null) {
            return ViEW_VISIBLE_TAG.equals(dwLiveReplay.getTemplateInfo().getQaView());
        }
        return false;
    }


    public ReplayLiveInfo getReplayLiveInfo() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            return dwLiveReplay.getReplayLiveInfo();
        }
        return null;
    }


    /******************************* 视频播放相关 ***************************************/


    /**
     * 开始播放
     */
    public void start(Surface surface) {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            dwLiveReplay.start(null);
        }
    }


    public void pause() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            dwLiveReplay.pause();
        }
    }

    /**
     * 释放资源
     */
    public void destroy() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            dwLiveReplay.onDestroy();
        }
        if (docView != null) {
            docView = null;
        }
    }

    /**
     * 重试播放
     * @param time:时间点，是否强制更新流地址
     */
    public void retryReplay(long time,boolean updateStream){
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            dwLiveReplay.retryReplay(time,updateStream);
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

    public void playError(int code) {
        if (replayRoomListener != null) {
            replayRoomListener.onPlayError(code);
        }
    }

    public void bufferStart() {
        if (replayRoomListener != null) {
            replayRoomListener.bufferStart();
        }
    }

    public void bufferEnd() {
        if (replayRoomListener != null) {
            replayRoomListener.bufferEnd();
        }
    }

    public void onPlayComplete() {
        if (replayRoomListener != null) {
            replayRoomListener.onPlayComplete();
        }
    }

    public void onRenderStart(){
        if(replayRoomListener != null){
            replayRoomListener.startRending();
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
        public void onPageChange(String docId, String docName, int width, int height, int pageNum, int docTotalPage) {
            Log.d(TAG, "onPageChange: pageNum:" + pageNum + " docTotalPage:" + docTotalPage + " docId=" + docId);
            Log.d(TAG, "onPageChange: pageNum:" + "w:" + width + "height:" + height);
            mDocSizeListener.updateSize(width, height);
        }

        @Override
        public void onException(DWLiveException exception) {

        }

        @Override
        public void onInitFinished() {

        }
    };


    public ReplayErrorListener mErrorListener = new ReplayErrorListener() {
        @Override
        public void onError(ErrorCode code, String msg) {
            if (code == DOC_LOAD_FAILED) {
                mDocActionListener.onDocLoadFailed();
            }
        }
    };

}
