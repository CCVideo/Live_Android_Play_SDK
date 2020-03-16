package com.bokecc.livemodule.localplay;

import android.view.Surface;

import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.pojo.RoomInfo;
import com.bokecc.sdk.mobile.live.pojo.TemplateInfo;
import com.bokecc.sdk.mobile.live.replay.DWLiveLocalReplay;
import com.bokecc.sdk.mobile.live.replay.DWLiveLocalReplayListener;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayBroadCastMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayChatMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayPageInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQAMsg;
import com.bokecc.sdk.mobile.live.widget.DocView;

import java.util.ArrayList;
import java.util.TreeSet;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 离线回放相关逻辑核心处理机制
 */
public class DWLocalReplayCoreHandler {

    private static DWLocalReplayCoreHandler dwLocalReplayCoreHandler = new DWLocalReplayCoreHandler();

    /**
     * 获取DWLocalReplayCoreHandler单例的实例
     */
    public static DWLocalReplayCoreHandler getInstance() {
        return dwLocalReplayCoreHandler;
    }

    /**
     * 私有构造函数
     */
    private DWLocalReplayCoreHandler() {

    }


    /******************************* 各类功能模块监听相关 ***************************************/
    private DWLocalDWReplayChatListener localDWReplayChatListener;

    /**
     * 设置聊天监听
     *
     * @param listener listener
     */
    public void setLocalReplayChatListener(DWLocalDWReplayChatListener listener) {
        localDWReplayChatListener = listener;
    }

    private DWLocalDWReplayIntroListener localDWReplayIntroListener;

    /**
     * 设置直播间信息监听
     *
     * @param listener listener
     */
    public void setLocalIntroListener(DWLocalDWReplayIntroListener listener) {
        this.localDWReplayIntroListener = listener;
    }

    private DWLocalDWReplayQAListener localDWReplayQAListener;

    /**
     * 设置问答监听
     *
     * @param listener listener
     */
    public void setReplayQAListener(DWLocalDWReplayQAListener listener) {
        localDWReplayQAListener = listener;
    }

    private DWLocalReplayRoomListener localReplayRoomListener;

    /**
     * 设置本地回放信息监听
     *
     * @param listener listener
     */
    public void setLocalDwReplayRoomListener(DWLocalReplayRoomListener listener) {
        localReplayRoomListener = listener;
    }

    private LocalTemplateUpdateListener localTemplateUpdateListener;

    /**
     * 设置回放模板监听
     *
     * @param listener listener
     */
    public void setLocalTemplateUpdateListener(LocalTemplateUpdateListener listener) {
        localTemplateUpdateListener = listener;
    }

    /**
     * 回放是否有文档
     *
     * @return true:有
     */
    public boolean hasPdfView() {
        return mTemplateInfo != null && "1".equals(mTemplateInfo.getPdfView());
    }

    /**
     * 回放是否有聊天组件
     *
     * @return true:有
     */
    public boolean hasChatView() {
        return mTemplateInfo != null && "1".equals(mTemplateInfo.getChatView());
    }

    /**
     * 直播间是否有问答组件
     *
     * @return true:有
     */
    public boolean hasQaView() {
        return mTemplateInfo != null && "1".equals(mTemplateInfo.getQaView());
    }

    /******************************* 设置"播放"组件/控件相关 ***************************************/

    private DWReplayPlayer player;
    //播放内容地址
    private String playPath;
    //显示文档的控件
    private DocView docView;
    //回放模板信息
    private TemplateInfo mTemplateInfo;


    /**
     * 设置播放器
     *
     * @param player 播放器
     */
    public void setPlayer(DWReplayPlayer player) {
        this.player = player;
        setDWLocalReplayPlayParams();
    }

    /**
     * 设置播放路径
     *
     * @param path ccr文件地址
     */
    public void setPlayPath(String path) {
        this.playPath = path;
        setDWLocalReplayPlayParams();
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
        setDWLocalReplayPlayParams();
    }

    /**
     * 设置播放的参数
     */
    private void setDWLocalReplayPlayParams() {
        DWLiveLocalReplay dwLiveLocalReplay = DWLiveLocalReplay.getInstance();
        if (dwLiveLocalReplay != null) {
            dwLiveLocalReplay.setReplayParams(dwLiveLocalReplayListener, player, docView, playPath);
        }
    }


    /******************************* 视频播放相关 ***************************************/

    /**
     * 开始播放
     */
    public void start() {
        DWLiveLocalReplay dwLiveLocalReplay = DWLiveLocalReplay.getInstance();
        if (dwLiveLocalReplay != null) {
            dwLiveLocalReplay.start(null);
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        DWLiveLocalReplay dwLiveLocalReplay = DWLiveLocalReplay.getInstance();
        if (dwLiveLocalReplay != null) {
            dwLiveLocalReplay.stop();
        }
    }

    /**
     * 释放资源
     */
    public void destroy() {
        DWLiveLocalReplay dwLiveLocalReplay = DWLiveLocalReplay.getInstance();
        if (dwLiveLocalReplay != null) {
            dwLiveLocalReplay.onDestroy();
        }
    }

    /******************************* 实现 DWLiveLocalReplayListener 定义的方法 ***************************************/

    private DWLiveLocalReplayListener dwLiveLocalReplayListener = new DWLiveLocalReplayListener() {

        @Override
        public void onQuestionAnswer(TreeSet<ReplayQAMsg> qaMsgs) {
            if (localDWReplayQAListener != null) {
                localDWReplayQAListener.onQuestionAnswer(qaMsgs);
            }
        }

        @Override
        public void onChatMessage(TreeSet<ReplayChatMsg> replayChatMsgs) {
            if (localDWReplayChatListener != null) {
                localDWReplayChatListener.onChatMessage(replayChatMsgs);
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
        public void onInfo(TemplateInfo templateInfo, RoomInfo roomInfo) {
            mTemplateInfo = templateInfo;
            if (localTemplateUpdateListener != null) {
                localTemplateUpdateListener.onLocalTemplateUpdate();
            }
            if (localDWReplayIntroListener != null) {
                localDWReplayIntroListener.updateRoomInfo(roomInfo);
            }
            if (localReplayRoomListener != null) {
                localReplayRoomListener.updateRoomTitle(roomInfo.getName());
            }
        }


        @Override
        public void onInitFinished() {

        }
    };


    /**
     * 准备播放
     */
    public void replayVideoPrepared() {
        if (localReplayRoomListener != null) {
            localReplayRoomListener.videoPrepared();
            if (player != null) {
                localReplayRoomListener.showVideoDuration(player.getDuration());
            }
        }
    }

    /**
     * 播放进度
     * @param percent
     */
    public void updateBufferPercent(int percent) {
        if (localReplayRoomListener != null) {
            localReplayRoomListener.updateBufferPercent(percent);
        }
    }



    public interface LocalTemplateUpdateListener {
        /**
         * 模板信息获取监听器，当本地回放模板信息获取后
         * 通过该监听器回调给上层。
         */
        void onLocalTemplateUpdate();
    }

}
