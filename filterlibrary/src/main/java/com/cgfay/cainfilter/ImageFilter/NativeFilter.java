package com.cgfay.cainfilter.ImageFilter;

/**
 * Native层滤镜组
 * Created by Administrator on 2018/3/6.
 */

public class NativeFilter {
    static {
        System.loadLibrary("imagefilter");
    }
    private NativeFilter() {}

    // 灰色滤镜
    public static native int[] grayFilter(int[] pixels, int width, int height);

}
