//
// Created by CainHuang on 2020-04-19.
//

#ifndef FILTER_H
#define FILTER_H

#include <string>

extern "C" {
#include <libavutil/frame.h>
};

class Filter {

public:
    virtual ~Filter() = default;

    // 设置音频参数
    virtual int setOption(std::string key, std::string value) = 0;

    // 初始化AVFilter
    virtual int init() = 0;

    // 添加一帧待过滤的数据
    virtual int addFrame(AVFrame *frame, int index = 0) = 0;

    // 获取过滤后的数据
    virtual AVFrame *getFrame() = 0;
};

#endif //FILTER_H
