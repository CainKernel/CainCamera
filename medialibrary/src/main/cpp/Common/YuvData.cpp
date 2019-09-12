//
// Created by CainHuang on 2019/8/17.
//

#include "YuvData.h"

YuvData::YuvData() : dataY(nullptr), dataU(nullptr), dataV(nullptr),
                     lineSizeY(0), lineSizeU(0), lineSizeV(0),
                     width(0), height(0) {

}

YuvData::YuvData(int width, int height) : dataY(new uint8_t[width * height]),
                                          dataU(new uint8_t[width * height / 4]),
                                          dataV(new uint8_t[width * height / 4]),
                                          lineSizeY(width), lineSizeU(width / 2), lineSizeV(width / 2),
                                          width(width), height(height) {

}

YuvData::~YuvData() {
    release();
}

void YuvData::alloc(int width, int height) {
    if (dataY || dataU || dataV) {
        release();
    }
    lineSizeY = width;
    lineSizeU = width / 2;
    lineSizeV = width / 2;
    dataY = new uint8_t[width * height];
    dataU = new uint8_t[width * height / 4];
    dataV = new uint8_t[width * height / 4];
    this->width = width;
    this->height = height;
}

void YuvData::setData(uint8_t *data) {
    memcpy(dataY, data, (size_t) (width * height));
    memcpy(dataU, data + width * height, (size_t) (width * height / 4));
    memcpy(dataV, data + width * height * 5 / 4, (size_t) (width * height / 4));
}

YuvData *YuvData::clone() {
    if (width <= 0 || height <= 0) {
        return nullptr;
    }
    YuvData *yuv = new YuvData();
    yuv->alloc(width, height);
    memcpy(yuv->dataY, dataY, (size_t) (width * height));
    memcpy(yuv->dataU, dataU, (size_t) (width * height / 4));
    memcpy(yuv->dataV, dataV, (size_t) (width * height / 4));
    return yuv;
}

void YuvData::release() {
    if (dataY != nullptr) {
        delete[] dataY;
        dataY = nullptr;
    }
    lineSizeY = 0;

    if (dataU != nullptr) {
        delete[] dataU;
        dataU = nullptr;
    }
    lineSizeU = 0;

    if (dataV != nullptr) {
        delete[] dataV;
        dataV = nullptr;
    }
    lineSizeV = 0;
}