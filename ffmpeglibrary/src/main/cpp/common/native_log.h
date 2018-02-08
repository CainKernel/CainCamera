//
// Created by cain on 2017/12/28.
//

#ifndef CAINCAMERA_NATIVE_LOG_H
#define CAINCAMERA_NATIVE_LOG_H
#include <android/log.h>

#define JNI_DEBUG 1
#define JNI_TAG "CainJni_ffmpeg"

#define ALOGE(format, ...) if (JNI_DEBUG) { __android_log_print(ANDROID_LOG_ERROR, JNI_TAG, format, ##__VA_ARGS__); }
#define ALOGI(format, ...) if (JNI_DEBUG) { __android_log_print(ANDROID_LOG_INFO,  JNI_TAG, format, ##__VA_ARGS__); }
#define ALOGD(format, ...) if (JNI_DEBUG) { __android_log_print(ANDROID_LOG_DEBUG, JNI_TAG, format, ##__VA_ARGS__); }
#define ALOGW(format, ...) if (JNI_DEBUG) { __android_log_print(ANDROID_LOG_WARN,  JNI_TAG, format, ##__VA_ARGS__); }
#endif //CAINCAMERA_NATIVE_LOG_H
