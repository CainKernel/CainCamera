//
// Created by admin on 2018/9/7.
//

#ifndef CAINCAMERA_COMMON_H
#define CAINCAMERA_COMMON_H

#ifdef __cplusplus
extern "C" {
#endif

// 最小值
#define min(x,y)  ((x) <= (y) ? (x) : (y))
// 最大值
#define max(x,y)  ((x) >= (y) ? (x) : (y))
// 限定 0 ~ 255之间
#define clamp(x) ((x) > 255 ? 255 : (x) < 0 ? 0 : (x))

// 将ARGB合并成一个值
#define ARGB_COLOR(alpha, red, green, blue) ((alpha) << 24)|((blue) << 16)|((green) << 8)|(red)

// 初始化整形数组
int newUnsignedIntArray(unsigned int** arrayPointer, unsigned int size);
// 释放无符号整形数组
void freeUnsignedIntArray(unsigned int** arrayPointer);


#ifdef __cplusplus
}
#endif

#endif //CAINCAMERA_COMMON_H
