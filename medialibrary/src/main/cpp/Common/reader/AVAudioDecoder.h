//
// Created by CainHuang on 2020-01-10.
//

#ifndef AVAUDIODECODER_H
#define AVAUDIODECODER_H


#include "AVMediaDecoder.h"

class AVAudioDecoder : public AVMediaDecoder {
public:
    AVAudioDecoder(const std::shared_ptr<AVMediaDemuxer> &mediaDemuxer);

    virtual ~AVAudioDecoder();

    AVMediaType getMediaType() override;

    // 获取采样率
    int getSampleRate();

    // 获取声道格式
    AVSampleFormat getSampleFormat();

    // 获取声道数量
    int getChannels();

protected:
    void initMetadata() override;

private:
    int mSampleRate;
    AVSampleFormat mSampleFormat;
    int mChannels;
};


#endif //AVAUDIODECODER_H
