package com.bokecc.dwlivedemo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_PLAY_PAUSE = "hd_notification_play_pause";
    public static final String ACTION_DESTROY = "hd_notification_destroy";
    public static NotifiCallBack notifiCallBack;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals("hd_notification_play_pause")){
            if (notifiCallBack!=null){
                notifiCallBack.clickPlay();
            }
        }else if(action.equals("hd_notification_destroy")){
            if (notifiCallBack!=null){
                notifiCallBack.clickExit();
            }
        }
    }
    public interface NotifiCallBack{
        void clickPlay();
        void clickExit();
    }
}
