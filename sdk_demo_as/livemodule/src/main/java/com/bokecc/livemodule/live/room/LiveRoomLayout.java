package com.bokecc.livemodule.live.room;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.bokecc.livemodule.live.chat.KeyboardHeightObserver;
import com.bokecc.livemodule.live.chat.adapter.EmojiAdapter;
import com.bokecc.livemodule.live.chat.util.EmojiUtil;
import com.bokecc.livemodule.live.chat.window.BanChatPopup;
import com.bokecc.livemodule.live.video.LiveVideoView;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.pojo.RoomInfo;

/**
 * 直播间信息组件
 */
public class LiveRoomLayout extends RelativeLayout implements DWLiveRoomListener, KeyboardHeightObserver {

    private Context mContext;
    private RelativeLayout mTopLayout;
    private RelativeLayout mBottomLayout;
    private ImageView mBarrageControl;
    // 直播间标题展示
    private TextView mLiveTitle;
    // 直播间人数展示
    private TextView mLiveUserNumberBottom;
    private TextView mLiveUserNumberTop;
    private boolean isShowUserCount;
    // 切换 文档/视频 按钮
    private TextView mLiveVideoDocSwitch;
    // 退出直播间按钮
    private ImageView mLiveClose;
    // 下方输入聊天信息框
    private LinearLayout mBottomChatLayout;
    private RelativeLayout mPortraitLiveBottom;
    // 全屏按钮
    private ImageView mLiveFullScreen;
    private Button mChatSend;
    private GridView mEmojiGrid;
    private ImageView mEmoji; // 表情按钮
    private EditText mInput;

    boolean isEmojiShow = false; // emoji是否显示
    boolean isSoftInput = false;
    private InputMethodManager mImm;
    //软键盘的高度
    private int softKeyHeight;
    private boolean showEmojiAction = false;
    private BanChatPopup banChatPopup;
    //当前视图状态
    public State viewState = State.VIDEO;
    private LiveVideoView mLiveVideoView;


    public enum State {
        VIDEO,//小窗口打开并且视频在大窗口上
        DOC,//小窗口打开并且文档在大窗口上
        OPEN_VIDEO,//小窗口关闭并且文档在大窗口上
        OPEN_DOC//小窗口关闭并且视频在大窗口上
    }

