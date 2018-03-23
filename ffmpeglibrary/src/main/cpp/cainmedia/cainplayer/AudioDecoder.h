//
// Created by Administrator on 2018/3/21.
//

#ifndef CAINCAMERA_AUDIODECODER_H
#define CAINCAMERA_AUDIODECODER_H

#include "BaseDecoder.h"

// 音频解码器
class AudioDecoder : public BaseDecoder {
public:
    AudioDecoder();

    void decodeFrame() override;

private:
    int decodeAudioFrame(AVFrame *frame);

    int next_pts;               // 下一帧的pts
    AVRational next_pts_tb;     // 下一帧的时间基准
};


#endif //CAINCAMERA_AUDIODECODER_H
