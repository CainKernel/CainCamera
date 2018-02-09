//
// Created by cain on 2018/2/9.
//

#include"jni.h"

extern "C" {
JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_setNativeDataSource(
        JNIEnv *env, jclass obj, jstring videoPath);

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_setNativeSurface(
        JNIEnv *env, jclass obj, jobject surface);

JNIEXPORT jint
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_getNativeCurrentPosition(
        JNIEnv *env, jclass obj);


JNIEXPORT jint
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_getNativeAudioSessionId(
        JNIEnv *env, jclass obj);

JNIEXPORT jint
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_getNativeDuration(
        JNIEnv *env, jclass obj);

JNIEXPORT jboolean
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_isNativeLooping(
        JNIEnv *env, jclass obj);

JNIEXPORT jboolean
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_isNativePlaying(
        JNIEnv *env, jclass obj);

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativePause(
        JNIEnv *env, jclass obj);

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeStart(
        JNIEnv *env, jclass obj);

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeStop(
        JNIEnv *env, jclass obj);

JNIEXPORT int
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativePrepareAsync(
        JNIEnv *env, jclass obj);

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeReset(
        JNIEnv *env, jclass obj);

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeRelease(
        JNIEnv *env, jclass obj);


JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeSeekTo(
        JNIEnv *env, jclass obj, jint msec);

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeSeekToRegion(
        JNIEnv *env, jclass obj, jint lmsec, jint rmsec);

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeSetLooping(
        JNIEnv *env, jclass obj, jboolean loop);

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeSetReverse(
        JNIEnv *env, jclass obj, jboolean loop);

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeSetPlayAudio(
        JNIEnv *env, jclass obj, jboolean loop);

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeChangedSize(
        JNIEnv *env, jclass obj, jint width, jint height);

}

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_setNativeDataSource(
        JNIEnv *env, jclass obj, jstring videoPath) {
    // TODO 设置数据源

}

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_setNativeSurface(
        JNIEnv *env, jclass obj, jobject surface) {
    // TODO 设置surface

}

JNIEXPORT jint
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_getNativeCurrentPosition(
        JNIEnv *env, jclass obj) {
    // TODO 获取视频当前时长
    return 0;
}


JNIEXPORT jint
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_getNativeAudioSessionId(
        JNIEnv *env, jclass obj) {
    // TODO 获取音频Session id
    return 0;
}

JNIEXPORT jint
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_getNativeDuration(
        JNIEnv *env, jclass obj) {
    // TODO 获取视频时长
    return 0;
}

JNIEXPORT jboolean
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_isNativeLooping(
        JNIEnv *env, jclass obj) {
    // TODO 是否循环播放
    return 0;
}

JNIEXPORT jboolean
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_isNativePlaying(
        JNIEnv *env, jclass obj) {
    // TODO 是否正在播放
    return 0;
}

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativePause(
        JNIEnv *env, jclass obj) {
    // TODO 暂停播放
}

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeStart(
        JNIEnv *env, jclass obj) {
    // TODO 开始播放
}

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeStop(
        JNIEnv *env, jclass obj) {
    // TODO 停止播放
}

JNIEXPORT int
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativePrepareAsync(
        JNIEnv *env, jclass obj) {
    // TODO 准备播放器
    return 0;
}

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeReset(
        JNIEnv *env, jclass obj) {
    // TODO 重置播放器未初始化状态
}

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeRelease(
        JNIEnv *env, jclass obj) {
    // TODO 释放资源
}


JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeSeekTo(
        JNIEnv *env, jclass obj, jint msec) {
    // TODO 查找/定位到某个位置播放
}

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeSeekToRegion(
        JNIEnv *env, jclass obj, jint lmsec, jint rmsec) {
    // TODO 查找/定位到某个区域播放
}

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeSetLooping(
        JNIEnv *env, jclass obj, jboolean loop) {
    // TODO 设置是否循环播放
}

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeSetReverse(
        JNIEnv *env, jclass obj, jboolean loop) {
    // TODO 设置是否倒放
}

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeSetPlayAudio(
        JNIEnv *env, jclass obj, jboolean loop) {
    // TODO 设置是否播放音乐
}

JNIEXPORT void
JNICALL Java_com_cgfay_cainffmpeg_nativehelper_CainPlayer_nativeChangedSize(
        JNIEnv *env, jclass obj, jint width, jint height) {
    // TODO 设置大小发生变化
}






