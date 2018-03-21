//
// Created by cain on 2018/3/21.
//

#ifndef CAINCAMERA_CAINRENDER_H
#define CAINCAMERA_CAINRENDER_H

#include <android/native_window.h>
#include "caingles/EglCore.h"
#include "caingles/WindowSurface.h"
#include "cainfilters/YUV420PImageFilter.h"
#include "cainfilters/GLImageFilter.h"
#include "Mutex.h"

enum RenderInputType {
    INPUT_YUV,      // 输入的是YUV
    INPUT_RGB,      // 输入的是RGB
};

class CainRender {
public:
    CainRender(RenderInputType type);
    virtual ~CainRender();
    void surfaceCreated(ANativeWindow *nativeWindow);
    void surfaceChanged(int width, int height);
    void surfaceDestroyed();
    void drawFrame(void *bufY, void *bufU, void *bufV);
    void changeFilter(GLImageFilter *filter);

private:
    void releaseFilters();
private:
    int mCurrentTexture;
    int mWidth;
    int mHeight;
    RenderInputType mInputType;
    ANativeWindow *mWindow;
    EglCore *mEglCore;
    WindowSurface *mWindowSurface;
    YUV420PImageFilter *mYUVFilter;
    GLImageFilter *mImageFilter;
    Mutex *mMutex;
};


#endif //CAINCAMERA_CAINRENDER_H
