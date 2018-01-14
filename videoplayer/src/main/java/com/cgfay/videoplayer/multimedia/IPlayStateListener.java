package com.cgfay.videoplayer.multimedia;

/**
 * 播放器状态回调
 * Created by cain on 2018/1/14.
 */

public interface IPlayStateListener {
    // 视频长宽比
    void videoAspect(int width, int height, float time);
}
