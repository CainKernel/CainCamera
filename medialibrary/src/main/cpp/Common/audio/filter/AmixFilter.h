//
// Created by CainHuang on 2020-04-19.
//

#ifndef AMIXFILTER_H
#define AMIXFILTER_H

#include "Filter.h"

#include <AVMediaHeader.h>


/**
 * 混音Filter
 */
class AmixFilter : public Filter {
public:
    AmixFilter();

    virtual ~AmixFilter();

    int setOption(std::string key, std::string value) override;

    int init() override;

    int addFrame(AVFrame *frame, int index) override;

    AVFrame *getFrame() override;

private:
    bool mInited;

    int mInSampleRate0;         // 第0路输入采样率
    int mInSampleRate1;         // 第1路输入采样率
    int mOutSampleRate;         // 输出采样率

    uint64_t mInChannelLayout0; // 第0路声道数
    uint64_t mInChannelLayout1; // 第1路声道数
    uint64_t mOutChannelLayout; // 输出声道数

    AVSampleFormat mInFormat0;  // 第0路采样格式
    AVSampleFormat mInFormat1;  // 第1路采样格式
    AVSampleFormat mOutFormat;  // 输出采样格式

    AVFilterGraph *pFilterGraph;

    AVFilterContext *pBufferSrcContext0;
    AVFilterContext *pBufferSrcContext1;
    AVFilterContext *pBufferSinkContext;

    AVFilterContext *pFormatContext;
    AVFilterContext *pAmixContext;
};


#endif //AMIXFILTER_H
