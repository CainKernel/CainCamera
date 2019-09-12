//
// Created by CainHuang on 2019/3/10.
//

#ifndef AUDIOCUTEDITOR_H
#define AUDIOCUTEDITOR_H

#include "MediaEditor.h"

class AudioCutEditor : public MediaEditor {

public:
    AudioCutEditor(const char *srcUrl, const char *dstUrl);

    virtual ~AudioCutEditor();

    // 设置采样区间
    void setDuration(long start, long duration);

    // 设置速度
    void setSpeed(float speed);

private:
    // 处理
    int process() override;

    // 打开输入文件
    int openInputFile(const char *filename, AVFormatContext **input_format_context,
                      int *audio_stream_idx, AVCodecContext **input_audio_codec_context);

    // 打开AAC输出文件
    int openAACOutputFile(const char *filename, AVFormatContext **output_format_context,
                          AVCodecContext *input_codec_context,
                          AVCodecContext **output_codec_context);

    // 创建重采样器
    int initResampler(AVCodecContext *input_codec_context,
                      AVCodecContext *output_codec_context,
                      SwrContext **resample_context);

    // 初始化重采样率
    int initConvertedSamples(uint8_t ***converted_input_samples,
                             AVCodecContext *output_codec_context, int frame_size);

    // 初始化AVAudioFifo缓冲区
    int initAudioFifo(AVAudioFifo **fifo, AVCodecContext *output_codec_context);

    // 音频重采样
    int convertSamples(const uint8_t **input_data, uint8_t **converted_data,
                       const int frame_size, SwrContext *resample_context);

    // 添加采样点到AVAudioFifo缓冲区
    int addSamplesToFifo(AVAudioFifo *fifo, uint8_t **converted_input_samples,
                         const int frame_size);

    // 解码音频帧
    int decodeAudioFrame(AVPacket *packet, AVFrame *frame,
                         AVCodecContext *input_codec_context, int *data_present);

    // 解码并写入AVAudioFifo缓冲区
    int decodeAndWriteToFifo(AVAudioFifo *fifo, AVCodecContext *input_codec_context,
                             AVCodecContext *output_codec_context,
                             SwrContext *resampler_context, AVPacket *packet);

    // 编码音频帧
    int encodeAudioFrame(AVFrame *frame, AVFormatContext *output_format_context,
                         AVCodecContext *output_codec_context, int *data_present);

    // 初始化输出帧
    int initAudioOutputFrame(AVFrame **frame, AVCodecContext *output_codec_context,
                             int frame_size);

    // 编码音频帧并写入文件
    int encodeAudioAndWrite(AVAudioFifo *fifo, AVFormatContext *output_format_context,
                            AVCodecContext *output_codec_context);
private:
    const char *srcUrl, *dstUrl;
    long mStart;
    long mDuration;
    float mSpeed;

    int64_t audio_pts;

    // 音频输出转码参数，为了方便对音频进行处理，需要进行重采样处理，转成 s16le 48kHz 的音频数据
    int output_channel = 2;
    int output_bit_rate = 96000;
    int output_sample_rate = 48000;
    AVSampleFormat output_sample_fmt = AV_SAMPLE_FMT_S16;
};


#endif //AUDIOCUTEDITOR_H
