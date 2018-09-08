//
// Created by cain on 2018/9/8.
//

#ifndef CAINCAMERA_GAUSSIANBLURFILTER_H
#define CAINCAMERA_GAUSSIANBLURFILTER_H

#include <vector>
#include "ImageFilter.h"

/**
 * 高斯模糊滤镜
 * 关于高斯模糊，可以看这篇文章，里面详细介绍了高斯模糊与图像卷积滤波的过程：
 * https://www.jianshu.com/p/8d2d93c4229b
 */
class GaussianBlurFilter : public ImageFilter {
public:
    GaussianBlurFilter();

    virtual ~GaussianBlurFilter();

    int process(void *pixels, unsigned int width, unsigned int height) override;

private:
    // 模糊过程
    std::vector<float> blur(std::vector<float> srcPixels, int width, int height);
    // 横向还是纵向模糊
    std::vector<float> passBlur(std::vector<float> pixels, int width, int height, float b0, float b1,
                                float b2, float b3, float b);
    // 颠倒
    std::vector<float> transpose(std::vector<float> input, std::vector<float> output, int width, int height);
    // 转换
    std::vector<float> convertPixelsWidthPadding(void *pixels, int width, int height);

private:
    int kernelSize;
    float sigma;
};


#endif //CAINCAMERA_GAUSSIANBLURFILTER_H
