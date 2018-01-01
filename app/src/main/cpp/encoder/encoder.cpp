//
// Created by cain on 2018/1/1.
//

#include "encoder.h"
#include "native_log.h"
MediaEncoder::MediaEncoder(EncoderParams *params) : mEncoderParams(params) {

}

/**
* 停止录制
*/
void MediaEncoder::sendStop() {
    isEnd = 1;
}

/**
* 释放资源
*/
void MediaEncoder::release() {
    isRelease = 1;
}

/**
* 发送帧到编码队列
* @param buf
* @return
*/
int MediaEncoder::sendOneFrame(uint8_t *buf) {
    uint8_t *new_buf = (uint8_t *) malloc(mBufferSize);
    memcpy(new_buf, buf, mBufferSize);
    mFrameQueue.push(new_buf);
    return 0;
}

/**
 * 获取多媒体码流
 * @return
 */
AVStream* MediaEncoder::getMediaStream() {
    return mMediaStream;
}