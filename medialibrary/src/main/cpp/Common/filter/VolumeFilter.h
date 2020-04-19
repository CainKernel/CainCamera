//
// Created by CainHuang on 2020-04-19.
//

#ifndef VOLUMEFILTER_H
#define VOLUMEFILTER_H

#include "Filter.h"

#include <AVMediaHeader.h>

/**
 * 音量过滤Filter
 */
class VolumeFilter : public Filter {
public:
    VolumeFilter();

    virtual ~VolumeFilter();

    int setOption(std::string key, std::string value) override;

    int init() override;

    int addFrame(AVFrame *frame, int index) override;

    AVFrame *getFrame() override;

private:
    AVFilterGraph *pFilterGraph;

    AVFilterContext *pBufferSrcContext;
    AVFilterContext *pBufferSinkContext;
    AVFilterContext *pVolumeContext;
    AVFilterContext *pFormatContext;

    int mSampleRate;
    int mChannelLayout;
    AVSampleFormat mSampleFormat;
    std::string mVolumeValue;

    bool mInited;
};


#endif //VOLUMEFILTER_H
