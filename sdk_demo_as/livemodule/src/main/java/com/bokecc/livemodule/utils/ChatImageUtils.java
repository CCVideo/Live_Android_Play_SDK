package com.bokecc.livemodule.utils;

import android.text.TextUtils;

/**
 * 聊天时图片处理工具类
 */
public class ChatImageUtils {

    // 目前聊天支持的图片类型为：jpg, png, gif, jpeg, bmp。

    /**
     * 判断公聊内容是否是图片内容
     */
    public static boolean isImgChatMessage(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        if (text.startsWith("[img_http") && text.endsWith("]")) {
            return true;
        }
        return false;
    }

    /**
     * 获取聊天内容的图片url
     */
    public static String getImgUrlFromChatMessage(String text) {
        // 检测内容是否为空
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        // 截取图片url
        text = text.substring(1, text.length() -1);
        // 去掉前缀，并将协议切换为https（兼容Android 9）
        return text.replaceAll("img_", "").replaceAll("http", "https");
    }

    /**
     * 判断是否是动图（gif格式的图片）
     */
    public static boolean isGifImgUrl(String text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        return text.toLowerCase().endsWith("gif");
    }
}
