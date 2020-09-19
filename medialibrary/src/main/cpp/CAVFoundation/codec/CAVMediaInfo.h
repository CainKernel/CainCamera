//
// Created by CainHuang on 2020/9/5.
//

#ifndef CAVMEDIAINFO_H
#define CAVMEDIAINFO_H

/**
 * 视频参数集合
 */
typedef struct CAVVideoInfo {
    // codec id
    AVCodecID codec_id;
    // track index in media file.
    int track;
    // video width
    int width;
    // video height
    int height;
    // rotate degrees
    int rotate;
    // frame rate
    int frame_rate;
    // gop size of group of picture
    int gop_size;
    // bit rate
    int bit_rate;
    // profile
    int profile;
    // level
    int level;
} CAVVideoInfo;

/**
 * 音频参数集合
 */
typedef struct CAVAudioInfo {
    // codec id
    AVCodecID codec_id;
    // tack index in media file.
    int track;
    // sample rate
    int sample_rate;
    // channels
    int channels;
    // aac format S16/S8, etc
    int audio_format;
    // aac bit rate
    int bit_rate;
    // audio profile，AAC_LC/AAC_Main/AAC_LOW
    int profile;
} CAVAudioInfo;

#endif //CAVMEDIAINFO_H
