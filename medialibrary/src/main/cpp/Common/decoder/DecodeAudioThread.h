//
// Created by CainHuang on 2020-02-22.
//

#ifndef DECODEAUDIOTHREAD_H
#define DECODEAUDIOTHREAD_H

#include <AVMediaHeader.h>
#include <map>
#include <demuxer/AVMediaDemuxer.h>
#include <decoder/AVAudioDecoder.h>
#include <AVMediaData.h>
#include <SafetyQueue.h>

/**
 * 音频解码线程
 */
class DecodeAudioThread : public Runnable {
public:
    DecodeAudioThread();

    virtual ~DecodeAudioThread();

    void setOnDecodeListener(const std::shared_ptr<OnDecodeListener> &listener);

    void setDecodeFrameQueue(SafetyQueue<AVMediaData *> *frameQueue);

    void setDataSource(const char *url);

    void setInputFormat(const char *format);

    void setDecodeName(const char *decoder);

    void addFormatOptions(std::string key, std::string value);

    void addDecodeOptions(std::string key, std::string value);

    void setOutput(int sampleRate, int channel, AVSampleFormat format = AV_SAMPLE_FMT_S16);

    void seekTo(float timeMs);

    void setLooping(bool looping);

    // 设置播放区间
    void setRange(float start, float end);

    int prepare();

    void start();

    void pause();

    void stop();

    int64_t getDuration();

    void run() override;

private:
    void seekFrame();

    void flush();

    void release();

    int readPacket();

    int decodePacket(AVPacket *packet);

    void initResampleContext();

    bool isDecodeWaiting();

    int reallocBuffer(int nb_samples);

    int64_t calculatePts(int64_t pts, AVRational time_base);

private:
    Mutex mMutex;
    Condition mCondition;
    Thread *mThread;

    std::map<std::string, std::string> mFormatOptions;
    std::map<std::string, std::string> mDecodeOptions;

    std::weak_ptr<OnDecodeListener> mDecodeListener;
    std::shared_ptr<AVMediaDemuxer> mAudioDemuxer;
    std::shared_ptr<AVAudioDecoder> mAudioDecoder;

    SafetyQueue<AVMediaData *> *mFrameQueue;

    int mMaxBufferSize;
    int mOutSampleRate;
    int mOutChannels;
    AVSampleFormat mOutFormat;

    SwrContext *pSwrContext;
    AVFrame *mFrame;
    uint8_t *mBuffer;
    AVPacket mPacket;
    int mMaxFrame;
    bool mAbortRequest;
    bool mPauseRequest;
    bool mLooping;
    bool mSeekRequest;
    float mSeekTime;

    bool mDecodeEnd;
    float mStartPosition;
    float mEndPosition;

    // 计算出来的下一帧时间戳
    int64_t mNextPts;
    AVRational mNextTimebase;
};


#endif //DECODEAUDIOTHREAD_H
