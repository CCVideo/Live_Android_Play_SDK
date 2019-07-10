package com.bokecc.livemodule.replay.chat;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.chat.module.ChatEntity;
import com.bokecc.livemodule.replay.DWReplayChatListener;
import com.bokecc.livemodule.replay.DWReplayCoreHandler;
import com.bokecc.livemodule.replay.chat.adapter.ReplayChatAdapter;
import com.bokecc.livemodule.replay.room.ReplayRoomLayout;
import com.bokecc.sdk.mobile.live.replay.DWReplayPlayer;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayChatMsg;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

/**
 * 回放聊天控件
 */
public class ReplayChatComponent extends RelativeLayout implements DWReplayChatListener, ReplayRoomLayout.SeekListener {

    // 回放聊天是否和视频播放时间同步展示
    private final static boolean REPLAY_CHAT_FOLLOW_TIME = true;

    Context mContext;
    RecyclerView mChatRecyclerView;

    int mChatInfoLength;

    private ArrayList<ChatEntity> tempChatEntities;

    public ReplayChatComponent(Context context) {
        super(context);
        mContext = context;
        initViews();
        mChatInfoLength = 0;
    }

    public ReplayChatComponent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initViews();
        mChatInfoLength = 0;
    }

    public void initViews() {
        LayoutInflater.from(mContext).inflate(R.layout.replay_portrait_chat_layout, this, true);
        mChatRecyclerView = findViewById(R.id.chat_container);
        initChat();
    }

    ReplayChatAdapter mChatAdapter;

    public void initChat() {

        tempChatEntities = new ArrayList<>();

        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mChatAdapter = new ReplayChatAdapter(mContext);
        mChatRecyclerView.setAdapter(mChatAdapter);

        // 设置监听
        DWReplayCoreHandler dwReplayCoreHandler = DWReplayCoreHandler.getInstance();
        if (dwReplayCoreHandler != null) {
            dwReplayCoreHandler.setReplayChatListener(this);
        }

        if (REPLAY_CHAT_FOLLOW_TIME) {
            lastChatTime = 0;
            startTimerTask();
        }
    }

    /**
     * 清空聊天数据
     */
    public void clearChatEntities() {
        mChatAdapter.clearChatData();
    }

    /**
     * 回放的聊天添加
     *
     * @param chatEntities
     */
    public void addChatEntities(ArrayList<ChatEntity> chatEntities) {
        mChatAdapter.add(chatEntities);
    }

    /**
     * 回放的聊天添加 (追加)
     *
     * @param chatEntities
     */
    public void appendChatEntities(ArrayList<ChatEntity> chatEntities) {
        mChatAdapter.append(chatEntities);
        // 如果只是需要追加数据，而不滑动，可以注释掉下面的调用
        mChatRecyclerView.smoothScrollToPosition(mChatAdapter.getChatListSize() - 1);
    }

    private ArrayList<ChatEntity> mChatEntities = new ArrayList<>();

    private ChatEntity getReplayChatEntity(ReplayChatMsg msg) {
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setUserId(msg.getUserId());
        chatEntity.setUserName(msg.getUserName());
        chatEntity.setUserRole(msg.getUserRole());
        chatEntity.setPrivate(false);
        chatEntity.setPublisher(true);
        chatEntity.setMsg(msg.getContent());
        chatEntity.setTime(String.valueOf(msg.getTime()));
        chatEntity.setUserAvatar(msg.getAvatar());
        return chatEntity;
    }

    /**
     * 聊天信息
     *
     * @param replayChatMsgs 聊天信息
     */
    @Override
    public void onChatMessage(TreeSet<ReplayChatMsg> replayChatMsgs) {
        ArrayList<ChatEntity> chatEntities = new ArrayList<>();

        for (ReplayChatMsg msg : replayChatMsgs) {
            // 判断聊天信息的状态 0：显示  1：不显示
            if ("0".equals(msg.getStatus())) {
                chatEntities.add(getReplayChatEntity(msg));
            }
        }

        mChatEntities = chatEntities;

        // 如果不随时间轴展示数据，就将所有数据加载
        if (!REPLAY_CHAT_FOLLOW_TIME) {
            mChatRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    addChatEntities(mChatEntities);
                }
            });
        }
    }

    // --------------------- Demo 随时间展示聊天 定时器  --------------------

    Timer timer = new Timer();
    TimerTask timerTask;
    int lastChatTime = 0;  //  单位：秒
    private int currentPos = 0;

    // 开始计时器，随时间展示聊天
    private void startTimerTask() {
        stopTimerTask();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                // 回放的聊天内容随时间轴推进展示
                if (REPLAY_CHAT_FOLLOW_TIME) {
                    DWReplayCoreHandler replayCoreHandler = DWReplayCoreHandler.getInstance();
                    // 判断是否为空
                    if (replayCoreHandler == null || replayCoreHandler.getPlayer() == null) {
                        return;
                    }
                    // 获取当前的player
                    final DWReplayPlayer player = replayCoreHandler.getPlayer();
                    if (player != null && player.isPlaying()) {

                        int time = Math.round(player.getCurrentPosition() / 1000);
                        //进度条回拉
                        if (time < lastChatTime) {
                            for (ChatEntity entity : mChatEntities) {
                                if (!TextUtils.isEmpty(entity.getTime()) && time >= Integer.valueOf(entity.getTime())) {
                                    tempChatEntities.add(entity);
                                }
                            }
                            currentPos = 0;
                            lastChatTime = time;
                            if (mChatRecyclerView != null) {
                                mChatRecyclerView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        clearChatEntities();
                                        addChatEntities(tempChatEntities);
                                        int chatDataSize = mChatAdapter.getChatListSize();
                                        if (chatDataSize > 0) {
                                            mChatRecyclerView.smoothScrollToPosition(chatDataSize - 1);
                                        }
                                    }
                                });
                            }
                        } else {
                            tempChatEntities.clear();
                            for (int i = currentPos; i < mChatEntities.size(); i++) {
                                ChatEntity entity = mChatEntities.get(i);
                                if (!TextUtils.isEmpty(entity.getTime()) && Integer.valueOf(entity.getTime()) <= time) {
                                    tempChatEntities.add(entity);
                                }
                            }
//                            for (ChatEntity entity : mChatEntities) {
//                                if (!TextUtils.isEmpty(entity.getTime()) && time >= Integer.valueOf(entity.getTime()) && lastChatTime <= Integer.valueOf(entity.getTime())) {
//                                    tempChatEntities.add(entity);
//                                }
//                            }
                            currentPos += tempChatEntities.size();
                            lastChatTime = time;
                            if (mChatRecyclerView != null && tempChatEntities.size() > 0) {
                                mChatRecyclerView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        appendChatEntities(tempChatEntities);
                                    }
                                });
                            }
                        }

                    }
                }
            }
        };
        // 推荐2秒刷新一次聊天数据
        timer.schedule(timerTask, 0, 2 * 1000);
    }

    public void stopTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    public void release() {
        startTimerTask();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onBackSeek(final long progress) {
        lastChatTime = (int) (progress/1000);
        currentPos = 0;
        mChatRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                clearChatEntities();
            }
        });
    }

}


