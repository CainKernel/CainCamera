//
// Created by cain on 2017/12/28.
//

#ifndef CAINCAMERA_NATIVE_LOG_H
#define CAINCAMERA_NATIVE_LOG_H
#include <android/log.h>

#define JNI_DEBUG 1

#define LOGE(format, ...) if (JNI_DEBUG) { __android_log_print(ANDROID_LOG_ERROR, "CainJni_ffmpeg", format, ##__VA_ARGS__); }
#define LOGI(format, ...) if(JNI_DEBUG){ __android_log_print(ANDROID_LOG_INFO, "CainJni_ffmpeg", format, ##__VA_ARGS__); }
#endif //CAINCAMERA_NATIVE_LOG_H
