//
// Created by CainHuang on 2020-03-29.
//

#ifndef PIXELBUFFER_H
#define PIXELBUFFER_H

#include <cstring>

#if defined(__ANDROID__)
#include <GLES3/gl3.h>
#include <GLES3/gl3ext.h>
#endif

/**
 * PBO像素处理对象，用于上载/提取纹理数据
 * 一个PixelBuffer对象只能用于上载或提取纹理数据，不能同时用于上载和提取纹理数据
 */
class PixelBuffer {
public:
    PixelBuffer();

    virtual ~PixelBuffer();

    // 初始化的
    void init(int width, int height);

    // 释放资源
    void release();

    // 提取纹理数据
    GLubyte *getPixelBuffer();

    // 上传纹理数据
    void pushPixelBuffer(void *data, int texture);

private:
    int getDataSize();

private:
    int mWidth;
    int mHeight;
    GLuint mPboIds[2];
    int mIndex;
};


#endif //PIXELBUFFER_H
