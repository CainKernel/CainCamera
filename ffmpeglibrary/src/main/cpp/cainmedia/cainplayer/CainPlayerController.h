//
// Created by Administrator on 2018/2/23.
//

#ifndef CAINCAMERA_CAINPLAYERCONTROLLER_H
#define CAINCAMERA_CAINPLAYERCONTROLLER_H

#include <jni.h>
#include <string>

#ifdef __cplusplus
extern "C" {
#endif

// 初始化
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeInit(JNIEnv *env, jclass obj);

// 设置数据源
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_setNativeDataSource(JNIEnv *env, jclass obj,
                                                                jstring path);

// 设置surface
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_setNativeSurface(JNIEnv *env, jclass obj,
                                                                jobject surface);

// 获取当前位置
JNIEXPORT jint
JNICALL Java_com_cgfay_cainmedia_CainPlayer_getNativeCurrentPosition(JNIEnv *env, jclass obj);

// 获取时长
JNIEXPORT jint
JNICALL Java_com_cgfay_cainmedia_CainPlayer_getNativeDuration(JNIEnv *env, jclass obj);

// 是否循环播放
JNIEXPORT jboolean
JNICALL Java_com_cgfay_cainmedia_CainPlayer_isNativeLooping(JNIEnv *env, jclass obj);

// 是否正在播放
JNIEXPORT jboolean
JNICALL Java_com_cgfay_cainmedia_CainPlayer_isNativePlaying(JNIEnv *env, jclass obj);

// 暂停
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativePause(JNIEnv *env, jclass obj);

// 开始
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeStart(JNIEnv *env, jclass obj);

// 停止
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeStop(JNIEnv *env, jclass obj);

// 准备
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativePrepare(JNIEnv *env, jclass obj);

// 释放资源
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeRelease(JNIEnv *env, jclass obj);

// 定位
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeSeekTo(JNIEnv *env, jclass obj, jint msec);

// 设置循环播放
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeSetLooping(JNIEnv *env, jclass obj,
                                                             jboolean loop);

// 设置倒放
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeSetReverse(JNIEnv *env, jclass obj,
                                                             jboolean reverse);

// 设置播放音频
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeSetPlayAudio(JNIEnv *env, jclass obj,
                                                               jboolean play);

// 大小发生改变
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeChangedSize(JNIEnv *env, jclass obj,
                                                              jint width, jint height);

#ifdef __cplusplus
};
#endif

#endif //CAINCAMERA_CAINPLAYERCONTROLLER_H
