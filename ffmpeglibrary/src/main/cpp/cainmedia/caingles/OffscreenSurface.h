//
// Created by Administrator on 2018/3/21.
//

#ifndef CAINCAMERA_OFFSCREENSURFACE_H
#define CAINCAMERA_OFFSCREENSURFACE_H


#include "EglSurfaceBase.h"

class OffscreenSurface : public EglSurfaceBase {
public:
    OffscreenSurface(EglCore *eglCore, int width, int height);
    void release();
};


#endif //CAINCAMERA_OFFSCREENSURFACE_H
