package com.bokecc.livemodule.utils.net;

import android.net.TrafficStats;
import android.util.Log;

import java.text.DecimalFormat;

public class NetSpeed {
    private static final String TAG = NetSpeed.class.getSimpleName();
    private long rxtxTotal = 0;
    private DecimalFormat showFloatFormat = new DecimalFormat("0.00");
    public String getNetSpeed(int uid) {
        long tempSum = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
        long rxtxLast = tempSum - rxtxTotal;
        double totalSpeed = rxtxLast * 1000 / 2000d;
        if (rxtxTotal == 0){
            rxtxTotal = tempSum;
            return showSpeed(0);
        }else{
            rxtxTotal = tempSum;
            return showSpeed(totalSpeed);
        }
    }


    private String showSpeed(double speed) {
        String speedString;
        if (speed >= 1048576d) {
            speedString = showFloatFormat.format(speed / 1048576d) + "MB/s";
        } else {
            speedString = showFloatFormat.format(speed / 1024d) + "KB/s";
        }
        return speedString;
    }
}
