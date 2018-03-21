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
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeInit(JNIEnv *env, jobject instance);

// 设置数据源
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_setNativeDataSource(JNIEnv *env, jobject instance,
                                                                jstring path);

// 设置surface
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_setNativeSurface(JNIEnv *env, jobject instance,
                                                                jobject surface);

// 获取当前位置
JNIEXPORT jint
JNICALL Java_com_cgfay_cainmedia_CainPlayer_getNativeCurrentPosition(JNIEnv *env, jobject instance);

// 获取时长
JNIEXPORT jint
JNICALL Java_com_cgfay_cainmedia_CainPlayer_getNativeDuration(JNIEnv *env, jobject instance);

// 是否循环播放
JNIEXPORT jboolean
JNICALL Java_com_cgfay_cainmedia_CainPlayer_isNativeLooping(JNIEnv *env, jobject instance);

// 是否正在播放
JNIEXPORT jboolean
JNICALL Java_com_cgfay_cainmedia_CainPlayer_isNativePlaying(JNIEnv *env, jobject instance);

// 暂停
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativePause(JNIEnv *env, jobject instance);

// 开始
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeStart(JNIEnv *env, jobject instance);

// 停止
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeStop(JNIEnv *env, jobject instance);

// 准备
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativePrepare(JNIEnv *env, jobject instance);

// 释放资源
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeRelease(JNIEnv *env, jobject instance);

// 定位
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeSeekTo(JNIEnv *env, jobject instance, jint msec);

// 设置循环播放
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeSetLooping(JNIEnv *env, jobject instance,
                                                             jboolean loop);

// 设置倒放
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeSetReverse(JNIEnv *env, jobject instance,
                                                             jboolean reverse);

// 设置播放音频
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeSetPlayAudio(JNIEnv *env, jobject instance,
                                                               jboolean play);

// 大小发生改变
JNIEXPORT void
JNICALL Java_com_cgfay_cainmedia_CainPlayer_nativeChangedSize(JNIEnv *env, jobject instance,
                                                              jint width, jint height);

#ifdef __cplusplus
};
#endif

#endif //CAINCAMERA_CAINPLAYERCONTROLLER_H
