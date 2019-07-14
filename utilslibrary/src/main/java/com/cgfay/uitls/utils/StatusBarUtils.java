package com.cgfay.uitls.utils;

import android.content.Context;
import android.content.res.Resources;

/**
 * 获取状态栏的高度
 * @author CainHuang
 * @date 2019/7/13
 */
public class StatusBarUtils {

    /**
     * 获取状态栏高度
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

}
