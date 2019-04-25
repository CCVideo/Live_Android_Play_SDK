package com.bokecc.livemodule.live.chat;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.DWLiveChatListener;
import com.bokecc.livemodule.live.DWLiveCoreHandler;
import com.bokecc.livemodule.live.chat.adapter.EmojiAdapter;
import com.bokecc.livemodule.live.chat.adapter.LivePublicChatAdapter;
import com.bokecc.livemodule.live.chat.barrage.BarrageLayout;
import com.bokecc.livemodule.live.chat.module.ChatEntity;
import com.bokecc.livemodule.live.chat.util.BaseOnItemTouch;
import com.bokecc.livemodule.live.chat.util.EmojiUtil;
import com.bokecc.livemodule.live.chat.util.SoftKeyBoardState;
import com.bokecc.livemodule.utils.ChatImageUtils;
import com.bokecc.livemodule.view.BaseRelativeLayout;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.pojo.ChatMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 直播间聊天展示控件（公共聊天）
 */
public class LiveChatComponent extends BaseRelativeLayout implements DWLiveChatListener {

    private final static String TAG = "LiveChatComponent";

    private RecyclerView mChatList;
    private RelativeLayout mChatLayout;
    private EditText mInput;
    private ImageView mEmoji;
    private Button mChatSend;
    private GridView mEmojiGrid;

    // 软键盘是否显示
    private boolean isSoftInput = false;
    // emoji是否需要显示 emoji是否显示
    private boolean isEmoji = false, isEmojiShow = false;
    // 聊天是否显示
    private boolean isChat = false;

    // 公共聊天适配器
    private LivePublicChatAdapter mChatAdapter;

    // 是否加载过了历史聊天
    private boolean hasLoadedHistoryChat;

    // 软键盘监听
    private SoftKeyBoardState mSoftKeyBoardState;
    private InputMethodManager mImm;

    // 定义当前支持的最大的可输入的文字数量
    private short maxInput = 300;

    public LiveChatComponent(Context context) {
        super(context);
        initChat();
    }