    //针对隐藏标题栏和聊天布局的延迟
    @SuppressLint("HandlerLeak")
    public final Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            //3s延迟隐藏
            if (msg.what == DELAY_HIDE_WHAT) {
                hide();
            }
        }
    };
    private final int DELAY_HIDE_WHAT = 1;

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

    DWLive.PlayMode playMode = DWLive.PlayMode.VIDEO;

    private void initViews() {
        mImm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.live_room_layout, this, true);
        inflate.setFocusableInTouchMode(true);
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

        RoomInfo roomInfo = DWLive.getInstance().getRoomInfo();
        if (roomInfo != null) {
            isShowUserCount = roomInfo.getShowUserCount() == 1;
            mLiveUserNumberBottom.setVisibility(isShowUserCount ? VISIBLE : GONE);
        }


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
                // 根据类型适配开关
                if (liveRoomStatusListener != null) {
                    if (viewState == State.VIDEO) {
                        viewState = State.DOC;
                        liveRoomStatusListener.switchVideoDoc(viewState);
                    } else if (viewState == State.DOC) {
                        viewState = State.VIDEO;
                        liveRoomStatusListener.switchVideoDoc(viewState);
                    } else if (viewState == State.OPEN_DOC) {
                        liveRoomStatusListener.switchVideoDoc(viewState);
                        viewState = State.VIDEO;
                    } else if (viewState == State.OPEN_VIDEO) {
                        liveRoomStatusListener.switchVideoDoc(viewState);
                        viewState = State.DOC;
                    }
                    setSwitchText(viewState);
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

        handler.sendEmptyMessageDelayed(DELAY_HIDE_WHAT, 3000);
    }

    public void setSwitchText(State state) {
        this.viewState = state;
        if (viewState == State.VIDEO) {
            mLiveVideoDocSwitch.setText("切换文档");
        } else if (viewState == State.DOC) {
            mLiveVideoDocSwitch.setText("切换视频");
        } else if (viewState == State.OPEN_DOC) {
            mLiveVideoDocSwitch.setText("打开文档");
        } else if (viewState == State.OPEN_VIDEO) {
            mLiveVideoDocSwitch.setText("打开视频");
        }
    }

    public void setVideo(LiveVideoView mLiveVideoView) {
        this.mLiveVideoView = mLiveVideoView;
    }

    public State isVideoMain() {
        return viewState;
    }


    // 进入全屏
    public void intoFullScreen() {
        if (liveRoomStatusListener != null) {
            liveRoomStatusListener.fullScreen();
        }
        mLiveFullScreen.setVisibility(GONE);
        mLiveUserNumberTop.setVisibility(isShowUserCount ? VISIBLE : GONE);
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
        mLiveUserNumberBottom.setVisibility(isShowUserCount ? VISIBLE : GONE);
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
     * @param state 视频是否为主区域
     */
    @Override
    public void onSwitchVideoDoc(final State state) {
        // 判断是否相同，相同没必要触发
        if (this.viewState == state) {
            return;
        }
        this.viewState = state;
        if (liveRoomStatusListener != null) {
            liveRoomStatusListener.switchVideoDoc(viewState);
            mLiveVideoDocSwitch.post(new Runnable() {
                @Override
                public void run() {
                    setSwitchText(viewState);
                }
            });
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

    private View rootView;

    public void setPopView(View rootView) {
        this.rootView = rootView;
    }

    @Override
    public void onInformation(final String msg) {
        if (rootView != null)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (banChatPopup == null) {
                        banChatPopup = new BanChatPopup(getContext());
                    }
                    if (banChatPopup.isShowing()) {
                        banChatPopup.onDestroy();
                    }
                    banChatPopup.banChat(msg);
                    banChatPopup.show(rootView);
                }
            });
    }

    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {
        if (height > 10) {
            isSoftInput = true;
            softKeyHeight = height;
            mBottomChatLayout.setTranslationY(-softKeyHeight);
            mEmoji.setImageResource(R.drawable.push_chat_emoji_normal);
            isEmojiShow = false;
        } else {
            if (!showEmojiAction) {
                hideEmoji();
                mBottomChatLayout.setTranslationY(0);
            }
            isSoftInput = false;

        }
        //结束动作指令
        showEmojiAction = false;
    }


    public interface LiveRoomStatusListener {

        /**
         * 切换 视频/文档 区域回调
         *
         * @param state 窗口状态
         * @return 是否切换成功 true是切换成功
         */
        void switchVideoDoc(State state);

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
                handler.removeMessages(DELAY_HIDE_WHAT);
                //如果当前软件盘处于显示状态
                if (isSoftInput) {
                    showEmojiAction = true;
                    //1显示表情键盘
                    showEmoji();
                    //2隐藏软键盘
                    mImm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
                } else if (isEmojiShow) {  //表情键盘显示，软键盘没有显示，则直接显示软键盘
                    boolean b = mImm.showSoftInput(mInput, 0);
                    if (b) {
                        hideEmoji();
                    }
                } else { //软键盘和表情键盘都没有显示
                    //显示表情键盘
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
                //判断是否是软键盘谈起  如果不是软键盘  需要单独去将输入框滑动到下方
                if (isSoftInput) {
                    hideKeyboard();
                } else {
                    hideEmoji();
                    mBottomChatLayout.setTranslationY(0);
                }

                handler.removeMessages(DELAY_HIDE_WHAT);
                handler.sendEmptyMessageDelayed(DELAY_HIDE_WHAT, 3000);

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
                    mBarrageControl.setImageResource(R.drawable.barrage_off);
                    isBarrageOn = false;
                    dwLiveCoreHandler.setBarrageStatus(false);
                } else {
                    mBarrageControl.setImageResource(R.drawable.barrage_on);
                    isBarrageOn = true;
                    dwLiveCoreHandler.setBarrageStatus(true);
                }
            }
        });

