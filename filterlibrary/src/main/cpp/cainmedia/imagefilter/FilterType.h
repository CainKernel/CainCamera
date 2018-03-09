//
// Created by Administrator on 2018/3/9.
//

#ifndef CAINCAMERA_FILTERTYPE_H
#define CAINCAMERA_FILTERTYPE_H

enum AdjustType {
    None,       // 没有
    Brightness, // 亮度
    Contrast,   // 对比度
    Exposure,   // 曝光
    Hue,        // 色调
    Saturation, // 饱和度
    Sharpness,  // 锐度
};

enum FilterType {
    NONE,   // 没有滤镜
    Gray,   // 灰色滤镜
    Mosaic, // 马赛克滤镜
};


#endif //CAINCAMERA_FILTERTYPE_H
