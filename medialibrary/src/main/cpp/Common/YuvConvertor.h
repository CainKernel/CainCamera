//
// Created by CainHuang on 2019/8/24.
//

#ifndef YUVCONVERTOR_H
#define YUVCONVERTOR_H

#include "AVFormatter.h"
#include "AVMediaData.h"
#include "YuvData.h"

/**
 * YUV转换器
 */
class YuvConvertor {

public:
    YuvConvertor();

    virtual ~YuvConvertor();

    // 设置输入参数
    void setInputParams(int width, int height, int pixelFormat);

    // 设置裁剪区域
    void setCrop(int x, int y, int width, int height);

    // 设置旋转角度
    void setRotate(int degree);

    // 设置缩放宽高
    void setScale(int width, int height);

    // 设置是否镜像
    void setMirror(bool mirror);

    // 准备yuv转换器
    int prepare();

    // 转换数据
    int convert(AVMediaData *mediaData);

    // 获取输出宽度
    int getOutputWidth();

    // 获取输出高度
    int getOutputHeight();

private:
    // 重置所有参数
    void reset();

    // 释放所有资源
    void release();

    // 缩放处理
    int scale(YuvData *src, int srcW, int srcH);

    // 镜像处理
    int mirror(YuvData *src, int srcW, int srcH);

    // 填充媒体数据
    void fillMediaData(AVMediaData *model, YuvData *src, int srcW, int srcH);

private:

    int mWidth;             // 源宽度
    int mHeight;            // 源高度
    int mPixelFormat;       // 图像格式
    bool mNeedConvert;    // 是否允许转换

    int mCropX;
    int mCropY;
    int mCropWidth;
    int mCropHeight;
    libyuv::RotationMode mRotationMode;
    int mScaleWidth;
    int mScaleHeight;
    bool mMirror;

    YuvData *pCropData;
    YuvData *pScaleData;
    YuvData *pMirrorData;
};


#endif //YUVCONVERTOR_H
