//
// Created by CainHuang on 2019/3/21.
//

#ifndef CAINEGLCONTEXT_H
#define CAINEGLCONTEXT_H

#include <mutex>

#if defined(__ANDROID__)

#include <android/native_window.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <EGL/eglplatform.h>
#include "EglHelper.h"

#endif

/**
 * EGLContext 上下文，为了方便使用SharedContext而造的
 */
class CainEGLContext {
public:
    static CainEGLContext *getInstance();

    void destroy();

    EGLContext getContext();

private:
    CainEGLContext();

    virtual ~CainEGLContext();

    bool init(int flags);

    void release();

    EGLConfig getConfig(int flags, int version);

    void checkEglError(const char *msg);

    static CainEGLContext *instance;
    static std::mutex mutex;

    EGLContext eglContext;
    EGLDisplay eglDisplay;
};


#endif //CAINEGLCONTEXT_H
