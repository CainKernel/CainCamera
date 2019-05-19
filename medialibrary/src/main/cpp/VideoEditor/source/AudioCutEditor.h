//
// Created by CainHuang on 2019/3/10.
//

#ifndef AUDIOCUTEDITOR_H
#define AUDIOCUTEDITOR_H

#include <Editor.h>

class AudioCutEditor : public Editor {

public:
    AudioCutEditor(const char *srcUrl, const char *dstUrl);

    virtual ~AudioCutEditor();

    void setDuration(long start, long duration);

    int process() override;

private:
    int openInputFile(const char *filename, AVFormatContext **input_format_context,
                      int *audio_stream_idx, AVCodecContext **input_audio_codec_context);

    int openAACOutputFile(const char *filename, AVFormatContext **output_format_context,
                          AVCodecContext *input_codec_context,
                          AVCodecContext **output_codec_context);

    int initResampler(AVCodecContext *input_codec_context,
                      AVCodecContext *output_codec_context,
                      SwrContext **resample_context);

    int initConvertedSamples(uint8_t ***converted_input_samples,
                             AVCodecContext *output_codec_context, int frame_size);

    int initAudioFifo(AVAudioFifo **fifo, AVCodecContext *output_codec_context);

    int convertSamples(const uint8_t **input_data, uint8_t **converted_data,
                       const int frame_size, SwrContext *resample_context);

    int addSamplesToFifo(AVAudioFifo *fifo, uint8_t **converted_input_samples,
                         const int frame_size);

    int decodeAudioFrame(AVPacket *packet, AVFrame *frame,
                         AVCodecContext *input_codec_context, int *data_present);

    int decodeAndWriteToFifo(AVAudioFifo *fifo, AVCodecContext *input_codec_context,
                             AVCodecContext *output_codec_context,
                             SwrContext *resampler_context, AVPacket *packet);

    int encodeAudioFrame(AVFrame *frame, AVFormatContext *output_format_context,
                         AVCodecContext *output_codec_context, int *data_present);

    int initAudioOutputFrame(AVFrame **frame, AVCodecContext *output_codec_context,
                             int frame_size);

    int encodeAudioAndWrite(AVAudioFifo *fifo, AVFormatContext *output_format_context,
                            AVCodecContext *output_codec_context);
private:
    const char *srcUrl, *dstUrl;
    long start;
    long duration;

    int64_t audio_pts;

    // 音频输出转码参数，为了方便对音频进行处理，需要进行重采样处理，转成 s16le 48kHz 的音频数据
    int output_channel = 2;
    int output_bit_rate = 96000;
    int output_sample_rate = 48000;
    AVSampleFormat output_sample_fmt = AV_SAMPLE_FMT_S16;
};


#endif //AUDIOCUTEDITOR_H
