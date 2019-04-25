package com.bokecc.livemodule.live.morefunction.privatechat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.chat.util.EmojiUtil;
import com.bokecc.livemodule.live.chat.module.PrivateUser;
import com.bokecc.livemodule.utils.UserRoleUtils;
import com.bokecc.livemodule.utils.ChatImageUtils;
import com.bokecc.livemodule.view.HeadView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * 私聊用户适配器
 */
public class PrivateUserAdapter extends RecyclerView.Adapter<PrivateUserAdapter.PrivateUserViewHolder> {

    private Context mContext;
    private ArrayList<PrivateUser> mPrivateUsers;
    private LayoutInflater mInflater;

    public PrivateUserAdapter(Context context) {
        mPrivateUsers = new ArrayList<>();
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    /**
     * 添加数据
     */
    public void add(PrivateUser privateUser) {
        int index = -1;
        PrivateUser existPrivateUser = null;
        for (PrivateUser user : mPrivateUsers) {
            if (user.getId().equals(privateUser.getId())) {
                index = mPrivateUsers.indexOf(user);
                existPrivateUser = user;
                break;
            }
        }
        if (index != -1) {
            existPrivateUser.setMsg(privateUser.getMsg());
            existPrivateUser.setRead(privateUser.isRead());
            existPrivateUser.setTime(privateUser.getTime());
            mPrivateUsers.remove(index);
            mPrivateUsers.add(0, existPrivateUser);
        } else {
            mPrivateUsers.add(0, privateUser);
        }
        notifyDataSetChanged();
    }

    public ArrayList<PrivateUser> getPrivateUsers() {
        return mPrivateUsers;
    }

    @Override
    public PrivateUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PrivateUserViewHolder(mInflater.inflate(R.layout.private_user_item, parent, false));
    }

    @Override
    public void onBindViewHolder(PrivateUserViewHolder holder, int position) {
        PrivateUser privateUser = mPrivateUsers.get(position);
        holder.mUserName.setText(privateUser.getName());
        // 检测如果是聊天图片，就显示"[图片]"，防止出现url链接，影响用户体验
        if (ChatImageUtils.isImgChatMessage(privateUser.getMsg())) {
            holder.mContent.setText("[图片]");
        } else {
            SpannableString ss = new SpannableString(privateUser.getMsg());
            holder.mContent.setText(EmojiUtil.parseFaceMsg(mContext, ss));
        }

        holder.mTime.setText(privateUser.getTime());
        //  如果头像字段有数据就展示头像，否则按照角色展示角色头像
        if (TextUtils.isEmpty(privateUser.getAvatar())) {
            holder.mHeadIcon.setImageResource(UserRoleUtils.getUserRoleAvatar(privateUser.getRole()));
        } else {
            Glide.with(mContext).load(privateUser.getAvatar()).placeholder(R.drawable.user_head_icon).into(holder.mHeadIcon);
        }
        if (privateUser.isRead()) {
            holder.mHeadIcon.clearNew();
        } else {
            holder.mHeadIcon.updateNew();
        }
    }

    @Override
    public int getItemCount() {
        return mPrivateUsers.size();
    }

    final class PrivateUserViewHolder extends RecyclerView.ViewHolder {

        HeadView mHeadIcon;
        TextView mTime;
        TextView mUserName;
        TextView mContent;

        PrivateUserViewHolder(View itemView) {
            super(itemView);
            mHeadIcon = itemView.findViewById(R.id.id_private_user_head);
            mTime = itemView.findViewById(R.id.id_private_time);
            mUserName = itemView.findViewById(R.id.id_private_user_name);
            mContent = itemView.findViewById(R.id.id_private_msg);
        }
    }

}

