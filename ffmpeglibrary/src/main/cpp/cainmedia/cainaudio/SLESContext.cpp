//
// Created by cain on 2018/2/11.
//

#include "SLESContext.h"
#include "native_log.h"

SLESContext *SLESContext::sInstance = new SLESContext();

/**
 * 初始化
 */
void SLESContext::init() {
    ALOGI("createEngine");
    SLresult result = createEngine();
    ALOGI("createEngine result is s%", resultToString(result));
    if (SL_RESULT_SUCCESS == result) {
        ALOGI("Realize the engine object");
        result = realizeObject(engineObject);
        if (SL_RESULT_SUCCESS == result) {
            ALOGI("Get the engine interface");
            result = GetEngineInterface();
        }
    }
}

/**
 * 构造方法
 */
SLESContext::SLESContext() {
    isInited = false;
}

/**
 * 析构方法
 */
SLESContext::~SLESContext() {

}

/**
 * 获取实例
 * @return
 */
SLESContext* SLESContext::GetInstance() {
    if (!sInstance) {
        sInstance = new SLESContext();
    }
    if (!sInstance->isInited) {
        sInstance->init();
        sInstance->isInited = true;
    }
    return sInstance;
}

