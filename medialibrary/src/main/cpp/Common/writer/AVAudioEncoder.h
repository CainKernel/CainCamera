//
// Created by CainHuang on 2020-01-09.
//

#ifndef AVAUDIOENCODER_H
#define AVAUDIOENCODER_H

#include "AVMediaEncoder.h"

/**
 * 音频编码器
 */
class AVAudioEncoder : public AVMediaEncoder {
public:
    AVAudioEncoder(const std::shared_ptr<AVMediaMuxer> &mediaMuxer);

    virtual ~AVAudioEncoder();

    // 设置音频参数
    void setAudioParams(int bitrate, int sampleRate, int channels);

    AVMediaType getMediaType() override;

    AVCodecID getCodecId() override;
};


#endif //AVAUDIOENCODER_H
