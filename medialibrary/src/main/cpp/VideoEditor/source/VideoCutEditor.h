//
// Created by CainHuang on 2019/2/26.
//

#ifndef VIDEOCUTEDITOR_H
#define VIDEOCUTEDITOR_H

#include <Editor.h>
#include <Mutex.h>


/**
 * 视频裁剪变速处理，最后生成音频PCM文件，和视频H264文件
 */
class VideoCutEditor : public Editor {

public:
    VideoCutEditor(const char *srcUrl, const char *videoUrl, MessageHandle *messageHandle);

    virtual ~VideoCutEditor();

    void setDuration(long start, long duration);

    void setSpeed(float speed);

    int process() override;

private:
    Mutex *mMutex;
    const char *srcUrl, *dstUrl;
    long start;
    long duration;
    float speed;

    // 音频输出转码参数，为了方便对音频进行处理，需要进行重采样处理，转成 s16le 48kHz 的音频数据
    int output_channel = 2;
    int output_bit_rate = 96000;
    int output_sample_rate = 48000;
    AVSampleFormat output_sample_fmt = AV_SAMPLE_FMT_S16;
};

#endif //VIDEOCUTEDITOR_H
