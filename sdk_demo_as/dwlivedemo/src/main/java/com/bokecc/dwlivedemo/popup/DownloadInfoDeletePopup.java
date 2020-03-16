package com.bokecc.dwlivedemo.popup;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;

import com.bokecc.dwlivedemo.R;
import com.bokecc.dwlivedemo.base.BasePopupWindow;
import com.bokecc.livemodule.utils.PopupAnimUtil;

/**
 * 作者 ${郭鹏飞}.<br/>
 */

public class DownloadInfoDeletePopup extends BasePopupWindow {
    public DownloadInfoDeletePopup(Context context) {
        super(context);
    }

    private ConfirmListener mListener;
    private TextView cancel;
    private TextView deleteItem;

    @Override
    protected void onViewCreated() {
        cancel = findViewById(R.id.id_cancel);
        deleteItem = findViewById(R.id.id_delete_item);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener != null){
                    mListener.onConfirmClick();
                }
                mListener = null;
                dismiss();
            }
        });
    }


    public void setListener(ConfirmListener listener){
        mListener = listener;
    }

    @Override
    protected int getContentView() {
        return R.layout.delete_download_layout;
    }

    @Override
    protected Animation getEnterAnimation() {
        return PopupAnimUtil.getDefScaleEnterAnim();
    }

    @Override
    protected Animation getExitAnimation() {
        return PopupAnimUtil.getDefScaleExitAnim();
    }

    public interface ConfirmListener{

        void onConfirmClick();

    }
}


