package com.cgfay.caincamera.eglnative;

/**
 * Created by cain.huang on 2017/7/19.
 */

public class EGLNativeHelper {

    static {
        System.loadLibrary("native-lib");
    }
    private EGLNativeHelper(){}

    /**
     * avcodec配置
     * @return
     */
    public native String configurationFromFFmpeg();
}
