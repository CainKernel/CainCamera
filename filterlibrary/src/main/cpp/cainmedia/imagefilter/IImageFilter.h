//
// Created by Administrator on 2018/3/2.
//

#ifndef CAINCAMERA_IIMAGEFILTER_H
#define CAINCAMERA_IIMAGEFILTER_H

#include <string.h>
#include "Color.h"


class IImageFilter {

public:
    IImageFilter(int *pixels = NULL, int width = 0, int height = 0)
            : width(width), height(height) {
        this->pixels = pixels;
    }

    virtual ~IImageFilter() {
        pixels = NULL;
    }

    void setPixels(int *pixels, int width, int height) {
        if (this->pixels != NULL) {
            delete[] pixels;
        }
        this->pixels = pixels;
        this->width = width;
        this->height = height;
    }

    virtual void processImage(int *destPixels) = 0;

protected:
    int *pixels;
    int width;
    int height;
};


#endif //CAINCAMERA_IIMAGEFILTER_H
