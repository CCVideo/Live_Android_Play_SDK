package com.bokecc.livemodule.replaymix;

import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.bokecc.sdk.mobile.live.DWLiveEngine;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.pojo.RoomInfo;
import com.bokecc.sdk.mobile.live.pojo.TemplateInfo;
import com.bokecc.sdk.mobile.live.replay.DWLiveLocalReplay;
import com.bokecc.sdk.mobile.live.replay.DWLiveLocalReplayListener;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplayListener;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplayLoginListener;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayBroadCastMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayChatMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayLoginInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayPageInfo;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQAMsg;
import com.bokecc.sdk.mobile.live.widget.DocView;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * "在线&离线混合回放" 核心处理机制（用于支持在线回放&离线回放同页面播放）
 */
public class DWReplayMixCoreHandler {

    private static final String TAG = "DWReplayMixCoreHandler";

    /**
     * 当前播放视频的类型
     */
    enum PlayType {
        LIVE/*在线回放*/, LOCAL/*本地离线回放*/
    }

    //当前播放视频的类型
    private PlayType mCurrentPlayType;

    //surface创建成功和登录完成先后顺序具有不确定性(作为同步处理)
    private volatile boolean hasLogin = false;

    private Surface surface;


    public static DWReplayMixCoreHandler mHandler = new DWReplayMixCoreHandler();

    public static DWReplayMixCoreHandler getInstance() {
        return mHandler;
    }



    /******************************* 各类功能模块监听相关 ***************************************/

    private DWReplayMixChatListener replayMixChatListener;

    /**
     * 设置回放聊天监听
     */
    public void setReplayMixChatListener(DWReplayMixChatListener chatListener) {
        replayMixChatListener = chatListener;
    }

    private DWReplayMixIntroListener dwReplayMixIntroListener;

    /**
     * 设置回放简介监听
     */
    public void setMixIntroListener(DWReplayMixIntroListener listener) {
        this.dwReplayMixIntroListener = listener;
    }

    private DWReplayMixQAListener dwReplayMixQAListener;

    /**
     * 设置回放问答监听
     */
    public void setReplayQAListener(DWReplayMixQAListener listener) {
        this.dwReplayMixQAListener = listener;
    }

    private DWReplayMixVideoListener dwReplayMixVideoListener;

    /**
     * 设置回放视频监听
     */
    public void setDwReplayMixVideoListener(DWReplayMixVideoListener dwReplayMixVideoListener) {
        this.dwReplayMixVideoListener = dwReplayMixVideoListener;
    }

    private DWReplayMixRoomListener dwReplayMixRoomListener;

    /**
     * 设置直播间状态回调监听
     */
    public void setDwReplayMixRoomListener(DWReplayMixRoomListener listener) {
        dwReplayMixRoomListener = listener;
    }

    public void replayVideoPrepared() {
        if (dwReplayMixRoomListener != null) {
            dwReplayMixRoomListener.videoPrepared();
            dwReplayMixRoomListener.showVideoDuration(ijkMediaPlayer.getDuration());
        }
    }

    public void updateBufferPercent(int percent) {
        if (dwReplayMixRoomListener != null) {
            dwReplayMixRoomListener.updateBufferPercent(percent);
        }
    }

    /******************************* 设置"播放"组件/控件相关 ***************************************/

    //播放器
    private DWReplayPlayer ijkMediaPlayer;

    //显示文档控件
    private DocView docView;


    /**
     * 设置播放器
     */
    public void setPlayer(DWReplayPlayer player) {
        this.ijkMediaPlayer = player;
    }

    /**
     * 获取播放器
     */
    public DWReplayPlayer getPlayer() {
        return this.ijkMediaPlayer;
    }

    /**
     * 设置文档展示控件
     *
     * @param docView 文档展示控件
     */
    public void setDocView(DocView docView) {
        this.docView = docView;
    }


    //******************************* SDK视频生命周期控制相关 ***************************************/

