//
// Created by CainHuang on 2020-02-27.
//

#ifndef AUDIOSTREAMPLAYER_H
#define AUDIOSTREAMPLAYER_H

#include <audio/AudioPlay.h>
#include <audio/AudioSLPlay.h>
#include <SafetyQueue.h>
#include <SonicAudioTranscoder.h>
#include <Resampler.h>
#include <decoder/DecodeAudioThread.h>
#include "StreamPlayListener.h"
#include "Timestamp.h"

/**
 * 音频流播放器
 */
class AudioStreamPlayer {
public:
    AudioStreamPlayer(const std::shared_ptr<StreamPlayListener> &listener = nullptr);

    virtual ~AudioStreamPlayer();

    void setTimestamp(std::shared_ptr<Timestamp> timestamp);

    void setDataSource(const char *path);

    void setDecoderName(const char *decoder);

    void setSpeed(float speed);

    void setLooping(bool looping);

    void setRange(float start, float end);

    void setVolume(float leftVolume, float rightVolume);

    void prepare();

    void start();

    void pause();

    void stop();

    void seekTo(float timeMs);

    float getDuration();

    bool isLooping();

    bool isPlaying();

    int onAudioProvide(short **buffer, int bufSize);

    void release();

    // 解码开始回调
    void onDecodeStart();

    // 解码结束回调
    void onDecodeFinish();

    // seek结束回调
    void onSeekComplete(float seekTime);

    // seek出错回调
    void onSeekError(int ret);

private:
    // 清空队列
    void flushQueue();

    // 设置当前播放时间戳
    void setCurrentTimestamp(float timeStamp);

private:
    std::shared_ptr<OnDecodeListener> mDecodeListener;
    std::shared_ptr<DecodeAudioThread> mAudioThread;
    std::shared_ptr<AudioProvider> mAudioProvider;
    std::shared_ptr<AudioPlay> mAudioPlayer;
    std::weak_ptr<StreamPlayListener> mPlayListener;
    SafetyQueue<AVMediaData *> *mFrameQueue;
    std::shared_ptr<SonicAudioTranscoder> mAudioTranscoder;
    std::weak_ptr<Timestamp> mTimestamp;

    float mSpeed;
    int mSampleRate;
    int mChannels;
    bool mLooping;
    bool mPrepared;
    bool mPlaying;
    int64_t mCurrentPts;
};

/**
 * 音频播放线程提供者
 */
class StreamAudioProvider : public AudioProvider {
public:
    StreamAudioProvider();

    virtual ~StreamAudioProvider();

    int onAudioProvide(short **buffer, int bufSize) override;

    void setPlayer(AudioStreamPlayer *player);

private:
    AudioStreamPlayer *player;
};

/**
 * 音频解码监听器
 */
class AudioDecodeListener : public OnDecodeListener {
public:
    AudioDecodeListener(AudioStreamPlayer *player);

    virtual ~AudioDecodeListener();

    void onDecodeStart(AVMediaType type) override;

    void onDecodeFinish(AVMediaType type) override;

    void onSeekComplete(AVMediaType type, float seekTime) override;

    void onSeekError(AVMediaType type, int ret) override;

private:
    AudioStreamPlayer *player;
};

#endif //AUDIOSTREAMPLAYER_H
