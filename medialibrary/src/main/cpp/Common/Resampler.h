//
// Created by CainHuang on 2020/1/11.
//

#ifndef RESAMPLER_H
#define RESAMPLER_H

#include "AVMediaHeader.h"

/**
 * convert audio pcm data to expected value
 */
class Resampler {
public:
    Resampler();

    virtual ~Resampler();

    void release();

    // set input pcm params
    void setInput(int sample_rate, int channels, AVSampleFormat sample_fmt);

    // set output pcm params
    void setOutput(int sample_rate, uint64_t channel_layout, AVSampleFormat sample_fmt,
                   int channels, int frame_size);

    // init convertor
    int init();

    // convert data
    int resample(const uint8_t *data, int nb_samples);

    // convert data
    int resample(AVFrame *frame);

    // get converted frame
    AVFrame *getConvertedFrame();

    int getInputSampleRate();

    int getInputChannels();

    AVSampleFormat getInputSampleFormat();

private:
    SwrContext *pSampleConvertCtx;  // resample context
    AVFrame *mSampleFrame;          // converted frame
    uint8_t **mSampleBuffer;        // resample buffer
    int mNbSamples;                 // sample numbers

    int mOutSampleSize;             // output sample size
    int mOutSampleRate;             // output sample rate
    int64_t mOutChannelLayout;      // output channel layout
    AVSampleFormat mOutSampleFormat;// output sample format
    int mOutFrameSize;              // output frame size
    int mOutChannels;               // output channels

    int mInSampleRate;              // input sample rate
    int mInChannels;                // input channels
    int64_t mInChannelLayout;       // input channel layout
    AVSampleFormat mInSampleFormat; // input sample format
};


#endif //RESAMPLER_H
