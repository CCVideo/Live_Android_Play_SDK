package com.bokecc.livemodule.replay.chat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.chat.module.ChatEntity;
import com.bokecc.livemodule.live.chat.util.EmojiUtil;
import com.bokecc.livemodule.utils.ChatImageUtils;
import com.bokecc.livemodule.utils.UserRoleUtils;
import com.bokecc.livemodule.view.HeadView;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.pojo.Viewer;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bokecc.livemodule.live.chat.adapter.LivePublicChatAdapter.regular;

public class ReplayChatAdapter extends RecyclerView.Adapter<ReplayChatAdapter.ChatViewHolder> {


    private final Pattern pattern;

    public interface OnChatComponentClickListener {
        void onChatComponentClick(View view, Bundle bundle);
    }

    private Context mContext;
    private List<ChatEntity> mChatEntities;
    private LayoutInflater mInflater;
    private String selfId;

    private OnChatComponentClickListener mChatcomponentClickListener;

    public static final String CONTENT_IMAGE_COMPONENT = "content_image";
    public static final String CONTENT_ULR_COMPONET = "content_url";


    public void setOnChatcomponentClickListener(OnChatComponentClickListener listener){
        mChatcomponentClickListener = listener;
    }


    public ReplayChatAdapter(Context context) {
        mChatEntities = new LinkedList<>();
        mContext = context;
        mInflater = LayoutInflater.from(context);
        Viewer viewer = DWLiveReplay.getInstance().getViewer();
        if (viewer == null) {
            selfId = "";
        } else {
            selfId = viewer.getId();
        }
        pattern = Pattern.compile(regular, Pattern.CASE_INSENSITIVE);
    }

    /**
     * 清空聊天数据
     */
    public void clearChatData() {
        mChatEntities.clear();
        notifyDataSetChanged();
    }

    /**
     * 添加数据，用于回放的添加
     */
    public void add(ArrayList<ChatEntity> chatEntities) {
        this.mChatEntities.addAll(chatEntities);
        notifyDataSetChanged();
    }

    /**
     * 添加数据，用于回放的添加
     */
    public void append(ArrayList<ChatEntity> mChatEntities) {
        this.mChatEntities.addAll(mChatEntities);
        // 当消息达到300条的时候，移除最早的消息
        while (this.mChatEntities.size() > 300) {
            this.mChatEntities.remove(0);
        }
        notifyDataSetChanged();
    }

    // 获取当前的聊天数量
    public int getChatListSize() {
        return mChatEntities == null ? 0 : mChatEntities.size();
    }

    /**
     * 添加数据
     */
    public void add(ChatEntity chatEntity) {
        mChatEntities.add(chatEntity);
        if (mChatEntities.size() > 300) { // 当消息达到300条的时候，移除最早的消息
            mChatEntities.remove(0);
        }
        notifyDataSetChanged();
    }