    /**
     * 若用户在界面的onCreate时做登录播放，同时surface被界面构造出来，两者的先后顺序具有不确定性
     * 若登录先完成，此时surface可用，则播放正常，若surface不可用，则播放器将会得到一个空的surface，
     * 此时播放将会出现异常，因此当播放在线时，做两次检测，当登录成功surface可用则进行播放，若surface可用
     * 则检测是否登录成功，同时满足条件则播放。
     *
     * needStartPlay: true：改回调调用时需要出发播放动作，false：不需要出发播放动作，只有当界面刚进入时
     * 该值才为true，原因见上文，其他动作引发surface发生改变时，needStartPlay始终为false
     * @param surface 渲染的surface
     * @param needStartPlay 是否需要触发播放动作，true:触发，false:不触发
     */
    public void onSurfaceAvailable(Surface surface, boolean needStartPlay) {
        this.surface = surface;
        if (needStartPlay) {
            if (mCurrentPlayType == PlayType.LIVE && hasLogin) {
                startPlayReplay();
            } else if (mCurrentPlayType == PlayType.LOCAL) {
                startPlayReplay();
            }
        }
    }

    public void pause() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            dwLiveReplay.pause();
        }
        DWLiveLocalReplay dwLiveLocalReplay = DWLiveLocalReplay.getInstance();
        if (dwLiveLocalReplay != null) {
            dwLiveLocalReplay.stop();
        }
    }

    public void destroy() {
        DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
        if (dwLiveReplay != null) {
            dwLiveReplay.onDestroy();
        }
        DWLiveLocalReplay dwLiveLocalReplay = DWLiveLocalReplay.getInstance();
        if (dwLiveLocalReplay != null) {
            dwLiveLocalReplay.onDestroy();
        }
    }

    //******************************* 切换回放和离线回放等操作相关 ***************************************/

    /**
     * 开始播放在线回放
     */
    public void startLiveReplay(ReplayLoginInfo info) {
        if(mCurrentPlayType == PlayType.LOCAL){
            DWLiveLocalReplay.getInstance().releasePlayer();
        }

        mCurrentPlayType = PlayType.LIVE;
        docView.getWebView().setVisibility(View.VISIBLE);
        docView.getImageView().setFastDoc(true);
        docView.clearDrawInfo();
        if (replayMixChatListener != null) {
            replayMixChatListener.onChatMessage(new TreeSet<ReplayChatMsg>());
        }
        // 设置登录参数
        DWLiveReplay.getInstance().setLoginParams(new DWLiveReplayLoginListener() {

            // 登录失败
            @Override
            public void onException(final DWLiveException exception) {

            }

            // 登录成功
            @Override
            public void onLogin(TemplateInfo templateInfo) {
                hasLogin = true;
                if (dwReplayMixVideoListener != null) {
                    dwReplayMixVideoListener.onPlayOtherReplayVideo();
                }
                if (dwReplayMixRoomListener != null) {
                    dwReplayMixRoomListener.onPlayOtherReplayVideo();
                }

                setPlayParam();
                startPlayReplay();
            }
        }, info);

        // 执行登录操作
        DWLiveReplay.getInstance().stop();
        DWLiveReplay.getInstance().startLogin();

    }

    /**
     * 设置在线回放播放参数
     */
    private void setPlayParam() {
        if (mCurrentPlayType == PlayType.LIVE) {
            DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
            if (dwLiveReplay != null) {
                dwLiveReplay.setReplayParams(dwLiveReplayListener, DWLiveEngine.getInstance().getContext(), ijkMediaPlayer, docView);
            }
        }
    }

    /**
     * 调用开始播放回放逻辑
     */
    private void startPlayReplay() {
        if (mCurrentPlayType == PlayType.LIVE) {
            DWLiveReplay dwLiveReplay = DWLiveReplay.getInstance();
            if (dwLiveReplay != null && surface != null) {
                dwLiveReplay.start(null);
                ijkMediaPlayer.updateSurface(surface);
            }
        } else if (mCurrentPlayType == PlayType.LOCAL) {
            final DWLiveLocalReplay liveLocalReplay = DWLiveLocalReplay.getInstance();
            if (liveLocalReplay != null && surface != null) {
                liveLocalReplay.start(surface);
            }
        }
    }

    /**
     * 开始播放离线回放
     */
    public void startLocalReplay(String localPath) {
        if (replayMixChatListener != null) {
            replayMixChatListener.onChatMessage(new TreeSet<ReplayChatMsg>());
        }

        if (mCurrentPlayType == PlayType.LIVE) {
            docView.getImageView().setFastDoc(false);
            docView.getWebView().setVisibility(View.GONE);
            docView.clearDrawInfo();
            final DWLiveReplay liveReplay = DWLiveReplay.getInstance();
            liveReplay.stop();
        }

        final DWLiveLocalReplay liveLocalReplay = DWLiveLocalReplay.getInstance();
        if(mCurrentPlayType == PlayType.LOCAL){
            liveLocalReplay.releasePlayer();
        }
        mCurrentPlayType = PlayType.LOCAL;
        if (dwReplayMixVideoListener != null) {
            dwReplayMixVideoListener.onPlayOtherReplayVideo();
        }

        if (dwReplayMixRoomListener != null) {
            dwReplayMixRoomListener.onPlayOtherReplayVideo();
        }


        liveLocalReplay.setReplayParams(localReplayListener, ijkMediaPlayer, docView, localPath);
        startPlayReplay();
    }


    //******************************* 在线回放和离线回放 功能 监听器 ***************************************/


    /**
     * 在线回放内部监听器
     */
    public DWLiveReplayListener dwLiveReplayListener = new DWLiveReplayListener() {

        /**
         * 提问回答信息
         *
         * @param qaMsgs 问答信息
         */
        @Override
        public void onQuestionAnswer(TreeSet<ReplayQAMsg> qaMsgs) {
            if (dwReplayMixQAListener != null) {
                dwReplayMixQAListener.onQuestionAnswer(qaMsgs);
            }
        }

        /**
         * 聊天信息
         *
         * @param replayChatMsgs 聊天信息
         */
        @Override
        public void onChatMessage(TreeSet<ReplayChatMsg> replayChatMsgs) {
            Log.d(TAG, "dwLiveReplayListener onChatMessage: ");
            if (replayMixChatListener != null) {
                replayMixChatListener.onChatMessage(replayChatMsgs);
            }
        }

        @Override
        public void onBroadCastMessage(ArrayList<ReplayBroadCastMsg> broadCastMsgList) {

        }

        @Override
        public void onPageInfoList(ArrayList<ReplayPageInfo> infoList) {

        }

        @Override
        public void onPageChange(String docId, String docName, int width ,int height,int pageNum, int docTotalPage) {

        }

        @Override
        public void onException(DWLiveException exception) {

        }

        @Override
        public void onInitFinished() {
            if (dwReplayMixRoomListener != null) {
                dwReplayMixRoomListener.updateRoomTitle(DWLiveReplay.getInstance().getRoomInfo().getName());
            }
            if (dwReplayMixIntroListener != null) {
                dwReplayMixIntroListener.updateRoomInfo(DWLiveReplay.getInstance().getRoomInfo());
            }
        }
    };


    /**
     * 本地回放内部监听器
     */
    private DWLiveLocalReplayListener localReplayListener = new DWLiveLocalReplayListener() {
        @Override
        public void onQuestionAnswer(TreeSet<ReplayQAMsg> qaMsgs) {
            if (dwReplayMixQAListener != null) {
                dwReplayMixQAListener.onQuestionAnswer(qaMsgs);
            }
        }

        @Override
        public void onChatMessage(TreeSet<ReplayChatMsg> replayChatMsgs) {
            Log.d(TAG, "localReplayListener onChatMessage: ");
            if (replayMixChatListener != null) {
                replayMixChatListener.onChatMessage(replayChatMsgs);
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
            if (dwReplayMixRoomListener != null) {
                dwReplayMixRoomListener.updateRoomTitle(roomInfo.getName());
            }
            if (dwReplayMixIntroListener != null) {
                dwReplayMixIntroListener.updateRoomInfo(roomInfo);
            }
        }

        @Override
        public void onInitFinished() {

        }
    };

}
