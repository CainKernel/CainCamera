//
// Created by cain on 2018/1/20.
//
#include <jni.h>
#include <string>

extern "C" {

#include "RtmpPush.h"

RtmpPusher *rtmpPusher;
int videoStart = -1;
int audioStart = -1;

static int CAMERA_BACK = 1; // 前置
static int CAMERA_FRONT = 2; // 后置
static int LANDSCAPE = 3;   // 横屏

/**
 * 初始化视频推流
 * @param env
 * @param url
 * @param width
 * @param height
 * @param bitRate
 */
JNIEXPORT int
JNICALL Java_com_cgfay_pushlibrary_RtmpPusher_initVideo(JNIEnv *env, jobject /* this*/, jstring url,
                                                        jint width, jint height, jint bitRate) {
    if (!rtmpPusher) {
        rtmpPusher = new RtmpPusher();
    }
    const char *url_char = env->GetStringUTFChars(url, 0);
    videoStart = rtmpPusher->initVideo(url_char, width, height, bitRate);
    env->ReleaseStringUTFChars(url, url_char);
    return videoStart;
}

/**
 * 推送视频YUV帧
 * @param env
 * @param data
 * @param clear
 */
JNIEXPORT void
JNICALL Java_com_cgfay_pushlibrary_RtmpPusher_pushYUV(JNIEnv *env, jobject /* this*/,
                                                      jbyteArray data, jint index) {
    if (videoStart != 0) {
        return;
    }
    if (!rtmpPusher) {
        return;
    }
    char *dataChar = (char *) env->GetByteArrayElements(data, 0);
    if (index == CAMERA_BACK) {
        rtmpPusher->avcEncodeWithBackCamera(dataChar);
    } else if (index == CAMERA_FRONT) {
        rtmpPusher->avcEncodeWithFrontCamera(dataChar);
    } else if (index == LANDSCAPE) {
        rtmpPusher->avcEncodeLandscape(dataChar);
    }

    env->ReleaseByteArrayElements(data, (jbyte *) dataChar, 0);
}

/**
 * 初始化音频编码
 * @param env
 * @param sampleRate
 * @param channel
 */
JNIEXPORT void
JNICALL Java_com_cgfay_pushlibrary_RtmpPusher_initAudio(JNIEnv *env, jobject /* this*/,
                                                        jint sampleRate, jint channel) {
    if (!rtmpPusher) {
        rtmpPusher = new RtmpPusher();
    }
    audioStart = rtmpPusher->initAudio(sampleRate, channel);
}

/**
 * 推送音频PCM帧
 * @param env
 * @param data
 */
JNIEXPORT void
JNICALL Java_com_cgfay_pushlibrary_RtmpPusher_pushPCM(JNIEnv *env, jobject /* this*/,
                                                      jbyteArray data) {
    if (audioStart != 0) {
        return;
    }
    if (!rtmpPusher) {
        return;
    }
    char *dataChar = (char *) env->GetByteArrayElements(data, 0);
    rtmpPusher->aacEncode(dataChar);
    env->ReleaseByteArrayElements(data, (jbyte *) dataChar, 0);
}

/**
 * 停止推送
 * @param env
 */
JNIEXPORT void
JNICALL Java_com_cgfay_pushlibrary_RtmpPusher_stop(JNIEnv *env, jobject /* this*/) {
    // 重置状态
    audioStart = -1;
    videoStart = -1;
    // 停止推流
    if (rtmpPusher) {
        rtmpPusher->stop();
        rtmpPusher = NULL;
    }
}

}