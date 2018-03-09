//
// Created by Administrator on 2018/3/9.
//

#ifndef CAINCAMERA_MOSAICFILTER_H
#define CAINCAMERA_MOSAICFILTER_H


#include "IImageFilter.h"

class MosaicFilter : public IImageFilter {

public:
    MosaicFilter(int *pixels, int width, int height);

    void setMosaicSize(int size) {
        mosaicSize = size;
    }

    void processImage(int *destPixels) override;

private:
    int mosaicSize;
};


#endif //CAINCAMERA_MOSAICFILTER_H
