//
// Created by Administrator on 2018/3/6.
//

#ifndef CAINCAMERA_IMAGEUTILS_H
#define CAINCAMERA_IMAGEUTILS_H

#include <android/log.h>
#include <jni.h>
#include <stddef.h>
#include <sys/time.h>

#define	LOG_TAG    "NativeImageFilter"
#define	ALOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define ALOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define	ALOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#define PI 3.14159265

#include <math.h>


static inline int argb2Color(int alpha, int r, int g, int b) {
    return alpha << 24 | r << 16 | g << 8 | b;
}

static inline int rgb2Color(int r, int g, int b) {
    return 255 << 24 | r << 16 | g << 8 | b;
}

static inline int min(int a, int b) {
    if (a < b) {
        return a;
    }
    return b;
}

static inline int max(int a, int b) {
    if (a > b) {
        return a;
    }
    return b;
}

static inline double min(double a, double b) {
    if (a < b) {
        return a;
    }
    return b;
}

static inline double max(double a, double b) {
    if (a > b) {
        return a;
    }
    return b;
}

static inline jintArray jintToArray(JNIEnv* env, jint size, jint* arr) {
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, arr);
    return result;
}

static inline long getCurrentTime() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}

#endif //CAINCAMERA_IMAGEUTILS_H
