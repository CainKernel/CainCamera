//
// Created by CainHuang on 2020-02-25.
//

#ifndef VIDEOPLAY_H
#define VIDEOPLAY_H

#include "../AVMediaHeader.h"

class VideoProvider {
public:
    virtual ~VideoProvider() = default;

    virtual int onVideoProvide(uint8_t *buffer, int width, int height, AVPixelFormat format = AV_PIX_FMT_RGBA) = 0;
};

class VideoPlay {
public:
    VideoPlay(const std::shared_ptr<VideoProvider> &videoProvider);

    virtual ~VideoPlay() = default;

    // 设置刷新频率
    void setRefreshRate(float rate);

    // 获取刷新频率
    float getRefteshRate();

    void setOutput(int width, int height);

    virtual void start() = 0;

    virtual void stop() = 0;

    virtual void pause() = 0;

    virtual void resume() = 0;

    virtual void requestRender() = 0;

    virtual bool isPlaying() = 0;
protected:
    std::weak_ptr<VideoProvider> mVideoProvider;
    int mWidth;
    int mHeight;
    float mRefreshRate;     // 刷新频率, 默认30fps
};

inline VideoPlay::VideoPlay(const std::shared_ptr<VideoProvider> &videoProvider) {
    mVideoProvider = videoProvider;
}

inline void VideoPlay::setRefreshRate(float rate) {
    mRefreshRate = rate;
}

inline float VideoPlay::getRefteshRate() {
    return mRefreshRate;
}

inline void VideoPlay::setOutput(int width, int height) {
    mWidth = width;
    mHeight = height;
}

#endif //VIDEOPLAY_H
