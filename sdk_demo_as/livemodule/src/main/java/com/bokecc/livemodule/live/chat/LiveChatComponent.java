package com.bokecc.livemodule.live.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
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
import android.view.ViewGroup;
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
import com.bokecc.livemodule.live.chat.util.EmojiUtil;
import com.bokecc.livemodule.live.chat.window.BanChatPopup;
import com.bokecc.livemodule.utils.ChatImageUtils;
import com.bokecc.livemodule.view.BaseRelativeLayout;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.pojo.BroadCastMsg;
import com.bokecc.sdk.mobile.live.pojo.ChatMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 直播间聊天展示控件（公共聊天）
 */
public class LiveChatComponent extends BaseRelativeLayout implements DWLiveChatListener, KeyboardHeightObserver {

    private final static String TAG = "LiveChatComponent";

    private RecyclerView mChatList;
    private RelativeLayout mChatLayout;
    private EditText mInput;
    private ImageView mEmoji;
    private GridView mEmojiGrid;

    // 软键盘是否显示
    private boolean isSoftInput = false;
    // emoji是否需要显示
    private boolean isEmojiShow = false;
    // 聊天是否显示
    private boolean isChat = false;

    // 公共聊天适配器
    private LivePublicChatAdapter mChatAdapter;

    // 软键盘监听
    private InputMethodManager mImm;

    // 定义当前支持的最大的可输入的文字数量
    private short maxInput = 300;

    private OnChatComponentClickListener mChatComponentClickListener;
    //软键盘的高度
    private int softKeyHeight;
    private boolean showEmojiAction = false;
    private BanChatPopup banChatPopup;

    public void setOnChatComponentClickListener(OnChatComponentClickListener listener) {
        mChatComponentClickListener = listener;
    }


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
        Button mChatSend = findViewById(R.id.id_push_chat_send);

