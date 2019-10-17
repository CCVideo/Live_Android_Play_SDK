package com.bokecc.livemodule.utils;

public class TimeUtil {

    public static String getFormatTime(long oriTime) {
        oriTime = oriTime / 1000; // 单位为毫秒
        StringBuilder sb = new StringBuilder();

        sb.append(getFillZero(oriTime / 60));
        sb.append(":");
        sb.append(getFillZero(oriTime % 60));

        return sb.toString();
    }

    public static String getFillZero(long number) {

        if (number < 10) {
            return "0" + number;
        } else {
            return String.valueOf(number);
        }
    }
}
