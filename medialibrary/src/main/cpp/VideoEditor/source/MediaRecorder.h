//
// Created by CainHuang on 2019/3/26.
//

#ifndef MEDIARECORDER_H
#define MEDIARECORDER_H

#include <AndroidLog.h>

extern "C" {
#include <libavutil/pixfmt.h>
#include <libavutil/imgutils.h>
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libavutil/avassert.h>
#include <libavutil/channel_layout.h>
#include <libavutil/opt.h>
#include <libavutil/time.h>
#include <libavutil/avutil.h>
#include <libavutil/mathematics.h>
#include <libswscale/swscale.h>
#include <libswresample/swresample.h>
};

typedef struct EncodeStream {
    AVStream *st;           // 媒体流
    AVCodecContext *enc;    // 编码上下文

    // 下一个pts
    int64_t next_pts;
    int samples_count;

    AVFrame *frame;
    AVFrame *tmp_frame;

    struct SwsContext *sws_ctx; // 转码上下文
    struct SwrContext *swr_ctx; // 重采样上下文
} EncodeStream;


/**
 * 媒体录制器
 */
class MediaRecorder {
public:
    MediaRecorder();

    virtual ~MediaRecorder();

    // 设置输出路径
    void setDataSource(const char *url);

    // 设置视频大小
    void setVideoSize(int width, int height);

    // 设置视频旋转角度
    void setVideoRotate(int rotate);

    // 设置帧率
    void setFrameRate(int frameRate);

    // 设置音频格式
    void setVideoFormat(AVPixelFormat format);

    // 设置是否允许音频编码
    void setAudioEnable(bool enable);

    // 设置音频码率
    void setAudioBitRate(int bitRate);

    // 初始化
    int openFile();

    // 输入视频数据
    int encodeAndWriteVideo(uint8_t *data);

    // 编码PCM并写入复用器
    int encodeAndWriteAudio(uint8_t *data, int len);

    // 清空编码器
    int flushEncoder();

    // 写入文件尾
    int writeTailer();

private:

    // 打开媒体流
    int openStream(EncodeStream *ost, AVFormatContext *oc, AVCodec **codec,
                   enum AVCodecID codec_id);

    // 关闭媒体流
    void closeStream(EncodeStream *ost);

    // 关闭文件
    void closeFile();

    // 打开音频编码器
    int openAudioEncoder(AVCodec *codec, EncodeStream *ost, AVDictionary *opt_arg);

    // 创建音频帧
    AVFrame *allocAudioFrame(int channels, enum AVSampleFormat sample_fmt, uint64_t channel_layout,
                             int sample_rate, int frame_size);

    // 打开视频编码器
    int openVideoEncoder(AVCodec *codec, EncodeStream *ost, AVDictionary *opt_arg);

    // 创建视频帧
    AVFrame *allocVideoFrame(enum AVPixelFormat pix_fmt, int width, int height);

    // 将编码后的数据写入复用器
    int writeFrame(AVFormatContext *fmt_ctx, AVPacket *pkt, int stream_index);

    MediaRecorder(const MediaRecorder&);
    MediaRecorder&operator=(const MediaRecorder&);

    bool isInited;
    AVOutputFormat *fmt;
    AVFormatContext *fmt_ctx;   // 复用上下文
    bool have_video;            // 是否存在视频流
    bool have_audio;            // 是否存在音频流

    // 视频编码流
    EncodeStream videoStream;
    // 音频编码流
    EncodeStream audioStream;
    // 输出文件
    const char *dstUrl;
    // 视频宽度
    int width;
    // 视频高度
    int height;
    int rotate;
    // 帧率
    int frameRate;
    // 码率
    long bitRate;
    // 是否允许音频编码
    int enableAudio;
    // 音频码率
    int audioBitRate;
    // 音频采样率
    int audioSampleRate;
    // 音频采样大小
    int audioSampleSize;
    // 像素格式
    AVPixelFormat pixelFmt;
};


#endif //MEDIARECORDER_H
