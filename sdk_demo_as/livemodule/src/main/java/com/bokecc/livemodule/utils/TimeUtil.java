package com.bokecc.livemodule.utils;

import android.content.Context;

import com.bokecc.livemodule.R;

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


    /**
     * 将毫秒转换为年月日时分秒
     */
    public static String getYearMonthDayHourMinuteSecond(Context mContext, long timeMillis) {
        int second = (int) (timeMillis % 60);// 秒
        long totalMinutes = timeMillis / 60;
        int minute = (int) (totalMinutes % 60);// 分
        long totalHours = totalMinutes / 60;
        int hour = (int) (totalHours % 24);// 时
        int day = (int) (totalHours / 24);
        if (day > 0) {
            return mContext.getString(R.string.string_utils_count_down_day_hour_minute_second,
                    day, hour, minute, second);
        } else if (hour > 0) {
            return mContext.getString(R.string.string_utils_count_down_hour_minute_second,
                    hour, minute, second);
        } else if (minute > 0) {
            return mContext.getString(
                    R.string.string_utils_count_down_minute_second, minute,
                    second);
        } else {
            return mContext.getString(R.string.string_utils_count_down_second,
                    second);
        }
    }
}
