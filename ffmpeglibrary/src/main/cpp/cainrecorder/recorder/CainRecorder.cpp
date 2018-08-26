//
// Created by cain.huang on 2018/1/4.
//

#include "CainRecorder.h"

/**
 * 构造方法
 * @param params
 */
CainRecorder::CainRecorder(EncoderParams * params)
        : params(params),
          recorderState(RECORDER_IDLE),
          encoder(NULL) {

}

/**
 * 初始化录制器
 * @return
 */
int CainRecorder::initRecorder() {
    if (!params) {
        ALOGE("error! EncoderParams is empty!");
        return -1;
    }

    encoder = new CainEncoder();
    encoder->setOutputFile(params->mediaPath);
    encoder->setVideoColorFormat(OMX_COLOR_FormatYUV420SemiPlanar);
    encoder->setVideoSize(params->previewWidth, params->previewHeight);
    encoder->setVideoFrameRate(params->frameRate);
    encoder->setVideoBitRate(params->bitRate);
    encoder->setEnableAudioEncode(params->enableAudio);
    encoder->setAudioBitRate(params->audioBitRate);
    encoder->setAudioSampleRate(params->audioSampleRate);
    if (encoder->initEncoder()) {
        return 0;
    }
    return -1;
}

/**
 * 开始录制
 */
void CainRecorder::startRecord() {
    recorderState = RECORDER_STARTED;
}

/**
 * 录制结尾
 */
void CainRecorder::recordEndian() {
    recorderState = RECORDER_STOPPED;
    if (encoder->stopEncode() != OK) {
        ALOGE("flushing encoder failed!\n");
    }
    // 释放资源
    release();
}

/**
 * h264编码
 * @param data yuv原始数据
 * @return
 */
int CainRecorder::avcEncode(jbyte *yuvData) {
    // 如果还没有开始录制，则跳过编码
    if (recorderState != RECORDER_STARTED) {
        return 0;
    }
    if (encoder != NULL) {
        encoder->videoEncode((uint8_t *) yuvData);
    }
    return 0;
}

/**
 * aac编码
 */
int CainRecorder::aacEncode(jbyte *pcmData) {
    // 如果还没有开始录制，则跳过编码
    if (recorderState != RECORDER_STARTED && !params->enableAudio) {
        return 0;
    }
    if (encoder != NULL) {
        encoder->audioEncode((uint8_t *) pcmData, encoder->getAudioEncodeSize());
    }
    return 0;
}


/**
 * 释放资源
 */
void CainRecorder::release() {
    recorderState = RECORDER_RELEASED;
    delete encoder;
}