package com.cgfay.caincamera.core;

/**
 * 录制管理器
 * Created by cain.huang on 2017/11/3.
 */

public class RecorderManager {
    private static RecorderManager mInstance;

    public static RecorderManager getInstance() {
        if (mInstance == null) {
            mInstance = new RecorderManager();
        }
        return mInstance;
    }

    private RecorderManager() {

    }


}
