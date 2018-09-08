//
// Created by cain on 2018/9/7.
//

#ifndef CAINCAMERA_BLENDFILTER_H
#define CAINCAMERA_BLENDFILTER_H

#ifdef __cplusplus
extern "C" {
#endif

// 默认模式 normal 公式 T = S

// 正片底叠模式 multiply 公式: T = S*D
unsigned char blendMultiply(unsigned char mask, unsigned char image);

// 正片底叠模式，带alpha
unsigned char blendMultiplyWithAlpha(unsigned char mask, unsigned char image, float alpha);

// 滤色模式 screen 公式: T = 1-(1-S)*(1-D);
unsigned char blendScreen(unsigned char mask, unsigned char image);

// 滤色模式，带alpha
unsigned char blendScreenWithAlpha(unsigned char mask, unsigned char image, float alpha);

// 叠加模式 overlay 公式: T = 2*S*D, D<0.5; T = 1-2*(1-S)*(1-D), D >= 0.5
unsigned char blendOverlay(unsigned char mask, unsigned char image);

// 叠加模式，带alpha
unsigned char blendOverlayWithAlpha(unsigned char mask, unsigned char image, float alpha);

// 强光模式 hardlight 公式: T = 2*S*D, S < 0.5; T = 1 - 2*(1-S)*(1-D), S >= 0.5;
unsigned char blendHardLight(unsigned char mask, unsigned char image);

// 柔光模式 softlight 公式: T = 2*S*D + D*D*(1-2*S), S < 0.5; T = 2*D*(1-S)+sqrt(D)*(2*S-1), S >= 0.5
unsigned char blendSoftLight(unsigned char mask, unsigned char image);

// 划分模式 divide 公式: T = D/S, D/S取值0~1之间
unsigned char blendDivide(unsigned char mask, unsigned char image);

// 增加模式 add 公式: T=D+S, D+S取值0~1之间
unsigned char blendAdd(unsigned char mask, unsigned char image);

// 减去模式 subtract 公式: T=D-S, D-S取值0~1之间
unsigned char blendSubtract(unsigned char mask, unsigned char image);

// 反色模式 diff 公式: T=|D-S|
unsigned char blendDiff(unsigned char mask, unsigned char image);

// 深色模式 darken 公式: T=MIN(D,S)
unsigned char blendDarken(unsigned char mask, unsigned char image);

// 浅色模式 lighten 公式: T=MAX(D,S)
unsigned char blendLighten(unsigned char mask, unsigned char image);

// 合并模式
unsigned char blendGrainMerge(unsigned char mask, unsigned char image);

#ifdef __cplusplus
}
#endif

#endif //CAINCAMERA_BLENDFILTER_H
