//
// Created by cain on 2018/9/8.
//

#include <cstdint>
#include "GaussianBlurFilter.h"
#include "FilterUtils.h"

GaussianBlurFilter::GaussianBlurFilter() : kernelSize(3), sigma(0.75f) {

}

GaussianBlurFilter::~GaussianBlurFilter() {

}

int GaussianBlurFilter::process(void *pixels, unsigned int width,
                                unsigned int height) {
    // 高斯模糊
    std::vector<float> imageArray = convertPixelsWidthPadding(pixels, width, height);
    imageArray = blur(imageArray, width, height);
    // 将结果赋值给resultPixels
    int32_t *currentPixels = (int32_t *) pixels;
    int newWidth = width + kernelSize * 2;
    for (int j = 0; j < height; j++) {
        int num = ((j + 3) * newWidth) + 3;
        for (int i = 0; i < width; ++i) {
            int pos = (num + i) * 3;
            int32_t color = currentPixels[j * width + i];
            currentPixels[j * width + i] = ARGB_COLOR(((color & 0xFF000000) >> 24),
                                                      (uint8_t)(imageArray[pos] * 255),
                                                      (uint8_t)(imageArray[pos + 1] * 255),
                                                      (uint8_t)(imageArray[pos + 2] * 255));
        }
    }
    return 0;
}

std::vector<float> GaussianBlurFilter::blur(std::vector<float> srcPixels, int width, int height) {

    std::vector<float> destPixels(srcPixels.begin(), srcPixels.end());
    int newWidth = width + kernelSize * 2;
    int newHeight = height + kernelSize * 2;

    // 计算系数
    float q = sigma;
    float q2 = q * q;
    float q3 = q2 * q;

    float b0 = 1.57825f + 2.44413f * q + 1.4281f * q2 + 0.422205f * q3;
    float b1 = 2.44413f * q + 2.85619f * q2 + 1.26661f * q3;
    float b2 = -(1.4281f * q2 + 1.26661f * q3);
    float b3 = 0.422205f * q3;

    float b = 1.0f - ((b1 + b2 + b3) / b0);

    // 横向高斯模糊处理
    destPixels = passBlur(destPixels, newWidth, newHeight, b0, b1, b2, b3, b);
    // 转换回来
    std::vector<float> transposedPixels(destPixels.size());
    transposedPixels = transpose(destPixels, transposedPixels, newWidth, newHeight);
    // 纵向高斯模糊处理
    transposedPixels = passBlur(transposedPixels, newHeight, newWidth, b0, b1, b2, b3, b);
    // 转换回来
    destPixels = transpose(transposedPixels, destPixels, newHeight, newWidth);
    // 返回高斯模糊的结果
    return destPixels;
}

std::vector<float> GaussianBlurFilter::passBlur(std::vector<float> pixels, int width, int height,
                                                float b0, float b1, float b2, float b3, float b) {
    float num = 1 / b0;
    int newWidth = width * 3;
    for (int i = 0; i < height; i++) {
        int stepLength = i * newWidth;
        for (int j = stepLength + 9; j < (stepLength + newWidth); j += 3) {
            pixels[j] = (b * pixels[j]) + ((((b1 * pixels[j - 3]) + (b2 * pixels[j - 6])) + (b3 * pixels[j - 9])) * num);
            pixels[j + 1] = (b * pixels[j + 1]) + ((((b1 * pixels[(j + 1) - 3]) + (b2 * pixels[(j + 1) - 6])) + (b3 * pixels[(j + 1) - 9])) * num);
            pixels[j + 2] = (b * pixels[j + 2]) + ((((b1 * pixels[(j + 2) - 3]) + (b2 * pixels[(j + 2) - 6])) + (b3 * pixels[(j + 2) - 9])) * num);
        }
        for (int k = ((stepLength + newWidth) - 9) - 3; k >= stepLength; k -= 3) {
            pixels[k] = (b * pixels[k]) + ((((b1 * pixels[k + 3]) + (b2 * pixels[k + 6])) + (b3 * pixels[k + 9])) * num);
            pixels[k + 1] = (b * pixels[k + 1]) + ((((b1 * pixels[(k + 1) + 3]) + (b2 * pixels[(k + 1) + 6])) + (b3 * pixels[(k + 1) + 9])) * num);
            pixels[k + 2] = (b * pixels[k + 2]) + ((((b1 * pixels[(k + 2) + 3]) + (b2 * pixels[(k + 2) + 6])) + (b3 * pixels[(k + 2) + 9])) * num);
        }
    }
    return pixels;
}

std::vector<float> GaussianBlurFilter::transpose(std::vector<float> input,
                                                 std::vector<float> output, int width,
                                                 int height) {
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < width; j++) {
            int index = (j * height) * 3 + (i * 3);
            int pos = (i * width) * 3 + (j * 3);
            output[index] = input[pos];
            output[index + 1] = input[pos + 1];
            output[index + 2] = input[pos + 2];
        }
    }
    return output;
}

std::vector<float> GaussianBlurFilter::convertPixelsWidthPadding(void *pixels, int width,
                                                                 int height) {
    int newHeight = height + kernelSize * 2;
    int newWidth = width + kernelSize * 2;
    std::vector<float> numArray((unsigned int)(newHeight * newWidth) * 3);
    int index = 0;
    int num = 0;
    int32_t *currentPixels = (int32_t *) pixels;
    for (int i = -3; num < newHeight; i++) {
        int y = i;
        if (i < 0)
        {
            y = 0;
        }
        else if (i >= height)
        {
            y = height - 1;
        }
        int count = 0;
        int negSize = -1 * kernelSize;
        while (count < newWidth) {
            int x = negSize;
            if (negSize < 0) {
                x = 0;
            } else if (negSize >= width) {
                x = width - 1;
            }
            // 计算一个高斯核的权重的值
            int32_t color = currentPixels[y * width + x];
            numArray[index] = (color & 0x000000FF) * 0.003921569f;
            numArray[index + 1] = ((color & 0x0000FF00) >> 8) * 0.003921569f;
            numArray[index + 2] = ((color & 0x00FF0000) >> 16) * 0.003921569f;

            count++;
            negSize++;
            index += 3;
        }
        num++;
    }
    return numArray;
}