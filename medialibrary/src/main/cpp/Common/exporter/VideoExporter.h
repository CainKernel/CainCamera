//
// Created by CainHuang on 2020-04-19.
//

#ifndef VIDEOEXPORTER_H
#define VIDEOEXPORTER_H

#include <Thread.h>
#include <decoder/DecodeVideoThread.h>
#include <writer/MediaWriter.h>
#include <video/GLVideoRender.h>

/**
 * 视频导出工具
 */
class VideoExporter : public Runnable {
public:
    VideoExporter();

    virtual ~VideoExporter();

    void setDataSource(const char *path);

    void setOutputPath(const char *path);

    void setSpeed(float speed);

    void setRange(float start, float end);

    void prepare();

    void cancel();

private:
    // 渲染编码线程
    Thread *mThread;
    // 解码监听器
    std::shared_ptr<OnDecodeListener> mDecodeListener;
    // 视频解码线程
    std::shared_ptr<DecodeVideoThread> mVideoThread;
    // 视频帧队列
    SafetyQueue<Picture *> *mFrameQueue;
    // 媒体写入器
    std::shared_ptr<MediaWriter> mMediaWriter;
    // 视频渲染器
    std::shared_ptr<GLVideoRender> mVideoRender;

    AVFrame *mConvertFrame;
    uint8_t *mBuffer;
    float mRefreshRate;     // 刷新频率, 默认30fps
    float mSpeed;

};


#endif //VIDEOEXPORTER_H
