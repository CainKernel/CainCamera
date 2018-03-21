//
// Created by cain on 2018/3/21.
//

#include "CainRender.h"

CainRender::CainRender(RenderInputType type) {
    mMutex = MutexCreate();
    mWindow = NULL;
    mEglCore = NULL;
    mWindowSurface = NULL;
    mYUVFilter = NULL;
    mImageFilter = NULL;
    mWidth = -1;
    mHeight = -1;
    mInputType = type;
}

CainRender::~CainRender() {
    releaseFilters();
    if (mWindow != NULL) {
        ANativeWindow_release(mWindow);
    }
    if (mWindowSurface != NULL) {
        mWindowSurface->release();
        delete mWindowSurface;
        mWindowSurface = NULL;
    }
    if (mEglCore != NULL) {
        mEglCore->release();
        delete mEglCore;
        mEglCore = NULL;
    }
    MutexDestroy(mMutex);
}

void CainRender::surfaceCreated(ANativeWindow *nativeWindow) {
    if (mWindow != NULL) {
        ANativeWindow_release(mWindow);
    }
    mWindow = nativeWindow;

    mEglCore = new EglCore();
    mWindowSurface = new WindowSurface(mEglCore, mWindow, false);
    mWindowSurface->makeCurrent();
    mYUVFilter = new YUV420PImageFilter();
    mImageFilter = new GLImageFilter();
    mYUVFilter->onInputSizeChanged(mWindowSurface->getWidth(), mWindowSurface->getHeight());
    mImageFilter->onInputSizeChanged(mWindowSurface->getWidth(), mWindowSurface->getHeight());
}

void CainRender::surfaceChanged(int width, int height) {
    mWidth = width;
    mHeight = height;
    mYUVFilter->onDisplayChanged(mWidth, mHeight);
    mImageFilter->onDisplayChanged(mWidth, mHeight);
}

void CainRender::surfaceDestroyed() {

}

void CainRender::releaseFilters() {
    if (mYUVFilter != NULL) {
        delete mYUVFilter;
        mYUVFilter = NULL;
    }
    if (mImageFilter != NULL) {
        delete mImageFilter;
        mImageFilter = NULL;
    }
}

/**
 * 绘制渲染
 * @param bufY
 * @param bufU
 * @param bufV
 */
void CainRender::drawFrame(void *bufY, void *bufU, void *bufV) {
    MutexLock(mMutex);
    mWindowSurface->makeCurrent();
    if (!mImageFilter) {
        mYUVFilter->drawFrame(bufY, bufU, bufV);
    } else {
        mCurrentTexture = mYUVFilter->drawFrameBuffer(bufY, bufU, bufV);
        mImageFilter->drawFrame(mCurrentTexture);
    }
    mWindowSurface->swapBuffers();
    MutexUnlock(mMutex);
}

/**
 * 切换滤镜
 * @param filter
 */
void CainRender::changeFilter(GLImageFilter *filter) {
    MutexLock(mMutex);
    if (mImageFilter != NULL) {
        delete mImageFilter;
        mImageFilter = NULL;
    }
    mImageFilter = filter;
    mImageFilter->onInputSizeChanged(mWindowSurface->getWidth(), mWindowSurface->getHeight());
    mImageFilter->onDisplayChanged(mWidth, mHeight);
    MutexUnlock(mMutex);
}