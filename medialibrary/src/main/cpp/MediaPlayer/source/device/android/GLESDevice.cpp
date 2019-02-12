//
// Created by cain on 2018/12/30.
//

#include <renderer/YUV420PRenderer.h>
#include <renderer/BGRARenderer.h>
#include "GLESDevice.h"

GLESDevice::GLESDevice() {
    mWindow = NULL;
    eglSurface = EGL_NO_SURFACE;
    eglHelper = new EglHelper();

    mHaveEGLSurface = false;
    mHaveEGlContext = false;
    mHasSurface = false;

    mVideoTexture = (Texture *) malloc(sizeof(Texture));
    memset(mVideoTexture, 0, sizeof(Texture));
    mRenderer = NULL;
}

GLESDevice::~GLESDevice() {
    mMutex.lock();
    terminate();
    mMutex.unlock();
}

void GLESDevice::surfaceCreated(ANativeWindow *window) {
    mMutex.lock();
    if (mWindow != NULL) {
        ANativeWindow_release(mWindow);
        mWindow = NULL;
        mSurfaceReset = true;
    }
    mWindow = window;
    mHasSurface = true;
    mMutex.unlock();
}

void GLESDevice::surfaceChanged(int width, int height) {
    mMutex.lock();
    mVideoTexture->viewWidth = width;
    mVideoTexture->viewHeight = height;
    mMutex.unlock();
}

void GLESDevice::surfaceDestroyed() {
    mMutex.lock();
    mHasSurface = false;
    mMutex.unlock();
}

void GLESDevice::terminate() {
    terminate(true);
}

void GLESDevice::terminate(bool releaseContext) {
    if (eglSurface != EGL_NO_SURFACE) {
        eglHelper->destroySurface(eglSurface);
        eglSurface = EGL_NO_SURFACE;
        mHaveEGLSurface = false;
    }
    if (eglHelper->getEglContext() != EGL_NO_CONTEXT && releaseContext) {
        eglHelper->release();
        mHaveEGlContext = false;
    }
}

void GLESDevice::onInitTexture(int width, int height, TextureFormat format, BlendMode blendMode) {
    mMutex.lock();

    // 创建EGLContext
    if (!mHaveEGlContext) {
        mHaveEGlContext = eglHelper->init(NULL, FLAG_TRY_GLES3);
        ALOGD("mHaveEGlContext = %d", mHaveEGlContext);
    }

    if (!mHaveEGlContext) {
        return;
    }

    // 重新设置Surface，兼容SurfaceHolder处理
    if (mHasSurface && mSurfaceReset) {
        terminate(false);
        mSurfaceReset = false;
    }

    // 创建/释放EGLSurface
    if (eglSurface == EGL_NO_SURFACE && mWindow != NULL) {
        if (mHasSurface && !mHaveEGLSurface) {
            eglSurface = eglHelper->createSurface(mWindow);
            if (eglSurface != EGL_NO_SURFACE) {
                mHaveEGLSurface = true;
                ALOGD("mHaveEGLSurface = %d", mHaveEGLSurface);
            }
        }
    } else if (eglSurface != EGL_NO_SURFACE && mHaveEGLSurface) {
        // 处于SurfaceDestroyed状态，释放EGLSurface
        if (!mHasSurface) {
            terminate(false);
        }
    }
    mVideoTexture->width = width;
    mVideoTexture->height = height;
    mVideoTexture->format = format;
    mVideoTexture->blendMode = blendMode;
    mVideoTexture->direction = FLIP_NONE;
    if (mRenderer == NULL) {
        if (format == FMT_YUV420P) {
            mRenderer = new YUV420PRenderer();
        } else if (format == FMT_ARGB) {
            mRenderer = new BGRARenderer();
        } else {
            mRenderer = NULL;
        }
        if (mRenderer != NULL) {
            eglHelper->makeCurrent(eglSurface);
            mRenderer->onInit(mVideoTexture);
            eglHelper->swapBuffers(eglSurface);
        }
    }
    mMutex.unlock();
}

int GLESDevice::onUpdateYUV(uint8_t *yData, int yPitch, uint8_t *uData, int uPitch, uint8_t *vData,
                            int vPitch) {
    if (!mHaveEGlContext) {
        return -1;
    }
    mMutex.lock();
    mVideoTexture->pitches[0] = yPitch;
    mVideoTexture->pitches[1] = uPitch;
    mVideoTexture->pitches[2] = vPitch;
    mVideoTexture->pixels[0] = yData;
    mVideoTexture->pixels[1] = uData;
    mVideoTexture->pixels[2] = vData;
    if (mRenderer != NULL && eglSurface != EGL_NO_SURFACE) {
        eglHelper->makeCurrent(eglSurface);
        mRenderer->uploadTexture(mVideoTexture);
    }
    mMutex.unlock();
    return 0;
}

int GLESDevice::onUpdateARGB(uint8_t *rgba, int pitch) {
    if (!mHaveEGlContext) {
        return -1;
    }
    mMutex.lock();
    mVideoTexture->pitches[0] = pitch;
    mVideoTexture->pixels[0] = rgba;
    if (mRenderer != NULL && eglSurface != EGL_NO_SURFACE) {
        eglHelper->makeCurrent(eglSurface);
        mRenderer->uploadTexture(mVideoTexture);
    }
    mMutex.unlock();
    return 0;
}

int GLESDevice::onRequestRender(FlipDirection direction) {
    if (!mHaveEGlContext) {
        return -1;
    }
    mMutex.lock();
    mVideoTexture->direction = direction;
    if (mRenderer != NULL && eglSurface != EGL_NO_SURFACE) {
        eglHelper->makeCurrent(eglSurface);
        mRenderer->renderTexture(mVideoTexture);
        eglHelper->swapBuffers(eglSurface);
    }
    mMutex.unlock();
    return 0;
}