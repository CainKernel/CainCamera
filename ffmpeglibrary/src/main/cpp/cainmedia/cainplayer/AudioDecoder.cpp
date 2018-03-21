//
// Created by Administrator on 2018/3/21.
//

#include "AudioDecoder.h"
#include "native_log.h"

AudioDecoder::AudioDecoder() : BaseDecoder() {

}

/**
 * 音频解码
 */
void AudioDecoder::decodeFrame() {
    int ret = 0;
    // 还没开始
    while (!mPrepared) {
        continue;
    }

}
