package com.bokecc.livemodule.live.function;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.FontRequest;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.live.DWLiveFunctionListener;
import com.bokecc.livemodule.live.function.lottery.LotteryHandler;
import com.bokecc.livemodule.live.function.practice.PracticeConfig;
import com.bokecc.livemodule.live.function.practice.PracticeHandler;
import com.bokecc.livemodule.live.function.practice.PracticeListener;
import com.bokecc.livemodule.live.function.prize.PrizeHandler;
import com.bokecc.livemodule.live.function.punch.PunchHandler;
import com.bokecc.livemodule.live.function.questionnaire.QuestionnaireHandler;
import com.bokecc.livemodule.live.function.rollcall.RollCallHandler;
import com.bokecc.livemodule.live.function.vote.VoteListener;
import com.bokecc.livemodule.live.function.vote.VoteConfig;
import com.bokecc.livemodule.live.function.vote.VoteHandler;
import com.bokecc.sdk.mobile.live.BaseCallback;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.broadcast.NetBroadcastReceiver;
import com.bokecc.sdk.mobile.live.pojo.PracticeInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeRankInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeStatisInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeSubmitResultInfo;
import com.bokecc.sdk.mobile.live.pojo.PunchAction;
import com.bokecc.sdk.mobile.live.pojo.QuestionnaireInfo;
import com.bokecc.sdk.mobile.live.pojo.QuestionnaireStatisInfo;
import com.bokecc.sdk.mobile.live.replay.utils.NetworkUtils;

import org.json.JSONObject;

import java.nio.file.attribute.GroupPrincipal;
import java.util.ArrayList;
import java.util.List;

/**
 * 直播功能处理机制（签到、答题卡/投票、问卷、抽奖）
 */
public class FunctionHandler implements DWLiveFunctionListener {

    private Context context;
    private View rootView;

    private RollCallHandler rollCallHandler;  // '签到' 功能处理机制
    private VoteHandler voteHandler; // '投票' 功能处理机制
    private LotteryHandler lotteryHandler; // '抽奖' 功能处理机制
    private QuestionnaireHandler questionnaireHandler; // '问卷' 功能处理机制
    private PracticeHandler practiceHandler;  // '随堂测' 功能处理机制
    private PrizeHandler prizeHandler;  // '奖品' 功能处理机制
    private PunchHandler punchHandler;//'打卡' 功能处理机制
    private NetStatusBroadcastReceiver mNetWorkChangReceiver;
    /**
     * 当前正在进行的随堂测ID
     */
    private String practiceId = "";
    public void initFunctionHandler(Context context,FunctionCallBack functionCallBack) {
        this.context = context.getApplicationContext();
        this.functionCallBack =functionCallBack;
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            dwLiveCoreHandler.setDwLiveFunctionListener(this);
        }

        rollCallHandler = new RollCallHandler();
        rollCallHandler.initRollCall(this.context);

        voteHandler = new VoteHandler();
        voteHandler.initVote(this.context);

        lotteryHandler = new LotteryHandler();
        lotteryHandler.initLottery(this.context);

        questionnaireHandler = new QuestionnaireHandler();
        questionnaireHandler.initQuestionnaire(this.context);

        practiceHandler = new PracticeHandler();
        practiceHandler.initPractice(this.context);

        prizeHandler = new PrizeHandler();
        prizeHandler.initPrize(this.context);

