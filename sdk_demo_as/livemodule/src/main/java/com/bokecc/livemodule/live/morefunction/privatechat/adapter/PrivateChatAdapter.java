package com.bokecc.livemodule.live.morefunction.privatechat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.chat.module.ChatEntity;
import com.bokecc.livemodule.live.chat.util.EmojiUtil;
import com.bokecc.livemodule.utils.UserRoleUtils;
import com.bokecc.livemodule.utils.ChatImageUtils;
import com.bokecc.livemodule.view.HeadView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * 私聊内容适配器
 */
public class PrivateChatAdapter extends RecyclerView.Adapter<PrivateChatAdapter.PrivateChatViewHolder> {

    private static final int ITEM_TYPE_COME = 0;
    private static final int ITEM_TYPE_SELF = 1;

    private Context mContext;
    private ArrayList<ChatEntity> mChatEntities;
    private LayoutInflater mInflater;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return true;
        }
    };

    public PrivateChatAdapter(Context context) {
        mContext = context;
        mChatEntities = new ArrayList<>();
        mInflater = LayoutInflater.from(context);
    }

    /**
     * 添加数据
     */
    public void setDatas(ArrayList<ChatEntity> chatEntities) {
        mChatEntities = chatEntities;
        notifyDataSetChanged();
    }

    public void add(ChatEntity chatEntity) {
        mChatEntities.add(chatEntity);
        notifyDataSetChanged();
    }

    @Override
    public PrivateChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == ITEM_TYPE_COME) {
            itemView = mInflater.inflate(R.layout.private_come, parent, false);
        } else {
            itemView = mInflater.inflate(R.layout.private_self, parent, false);
        }
        return new PrivateChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PrivateChatViewHolder holder, int position) {
        ChatEntity chatEntity = mChatEntities.get(position);
        if (ChatImageUtils.isImgChatMessage(chatEntity.getMsg())) {
            holder.mContent.setText("");
            holder.mContent.setVisibility(View.GONE);
            holder.mChatImg.setVisibility(View.VISIBLE);
            if (ChatImageUtils.isGifImgUrl(ChatImageUtils.getImgUrlFromChatMessage(chatEntity.getMsg()))) {
                Glide.with(mContext).load(ChatImageUtils.getImgUrlFromChatMessage(chatEntity.getMsg())).asGif().into(holder.mChatImg);
            } else {
                Glide.with(mContext).load(ChatImageUtils.getImgUrlFromChatMessage(chatEntity.getMsg())).asBitmap().into(holder.mChatImg);
            }
        } else {
            SpannableString ss = new SpannableString(chatEntity.getMsg());
            holder.mContent.setText(EmojiUtil.parseFaceMsg(mContext, ss));
            holder.mContent.setVisibility(View.VISIBLE);
            holder.mChatImg.setVisibility(View.GONE);
        }
        //  如果头像字段有数据就展示头像，否则按照角色展示角色头像
        if (TextUtils.isEmpty(chatEntity.getUserAvatar())) {
            holder.mHeadIcon.setImageResource(UserRoleUtils.getUserRoleAvatar(chatEntity.getUserRole()));
        } else {
            Glide.with(mContext).load(chatEntity.getUserAvatar()).placeholder(R.drawable.user_head_icon).into(holder.mHeadIcon);
        }
        holder.mContent.setOnTouchListener(mTouchListener);
        holder.mHeadIcon.setOnTouchListener(mTouchListener);
    }

    @Override
    public int getItemCount() {
        return mChatEntities.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mChatEntities.get(position).isPublisher()) {
            return ITEM_TYPE_SELF;
        } else {
            return ITEM_TYPE_COME;
        }
    }

    public final class PrivateChatViewHolder extends RecyclerView.ViewHolder {

        public HeadView mHeadIcon;
        TextView mContent;
        ImageView mChatImg;

        PrivateChatViewHolder(View itemView) {
            super(itemView);
            mHeadIcon = itemView.findViewById(R.id.id_private_head);
            mContent = itemView.findViewById(R.id.id_private_msg);
            mChatImg = itemView.findViewById(R.id.pc_chat_img);
        }
    }

}