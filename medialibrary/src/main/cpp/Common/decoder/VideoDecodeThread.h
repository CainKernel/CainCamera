//
// Created by CainHuang on 2020-02-24.
//

#ifndef VIDEODECODETHREAD_H
#define VIDEODECODETHREAD_H

#include <AVMediaHeader.h>
#include <map>
#include <demuxer/AVMediaDemuxer.h>
#include <decoder/AVAudioDecoder.h>
#include <AVMediaData.h>
#include <SafetyQueue.h>
#include "AVVideoDecoder.h"

typedef struct Picture {
    float pts;
    AVFrame *frame;
} Picture;

class VideoDecodeThread : public Runnable {
public:
    VideoDecodeThread(SafetyQueue<Picture *> *frameQueue);

    virtual ~VideoDecodeThread();

    void setDataSource(const char *url);

    void setInputFormat(const char *format);

    void setDecodeName(const char *decoder);

    void addFormatOptions(std::string key, std::string value);

    void addDecodeOptions(std::string key, std::string value);

    void seekTo(float timeMs);

    void setLooping(bool looping);

    // 设置播放区间
    void setRange(float start, float end);

    int prepare();

    void start();

    void pause();

    void stop();

    int getWidth();

    int getHeight();

    int getAvgFrameRate();

    int64_t getDuration();

    double getRotation();

    void run() override;

private:
    void flush();

    void release();

    int readPacket();

    int decodePacket(AVPacket *packet);

    bool isDecodeWaiting();

    int64_t calculatePts(int64_t pts, AVRational time_base);

    void freeFrame(AVFrame *frame);

    void seekFrame();

private:
    Mutex mMutex;
    Condition mCondition;
    Thread *mThread;

    std::map<std::string, std::string> mFormatOptions;
    std::map<std::string, std::string> mDecodeOptions;

    std::shared_ptr<AVMediaDemuxer> mVideoDemuxer;
    std::shared_ptr<AVVideoDecoder> mVideoDecoder;

    SafetyQueue<Picture *> *mFrameQueue;
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

};


#endif //VIDEODECODETHREAD_H
