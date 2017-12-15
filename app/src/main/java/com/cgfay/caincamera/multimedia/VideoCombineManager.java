package com.cgfay.caincamera.multimedia;

import android.util.Log;

import java.util.List;

/**
 * 多段视频合并管理器
 * Created by cain.huang on 2017/12/15.
 */

public final class VideoCombineManager {

    private static final String TAG = "VideoCombineManage";

    private static VideoCombineManager mInstance;


    public static VideoCombineManager getInstance() {
        if (mInstance == null) {
            mInstance = new VideoCombineManager();
        }
        return mInstance;
    }

    /**
     * 初始化媒体合并器
     * @param videoPath
     * @param destPath
     */
    public void startVideoCombiner(final List<String> videoPath, final String destPath,
                                   final VideoCombiner.VideoCombineListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                VideoCombiner videoCombiner = new VideoCombiner(videoPath, destPath, listener);
                videoCombiner.combineVideo();
            }
        }).start();
    }
}
