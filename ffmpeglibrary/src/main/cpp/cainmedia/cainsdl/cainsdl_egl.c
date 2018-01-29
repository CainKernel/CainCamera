//
// Created by Administrator on 2018/1/29.
//

#include <malloc.h>
#include <string.h>
#include "cainsdl_egl.h"

static EGLBoolean CAIN_EGL_IsValid(CAIN_EGL *egl) {
    if (egl && egl->window && egl->display && egl->surface && egl->context) {
        return EGL_TRUE;
    }
    return EGL_FALSE;
}


/**
 * 创建EGL
 * @return
 */
CAIN_EGL *EGL_Create(void) {
    CAIN_EGL *egl = (CAIN_EGL *)malloc(sizeof(CAIN_EGL));
    if (!egl) {
        return NULL;
    }
    memset(egl, 0, sizeof(CAIN_EGL));

    return egl;
}

/**
 * 释放EGL
 * @param egl
 */
void EGL_Free(CAIN_EGL *egl) {
    if (!egl) {
        return;
    }
    EGL_Terminate(egl);
    memset(egl, 0, sizeof(CAIN_EGL));
    free(egl);
}

/**
 * 关闭EGL
 * @param egl
 */
void EGL_Terminate(CAIN_EGL *egl) {
    if (!CAIN_EGL_IsValid(egl)) {
        return;
    }
    if (egl->display) {
        eglMakeCurrent(egl->display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        if (egl->context)
            eglDestroyContext(egl->display, egl->context);
        if (egl->surface)
            eglDestroySurface(egl->display, egl->surface);
        eglTerminate(egl->display);
        eglReleaseThread();
    }
    egl->context = EGL_NO_CONTEXT;
    egl->surface = EGL_NO_SURFACE;
    egl->display = EGL_NO_DISPLAY;
}
