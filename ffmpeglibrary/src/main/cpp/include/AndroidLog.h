//
// Created by cain on 2018/11/28.
//

#ifndef CAINCAMERA_ANDROIDLOG_H
#define CAINCAMERA_ANDROIDLOG_H

#include <android/log.h>

#define TAG "CainCamera"

#define ALOGE(format, ...) __android_log_print(ANDROID_LOG_ERROR, TAG, format, ##__VA_ARGS__)
#define ALOGI(format, ...) __android_log_print(ANDROID_LOG_INFO,  TAG, format, ##__VA_ARGS__)
#define ALOGD(format, ...) __android_log_print(ANDROID_LOG_DEBUG, TAG, format, ##__VA_ARGS__)
#define ALOGW(format, ...) __android_log_print(ANDROID_LOG_WARN,  TAG, format, ##__VA_ARGS__)

#endif //CAINCAMERA_ANDROIDLOG_H
