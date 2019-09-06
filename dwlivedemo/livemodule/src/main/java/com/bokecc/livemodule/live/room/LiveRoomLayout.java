package com.bokecc.livemodule.live.room;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.live.DWLiveRoomListener;
import com.bokecc.livemodule.live.chat.adapter.EmojiAdapter;
import com.bokecc.livemodule.live.chat.util.EmojiUtil;
import com.bokecc.livemodule.live.chat.util.SoftKeyBoardState;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.pojo.RoomInfo;

/**
 * 直播间信息组件
 */
public class LiveRoomLayout extends RelativeLayout implements DWLiveRoomListener {

    private final int SCALE_CENTER_INSIDE = 0;
    private final int SCALE_FIT_XY = 1;
    private final int SCALE_CROP_CENTER = 2;
    private int mCurrentScaleType = SCALE_CENTER_INSIDE;

    Context mContext;
    RelativeLayout mTopLayout;
    RelativeLayout mBottomLayout;
    ImageView mBarrageControl;
    // 直播间标题展示
    TextView mLiveTitle;
    // 直播间人数展示
    TextView mLiveUserNumberBottom;
    TextView mLiveUserNumberTop;
    // 切换 文档/视频 按钮
    TextView mLiveVideoDocSwitch;
    // 退出直播间按钮
    ImageView mLiveClose;
    // 下方输入聊天信息框
    LinearLayout mBottomChatLayout;
    RelativeLayout mPortraitLiveBottom;
    // 全屏按钮
    ImageView mLiveFullScreen;

    boolean isEmoji = false; // emoji是否需要显示
    boolean isEmojiShow = false; // emoji是否显示
    Button mChatSend;
    GridView mEmojiGrid;
    ImageView mEmoji; // 表情按钮
    EditText mInput;
    boolean isSoftInput = false;
    SoftKeyBoardState mSoftKeyBoardState;
    InputMethodManager mImm;
    private TextView mDocScaleType;

    // 是否视频为主 （用于文档和视频区域展示切换）
    boolean isVideoMain = true;

    //当前文档的类型
    int currentType = 0;

    public LiveRoomLayout(Context context) {
        super(context);
        mContext = context;
        initViews();
        initRoomListener();
    }

