package com.bokecc.livemodule.live;

import android.util.Log;
import android.view.Surface;

import com.bokecc.livemodule.live.chat.module.ChatEntity;
import com.bokecc.livemodule.live.doc.LiveDocSizeChangeListener;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.DWLiveEngine;
import com.bokecc.sdk.mobile.live.DWLiveListener;
import com.bokecc.sdk.mobile.live.DWLivePlayer;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.Exception.ErrorCode;
import com.bokecc.sdk.mobile.live.pojo.Answer;
import com.bokecc.sdk.mobile.live.pojo.BroadCastMsg;
import com.bokecc.sdk.mobile.live.pojo.ChatMessage;
import com.bokecc.sdk.mobile.live.pojo.LiveInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeRankInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeStatisInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeSubmitResultInfo;
import com.bokecc.sdk.mobile.live.pojo.PrivateChatInfo;
import com.bokecc.sdk.mobile.live.pojo.QualityInfo;
import com.bokecc.sdk.mobile.live.pojo.Question;
import com.bokecc.sdk.mobile.live.pojo.QuestionnaireInfo;
import com.bokecc.sdk.mobile.live.pojo.QuestionnaireStatisInfo;
import com.bokecc.sdk.mobile.live.pojo.SettingInfo;
import com.bokecc.sdk.mobile.live.pojo.TeacherInfo;
import com.bokecc.sdk.mobile.live.rtc.CCRTCRender;
import com.bokecc.sdk.mobile.live.rtc.RtcClient;
import com.bokecc.sdk.mobile.live.widget.DocView;

import org.json.JSONObject;
import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 直播相关逻辑核心处理机制
 */
public class DWLiveCoreHandler {

    private static DWLiveCoreHandler dwLiveCoreHandler = new DWLiveCoreHandler();

    /**
     * 获取DWLiveCoreHandler单例的实例
     *
     * @return dwLiveCoreHandler
     */
    public static DWLiveCoreHandler getInstance() {
        return dwLiveCoreHandler;
    }

    /**
     * 私有构造函数
     */
    private DWLiveCoreHandler() {

    }

    /******************************* 各类功能模块监听相关 ***************************************/

    private DWLiveQAListener dwLiveQAListener;

    /**
     * 设置问答监听
     */
    public void setDwLiveQAListener(DWLiveQAListener listener) {
        dwLiveQAListener = listener;
    }

    private DWLiveChatListener dwLiveChatListener;

    /**
     * 设置聊天监听
     */
    public void setDwLiveChatListener(DWLiveChatListener listener) {
        dwLiveChatListener = listener;
    }

    private DWLiveRoomListener dwLiveRoomListener;

    /**
     * 设置直播间信息监听
     */
    public void setDwLiveRoomListener(DWLiveRoomListener dwLiveRoomListener) {
        this.dwLiveRoomListener = dwLiveRoomListener;
    }

    private DWLiveVideoListener dwLiveVideoListener;

    /**
     * 设置直播视频监听
     */
    public void setDwLiveVideoListener(DWLiveVideoListener dwLiveVideoListener) {
        this.dwLiveVideoListener = dwLiveVideoListener;
    }

    private DWLiveBarrageListener dwLiveBarrageListener;

    /**
     * 设置直播弹幕监听
     */
    public void setDwLiveBarrageListener(DWLiveBarrageListener dwLiveBarrageListener) {
        this.dwLiveBarrageListener = dwLiveBarrageListener;
    }

    private DWLiveFunctionListener dwLiveFunctionListener;

    /**
     * 设置直播功能监听（签到、答题卡/投票、问卷、抽奖）
     */
    public void setDwLiveFunctionListener(DWLiveFunctionListener dwLiveFunctionListener) {
        this.dwLiveFunctionListener = dwLiveFunctionListener;
    }

    private DWLiveMoreFunctionListener dwLiveMoreFunctionListener;

    /**
     * 设置直播更多功能监听（公告、连麦、私聊）
     */
    public void setDwLiveMoreFunctionListener(DWLiveMoreFunctionListener dwLiveMoreFunctionListener) {
        this.dwLiveMoreFunctionListener = dwLiveMoreFunctionListener;
    }

