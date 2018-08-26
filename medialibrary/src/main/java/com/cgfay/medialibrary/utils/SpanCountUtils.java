package com.cgfay.medialibrary.utils;

import android.content.Context;

public class SpanCountUtils {

    /**
     * 计算span的数量
     * @param context
     * @param itemSize
     * @return
     */
    public static int calculateSpanCount(Context context, int itemSize) {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        float expected = (float) screenWidth / (float) itemSize;
        int spanCount = Math.round(expected);
        if (spanCount == 0) {
            spanCount = 1;
        }
        return spanCount;
    }
}