    @Override
    public ReplayChatAdapter.ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if (viewType == selfType) {
            // 展示自己发出去的公聊
            itemView = mInflater.inflate(R.layout.live_portrait_chat_single, parent, false);
            return new ReplayChatAdapter.ChatViewHolder(itemView);
        } else if (viewType == otherType) {
            // 展示收到的别人发出去的公聊
            itemView = mInflater.inflate(R.layout.live_portrait_chat_single, parent, false);
            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true;
                }
            });
            return new ReplayChatAdapter.ChatViewHolder(itemView);
        } else {
            // 展示收到的广播消息
            itemView = mInflater.inflate(R.layout.live_protrait_system_broadcast, parent, false);
            return new ReplayChatAdapter.ChatViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(final ReplayChatAdapter.ChatViewHolder holder, int position) {
        final ChatEntity chatEntity = mChatEntities.get(position);

        // 判断是是否是广播，如果是，就展示广播信息
        if (chatEntity.getUserId().isEmpty() && chatEntity.getUserName().isEmpty() && !chatEntity.isPrivate()
                && chatEntity.isPublisher() && chatEntity.getTime().isEmpty() && chatEntity.getUserAvatar().isEmpty()) {
            // 展示广播信息
            holder.mBroadcast.setText(chatEntity.getMsg());
        } else {

            // 展示聊天信息

            // 1. 判断聊天内容是否是图片，如果是就提取出图片链接地址，然后展示图片
            if (ChatImageUtils.isImgChatMessage(chatEntity.getMsg())) {
                String msg = chatEntity.getUserName() + ": ";
                SpannableString ss = new SpannableString(msg);
                ss.setSpan(UserRoleUtils.getUserRoleColorSpan(chatEntity.getUserRole()), 0, (chatEntity.getUserName() + ":").length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.mContent.setText(EmojiUtil.parseFaceMsg(mContext, ss));
                holder.mContent.setVisibility(View.VISIBLE);
                holder.mChatImg.setVisibility(View.VISIBLE);
                if (ChatImageUtils.isGifImgUrl(ChatImageUtils.getImgUrlFromChatMessage(chatEntity.getMsg()))) {
                    Glide.with(mContext).load(ChatImageUtils.getImgUrlFromChatMessage(chatEntity.getMsg())).asGif().into(holder.mChatImg);
                } else {
                    Glide.with(mContext).load(ChatImageUtils.getImgUrlFromChatMessage(chatEntity.getMsg())).asBitmap().into(holder.mChatImg);
                }

                holder.mChatImg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mChatcomponentClickListener != null) {
                            Bundle bundle = new Bundle();
                            bundle.putString("type",CONTENT_IMAGE_COMPONENT);
                            bundle.putString("url", ChatImageUtils.getImgUrlFromChatMessage(chatEntity.getMsg()));
                            mChatcomponentClickListener.onChatComponentClick(holder.mChatImg,bundle);
                        }
                    }
                });

            } else {
                String msg = chatEntity.getUserName() + ": " + chatEntity.getMsg();

                String url = null;
                int start = -1;
                int end = -1;
                Matcher matcher = pattern.matcher(msg);
                if (matcher.find()) {
                    start = matcher.start();
                    end = matcher.end();
                    url = matcher.group();
                }

                SpannableString ss = new SpannableString(msg);
                ss.setSpan(UserRoleUtils.getUserRoleColorSpan(chatEntity.getUserRole()),
                        0, (chatEntity.getUserName() + ":").length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ss.setSpan(new ForegroundColorSpan(Color.parseColor("#1E1F21")),
                        (chatEntity.getUserName() + ":").length() + 1, msg.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                if (url != null) {
                    final String finalUrl = url;
                    ss.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            if(mChatcomponentClickListener != null){
                                Bundle bundle = new Bundle();
                                bundle.putString("type",CONTENT_ULR_COMPONET);
                                bundle.putString("url",finalUrl);
                                mChatcomponentClickListener.onChatComponentClick(widget,bundle);
                            }
                        }
                        //去除连接下划线
                        @Override
                        public void updateDrawState(TextPaint ds) {
                            ds.setUnderlineText(false);
                        }

                    },start,end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    ss.setSpan(new ForegroundColorSpan(Color.parseColor("#2292DD")), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.mContent.setMovementMethod(LinkMovementMethod.getInstance());
                }



                holder.mContent.setText(EmojiUtil.parseFaceMsg(mContext, ss));
                holder.mContent.setVisibility(View.VISIBLE);
                holder.mChatImg.setVisibility(View.GONE);
            }

            //  如果头像字段有数据就展示头像，否则按照角色展示角色头像
            if (TextUtils.isEmpty(chatEntity.getUserAvatar())) {
                holder.mUserHeadView.setImageResource(UserRoleUtils.getUserRoleAvatar(chatEntity.getUserRole()));
            } else {
                Glide.with(mContext).load(chatEntity.getUserAvatar()).placeholder(R.drawable.user_head_icon).into(holder.mUserHeadView);
            }
        }
    }

    private int otherType = 0; // 别人发送的聊天
    private int selfType = 1; // 自己发送的聊天
    private int systemType = 2;  // 系统广播

    @Override
    public int getItemViewType(int position) {

        ChatEntity chat = mChatEntities.get(position);

        // 系统广播 --- 只有 chatEntity.getMsg() 不为空
        if (chat.getUserId().isEmpty() && chat.getUserName().isEmpty() && !chat.isPrivate() && chat.isPublisher()
                && chat.getTime().isEmpty() && chat.getUserAvatar().isEmpty()) {
            return systemType;
        }

        // 聊天
        if (chat.getUserId().equals(selfId)) {
            return selfType; // 自己发出去的
        } else {
            return otherType; // 收到别人的
        }
    }

    @Override
    public int getItemCount() {
        return mChatEntities == null ? 0 : mChatEntities.size();
    }

    final class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView mContent;

        TextView mBroadcast;

        HeadView mUserHeadView;

        ImageView mChatImg;

        ChatViewHolder(View itemView) {
            super(itemView);
            mContent = itemView.findViewById(R.id.pc_chat_single_msg);
            mBroadcast = itemView.findViewById(R.id.pc_chat_system_broadcast);
            mUserHeadView = itemView.findViewById(R.id.id_private_head);
            mChatImg = itemView.findViewById(R.id.pc_chat_img);
        }
    }
}