    private DWLiveRTCListener dwLiveRTCListener;

    /**
     * 设置直播连麦监听 -- 用于视频和连麦画面展示
     */
    public void setDwLiveRTCListener(DWLiveRTCListener dwLiveRTCListener) {
        this.dwLiveRTCListener = dwLiveRTCListener;
    }

    private DWLiveRTCStatusListener dwLiveRTCStatusListener;

    /**
     * 设置直播连麦状态监听 -- 用于连麦控制控件展示
     */
    public void setDwLiveRTCStatusListener(DWLiveRTCStatusListener dwLiveRTCStatusListener) {
        this.dwLiveRTCStatusListener = dwLiveRTCStatusListener;
    }

    /******************************* 设置"播放"组件/控件相关 ***************************************/

    private LiveDocSizeChangeListener mDocSizeListener;
    public void setDocSizeChangeListener(LiveDocSizeChangeListener listener) {
        mDocSizeListener = listener;
    }

    private DWLivePlayer dwLivePlayer;

    private DocView docView;

    /**
     * 设置播放器
     *
     * @param player 播放器
     */
    public void setPlayer(DWLivePlayer player) {
        this.dwLivePlayer = player;
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            dwLive.setDWLivePlayer(dwLivePlayer);
        }
    }

    /**
     * 设置文档展示控件
     *
     * @param docView 文档展示控件
     */
    public void setDocView(DocView docView) {
        this.docView = docView;
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            dwLive.setDWLivePlayDocView(this.docView);
        }
    }

    /******************************* 直播间模版状态相关 ***************************************/

    private final static String ViEW_VISIBLE_TAG = "1";
    private final static String ONLY_VIDEO_TEMPLATE_TYPE = "1";

    /**
     * 当前直播间是否有'文档'
     */
    public boolean hasPdfView() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null && dwLive.getTemplateInfo() != null) {
            return ViEW_VISIBLE_TAG.equals(dwLive.getTemplateInfo().getPdfView());
        }
        return false;
    }

    /**
     * 当前直播间是否有'聊天'
     */
    public boolean hasChatView() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null && dwLive.getTemplateInfo() != null) {
            return ViEW_VISIBLE_TAG.equals(dwLive.getTemplateInfo().getChatView());
        }
        return false;
    }

    /**
     * 当前直播间是否有'问答'
     */
    public boolean hasQaView() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null && dwLive.getTemplateInfo() != null) {
            return ViEW_VISIBLE_TAG.equals(dwLive.getTemplateInfo().getQaView());
        }
        return false;
    }

    /**
     * 当前模版是否只有视频(大屏模式-->视频)
     * <p>
     * 注：<大屏模式-->视频>的TemplateInfo.type == 1
     *
     * @return true 为是，false为否
     */
    public boolean isOnlyVideoTemplate() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null && dwLive.getTemplateInfo() != null) {
            return ONLY_VIDEO_TEMPLATE_TYPE.equals(dwLive.getTemplateInfo().getType());
        }
        return false;
    }

    /**
     * 设置弹幕状态
     *
     * @param isBarrageOn true 开启，false 关闭
     */
    public void setBarrageStatus(boolean isBarrageOn) {
        if (dwLiveBarrageListener != null) {
            if (isBarrageOn) {
                dwLiveBarrageListener.onBarrageOn();
            } else {
                dwLiveBarrageListener.onBarrageOff();
            }
        }
    }

    //******************************* 直播SDK视频生命周期控制相关 ***************************************/

    /**
     * 开始播放
     */
    public void start(Surface surface) {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            // 设置播放参数 （回调 及 上下文）
            dwLive.setDWLivePlayParams(dwLiveListener, DWLiveEngine.getInstance().getContext());
            // 调用开始播放逻辑
            dwLive.start(null);
            DWLive.getInstance().getPracticeStatis("");
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            if (dwLivePlayer != null && dwLivePlayer.isPlaying()) {
                dwLivePlayer.pause();
            }
            dwLive.stop();
        }
    }

    /**
     * 释放资源
     */
    public void destroy() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            dwLive.onDestroy();
        }
    }

    //----------------------------------- 直播SDK(DWLive)提供的的主动调用方法 -----------------------/


    /**
     * 获取直播信息
     *
     * @return 直播信息 其中：LiveInfo.liveStartTime 为直播开始时间，LiveInfo.liveDuration "直播持续时间，单位（s），直播未开始返回-1"
     */
    public LiveInfo getLiveInfo() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            return dwLive.getLiveInfo();
        }
        return null;
    }

    /**
     * 发送签到信息
     */
    public void sendRollCall() {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            dwLive.sendRollCall();
        }
    }

    private Map<String, ArrayList<Integer>> practiceResultIndexs;  // 随堂测答题结果列表

    /**
     * 发送随堂测答题结果
     *
     * @param practiceId  随堂测ID
     * @param answerOptions  随堂测回答的结果信息
     */
    public void sendPracticeAnswer(String practiceId, ArrayList<String> answerOptions) {
        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            dwLive.sendPracticeAnswer(practiceId, answerOptions);
        }
    }

    // 缓存随堂测答题记录
    public void cachePracticeResult(String practiceId, ArrayList<Integer> resultIndexs) {
        if (practiceResultIndexs == null) {
            practiceResultIndexs = new HashMap<>();
        }
        practiceResultIndexs.put(practiceId, resultIndexs);
    }

    /**
     * 获取随堂测答题记录（根据随堂测ID）
     *
     * @param practiceId 随堂测ID
     */
    public ArrayList<Integer> getPracticeResult(String practiceId) {
        if (practiceResultIndexs == null) {
            return null;
        }
        return practiceResultIndexs.get(practiceId);
    }

    //--------------------------------- 模块间UI事件处理方法(中转事件) -----------------------------------/

    /**
     * 跳转到私聊列表页
     *
     * @param chatEntity 要跳转到的私聊的内容指引
     */
    public void jump2PrivateChat(ChatEntity chatEntity) {
        if (dwLiveMoreFunctionListener != null) {
            dwLiveMoreFunctionListener.jump2PrivateChat(chatEntity);
        }
    }

    /******************************* 实现直播相关功能事件监听 ***************************************/

    private DWLiveListener dwLiveListener = new DWLiveListener() {

        /**
         * 提问
         *
         * @param question 问题信息
         */
        @Override
        public void onQuestion(Question question) {
            if (dwLiveQAListener != null) {
                dwLiveQAListener.onQuestion(question);
            }
        }

        /**
         * 收到客户端发布的问题的编号
         *
         * @param questionId 问题id
         */
        @Override
        public void onPublishQuestion(String questionId) {
            if (dwLiveQAListener != null) {
                dwLiveQAListener.onPublishQuestion(questionId);
            }
        }

        /**
         * 回答
         *
         * @param answer 回答信息
         */
        @Override
        public void onAnswer(Answer answer) {
            if (dwLiveQAListener != null) {
                dwLiveQAListener.onAnswer(answer);
            }
        }

        /**
         * 播放状态
         *
         * @param status 包括PLAYING, PREPARING共2种状态
         */
        @Override
        public void onLiveStatus(DWLive.PlayStatus status) {
            if (dwLiveVideoListener != null) {
                dwLiveVideoListener.onLiveStatus(status);
            }
        }

        /**
         * 流结束
         *
         * @param isNormal 流是否正常结束
         */
        @Override
        public void onStreamEnd(boolean isNormal) {
            if (dwLiveVideoListener != null) {
                dwLiveVideoListener.onStreamEnd(isNormal);
            }
        }

        /**
         * 收到直播历史公聊
         *
         * @param chatLogs 补推的历史聊天信息
         */
        @Override
        public void onHistoryChatMessage(ArrayList<ChatMessage> chatLogs) {
            if (dwLiveChatListener != null) {
                dwLiveChatListener.onHistoryChatMessage(chatLogs);
            }
        }

        /**
         * 公共聊天
         *
         * @param msg 聊天信息
         */
        @Override
        public void onPublicChatMessage(ChatMessage msg) {
            if (dwLiveChatListener != null) {
                dwLiveChatListener.onPublicChatMessage(msg);
            }
        }

        /**
         * 收到聊天信息状态管理事件
         *
         * @param msgStatusJson 聊天信息状态管理事件json
         */
        @Override
        public void onChatMessageStatus(String msgStatusJson) {
            if (dwLiveChatListener != null) {
                dwLiveChatListener.onChatMessageStatus(msgStatusJson);
            }
        }

        /**
         * 禁言消息，该消息是单个用户被禁言情况下发送消息的回调
         *
         * @param msg 聊天信息
         */
        @Override
        public void onSilenceUserChatMessage(ChatMessage msg) {
            if (dwLiveChatListener != null) {
                dwLiveChatListener.onSilenceUserChatMessage(msg);
            }
        }

        /**
         * 收到禁言事件
         *
         * @param mode 禁言类型 1：个人禁言  2：全员禁言
         */
        @Override
        public void onBanChat(int mode) {
            if (dwLiveChatListener != null) {
                dwLiveChatListener.onBanChat(mode);
            }
        }

        /**
         * 收到解除禁言事件
         *
         * @param mode 禁言类型 1：个人禁言  2：全员禁言
         */
        @Override
        public void onUnBanChat(int mode) {
            if (dwLiveChatListener != null) {
                dwLiveChatListener.onUnBanChat(mode);
            }
        }

        /**
         * 别人私聊我
         *
         * @param info
         */
        @Override
        public void onPrivateChat(PrivateChatInfo info) {
            if (dwLiveMoreFunctionListener != null) {
                dwLiveMoreFunctionListener.onPrivateChat(info);
            }
        }

        /**
         * 我发出的私聊
         *
         * @param info
         */
        @Override
        public void onPrivateChatSelf(PrivateChatInfo info) {
            if (dwLiveMoreFunctionListener != null) {
                dwLiveMoreFunctionListener.onPrivateChatSelf(info);
            }
        }

        /**
         * 在线人数<br/>
         * 刷新频率：15秒
         *
         * @param count 人数统计
         */
        @Override
        public void onUserCountMessage(int count) {
            if (dwLiveRoomListener != null) {
                dwLiveRoomListener.showRoomUserNum(count);
            }
        }

        @Override
        public void onOnlineTeachers(List<TeacherInfo> infoList) {

        }

        /**
         * 回调当前翻页的信息<br/>
         * 注意：<br/>
         * 白板docTotalPage一直为0，pageNum从1开始<br/>
         * 其他文档docTotalPage为正常页数，pageNum从0开始
         *
         * @param docId        文档Id
         * @param docName      文档名称
         * @param pageNum      当前页码
         * @param docTotalPage 当前文档总共的页数
         */
        @Override
        public void onPageChange(String docId, String docName, int width ,int height, int pageNum, int docTotalPage) {
            Log.d("SocketRoomHandler", "onPageChange: width:"+width +"  height:"+height);
           // mDocSizeListener.updateSize(width,height);
        }

        /**
         * 通知
         *
         * @param msg
         */
        @Override
        public void onNotification(String msg) {

        }

        /**
         * 切换数据源
         *
         * @param switchInfo 切换数据源信息 <br>
         * 注：<br>
         * 1. 返回数据格式为：{"source_type":"10","source_type_desc":"数据源类型：摄像头打开"} <br>
         * 2. 目前此回调只会在有文档的直播间模版下才会触发
         */
        @Override
        public void onSwitchSource(String switchInfo) {

        }

        /**
         * 切换视频文档区域
         *
         * @param isVideoMain 视频是否为主区域
         */
        @Override
        public void onSwitchVideoDoc(boolean isVideoMain) {
            if (dwLiveRoomListener != null) {
                dwLiveRoomListener.onSwitchVideoDoc(isVideoMain);
            }
        }

        @Override
        public void onRoomSettingInfo(SettingInfo info) {

        }

        /**
         * 收到历史广播信息(目前服务端只返回最后一条历史广播)
         *
         * @param msgs 广播消息列表
         */
        @Override
        public void onHistoryBroadcastMsg(ArrayList<BroadCastMsg> msgs) {
            if (dwLiveChatListener != null) {
                // 判断空
                if (msgs == null) {
                    return;
                }
                // 展示历史广播信息
                for (int i = 0; i < msgs.size(); i++) {
                    dwLiveChatListener.onBroadcastMsg(msgs.get(i).getContent());
                }
            }
        }

        /**
         * 收到广播信息（实时）
         *
         * @param msg 广播消息
         */
        @Override
        public void onBroadcastMsg(String msg) {
            if (dwLiveChatListener != null) {
                dwLiveChatListener.onBroadcastMsg(msg);
            }
        }

        /**
         * 信息，一般包括被禁言等
         *
         * @param msg
         */
        @Override
        public void onInformation(String msg) {

        }

        /**
         * 异常的回调
         *
         * @param exception
         */
        @Override
        public void onException(DWLiveException exception) {
            if (dwLiveFunctionListener != null) {
                if (exception.getErrorCode() == ErrorCode.INVALID_REQUEST) {
                    dwLiveFunctionListener.onException("无效请求：" + exception.getMessage());
                } else if (exception.getErrorCode() == ErrorCode.NETWORK_ERROR) {
                    dwLiveFunctionListener.onException("网络错误：" + exception.getMessage());
                } else if (exception.getErrorCode() == ErrorCode.PROCESS_FAIL) {
                    dwLiveFunctionListener.onException("过程失败：" + exception.getMessage());
                }
            }
        }

        /**
         * 初始化完成的回调
         *
         * @param playSourceCount 播放源的个数
         * @param qualityInfoList 可用的清晰度信息
         */
        @Override
        public void onInitFinished(int playSourceCount, List<QualityInfo> qualityInfoList) {
            if (dwLiveRoomListener != null && DWLive.getInstance().getRoomInfo() != null) {
                dwLiveRoomListener.showRoomTitle(DWLive.getInstance().getRoomInfo().getName());
            }

            /**
             * TODO 可以尝试获取一下当前正在进行的随堂测，需要考虑用户重新登录及Home桌面的问题
             * TODO（根据实际需求场景，自行实现）
             */
           // if(DWLive.getInstance() != null) {
           //    DWLive.getInstance().getRealTimePractice();
           // }
        }

        /**
         * 用户被踢出房间的回调
         *
         * @param type 踢出房间的类型<br>
         * 10:在允许重复登录前提下，后进入者会登录会踢出先前登录者<br>
         * 20:讲师、助教、主持人通过页面踢出按钮踢出用户
         */
        @Override
        public void onKickOut(int type) {
            if (dwLiveRoomListener != null) {
                dwLiveRoomListener.onKickOut();
            }
        }

        /**
         * 回调已播放时长
         *
         * @param playedTime 已播放时长，如果未开始，则时间为-1
         */
        @Override
        public void onLivePlayedTime(int playedTime) {

        }

        /**
         * 获取已播放时长请求异常
         *
         * @param exception 获取已播放时长异常
         */
        @Override
        public void onLivePlayedTimeException(Exception exception) {

        }

        /**
         * 是否是时移播放
         *
         * @param isPlayedBack
         */
        @Override
        public void isPlayedBack(boolean isPlayedBack) {

        }

        /**
         * 统计需要使用的参数
         *
         * @param statisticsMap 统计相关参数
         * @deprecated 已弃用
         */
        @Override
        public void onStatisticsParams(Map<String, String> statisticsMap) {

        }

        /**
         * 定制聊天
         *
         * @param customMessage customMessage
         */
        @Override
        public void onCustomMessage(String customMessage) {

        }

        /**
         * 禁播
         *
         * @param reason 禁播原因
         */
        @Override
        public void onBanStream(String reason) {
            if (dwLiveVideoListener != null) {
                dwLiveVideoListener.onBanStream(reason);
            }
        }

        /**
         * 解禁
         */
        @Override
        public void onUnbanStream() {
            if (dwLiveVideoListener != null) {
                dwLiveVideoListener.onUnbanStream();
            }
        }

        /**
         * 公告
         *
         * @param isRemove     是否是公告删除，如果为true，表示公告删除且announcement参数为null
         * @param announcement 公告内容
         */
        @Override
        public void onAnnouncement(boolean isRemove, String announcement) {
            if (dwLiveMoreFunctionListener != null) {
                dwLiveMoreFunctionListener.onAnnouncement(isRemove, announcement);
            }
        }

        /**
         * 签到回调
         *
         * @param duration 签到持续时间，单位为秒
         */
        @Override
        public void onRollCall(int duration) {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onRollCall(duration);
            }
        }

        /**
         * 开始抽奖
         *
         * @param lotteryId 本次抽奖的id
         */
        @Override
        public void onStartLottery(String lotteryId) {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onStartLottery(lotteryId);
            }
        }

        /**
         * 抽奖结果
         *
         * @param isWin       是否中奖，true表示中奖了
         * @param lotteryCode 中奖码
         * @param lotteryId   本次抽奖的id
         * @param winnerName  中奖者的名字
         */
        @Override
        public void onLotteryResult(boolean isWin, String lotteryCode, String lotteryId, String winnerName) {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onLotteryResult(isWin, lotteryCode, lotteryId, winnerName);
            }
        }

        /**
         * 结束抽奖
         *
         * @param lotteryId 本次抽奖的id
         */
        @Override
        public void onStopLottery(String lotteryId) {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onStopLottery(lotteryId);
            }
        }

        /**
         * 开始投票
         *
         * @param voteCount 总共的选项个数2-5
         * @param VoteType  0表示单选，1表示多选，目前只有单选
         */
        @Override
        public void onVoteStart(int voteCount, int VoteType) {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onVoteStart(voteCount, VoteType);
            }
        }

        /**
         * 结束投票
         */
        @Override
        public void onVoteStop() {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onVoteStop();
            }
        }

        /**
         * 投票结果统计
         *
         * @param jsonObject 投票结果数据
         */
        @Override
        public void onVoteResult(JSONObject jsonObject) {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onVoteResult(jsonObject);
            }
        }

        /**
         * 收到奖品发送事件
         *
         * @param type       奖品类型: 1 奖杯 2 其他(后续扩展使用)
         * @param viewerId   观看者的id
         * @param viewerName 观看者的昵称
         */
        @Override
        public void onPrizeSend(int type, String viewerId, String viewerName) {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onPrizeSend(type, viewerId, viewerName);
            }
        }

        /**
         * 发布问卷
         *
         * @param info 问卷内容
         */
        @Override
        public void onQuestionnairePublish(QuestionnaireInfo info) {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onQuestionnairePublish(info);
            }
        }

        /**
         * 停止问卷
         *
         * @param questionnaireId 问卷Id
         */
        @Override
        public void onQuestionnaireStop(String questionnaireId) {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onQuestionnaireStop(questionnaireId);
            }
        }

        /**
         * 问卷统计信息
         *
         * @param info
         */
        @Override
        public void onQuestionnaireStatis(QuestionnaireStatisInfo info) {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onQuestionnaireStatis(info);
            }
        }

        /**
         * 发布第三方问卷
         *
         * @param title       问卷标题
         * @param externalUrl 第三方问卷链接
         */
        @Override
        public void onExeternalQuestionnairePublish(String title, String externalUrl) {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onExeternalQuestionnairePublish(title, externalUrl);
            }
        }

        /**
         * 发布随堂测
         *
         * @param info 随堂测内容
         */
        @Override
        public void onPracticePublish(PracticeInfo info) {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onPracticePublish(info);
            }
        }

        /**
         * 收到随堂测提交结果
         *
         * @param info 随堂测结果
         */
        @Override
        public void onPracticeSubmitResult(PracticeSubmitResultInfo info) {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onPracticeSubmitResult(info);
            }
        }

        /**
         * 收到随堂测统计信息
         *
         * @param info 随堂测排名信息
         */
        @Override
        public void onPracticStatis(PracticeStatisInfo info) {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onPracticStatis(info);
            }
        }

        @Override
        public void onPracticRanking(PracticeRankInfo info) {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onPracticRanking(info);
            }
        }

        /**
         * 收到停止随堂测
         *
         * @param practiceId 随堂测ID
         */
        @Override
        public void onPracticeStop(String practiceId) {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onPracticeStop(practiceId);
            }
        }

        /**
         * 收到关闭随堂测
         *
         * @param practiceId 随堂测ID
         */
        @Override
        public void onPracticeClose(String practiceId) {
            if (dwLiveFunctionListener != null) {
                dwLiveFunctionListener.onPracticeClose(practiceId);
            }
        }
    };


    //************************************ 直播连麦模块 ***************************************/


    /**
     * 初始化连麦模块
     */
    public void initRtc(SurfaceViewRenderer localRender, CCRTCRender remoteRender) {
        EglBase rootEglBase = EglBase.create();
        localRender.init(rootEglBase.getEglBaseContext(), null);
        remoteRender.init(rootEglBase.getEglBaseContext(), null);

        localRender.setMirror(true);
        localRender.setZOrderMediaOverlay(true); // 设置让本地摄像头置于最顶层

        DWLive dwLive = DWLive.getInstance();
        if (dwLive != null) {
            dwLive.setRtcClientParameters(rtcClientListener, localRender, remoteRender);
        }
    }


    private boolean isAllowRtc = false;

    /**
     * 当前是否允许连麦
     */
    public boolean isAllowRtc() {
        return isAllowRtc;
    }