        punchHandler = new PunchHandler();
        punchHandler.initPunch(this.context);
        //注册打卡事件
        DWLive.getInstance().setPunchCallback(new BaseCallback<PunchAction>() {
            @Override
            public void onError(final String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        punchHandler.stopPunch(error);
                    }
                });
            }

            @Override
            public void onSuccess(final PunchAction msg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        punchHandler.startPunch(rootView, msg);
                    }
                });
            }
        });
        //注册网络监听
        mNetWorkChangReceiver = new NetStatusBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(mNetWorkChangReceiver, filter);
    }

    /**
     * 设置弹窗的根View
     */
    public void setRootView(View rootView) {
        this.rootView = rootView;
    }

    /**
     * 移除弹窗的根View
     */
    public void removeRootView() {
        this.rootView = null;
    }


    /**
     * 开始签到回调
     *
     * @param duration 签到持续时间，单位为秒
     */
    @Override
    public void onRollCall(final int duration) {
        if (rootView == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rollCallHandler.startRollCall(rootView, duration);
            }
        });
    }
    private boolean isNeedShowVote = false;
    /**
     * 开始投票
     *
     * @param voteCount 总共的选项个数2-5
     * @param VoteType  0表示单选，1表示多选，目前只有单选
     */
    @Override
    public void onVoteStart(final int voteCount, final int VoteType) {
        isNeedShowVote = true;
        if (rootView == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                voteHandler.startVote(rootView, voteCount, VoteType,minimizeListener );
            }
        });
    }

    /**
     * 结束投票
     */
    @Override
    public void onVoteStop() {
        if (rootView == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                voteHandler.stopVote();
            }
        });
    }

    /**
     * 投票结果统计
     *
     * @param jsonObject 投票结果数据
     */
    @Override
    public void onVoteResult(final JSONObject jsonObject) {
        if(functionCallBack!=null){
            functionCallBack.onClose();
        }
        if (rootView == null) {
            return;
        }
        if (isNeedShowVote){
            isNeedShowVote = !isNeedShowVote;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    voteHandler.showVoteResult(rootView, jsonObject);
                }
            });
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
    public void onPrizeSend(final int type, final String viewerId, final String viewerName) {
        if (rootView == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (type) {
                    case 1: // 奖杯
                        prizeHandler.showPrize(rootView, viewerName, viewerId);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 开始抽奖
     *
     * @param lotteryId 本次抽奖的id
     */
    @Override
    public void onStartLottery(final String lotteryId) {
        if (rootView == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lotteryHandler.startLottery(rootView, lotteryId);
            }
        });
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
    public void onLotteryResult(final boolean isWin, final String lotteryCode, final String lotteryId, final String winnerName) {
        if (rootView == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lotteryHandler.showLotteryResult(rootView, isWin, lotteryCode, lotteryId, winnerName);
            }
        });
    }

    /**
     * 结束抽奖
     *
     * @param lotteryId 本次抽奖的id
     */
    @Override
    public void onStopLottery(final String lotteryId) {
        if (rootView == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lotteryHandler.stopLottery(rootView, lotteryId);
            }
        });
    }

    /**
     * 发布问卷
     *
     * @param info 问卷内容
     */
    @Override
    public void onQuestionnairePublish(final QuestionnaireInfo info) {
        if (rootView == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                questionnaireHandler.startQuestionnaire(rootView, info);
            }
        });
    }

    /**
     * 停止问卷
     *
     * @param questionnaireId 问卷Id
     */
    @Override
    public void onQuestionnaireStop(String questionnaireId) {
        if (rootView == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                questionnaireHandler.stopQuestionnaire(rootView);
            }
        });
    }

    /**
     * 问卷统计信息
     *
     * @param info
     */
    @Override
    public void onQuestionnaireStatis(final QuestionnaireStatisInfo info) {
        if (rootView == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                questionnaireHandler.showQuestionnaireStatis(rootView, info);
            }
        });
    }

    /**
     * 发布第三方问卷
     *
     * @param title       问卷标题
     * @param externalUrl 第三方问卷链接
     */
    @Override
    public void onExeternalQuestionnairePublish(final String title, final String externalUrl) {
        if (rootView == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                questionnaireHandler.showExeternalQuestionnaire(rootView, title, externalUrl);
            }
        });
    }

    /**
     * 发布随堂测
     *
     * @param info 随堂测内容
     */
    @Override
    public void onPracticePublish(final PracticeInfo info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (rootView == null) {
                    return;
                }
                if ( info == null){
                    practiceHandler.onPracticeClose("");
                    return;
                }
                if (info.getIsExist() == 0){
                    //如果不存在随堂测 关闭所有的随堂测弹框
                    practiceHandler.onPracticeClose("");
                    return;
                }
                practiceId = info.getId();
                // 如果此随堂测已经回答过了，就不展示随堂测做题的界面
                if (info.getStatus() ==1) {
                    if (info.isAnswered()){
                        DWLive.getInstance().getPracticeStatis(info.getId());
                    }else{
                        practiceHandler.startPractice(rootView, info,practiceListener);
                    }
                }else if (info.getStatus() == 2) {//停止发布
                    DWLive.getInstance().getPracticeStatis(info.getId());
                }else if (info.getStatus() == 3){//关闭
                    onPracticeStop(info.getId());
                }
            }
        });


    }
    /**
     * 收到随堂测提交结果
     *
     * @param info 随堂测结果
     */
    @Override
    public void onPracticeSubmitResult(final PracticeSubmitResultInfo info) {
        if (rootView == null&&info!=null) {
            return;
        }
        practiceId = info.getId();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toastOnUiThread("展示随堂测提交结果");
                practiceHandler.showPracticeSubmitResult(rootView, info);
                if (functionCallBack!=null){
                    functionCallBack.onClose();
                }
            }
        });
    }

    /**
     * 收到随堂测统计信息
     *
     * @param info 随堂测排名信息
     */
    @Override
    public void onPracticStatis(final PracticeStatisInfo info) {
        if (rootView == null&&info!=null) {
            return;
        }
        practiceId = info.getId();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                practiceHandler.showPracticeStatis(rootView, info);
                if (functionCallBack!=null){
                    functionCallBack.onClose();
                }
            }
        });
    }

    @Override
    public void onPracticRanking(PracticeRankInfo info) {

    }

    /**
     * 收到停止随堂测
     *
     * @param practiceId 随堂测ID
     */
    @Override
    public void onPracticeStop(final String practiceId) {
        this.practiceId = practiceId;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toastOnUiThread("随堂测停止");
                practiceHandler.onPracticeStop(practiceId,rootView,functionCallBack==null?false:functionCallBack.isSmall);
                if (functionCallBack!=null){
                    functionCallBack.onClose();
                }
            }
        });
    }

    /**
     * 收到关闭随堂测
     *
     * @param practiceId 随堂测ID
     */
    @Override
    public void onPracticeClose(final String practiceId) {
        this.practiceId = "";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toastOnUiThread("随堂测关闭");
                practiceHandler.onPracticeClose(practiceId);
                if (functionCallBack!=null){
                    functionCallBack.onClose();
                }
            }
        });
    }

    /**
     * 功能异常的回调
     *
     * @param message
     */
    @Override
    public void onException(String message) {
        toastOnUiThread(message);
    }


    // ------------------------------ 工具方法 ------------------------------------------

    public void runOnUiThread(Runnable runnable) {
        // 判断是否处在UI线程
        if (!checkOnMainThread()) {
            new Handler(Looper.getMainLooper()).post(runnable);
        } else {
            runnable.run();
        }
    }

    // 在UI线程上进行吐司提示
    public void toastOnUiThread(final String msg) {
        // 判断是否处在UI线程
        if (!checkOnMainThread()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    showToast(msg);
                }
            });
        } else {
            showToast(msg);
        }
    }

    // 进行吐司提示
    private void showToast(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    // 判断当前的线程是否是UI线程
    private boolean checkOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }


    private VoteConfig voteConfig;
    private PracticeConfig practiceConfig;
    private VoteListener minimizeListener = new VoteListener() {
        @Override
        public void onMinimize( int voteCount, int VoteType, int selectIndex, List<Integer> selectIndexs) {
            //用户点击缩小的时候 需要记录当前的答题记录 以及已经选择的条目
            if (voteConfig == null){
                voteConfig = new VoteConfig();
            }
            voteConfig.setVoteCount(voteCount);
            voteConfig.setVoteType(VoteType);
            voteConfig.setSelectIndex(selectIndex);
            voteConfig.setSelectIndexs(selectIndexs);
            if (functionCallBack!=null){
                functionCallBack.onMinimize(true);
            }
        }
    };
    private PracticeListener practiceListener = new PracticeListener() {
        @Override
        public void onMinimize(PracticeInfo practiceInfo, int VoteType, int selectIndex, List<Integer> selectIndexs) {
            if (practiceConfig == null){
                practiceConfig = new PracticeConfig();
            }
            practiceConfig.setPracticeInfo(practiceInfo);
            practiceConfig.setVoteType(VoteType);
            practiceConfig.setSelectIndex(selectIndex);
            practiceConfig.setSelectIndexs(selectIndexs);
            if (functionCallBack!=null){
                functionCallBack.onMinimize(false);
            }
        }
    };
    /**
     * 针对缩小的回调
     */
    private FunctionCallBack functionCallBack;
    /**
     * 展示答题卡选择筐 只针对缩小之后进行的展示
     */
    public void onVoteStart(){
        if (voteConfig!=null){
            voteHandler.startVote(rootView, voteConfig.getVoteCount(), voteConfig.getVoteType(),voteConfig.getSelectIndex(),voteConfig.getSelectIndexs(),minimizeListener );
        }
    }
    /**
     * 展示随堂测选择筐 只针对缩小之后进行的展示
     */
    public void onPractic(){
        if (practiceConfig!=null){
            practiceHandler.startPractice(rootView, practiceConfig,practiceListener);
        }
    }

    public void onDestroy(Context context) {
        if (mNetWorkChangReceiver!=null){
            context.unregisterReceiver(mNetWorkChangReceiver);
        }
    }
    public class NetStatusBroadcastReceiver extends BroadcastReceiver {
        private boolean isNeed = false;
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())&&practiceHandler!=null){
                boolean isConnected = NetworkUtils.isConnected();
                if (isConnected){
                    Log.e("###","isNeed = "+isNeed);
                    if (isNeed){
//                        onPracticeClose("");
                        DWLive.getInstance().getPracticeInformation();
                        if (functionCallBack!=null){
                            functionCallBack.onClose();
                        }
                    }
                    isNeed = false;
                }else{
                    isNeed = true;
                }
            }
        }
    }
}