    public LiveChatComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initChat();
    }

    public void initViews() {
        LayoutInflater.from(mContext).inflate(R.layout.live_portrait_chat_layout, this, true);
        mChatList = findViewById(R.id.chat_container);
        mChatLayout = findViewById(R.id.id_push_chat_layout);
        mInput = findViewById(R.id.id_push_chat_input);
        mEmoji = findViewById(R.id.id_push_chat_emoji);
        mEmojiGrid = findViewById(R.id.id_push_emoji_grid);
        mChatSend = findViewById(R.id.id_push_chat_send);

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
        mChatSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = mInput.getText().toString().trim();
                if (TextUtils.isEmpty(msg)) {
                    toastOnUiThread("聊天内容不能为空");
                    return;
                }
                DWLive.getInstance().sendPublicChatMsg(msg);
                clearChatInput();
            }
        });
    }

    public void initChat() {
        hasLoadedHistoryChat = false;
        mImm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mChatList.setLayoutManager(new LinearLayoutManager(mContext));
        mChatAdapter = new LivePublicChatAdapter(mContext);
        mChatList.setAdapter(mChatAdapter);
        mChatList.addOnItemTouchListener(new BaseOnItemTouch(mChatList, new com.bokecc.livemodule.live.chat.util.OnClickListener() {
            @Override
            public void onClick(RecyclerView.ViewHolder viewHolder) {
                int position = mChatList.getChildAdapterPosition(viewHolder.itemView);
                ChatEntity chatEntity = mChatAdapter.getChatEntities().get(position);
                // 判断聊天的角色，目前机制只支持和主讲、助教、主持人进行私聊
                // 主讲（publisher）、助教（teacher）、主持人（host）、学生或观众（student）、其他没有角色（unknow）
                if (chatEntity.getUserRole() == null || "student".equals(chatEntity.getUserRole()) || "unknow".equals(chatEntity.getUserRole())) {
                    Log.w(TAG, "只支持和主讲、助教、主持人进行私聊");
                    //toastOnUiThread("只支持和主讲、助教、主持人进行私聊");
                    return;
                }
                // 调用DWLiveCoreHandler.jump2PrivateChat方法，通知私聊控件，展示和此聊天相关的私聊内容列表
                DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
                if (dwLiveCoreHandler != null) {
                    dwLiveCoreHandler.jump2PrivateChat(chatEntity);
                }
            }
        }));

        mChatList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideKeyboard();
                return false;
            }
        });

        initChatView();

        DWLiveCoreHandler dwLiveCoreHandler = DWLiveCoreHandler.getInstance();
        if (dwLiveCoreHandler != null) {
            dwLiveCoreHandler.setDwLiveChatListener(this);
        }
    }


    public void initChatView() {
        mInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideEmoji();
                return false;
            }
        });
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
                    toastOnUiThread("字符数超过300字");
                    return;
                }
                if (position == EmojiUtil.imgs.length - 1) {
                    EmojiUtil.deleteInputOne(mInput);
                } else {
                    EmojiUtil.addEmoji(mContext, mInput, position);
                }
            }
        });

        onSoftInputChange();
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
                    } else {
                        hideChatLayout(); // 隐藏聊天操作区域
                    }
                } else {
                    hideEmoji();
                }
            }
        });
    }


    public void hideChatLayout() {
        if (isChat) {
            AlphaAnimation animation = new AlphaAnimation(0f, 1f);
            animation.setDuration(300L);
            mInput.setFocusableInTouchMode(false);
            mInput.clearFocus();
            mChatLayout.setVisibility(View.VISIBLE);
            isChat = false;
        }
    }

    /**
     * 显示emoji
     */
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

    /**
     * 隐藏emoji
     */
    public void hideEmoji() {
        if (isEmojiShow) { // 如果emoji显示
            mEmojiGrid.setVisibility(View.GONE);
            isEmojiShow = false; // 修改emoji显示标记
            mEmoji.setImageResource(R.drawable.push_chat_emoji_normal);
            if (!isSoftInput) {
                mChatList.setVisibility(View.VISIBLE);
            }
        }
    }

    public void clearChatInput() {
        mInput.setText("");
        hideKeyboard();
    }

    public void hideKeyboard() {
        hideEmoji();
        mImm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
    }

    public boolean onBackPressed() {
        if (isEmojiShow) {
            hideEmoji();
            hideChatLayout();
            return true;
        }
        return false;
    }

    public void addChatEntity(ChatEntity chatEntity) {
        mChatAdapter.add(chatEntity);
        if (mChatAdapter.getItemCount() - 1 > 0) {
            mChatList.smoothScrollToPosition(mChatAdapter.getItemCount() - 1);
        }
    }

    /*** 修改聊天内容的显示状态（0：显示  1：不显示）***/
    public void changeChatStatus(String status, ArrayList<String> chatIds) {
        mChatAdapter.changeStatus(status, chatIds);
    }


    private ChatEntity getChatEntity(ChatMessage msg) {
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setChatId(msg.getChatId());
        chatEntity.setUserId(msg.getUserId());
        chatEntity.setUserName(msg.getUserName());
        chatEntity.setPrivate(!msg.isPublic());
        chatEntity.setUserRole(msg.getUserRole());

        if (msg.getUserId().equals(DWLive.getInstance().getViewer().getId())) {
            chatEntity.setPublisher(true);
        } else {
            chatEntity.setPublisher(false);
        }

        chatEntity.setMsg(msg.getMessage());
        chatEntity.setTime(msg.getTime());
        chatEntity.setUserAvatar(msg.getAvatar());
        chatEntity.setStatus(msg.getStatus());
        return chatEntity;
    }

    /** 展示广播内容 **/
    private void showBroadcastMsg(final String msg) {
        if (msg == null || msg.isEmpty()) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 构建一个对象
                ChatEntity chatEntity = new ChatEntity();
                chatEntity.setUserId("");
                chatEntity.setUserName("");
                chatEntity.setPrivate(false);
                chatEntity.setPublisher(true);
                chatEntity.setMsg("系统消息: " + msg);
                chatEntity.setTime("");
                chatEntity.setStatus("0");  // 显示
                chatEntity.setUserAvatar("");
                addChatEntity(chatEntity);
            }
        });
    }

    //------------------------ 处理直播聊天回调信息 ------------------------------------

    // 收到历史聊天信息
    @Override
    public void onHistoryChatMessage(final ArrayList<ChatMessage> historyChats) {
        // 如果之前已经加载过了历史聊天信息，就不再接收
        if (hasLoadedHistoryChat) {
            return;
        }
        if (historyChats == null || historyChats.size() == 0) {
            return;
        }
        hasLoadedHistoryChat = true;
        // 注：历史聊天信息中 ChatMessage 的 currentTime = ""
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 将历史聊天信息添加到UI
                for (int i = 0; i < historyChats.size(); i++) {
                    if (barrageLayout != null) {
                        // 聊天支持发送图片，需要判断聊天内容是否为图片，如果不是图片，再添加到弹幕 && 聊天状态为显示
                        if (!ChatImageUtils.isImgChatMessage(historyChats.get(i).getMessage()) && "0".equals(historyChats.get(i).getStatus())) {
                            barrageLayout.addNewInfo(historyChats.get(i).getMessage());
                        }
                    }
                    addChatEntity(getChatEntity(historyChats.get(i)));
                }
            }
        });
    }

    @Override
    public void onPublicChatMessage(final ChatMessage msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (barrageLayout != null) {
                    // 聊天支持发送图片，需要判断聊天内容是否为图片，如果不是图片，再添加到弹幕
                    if (!ChatImageUtils.isImgChatMessage(msg.getMessage()) && "0".equals(msg.getStatus())) {
                        barrageLayout.addNewInfo(msg.getMessage());
                    }
                }
                addChatEntity(getChatEntity(msg));
            }
        });
    }

    /**
     * 收到聊天信息状态管理事件
     *
     * @param msgStatusJson 聊天信息状态管理事件json
     */
    @Override
    public void onChatMessageStatus(final String msgStatusJson) {
        if (TextUtils.isEmpty(msgStatusJson)) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = new JSONObject(msgStatusJson);
                    String status = jsonObject.getString("status");
                    JSONArray chatIdJson = jsonObject.getJSONArray("chatIds");
                    ArrayList<String> chatIds = new ArrayList<>();
                    for (int i = 0; i < chatIdJson.length(); i++) {
                        chatIds.add(chatIdJson.getString(i));
                    }
                    changeChatStatus(status, chatIds);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onSilenceUserChatMessage(final ChatMessage msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addChatEntity(getChatEntity(msg));
            }
        });
    }

    /**
     * 收到禁言事件
     *
     * @param mode 禁言类型 1：个人禁言  2：全员禁言
     */
    @Override
    public void onBanChat(int mode) {
        if (mode == 1) {
            Log.i(TAG, "个人被禁言");
        } else if (mode == 2) {
            Log.i(TAG, "全员被禁言");
        }
    }

    /**
     * 收到解除禁言事件
     *
     * @param mode 禁言类型 1：个人禁言  2：全员禁言
     */
    @Override
    public void onUnBanChat(int mode) {
        if (mode == 1) {
            Log.i(TAG, "解除个人禁言");
        } else if (mode == 2) {
            Log.i(TAG, "解除全员被禁言");
        }
    }

    /**
     * 收到广播信息
     */
    @Override
    public void onBroadcastMsg(String msg) {
        showBroadcastMsg(msg);
    }

    /***************************** 弹幕 ******************************/
    private BarrageLayout barrageLayout;

    /**
     * 设置弹幕组件
     */
    public void setBarrageLayout(BarrageLayout layout) {
        barrageLayout = layout;
    }
}
