package com.bokecc.livemodule.utils;

import android.graphics.Color;
import android.text.style.ForegroundColorSpan;

import com.bokecc.livemodule.R;

/**
 * 用户角色工具类（用于聊天、私聊、问答）
 */
public class UserRoleUtils {

    /**
     * 获取用户的角色头像
     */
    public static int getUserRoleAvatar(String userRole) {
        // 主讲（publisher）、助教（teacher）、主持人（host）、学生或观众（student）、其他没有角色（unknow）
        if ("publisher".equalsIgnoreCase(userRole)) {
            return R.mipmap.head_view_publisher;
        }
        if ("teacher".equalsIgnoreCase(userRole)) {
            return R.mipmap.head_view_teacher;
        }
        if ("host".equalsIgnoreCase(userRole)) {
            return R.mipmap.head_view_host;
        }
        if ("student".equalsIgnoreCase(userRole)) {
            return R.mipmap.head_view_student;
        }
        return R.mipmap.head_view_student;
    }

    /**
     * 获取角色对应的名字的颜色
     */
    public static ForegroundColorSpan getUserRoleColorSpan(String userRole) {
        // 主讲（publisher）、助教（teacher）、主持人（host）、学生或观众（student）、其他没有角色（unknow）
        if ("publisher".equalsIgnoreCase(userRole)) {
            return new ForegroundColorSpan(Color.parseColor("#12ad1a"));
        }
        if ("teacher".equalsIgnoreCase(userRole)) {
            return new ForegroundColorSpan(Color.parseColor("#12ad1a"));
        }
        if ("host".equalsIgnoreCase(userRole)) {
            return new ForegroundColorSpan(Color.parseColor("#12ad1a"));
        }
        if ("student".equalsIgnoreCase(userRole)) {
            return new ForegroundColorSpan(Color.parseColor("#79808b"));
        }
        return new ForegroundColorSpan(Color.parseColor("#79808b"));
    }

}
