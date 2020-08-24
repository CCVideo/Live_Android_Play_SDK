package com.bokecc.dwlivedemo.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


/**
 * Created by dds on 2016/6/24.
 * <p>
 * 状态栏的操作工具(主要是修改状态栏的文字颜色和背景颜色)
 */
public class StatusBarUtil {
    public static int DEVICE_FIRM = -1;

    /**
     * 修改状态栏为全透明
     *
     * @param activity
     */
    @TargetApi(19)
    public static void transparencyBar(FragmentActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 需要MIUIV6以上
     *
     * @param dark 是否把状态栏文字及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    private static void MIUISetStatusBarLightMode(Object object, boolean dark) {
        Window window = null;
        if (object instanceof Activity) {
            window = ((Activity) object).getWindow();
        } else if (object instanceof Window) {
            window = (Window) object;
        }

        if (window != null) {
            Class clazz = window.getClass();
            try {
                int darkModeFlag;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && RomUtils.isMiUIV7OrAbove()) {
                    //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错，所以两个方式都要加上
                    if (dark) {
                        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    } else {
                        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                    }
                }

            } catch (Exception ignore) {

            }
        }
    }


    /**
     * @param dark       true 字体颜色为黑色，false为白色
     * @param isFullMode 是否在全屏模式下
     */
    public static void setLightStatusBar(final Activity activity, final boolean dark, boolean isFullMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (DEVICE_FIRM == -1) {
                DEVICE_FIRM = RomUtils.getLightStatusBarAvailableRomType();
            }
            switch (DEVICE_FIRM) {
                case RomUtils.AvailableRomType.MIUI:
                    MIUISetStatusBarLightMode(activity, dark);
                    break;

                case RomUtils.AvailableRomType.FLYME:
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        setAndroidNativeLightStatusBar(activity, dark, isFullMode);
                    } else {
                        setFlymeLightStatusBar(activity, dark);
                    }
                    break;

                case RomUtils.AvailableRomType.ANDROID_NATIVE:
                    setAndroidNativeLightStatusBar(activity, dark, isFullMode);
                    break;

                case RomUtils.AvailableRomType.NA:
                    // N/A do nothing
                    break;
            }
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void setLightStatusBar(final Window window, final boolean dark, boolean isFullMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            switch (RomUtils.getLightStatusBarAvailableRomType()) {
                case RomUtils.AvailableRomType.MIUI:
                    MIUISetStatusBarLightMode(window, dark);
                    break;

                case RomUtils.AvailableRomType.FLYME:
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                        setAndroidNativeLightStatusBar(window, dark, isFullMode);
                    } else {
                        setFlymeLightStatusBar(window, dark);
                    }
                    break;

                case RomUtils.AvailableRomType.ANDROID_NATIVE:
                    setAndroidNativeLightStatusBar(window, dark, isFullMode);
                    break;

                case RomUtils.AvailableRomType.NA:
                    // N/A do nothing
                    break;
            }
        }


    }

    private static boolean setFlymeLightStatusBar(Object obj, boolean dark) {
        boolean result = false;
        Window window = null;
        if (obj instanceof Activity) {
            window = ((Activity) obj).getWindow();
        } else if (obj instanceof Window) {
            window = ((Window) obj);
        }

        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception ignore) {
            }
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static void setAndroidNativeLightStatusBar(Object obj, boolean dark, boolean isFullMode) {
        View decor = null;
        if (obj instanceof Activity) {
            decor = ((Activity) obj).getWindow().getDecorView();
        } else if (obj instanceof Window) {
            decor = ((Window) obj).getDecorView();
        }

        if (decor == null) {
            return;
        }

        if (dark) {
            if (isFullMode) {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        } else {
            // We want to change tint color to white again.
            // You can also record the flags in advance so that you can turn UI back completely if
            // you have set other flags before, such as translucent or full screen.
            if (isFullMode) {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            } else {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        }
    }

    /**
     * 获得通知栏高度
     */
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }
}
