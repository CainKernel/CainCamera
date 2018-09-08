//
// Created by cain on 2018/9/8.
//

#include "LookupTableFilter.h"
#include <stdio.h>

LookupTableFilter::LookupTableFilter() :stride(0), lookupTable(NULL) {

}

LookupTableFilter::~LookupTableFilter() {
    lookupTable = NULL;
}

int LookupTableFilter::process(void *pixels, unsigned int width,
                               unsigned int height) {
    if (width == 0 || height == 0 || lookupTable == NULL) {
        return -1;
    }

    int red, green, blue, offset, pos, nx, ny, k;
    unsigned char* srcPixels = (unsigned char *)pixels;
    // 计算偏移值
    offset = stride - (width << 2);
    for (int j = 0; j < height; j++) {
        for (int i = 0; i < width; i++) {
            // 取出原颜色值
            blue = srcPixels[0];
            green = srcPixels[1];
            red = srcPixels[2];
            // 根据颜色值计算出在LUT的位置
            k  =  (blue  >>  2);
            nx  =  (int)(red  >>  2)  +  ((k  -  ((k  >>  3)  <<  3))  <<  6);
            ny  =  (int)(((blue  >>  5)  <<  6)  +  (green  >>  2));
            pos  =  (nx  <<  2)  +  (ny  <<  11);
            // 将得到查找的颜色赋值
            srcPixels[0]  =  lookupTable[pos];
            srcPixels[1]  =  lookupTable[pos  +  1];
            srcPixels[2]  =  lookupTable[pos  +  2];
            srcPixels  +=  4; // 这里+4主要是保留alpha值
        }
        // 计算完每行像素需要进行偏移对齐
        srcPixels += offset;
    }
    return 0;
}

void LookupTableFilter::setStride(int stride) {
    this->stride = stride;
}

void LookupTableFilter::setLookupPixels(void *lookupTable) {
    this->lookupTable = (unsigned char *)lookupTable;
}