        mEmoji.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //如果当前软键盘处于显示状态
                if (isSoftInput) {
                    showEmojiAction = true;
                    //1显示表情键盘
                    showEmoji();
                    //2隐藏软键盘
                    mImm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
                } else if (isEmojiShow) {  //表情键盘显示，软键盘没有显示，则直接显示软键盘
                    mImm.showSoftInput(mInput, 0);
                } else { //软键盘和表情键盘都没有显示
                    //显示表情键盘
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
                mImm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    public void initChat() {
        mImm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mChatList.setLayoutManager(new LinearLayoutManager(mContext));
        mChatAdapter = new LivePublicChatAdapter(mContext);
        mChatList.setAdapter(mChatAdapter);

        mChatAdapter.setOnChatImageClickListener(new LivePublicChatAdapter.onChatComponentClickListener() {
            @Override
            public void onChatComponentClick(View view, Bundle bundle) {
                if (mChatComponentClickListener != null) {
                    mChatComponentClickListener.onClickChatComponent(bundle);
                }
            }
        });

        mChatAdapter.setOnItemClickListener(new LivePublicChatAdapter.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
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
        });

        mChatList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mChatLayout.setTranslationY(0);
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


    @SuppressLint("ClickableViewAccessibility")
    public void initChatView() {
        mInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                hideEmoji();
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
        mChatLayout.setTranslationY(transY);
    }

    /**
     * 隐藏emoji
     */
    public void hideEmoji() {
        mEmojiGrid.setVisibility(View.GONE);
        mEmoji.setImageResource(R.drawable.push_chat_emoji_normal);
        isEmojiShow = false;
    }

    public void clearChatInput() {
        mInput.setText("");
//        hideKeyboard();
    }

    public void hideKeyboard() {
        hideEmoji();
        mImm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
    }

    public boolean onBackPressed() {
        if (isEmojiShow) {
            mChatLayout.setTranslationY(0);
            hideEmoji();
            hideChatLayout();
            return true;
        }
        return false;
    }

    private void clearChatEntities() {
        if (mChatAdapter != null) {
            mChatAdapter.clearData();
        }
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

    public void delChatEntity(String chatId) {
        mChatAdapter.remove(chatId);
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

    /**
     * 展示广播内容
     *
     * @param msg
     */
    private void showBroadcastMsg(final BroadCastMsg msg) {
        if (msg == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 构建一个对象
                ChatEntity chatEntity = new ChatEntity();
                chatEntity.setChatId(msg.getId());
                chatEntity.setIsBroadcast(true);
                chatEntity.setUserId("");
                chatEntity.setUserName("");
                chatEntity.setPrivate(false);
                chatEntity.setPublisher(true);
                chatEntity.setMsg(String.format("系统消息: %s", msg.getContent()));
                chatEntity.setTime("");
                chatEntity.setStatus("0");  // 显示
                chatEntity.setUserAvatar("");
                addChatEntity(chatEntity);
            }
        });
    }

    //------------------------ 处理直播聊天回调信息 ------------------------------------

    // 获取历史消息集合
    private ArrayList<ChatEntity> list = new ArrayList<>();
    private boolean hasBroadcastMsg = false; // 主要为了防止数据重复

    // 收到历史广播信息，在收到历史消息之前处理
    @Override
    public void onHistoryBroadcastMsg(ArrayList<BroadCastMsg> msgs) {
        hasBroadcastMsg = true;
        list.clear();
        final ArrayList<ChatEntity> list1 = mChatAdapter.getChatEntities();
        for (final BroadCastMsg castMsg : msgs) {
            ChatEntity chatEntity = new ChatEntity();
            chatEntity.setChatId(castMsg.getId());
            chatEntity.setIsBroadcast(true);
            chatEntity.setUserId("");
            chatEntity.setUserName("");
            chatEntity.setPrivate(false);
            chatEntity.setPublisher(true);
            chatEntity.setMsg(String.format("系统消息: %s", castMsg.getContent()));
            chatEntity.setTime(String.valueOf(castMsg.getTime()));
            chatEntity.setStatus("0");  // 显示
            chatEntity.setUserAvatar("");
            if (list1.contains(chatEntity)) {
                continue;
            }
            list.add(chatEntity);
        }

    }

    // 收到历史聊天信息
    @Override
    public void onHistoryChatMessage(final ArrayList<ChatMessage> historyChats) {
        if (historyChats == null || historyChats.size() == 0) {
            return;
        }
        if (!hasBroadcastMsg) {
            list.clear();
        }
        hasBroadcastMsg = false;
        for (int i = 0; i < historyChats.size(); i++) {

            ChatEntity chatEntity = getChatEntity(historyChats.get(i));
            ArrayList<ChatEntity> chatEntities = mChatAdapter.getChatEntities();
            if (chatEntities.contains(chatEntity)) {
                continue;
            }
            if (barrageLayout != null) {
                // 聊天支持发送图片，需要判断聊天内容是否为图片，如果不是图片，再添加到弹幕 && 聊天状态为显示
                if (!ChatImageUtils.isImgChatMessage(historyChats.get(i).getMessage()) && "0".equals(historyChats.get(i).getStatus())) {
                    //判断横竖屏
                    if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        barrageLayout.addNewInfo(historyChats.get(i).getMessage());
                    } else if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        //竖屏不需要添加
                    }
                }
            }
            list.add(chatEntity);
        }
        // 排序 -->兼容历史服务器版本处理，服务器最新版不需要加try catch
        try {
            Collections.sort(list, new Comparator<ChatEntity>() {
                @Override
                public int compare(ChatEntity o1, ChatEntity o2) {
                    int time = Integer.parseInt(o1.getTime());
                    int time2 = Integer.parseInt(o2.getTime());
                    if (time == time2) {
                        return 0;
                    }
                    if (time > time2) {
                        return 1;
                    } else {
                        return -1;
                    }
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 注：历史聊天信息中 ChatMessage 的 currentTime = ""
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 将历史聊天信息添加到UI
//                 clearChatEntities();
                mChatAdapter.add(list);

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
                    if (!ChatImageUtils.isImgChatMessage(msg.getMessage()) && "0".equals(msg.getStatus()) && getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        barrageLayout.addNewInfo(msg.getMessage());
                    }
                }
                addChatEntity(getChatEntity(msg));
            }
        });
    }

    @Override
    public void onBanDeleteChat(final String userId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mChatAdapter != null) {
                    mChatAdapter.banDeleteChat(userId);
                }
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
    public void onBanChat(final int mode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (banChatPopup == null) {
                    banChatPopup = new BanChatPopup(getContext());
                }
                if (banChatPopup.isShowing()) {
                    banChatPopup.onDestroy();
                }
                if (mode == 1) {
                    banChatPopup.banChat("个人被禁言");
                } else if (mode == 2) {
                    banChatPopup.banChat("全员被禁言");
                }
                banChatPopup.show(rootView);
            }
        });

    }

    private View rootView;

    public void setPopView(View rootView) {
        this.rootView = rootView;
    }

    /**
     * 收到解除禁言事件
     *
     * @param mode 禁言类型 1：个人禁言  2：全员禁言
     */
    @Override
    public void onUnBanChat(final int mode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (banChatPopup == null) {
                    banChatPopup = new BanChatPopup(getContext());
                }
                if (banChatPopup.isShowing()) {
                    banChatPopup.onDestroy();
                }
                if (mode == 1) {
                    banChatPopup.banChat("解除个人禁言");
                } else if (mode == 2) {
                    banChatPopup.banChat("解除全员被禁言");
                }
                banChatPopup.show(rootView);
            }
        });

    }


    @Deprecated
    @Override
    public void onBroadcastMsg(String msg) {
        // showBroadcastMsg(msg);
    }


    /**
     * 收到广播信息
     */
    @Override
    public void onBroadcastMsg(BroadCastMsg msg) {
        showBroadcastMsg(msg);
    }


    /*删除广播消息*/
    @Override
    public void onBroadcastMsgDel(final String id) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                delChatEntity(id);
            }
        });

    }

    /***************************** 弹幕 ******************************/
    private BarrageLayout barrageLayout;

    /**
     * 设置弹幕组件
     */
    public void setBarrageLayout(BarrageLayout layout) {
        barrageLayout = layout;
    }

    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {
        if (height > 10) {
            isSoftInput = true;
            softKeyHeight = height;
            mChatLayout.setTranslationY(-softKeyHeight);
            mEmoji.setImageResource(R.drawable.push_chat_emoji_normal);
            isEmojiShow = false;
        } else {
            if (!showEmojiAction) {
                mChatLayout.setTranslationY(0);
                hideEmoji();
            }
            isSoftInput = false;

        }
        //结束动作指令
        showEmojiAction = false;
    }
}
