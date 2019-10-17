package com.bokecc.livemodule.live.morefunction.fab;

/*
 * 项目名:    SuspensionFAB
 * 包名       com.azhon.suspensionfab
 * 文件名:    ExpandOrientation
 * 创建者:    ZSY
 * 创建时间:  2017/6/29 on 16:57
 * 描述:     TODO 折叠按钮的展开方向
 */
public enum ExpandOrientation {

    /**
     * 按钮的上边
     */
    FAB_TOP(1),
    /**
     * 按钮的下边
     */
    FAB_BOTTOM(2),
    /**
     * 按钮的左边
     */
    FAB_LEFT(3),
    /**
     * 按钮的右边
     */
    FAB_RIGHT(4);

    ExpandOrientation(int value) {
        this.value = value;
    }

    private int value = 0;

    /**
     * 根据状态值获取枚举值
     *
     * @param value 值
     * @return ExpandOrientation
     */
    public static ExpandOrientation getEnum(int value) {
        if (value == FAB_TOP.getValue()) {
            return ExpandOrientation.FAB_TOP;
        } else if (value == FAB_BOTTOM.getValue()) {
            return ExpandOrientation.FAB_BOTTOM;
        } else if (value == FAB_LEFT.getValue()) {
            return ExpandOrientation.FAB_LEFT;
        } else if (value == FAB_RIGHT.getValue()) {
            return ExpandOrientation.FAB_RIGHT;
        } else {
            return ExpandOrientation.FAB_TOP;
        }
    }

    /**
     * @return 枚举变量实际返回值
     */
    public int getValue() {
        return value;
    }
}
