//
// Created by Administrator on 2018/1/29.
//

#ifndef CAINCAMERA_CAINSDL_EGL_H
#define CAINCAMERA_CAINSDL_EGL_H

#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <EGL/eglplatform.h>

typedef struct CAIN_EGL {

    // ANativeWindow* 类型
    EGLNativeWindowType window;

    EGLDisplay display;
    EGLSurface surface;
    EGLContext context;

    EGLint width;
    EGLint height;

} CAIN_EGL;

// 创建EGL
CAIN_EGL *EGL_Create(void);

// 释放EGL
void EGL_Free(CAIN_EGL *egl);

// TODO 显示函数
//EGLBoolean EGL_Display(CAIN_EGL *egl, EGLNativeWindowType windowType);

// 关闭EGL
void EGL_Terminate(CAIN_EGL *egl);

#endif //CAINCAMERA_CAINSDL_EGL_H
