package com.bokecc.livemodule.live.chat.adapter;


import android.content.Context;
import android.widget.ImageView;

import com.bokecc.livemodule.R;
import com.bokecc.livemodule.live.chat.util.ViewHolder;

public class EmojiAdapter extends CommonArrayAdapter<Integer> {

    public EmojiAdapter(Context context) {
        super(context);
    }

    @Override
    protected void onBindViewHolder(ViewHolder viewHolder, int position) {
        ImageView emoji = viewHolder.getView(R.id.id_item_emoji);
        emoji.setImageResource(datas[position]);
    }

    @Override
    protected int getItemViewId() {
        return R.layout.item_emoji;
    }
}
