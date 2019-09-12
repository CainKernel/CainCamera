//
// Created by CainHuang on 2019/8/17.
//

#ifndef YUVDATA_H
#define YUVDATA_H

#include "AVMediaHeader.h"

/**
 * Yuv数据对象
 */
class YuvData {
public:
    YuvData();

    YuvData(int width, int height);

    ~YuvData();

    void alloc(int width, int height);

    YuvData* clone();

    void setData(uint8_t *data);

    void release();

public:
    int width;
    int height;

    uint8_t *dataY;
    uint8_t *dataU;
    uint8_t *dataV;

    int lineSizeY;
    int lineSizeU;
    int lineSizeV;
};


#endif //YUVDATA_H
