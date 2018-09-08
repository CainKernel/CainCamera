//
// Created by cain on 2018/9/7.
//

#include "BlendFilter.h"
#include "FilterUtils.h"
#include <math.h>
#include <stdlib.h>
/**
 * 正片底叠模式 multiply 公式: T = S*D
 * @param mask
 * @param image
 * @return
 */
unsigned char blendMultiply(unsigned char mask, unsigned char image) {
    return (unsigned char)((float)((int)image * (int)mask)/255);
}

/**
 * 正片底叠模式，带alpha
 * @param mask
 * @param image
 * @param alpha
 * @return
 */
unsigned char blendMultiplyWithAlpha(unsigned char mask, unsigned char image, float alpha) {
    return (unsigned char)((float)((int)image * ((int)mask * alpha))/255);
}

/**
 * 滤色模式 screen 公式: T = 1-(1-S)*(1-D);
 * @param mask
 * @param image
 * @return
 */
unsigned char blendScreen(unsigned char mask, unsigned char image) {
    return (unsigned char)(255.0f - (((255.0f - (float)mask) * (255.0f - image)) / 255.0f));
}

/**
 * 滤色模式，带alpha
 * @param mask
 * @param image
 * @param alpha
 * @return
 */
unsigned char blendScreenWithAlpha(unsigned char mask, unsigned char image, float alpha) {
    return (unsigned char)(255.0f - (((255.0f - ((float)mask * alpha)) * (255.0f - image)) / 255.0f));
}

/**
 * 叠加模式 overlay 公式: T = 2*S*D, D<0.5; T = 1-2*(1-S)*(1-D), D >= 0.5
 * @param mask
 * @param image
 * @return
 */
unsigned char blendOverlay(unsigned char mask, unsigned char image) {
    return (unsigned char)((image > 128) ? 255 - ((2 * (255 - mask) * (255 - image))/256) : (2 * mask * image) / 256);
}

/**
 * 叠加模式，带alpha
 * @param mask
 * @param image
 * @param alpha
 * @return
 */
unsigned char blendOverlayWithAlpha(unsigned char mask, unsigned char image, float alpha) {
    float underlay = image * alpha;
    return (unsigned char)((image > 128) ? 255 - ((2 * (255 - mask) * (255 - underlay))/256) : (2 * mask * underlay) / 256);
}

/**
 * 强光模式 hardlight 公式: T = 2*S*D, S < 0.5; T = 1 - 2*(1-S)*(1-D), S >= 0.5;
 * @param mask
 * @param image
 * @return
 */
unsigned char blendHardLight(unsigned char mask, unsigned char image) {
    return (unsigned char)((mask < 128) ? (2 * mask * image) / 256 : 255 - ((2 * (255 - mask) * (255 - image))/256));
}

/**
 * 柔光模式 softlight 公式: T = 2*S*D + D*D*(1-2*S), S < 0.5; T = 2*D*(1-S)+sqrt(D)*(2*S-1), S >= 0.5
 * @param mask
 * @param image
 * @return
 */
unsigned char blendSoftLight(unsigned char mask, unsigned char image) {
    double result = ((mask < 128) ? (2 * mask * image) / 256 + (image / 256) * (image / 256) * (255 - 2 * mask) : (2 * image *(255 - mask) / 256) + sqrt(image / 256) * (2 * mask - 255));
    return (unsigned char)clamp(result);
}

/**
 * 划分模式 divide 公式: T = D/S, D/S取值0~1之间
 * @param mask
 * @param image
 * @return
 */
unsigned char blendDivide(unsigned char mask, unsigned char image) {
    return (unsigned char)clamp(256 * image / mask);
}

/**
 * 增加模式 add 公式: T=D+S, D+S取值0~之间
 * @param mask
 * @param image
 * @return
 */
unsigned char blendAdd(unsigned char mask, unsigned char image) {
    return (unsigned char)clamp(image + mask);
}

/**
 * 减去模式 subtract 公式: T=D-S, D-S取值0~1之间
 * @param mask
 * @param image
 * @return
 */
unsigned char blendSubtract(unsigned char mask, unsigned char image) {
    return (unsigned char)clamp(image - mask);
}

/**
 * 反色模式 diff 公式: T=|D-S|
 * @param mask
 * @param image
 * @return
 */
unsigned char blendDiff(unsigned char mask, unsigned char image) {
    return (unsigned char)abs(image - mask);
}

/**
 * 深色模式 darken 公式: T=MIN(D,S)
 * @param mask
 * @param image
 * @return
 */
unsigned char blendDarken(unsigned char mask, unsigned char image) {
    return min(mask, image);
}

/**
 * 浅色模式 lighten 公式: T=MAX(D,S)
 * @param mask
 * @param image
 * @return
 */
unsigned char blendLighten(unsigned char mask, unsigned char image) {
    return max(mask, image);
}

/**
 * 合并模式
 * @param mask
 * @param image
 * @return
 */
unsigned char blendGrainMerge(unsigned char mask, unsigned char image) {
    return (unsigned char)clamp(((int)mask + image) - 128);
}