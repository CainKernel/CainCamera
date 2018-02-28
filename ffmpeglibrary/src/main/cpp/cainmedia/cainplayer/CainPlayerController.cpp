//
// Created by Administrator on 2018/2/23.
//

#include "CainPlayerController.h"
#include "CainPlayer.h"

CainPlayer *player;

/**
 * 初始化
 * @param env
 * @param obj
 */
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeInit(JNIEnv *env, jclass obj) {
    if (player) {
        delete player;
    }
    player = new CainPlayer();
}

/**
 * 设置数据源
 * @param env
 * @param obj
 * @param path
 */
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_setNativeDataSource(JNIEnv *env, jclass obj,
                                                                jstring path) {
    if (player) {
        // 配置参数
        const char *videoPath = env->GetStringUTFChars(path, 0);
        player->setDataSource(videoPath);
        env->ReleaseStringUTFChars(path, videoPath);
    }
}

/**
 * 设置surface
 * @param env
 * @param obj
 * @param surface
 */
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_setNativeSurface(JNIEnv *env, jclass obj,
                                                             jobject surface) {
    if (player) {
        player->setSurface(env, surface);
    }
}

/**
 * 获取当前位置
 * @param env
 * @param obj
 * @return
 */
JNIEXPORT jint
JNICALL Java_com_cgfay_cainmedia_CainPlayer_getNativeCurrentPosition(JNIEnv *env, jclass obj) {
    if (player) {
        return player->getCurrentPosition();
    }
    return -1;
}

/**
 * 获取时长
 * @param env
 * @param obj
 * @return
 */
JNIEXPORT jint
JNICALL Java_com_cgfay_cainmedia_CainPlayer_getNativeDuration(JNIEnv *env, jclass obj) {
    if (player) {
        return player->getDuration();
    }
    return -1;
}

/**
 * 是否循环播放
 * @param env
 * @param obj
 * @return
 */
JNIEXPORT jboolean
JNICALL Java_com_cgfay_cainmedia_CainPlayer_isNativeLooping(JNIEnv *env, jclass obj) {
    if (player) {
        return player->isLooping();
    }
    return false;
}

/**
 * 是否正在播放
 * @param env
 * @param obj
 * @return
 */
JNIEXPORT jboolean
JNICALL Java_com_cgfay_cainmedia_CainPlayer_isNativePlaying(JNIEnv *env, jclass obj) {
    if (player) {
        return player->isPlaying();
    }
    return false;
}

/**
 * 暂停
 * @param env
 * @param obj
 */
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativePause(JNIEnv *env, jclass obj) {
    if (player) {
        player->stop();
    }
}

/**
 * 开始
 * @param env
 * @param obj
 */
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeStart(JNIEnv *env, jclass obj) {
    if (player) {
        player->start();
    }
}

/**
 * 停止
 * @param env
 * @param obj
 */
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeStop(JNIEnv *env, jclass obj) {
    if (player) {
        player->stop();
    }
}

/**
 * 准备
 * @param env
 * @param obj
 * @return
 */
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativePrepare(JNIEnv *env, jclass obj) {
    if (player) {
        player->prepare();
    }
}

/**
 * 释放资源
 * @param env
 * @param obj
 */
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeRelease(JNIEnv *env, jclass obj) {
    if (player) {
        player->release();
    }
    delete player;
}

/**
 * 定位
 * @param env
 * @param obj
 * @param msec
 */
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeSeekTo(JNIEnv *env, jclass obj, jint msec) {
    if (player) {
        player->seekTo(msec);
    }
}

/**
 * 设置循环播放
 * @param env
 * @param obj
 * @param loop
 */
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeSetLooping(JNIEnv *env, jclass obj,
                                                             jboolean loop) {
    if (player) {
        player->setLooping(loop);
    }
}

/**
 * 设置倒放
 * @param env
 * @param obj
 * @param reverse
 */
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeSetReverse(JNIEnv *env, jclass obj,
                                                             jboolean reverse) {
    if (player) {
        player->setReverse(reverse);
    }
}

/**
 * 设置播放音频
 * @param env
 * @param obj
 * @param play
 */
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeSetPlayAudio(JNIEnv *env, jclass obj,
                                                               jboolean play) {
    if (player) {
        player->setPlayAudio(play);
    }
}

/**
 * 大小发生改变
 * @param env
 * @param obj
 * @param play
 */
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeChangedSize(JNIEnv *env, jclass obj,
                                                              jint width, jint height) {
    if (player) {
        player->changedSize(width, height);
    }
}