//    private boolean isVideoRtc = true;

    private boolean isRtcing = false;

    /**
     * 当前是否正在连麦中
     */
    public boolean isRtcing() {
        return isRtcing;
    }

    /**
     * 申请连麦
     *
     * @param videoRtc 是否为视频连麦
     */
    public void startRTCConnect(boolean videoRtc) {
        // 判断当前是否允许连麦，如果不允许，则不继续做任何操作
        if (!isAllowRtc) {
            return;
        }
        if (videoRtc) {
            DWLive.getInstance().startRtcConnect();
        } else {
//            isVideoRtc = false;
            DWLive.getInstance().startVoiceRTCConnect();
        }
    }

    /**
     * 取消连麦申请
     */
    public void cancelRTCConnect() {
        DWLive.getInstance().disConnectApplySpeak();
        DWLive.getInstance().closeCamera();
        isRtcing = false;
    }

    //------------------------------ 实现直播连麦功能事件监听 --------------------------------/

    private RtcClient.RtcClientListener rtcClientListener = new RtcClient.RtcClientListener() {

        @Override
        public void onAllowSpeakStatus(final boolean isAllowSpeak) {
            isAllowRtc = isAllowSpeak;
            if (!isAllowSpeak) {
                if (dwLiveRTCStatusListener != null) {
                    dwLiveRTCStatusListener.onCloseSpeak();
                }
            }
        }

        /**
         * 主播端接通连麦,开始
         * @param videoSize 视频的宽高，值为"600x400"
         */
        @Override
        public void onEnterSpeak(boolean isVideoRtc, boolean needAdjust, String videoSize) {
            if (dwLiveRTCListener != null) {
                dwLiveRTCListener.onEnterSpeak(isVideoRtc,needAdjust, videoSize);
            }
            if (dwLiveRTCStatusListener != null) {
                dwLiveRTCStatusListener.onEnterRTC(isVideoRtc);
            }
            isRtcing = true;
        }

        @Override
        public void onDisconnectSpeak() {
            if (dwLiveRTCListener != null && isRtcing) {
                dwLiveRTCListener.onDisconnectSpeak();
            }
            if (dwLiveRTCStatusListener != null) {
                dwLiveRTCStatusListener.onExitRTC();
            }
            isRtcing = false;
        }

        @Override
        public void onSpeakError(final Exception e) {
            if (dwLiveRTCListener != null) {
                dwLiveRTCListener.onSpeakError(e);
            }
            if (dwLiveRTCStatusListener != null) {
                dwLiveRTCStatusListener.onExitRTC();
            }
            isRtcing = false;
        }

        @Override
        public void onCameraOpen(final int width, final int height) {

        }
    };

}
