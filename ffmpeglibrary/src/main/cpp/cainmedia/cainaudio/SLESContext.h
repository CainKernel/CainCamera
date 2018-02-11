//
// Created by cain on 2018/2/11.
//

#ifndef CAINCAMERA_SLESCONTEXT_H
#define CAINCAMERA_SLESCONTEXT_H

#include "SlUtils.h"

class SLESContext {

    SLObjectItf engineObject;
    SLEngineItf engineEngine;
    bool isInited;
    /**
     * 创建 OpenSL ES 的engine对象
     */
    SLresult createEngine() {
        // engine对象参数
        SLEngineOption engineOptions[] = {
                { (SLuint32) SL_ENGINEOPTION_THREADSAFE, (SLuint32) SL_BOOLEAN_TRUE }
        };
        // 创建 OpenSL ES 的engine对象
        return slCreateEngine(&engineObject, sizeof(engineOptions)/ sizeof(engineOptions[0]),
                              engineOptions, 0, // no interfaces
                              0, // no interfaces
                              0); // no required
    };

    /**
     * 实现engine对象
     * @param object
     * @return
     */
    SLresult realizeObject(SLObjectItf object) {
        return (*object)->Realize(object, SL_BOOLEAN_FALSE); // No async, blocking call
    };

    /**
     * 获取engine接口
     * @return
     */
    SLresult GetEngineInterface() {
        return (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    };

    SLESContext();
    void init();
    static SLESContext* sInstance;

public:
    // 工厂方法
    static SLESContext* GetInstance();
    virtual ~SLESContext();
    SLEngineItf getEngine() {
        return engineEngine;
    };
};


#endif //CAINCAMERA_SLESCONTEXT_H
