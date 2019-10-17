package com.bokecc.livemodule.live.function;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.live.DWLiveFunctionListener;
import com.bokecc.livemodule.live.function.lottery.LotteryHandler;
import com.bokecc.livemodule.live.function.practice.PracticeHandler;
import com.bokecc.livemodule.live.function.prize.PrizeHandler;
import com.bokecc.livemodule.live.function.questionnaire.QuestionnaireHandler;
import com.bokecc.livemodule.live.function.rollcall.RollCallHandler;
import com.bokecc.livemodule.live.function.vote.VoteHandler;
import com.bokecc.sdk.mobile.live.pojo.PracticeInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeRankInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeStatisInfo;
import com.bokecc.sdk.mobile.live.pojo.PracticeSubmitResultInfo;
import com.bokecc.sdk.mobile.live.pojo.QuestionnaireInfo;
import com.bokecc.sdk.mobile.live.pojo.QuestionnaireStatisInfo;

import org.json.JSONObject;

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

    public void initFunctionHandler(Context context) {
        this.context = context.getApplicationContext();

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

    /**
     * 开始投票
     *
     * @param voteCount 总共的选项个数2-5
     * @param VoteType  0表示单选，1表示多选，目前只有单选
     */
    @Override
    public void onVoteStart(final int voteCount, final int VoteType) {
        if (rootView == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                voteHandler.startVote(rootView, voteCount, VoteType);
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
        if (rootView == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                voteHandler.showVoteResult(rootView, jsonObject);
            }
        });
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
        if (rootView == null || info == null) {
            return;
        }

        // 如果此随堂测已经回答过了，就不展示随堂测做题的界面
        if (info.isAnswered()) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                practiceHandler.startPractice(rootView, info);
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
        if (rootView == null) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toastOnUiThread("展示随堂测提交结果");
                practiceHandler.showPracticeSubmitResult(rootView, info);
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
        if (rootView == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                practiceHandler.showPracticeStatis(rootView, info);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toastOnUiThread("随堂测停止");
                practiceHandler.onPracticeStop(practiceId);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toastOnUiThread("随堂测关闭");
                practiceHandler.onPracticeClose(practiceId);
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
}