    public LiveRoomLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
        initRoomListener();
    }

    private void initViews() {
        mImm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        LayoutInflater.from(mContext).inflate(R.layout.live_room_layout, this, true);
        mLiveTitle = findViewById(R.id.tv_portrait_live_title);
        mLiveUserNumberBottom = findViewById(R.id.tv_portrait_live_user_count_bottom);
        mLiveUserNumberTop = findViewById(R.id.tv_portrait_live_user_count_top);
        mBarrageControl = findViewById(R.id.iv_barrage_control);
        mTopLayout = findViewById(R.id.rl_portrait_live_top_layout);
        mBottomLayout = findViewById(R.id.rl_portrait_live_bottom_layout);
        mLiveVideoDocSwitch = findViewById(R.id.video_doc_switch);
        mLiveFullScreen = findViewById(R.id.iv_portrait_live_full);
        mPortraitLiveBottom = findViewById(R.id.portrait_live_bottom);
        mLiveClose = findViewById(R.id.iv_portrait_live_close);
        mBottomChatLayout = findViewById(R.id.id_chat_bottom);
        mEmoji = findViewById(R.id.id_push_chat_emoji);
        mEmojiGrid = findViewById(R.id.id_push_emoji_grid);
        mChatSend = findViewById(R.id.id_push_chat_send);
        mInput = findViewById(R.id.id_push_chat_input);
        TextView estimateStartTime = findViewById(R.id.estimateStartTime);

        RoomInfo roomInfo = DWLive.getInstance().getRoomInfo();
        if (roomInfo != null) {
            estimateStartTime.setVisibility(VISIBLE);
            estimateStartTime.setText(roomInfo.getEstimateStartTime());
        }

        mDocScaleType = findViewById(R.id.id_scale_type);

        initEmojiAndChat();

        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            // 判断当前直播间模版是否有"文档"功能，如果没文档，则小窗功能也不应该有
            if (!dwLiveCoreHandler.hasPdfView()) {
                mLiveVideoDocSwitch.setVisibility(GONE);
            }
        }

        this.setOnClickListener(mRoomAnimatorListener);

        mLiveVideoDocSwitch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DWLiveCoreHandler.getInstance().isRtcing()) {
                    toastOnUiThread("连麦中，暂不支持切换");
                    return;
                }
                if (liveRoomStatusListener != null) {
                    if (isVideoMain) {
                        mLiveVideoDocSwitch.setText("切换视频");
                        isVideoMain = false;
                    } else {
                        mLiveVideoDocSwitch.setText("切换文档");
                        isVideoMain = true;
                    }
                    liveRoomStatusListener.switchVideoDoc(isVideoMain);
                }
            }
        });

        mLiveFullScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                intoFullScreen();
            }
        });

        mLiveClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (liveRoomStatusListener != null) {
                    liveRoomStatusListener.closeRoom();
                }
            }
        });


        mDocScaleType.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (liveRoomStatusListener != null) {
                    if (currentType == 0) {
                        currentType = 1;
                        mDocScaleType.setText("FitXY");
                    } else if (currentType == 1) {
                        currentType = 2;
                        mDocScaleType.setText("CropCenter");
                    } else if (currentType == 2) {
                        currentType = 0;
                        mDocScaleType.setText("centerInside");
                    }

                    liveRoomStatusListener.onClickDocScaleType(currentType);
                }
            }
        });

    }

    /**
     * 设置视频/文档切换的状态
     */
    public void setVideoDocSwitchStatus(boolean isVideoMain) {
        this.isVideoMain = isVideoMain;
        if (this.isVideoMain) {
            mLiveVideoDocSwitch.setText("切换文档");
        } else {
            mLiveVideoDocSwitch.setText("切换视频");
        }
    }

    // 进入全屏
    public void intoFullScreen() {
        if (liveRoomStatusListener != null) {
            liveRoomStatusListener.fullScreen();
        }
        mLiveFullScreen.setVisibility(GONE);
        mLiveUserNumberTop.setVisibility(View.VISIBLE);
        mLiveUserNumberBottom.setVisibility(View.GONE);
        mBottomChatLayout.setVisibility(VISIBLE);
        mPortraitLiveBottom.setVisibility(GONE);

        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            // 判断当前直播间模版是否有"聊天"功能，如果没有，就不展示下方聊天布局
            if (!dwLiveCoreHandler.hasChatView()) {
                mBottomChatLayout.setVisibility(GONE);
            }
        }
    }

    // 退出全屏
    public void quitFullScreen() {
        mLiveUserNumberBottom.setVisibility(View.VISIBLE);
        mLiveUserNumberTop.setVisibility(View.GONE);
        mLiveFullScreen.setVisibility(VISIBLE);
        mBottomChatLayout.setVisibility(GONE);
        mPortraitLiveBottom.setVisibility(VISIBLE);
    }

    private void initRoomListener() {
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler == null) {
            return;
        }
        dwLiveCoreHandler.setDwLiveRoomListener(this);
    }

    /**
     * 切换视频文档区域
     *
     * @param isVideoMain 视频是否为主区域
     */
    @Override
    public void onSwitchVideoDoc(final boolean isVideoMain) {
        // 判断是否相同，相同没必要触发
        if (this.isVideoMain == isVideoMain) {
            return;
        }

        if (liveRoomStatusListener != null) {
            mLiveVideoDocSwitch.post(new Runnable() {
                @Override
                public void run() {
                    if (isVideoMain) {
                        mLiveVideoDocSwitch.setText("切换文档");
                    } else {
                        mLiveVideoDocSwitch.setText("切换视频");
                    }
                }
            });
            this.isVideoMain = isVideoMain;
            liveRoomStatusListener.switchVideoDoc(isVideoMain);
        }
    }

    /**
     * 展示直播间标题
     */
    @Override
    public void showRoomTitle(final String title) {
        mLiveTitle.post(new Runnable() {
            @Override
            public void run() {
                mLiveTitle.setText(title);
            }
        });
    }

    /**
     * 展示直播间人数
     */
    @Override
    public void showRoomUserNum(final int number) {
        mLiveUserNumberBottom.post(new Runnable() {
            @Override
            public void run() {
                mLiveUserNumberBottom.setText(String.valueOf(number));
                mLiveUserNumberTop.setText(String.valueOf(number));
            }
        });
    }

    /**
     * 踢出用户
     */
    @Override
    public void onKickOut() {
        if (liveRoomStatusListener != null) {
            liveRoomStatusListener.kickOut();
        }
    }

    public interface LiveRoomStatusListener {

        /**
         * 切换 视频/文档 区域回调
         *
         * @param videoMain 是否是视频区域为主
         */
        void switchVideoDoc(boolean videoMain);

        /**
         * 点击 Back 按钮 退出直播间回调
         */
        void closeRoom();

        /**
         * 点击 全屏 按钮 进入全屏回调
         */
        void fullScreen();

        /**
         * 用户踢出 事件回调 #Called From DWLiveCoreHandler
         */
        void kickOut();

        /**
         * 点击文档类型
         */
        void onClickDocScaleType(int scaleType);
    }

    // 直播间状态监听
    private LiveRoomStatusListener liveRoomStatusListener;

    // 设置直播间状态监听
    public void setLiveRoomStatusListener(LiveRoomStatusListener listener) {
        this.liveRoomStatusListener = listener;
    }

    //***************************************** 聊天相关方法 ************************************************

    // 最大输入字符数
    private short maxInput = 300;

    // 弹幕是否开启
    private boolean isBarrageOn = true;

    // 初始化表情和聊天相关
    private void initEmojiAndChat() {
        // 限制输入字符数为300
        mInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String inputText = mInput.getText().toString();
                if (inputText.length() > maxInput) {
                    Toast.makeText(mContext, "字数超过300字", Toast.LENGTH_SHORT).show();
                    mInput.setText(inputText.substring(0, maxInput));
                    mInput.setSelection(maxInput);
                }
            }
        });

        mEmoji.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEmojiShow) {
                    hideEmoji();
                    mInput.requestFocus();
                    mInput.setSelection(mInput.getEditableText().length());
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                } else {
                    showEmoji();
                }
            }
        });

        initEmojiAdapter();

        mChatSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = mInput.getText().toString().trim();
                if (TextUtils.isEmpty(msg)) {
                    Toast.makeText(mContext, "聊天内容不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                DWLive.getInstance().sendPublicChatMsg(msg);
                clearChatInput();
            }
        });

        mBarrageControl.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
                if (dwLiveCoreHandler == null) {
                    return;
                }
                if (isBarrageOn) {
                    mBarrageControl.setImageResource(R.mipmap.barrage_off);
                    isBarrageOn = false;
                    dwLiveCoreHandler.setBarrageStatus(false);
                } else {
                    mBarrageControl.setImageResource(R.mipmap.barrage_on);
                    isBarrageOn = true;
                    dwLiveCoreHandler.setBarrageStatus(true);
                }
            }
        });

        onSoftInputChange();
    }

    private void initEmojiAdapter() {
        EmojiAdapter emojiAdapter = new EmojiAdapter(mContext);
        emojiAdapter.bindData(EmojiUtil.imgs);
        mEmojiGrid.setAdapter(emojiAdapter);
        mEmojiGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mInput == null) {
                    return;
                }
                // 一个表情span占位8个字符
                if (mInput.getText().length() + 8 > maxInput) {
                    Toast.makeText(mContext, "字符数超过300字", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (position == EmojiUtil.imgs.length - 1) {
                    EmojiUtil.deleteInputOne(mInput);
                } else {
                    EmojiUtil.addEmoji(mContext, mInput, position);
                }
            }
        });
    }

    private void onSoftInputChange() {
        mSoftKeyBoardState = new SoftKeyBoardState(this, false);
        mSoftKeyBoardState.setOnSoftKeyBoardStateChangeListener(new SoftKeyBoardState.OnSoftKeyBoardStateChangeListener() {
            @Override
            public void onChange(boolean isShow) {
                isSoftInput = isShow;
                if (!isSoftInput) { // 软键盘隐藏
                    if (isEmoji) {
                        mEmojiGrid.setVisibility(View.VISIBLE);// 避免闪烁
                        isEmojiShow = true; // 修改emoji显示标记
                        isEmoji = false; // 重置
                    }
                } else {
                    hideEmoji();
                }
            }
        });
    }

    // 隐藏视频文档切换功能
    public void hideSwitchVideoDoc() {
        mLiveVideoDocSwitch.setVisibility(GONE);
    }

    public void clearChatInput() {
        mInput.setText("");
        hideKeyboard();
    }

    public void hideKeyboard() {
        hideEmoji();
        mImm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
    }

    // 显示emoji
    public void showEmoji() {
        if (isSoftInput) {
            isEmoji = true; // 需要显示emoji
            mInput.clearFocus();
            mImm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
        } else {
            mEmojiGrid.setVisibility(View.VISIBLE);// 避免闪烁
            isEmojiShow = true; // 修改emoji显示标记
        }
        mEmoji.setImageResource(R.drawable.push_chat_emoji);
    }

    // 隐藏emoji
    public void hideEmoji() {
        if (isEmojiShow) { // 如果emoji显示
            mEmojiGrid.setVisibility(View.GONE);
            isEmojiShow = false; // 修改emoji显示标记
            mEmoji.setImageResource(R.drawable.push_chat_emoji_normal);
        }
    }

    //***************************************** 控制布局动画相关方法 ******************************

    private OnClickListener mRoomAnimatorListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            hideKeyboard();
            if (mTopLayout.isShown()) {
                ObjectAnimator bottom_y = ObjectAnimator.ofFloat(mBottomLayout, "translationY", mBottomLayout.getHeight());
                ObjectAnimator top_y = ObjectAnimator.ofFloat(mTopLayout, "translationY", -1 * mTopLayout.getHeight());
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.play(top_y).with(bottom_y);

                //播放动画的持续时间
                animatorSet.setDuration(500);
                animatorSet.start();

                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mBottomLayout.setVisibility(GONE);
                        mTopLayout.setVisibility(GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
            } else {
                mTopLayout.setVisibility(VISIBLE);
                mBottomLayout.setVisibility(VISIBLE);
                ObjectAnimator bottom_y = ObjectAnimator.ofFloat(mBottomLayout, "translationY", 0);
                ObjectAnimator top_y = ObjectAnimator.ofFloat(mTopLayout, "translationY", 0);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.play(top_y).with(bottom_y);
                //播放动画的持续时间
                animatorSet.setDuration(500);
                animatorSet.start();
                animatorSet.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
            }
        }
    };


    //***************************************** 工具方法 *****************************************

    // 在Ui线程上进行吐司提示
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

    // 在UI线程执行一些操作
    public void runOnUiThread(Runnable runnable) {
        if (!checkOnMainThread()) {
            new Handler(Looper.getMainLooper()).post(runnable);
        } else {
            runnable.run();
        }
    }

    // 进行吐司提示
    private void showToast(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    // 判断当前的线程是否是UI线程
    protected boolean checkOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

}