//        onSoftInputChange();
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

    // 隐藏视频文档切换功能
    public void hideSwitchVideoDoc() {
        mLiveVideoDocSwitch.setVisibility(GONE);
    }

    public void clearChatInput() {
        mInput.setText("");
//        hideKeyboard();
    }

    public void hideKeyboard() {
        hideEmoji();
        mImm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
    }

    // 显示emoji
    public void showEmoji() {
        if (mEmojiGrid.getHeight() != softKeyHeight && softKeyHeight != 0) {
            ViewGroup.LayoutParams lp = mEmojiGrid.getLayoutParams();
            lp.height = softKeyHeight;
            mEmojiGrid.setLayoutParams(lp);
        }
        mEmojiGrid.setVisibility(View.VISIBLE);
        mEmoji.setImageResource(R.drawable.push_chat_emoji);
        isEmojiShow = true;
        float transY;
        if (softKeyHeight == 0) {
            transY = -mEmojiGrid.getHeight();
        } else {
            transY = -softKeyHeight;
        }
        mBottomChatLayout.setTranslationY(transY);
    }

    // 隐藏emoji
    public void hideEmoji() {
        mEmojiGrid.setVisibility(View.GONE);
        mEmoji.setImageResource(R.drawable.push_chat_emoji_normal);
        isEmojiShow = false;
    }

    //***************************************** 控制布局动画相关方法 ******************************
    private OnClickListener mRoomAnimatorListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            mBottomChatLayout.setTranslationY(0);
            handler.removeMessages(DELAY_HIDE_WHAT);
            hideKeyboard();
            toggleTopAndButtom();
        }
    };

    private void hide() {
        mTopLayout.clearAnimation();
        mBottomLayout.clearAnimation();
        if (mLiveFullScreen.getVisibility() == VISIBLE) {
            //根据改字段判断是否是全屏 不是全屏的话就隐藏
            mBottomLayout.setVisibility(GONE);
            mTopLayout.setVisibility(GONE);
        } else {
            //如果是全屏 还需要判断输入框是否显示  如果输入框显示 说明用户正在输入 不需要隐藏
            if (!mInput.hasFocus() || (!isSoftInput && !isEmojiShow)) {
                mBottomLayout.setVisibility(GONE);
                mTopLayout.setVisibility(GONE);
            }
        }

//        ObjectAnimator bottom_y = ObjectAnimator.ofFloat(mBottomLayout, "translationY", mBottomLayout.getHeight());
//        ObjectAnimator top_y = ObjectAnimator.ofFloat(mTopLayout, "translationY", -1 * mTopLayout.getHeight());
//        AnimatorSet animatorSet = new AnimatorSet();
//        animatorSet.play(top_y).with(bottom_y);
//
//        //播放动画的持续时间
//        animatorSet.setDuration(500);
//        animatorSet.start();
//
//        animatorSet.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//                mBottomLayout.setVisibility(GONE);
//                mTopLayout.setVisibility(GONE);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//            }
//        });
    }

    private void show() {
        mTopLayout.clearAnimation();
        mBottomLayout.clearAnimation();
        mTopLayout.setVisibility(VISIBLE);
        mBottomLayout.setVisibility(VISIBLE);
//        ObjectAnimator bottom_y = ObjectAnimator.ofFloat(mBottomLayout, "translationY", 0);
//        ObjectAnimator top_y = ObjectAnimator.ofFloat(mTopLayout, "translationY", 0);
//        AnimatorSet animatorSet = new AnimatorSet();
//        animatorSet.play(top_y).with(bottom_y);
//        //播放动画的持续时间
//        animatorSet.setDuration(500);
//        animatorSet.start();
//        animatorSet.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//            }
//        });
        handler.sendEmptyMessageDelayed(DELAY_HIDE_WHAT, 3000);
    }

    private void toggleTopAndButtom() {
        if (mTopLayout.isShown()) {
            hide();
        } else {
            show();
        }
    }
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
                performClick();
                return false;

        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public View getFullView() {
        return mLiveFullScreen;
    }

    /**
     * 开关弹幕 目前只针对首次进入直播间 服务器返回的弹幕开关数据的时候调用
     *
     * @param isBarrageOn
     */
    public void controlBarrageControl(boolean isBarrageOn) {
        this.isBarrageOn = isBarrageOn;
        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler == null) {
            return;
        }
        if (mBarrageControl != null) {
            if (isBarrageOn) {
                mBarrageControl.setImageResource(R.drawable.barrage_on);
                dwLiveCoreHandler.setBarrageStatus(true);
            } else {
                mBarrageControl.setImageResource(R.drawable.barrage_off);
                dwLiveCoreHandler.setBarrageStatus(false);
            }
        }
    }

    /**
     * 设置视频/文档切换的状态
     */
    public void setVideoDocSwitchStatus(State status) {
        this.viewState = status;
        setSwitchText(status);
    }


}
