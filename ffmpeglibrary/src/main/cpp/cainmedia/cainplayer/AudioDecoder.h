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
};


#endif //CAINCAMERA_AUDIODECODER_H
