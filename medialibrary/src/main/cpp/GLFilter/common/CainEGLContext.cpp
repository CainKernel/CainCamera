//
// Created by CainHuang on 2019/3/21.
//

#include <AndroidLog.h>
#include "CainEGLContext.h"

CainEGLContext *CainEGLContext::instance;
std::mutex CainEGLContext::mutex;

CainEGLContext::CainEGLContext() {
    eglDisplay = EGL_NO_DISPLAY;
    eglContext = EGL_NO_CONTEXT;
    init(FLAG_TRY_GLES3);
}

CainEGLContext::~CainEGLContext() {

}

CainEGLContext *CainEGLContext::getInstance() {
    if (!instance) {
        std::unique_lock<std::mutex> lock(mutex);
        if (!instance) {
            instance = new (std::nothrow) CainEGLContext();
        }
    }
    return instance;
}

bool CainEGLContext::init(int flags) {
    if (eglDisplay != EGL_NO_DISPLAY) {
        ALOGE("EGL already set up");
        return false;
    }

    // 获取EGLDisplay
    eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (eglDisplay == EGL_NO_DISPLAY) {
        ALOGE("unable to get EGLDisplay.\n");
        return false;
    }

    // 初始化mEGLDisplay
    if (!eglInitialize(eglDisplay, 0, 0)) {
        eglDisplay = EGL_NO_DISPLAY;
        ALOGE("unable to initialize EGLDisplay.");
        return false;
    }

    // 判断是否尝试使用GLES3
    if ((flags & FLAG_TRY_GLES3) != 0) {
        EGLConfig config = getConfig(flags, 3);
        if (config != NULL) {
            int attrib3_list[] = {
                    EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL_NONE
            };
            EGLContext context = eglCreateContext(eglDisplay, config, EGL_NO_CONTEXT, attrib3_list);
            checkEglError("eglCreateContext");
            if (eglGetError() == EGL_SUCCESS) {
                eglContext = context;
            }
        }
    }

    // 判断如果GLES3的EGLContext没有获取到，则尝试使用GLES2
    if (eglContext == EGL_NO_CONTEXT) {
        EGLConfig config = getConfig(flags, 2);
        int attrib2_list[] = {
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL_NONE
        };
        EGLContext context = eglCreateContext(eglDisplay, config, EGL_NO_CONTEXT, attrib2_list);
        checkEglError("eglCreateContext");
        if (eglGetError() == EGL_SUCCESS) {
            eglContext = context;
        }
    }

    int values[1] = {0};
    eglQueryContext(eglDisplay, eglContext, EGL_CONTEXT_CLIENT_VERSION, values);
    ALOGD("EGLContext created, client version %d", values[0]);
    return true;
}

EGLConfig CainEGLContext::getConfig(int flags, int version) {
    int renderableType = EGL_OPENGL_ES2_BIT;
    if (version >= 3) {
        renderableType |= EGL_OPENGL_ES3_BIT_KHR;
    }
    int attribList[] = {
            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_ALPHA_SIZE, 8,
            //EGL_DEPTH_SIZE, 16,
            //EGL_STENCIL_SIZE, 8,
            EGL_RENDERABLE_TYPE, renderableType,
            EGL_NONE, 0,      // placeholder for recordable [@-3]
            EGL_NONE
    };
    int length = sizeof(attribList) / sizeof(attribList[0]);
    if ((flags & FLAG_RECORDABLE) != 0) {
        attribList[length - 3] = EGL_RECORDABLE_ANDROID;
        attribList[length - 2] = 1;
    }
    EGLConfig configs = NULL;
    int numConfigs;
    if (!eglChooseConfig(eglDisplay, attribList, &configs, 1, &numConfigs)) {
        ALOGW("unable to find RGB8888 / %d  EGLConfig", version);
        return NULL;
    }
    return configs;
}

void CainEGLContext::release() {
    if (eglDisplay != EGL_NO_DISPLAY) {
        eglMakeCurrent(eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    }
    if (eglContext != EGL_NO_CONTEXT) {
        eglDestroyContext(eglDisplay, eglContext);
    }
    eglDisplay = EGL_NO_DISPLAY;
    eglContext = EGL_NO_CONTEXT;
}

void CainEGLContext::destroy() {
    if (instance) {
        std::unique_lock<std::mutex> lock(mutex);
        if (!instance) {
            delete instance;
            instance = nullptr;
        }
    }
}

void CainEGLContext::checkEglError(const char *msg) {
    int error;
    if ((error = eglGetError()) != EGL_SUCCESS) {
        ALOGE("%s: EGL error: %x", msg, error);
    }
}

EGLContext CainEGLContext::getContext() {
    return eglContext;
}

