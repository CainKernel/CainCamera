//
// Created by CainHuang on 2019/3/13.
//

#ifndef MACROS_H
#define MACROS_H

// 将s转成字符串
#define SHADER_TO_STRING(s) #s

#define PI 3.14159265358979323846264338327950288

// 限定最大最小值范围
#define clamp(value, mix, max) ((value) < (mix) ? (mix) : ((value) > (max) ? (max) : (value)))

#endif //MACROS_H
