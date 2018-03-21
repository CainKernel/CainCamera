//
// Created by Administrator on 2018/3/21.
//

#include "VideoDecoder.h"

VideoDecoder::VideoDecoder() : BaseDecoder() {

}

/**
 * 视频解码
 */
void VideoDecoder::decodeFrame() {
    int ret = 0;
    // 还没开始
    while (!mPrepared) {
        continue;
    }


}
