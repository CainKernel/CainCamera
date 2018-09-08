//
// Created by cain on 2018/9/8.
//

#ifndef CAINCAMERA_LOOKUPTABLEFILTER_H
#define CAINCAMERA_LOOKUPTABLEFILTER_H


#include "ImageFilter.h"

class LookupTableFilter : public ImageFilter {
public:
    LookupTableFilter();

    virtual ~LookupTableFilter();

    int process(void *pixels, unsigned int width, unsigned int height) override;

    void setLookupPixels(void *lookupTable);

    void setStride(int stride);

private:
    int stride;
    unsigned char* lookupTable;
};


#endif //CAINCAMERA_LOOKUPTABLEFILTER_H
