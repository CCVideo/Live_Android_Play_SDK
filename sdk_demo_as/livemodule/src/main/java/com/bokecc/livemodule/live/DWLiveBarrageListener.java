package com.bokecc.livemodule.live;

/**
 * 直播弹幕控制回调监听
 */
public interface DWLiveBarrageListener {

    /**
     * 收到弹幕开启事件
     */
    void onBarrageOn();

    /**
     * 收到弹幕关闭事件
     */
    void onBarrageOff();
}
