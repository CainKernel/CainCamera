//
// Created by CainHuang on 2020-02-24.
//

#ifndef DECODEVIDEOTHREAD_H
#define DECODEVIDEOTHREAD_H

#include <AVMediaHeader.h>
#include <map>
#include <demuxer/AVMediaDemuxer.h>
#include <decoder/AVAudioDecoder.h>
#include <AVMediaData.h>
#include <SafetyQueue.h>
#include "AVVideoDecoder.h"

#define MAX_FRAME 30

typedef struct Picture {
    float pts;
    AVFrame *frame;
} Picture;

class DecodeVideoThread : public Runnable {
public:
    DecodeVideoThread();

    virtual ~DecodeVideoThread();

    void setDecodeFrameQueue(SafetyQueue<Picture *> *frameQueue);

    void setDataSource(const char *url);

    void setInputFormat(const char *format);

    void setDecodeName(const char *decoder);

    void addFormatOptions(std::string key, std::string value);

    void addDecodeOptions(std::string key, std::string value);

    void setDecodeOnPause(bool decodeOnPause);

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

    int getFrameRate();

    int64_t getDuration();

    double getRotation();

    bool isSeeking();

    void run() override;

private:
    void flush();

    void release();

    int readPacket();

    int readAndDecode();

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

    bool mDecodeOnPause;    // 允许暂停状态下解码标志
    bool mSeekRequest;
    float mSeekTime; // 毫秒(ms)
    int64_t mSeekPos;

    bool mDecodeEnd;
    float mStartPosition;
    float mEndPosition;

};


#endif //DECODEVIDEOTHREAD_H
