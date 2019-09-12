package com.cgfay.uitls.utils;

/**
 * 播放状态
 * Created by cain.huang on 2018/1/8.
 */

public enum  StateType {
    ERROR,      // 出错
    IDLE,       // 空闲
    PREPARING,  // 准备中
    PREPARED,   // 已经准备完成
    PLAYING,    // 播放过程
    PAUSED,     // 暂停
    STOP,       // 停止
    RELEASED,   // 已释放
    COMPLETED,  // 正常播放完毕
}
