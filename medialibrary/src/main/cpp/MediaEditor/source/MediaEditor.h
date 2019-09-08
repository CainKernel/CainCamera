//
// Created by CainHuang on 2019/2/26.
//

#ifndef EDITOR_H
#define EDITOR_H

#include <Thread.h>
#include "EditorHeader.h"

/**
 * 编辑监听器
 */
class EditListener {
public:
    // 正在处理
    virtual void onProcessing(int percent) = 0;

    // 处理失败
    virtual void onFailed(const char *msg) = 0;

    // 处理成功
    virtual void onSuccess() = 0;
};

/**
 * 编辑器
 */
class MediaEditor : public Runnable {
public:
    MediaEditor();

    virtual ~MediaEditor() {
        release();
    }

    // 设置监听器
    void setListener(EditListener *listener);

    // 开始
    void start();

    // 停止处理
    void stop();

    void run() override;

protected:

    // 处理方法实体
    virtual int process() {
        return -1;
    };

    // 释放资源
    virtual void release();

    // 打开输入文件
    int openInputFile(const char *filename, AVFormatContext **fmt_ctx);
    // 获取解码上下文
    int getDecodeContext(AVFormatContext *fmt_ctx, AVCodecContext **dec_ctx, AVMediaType mediaType);
    // 获取音频解码上下文
    int getAudioDecodeContext(AVFormatContext *fmt_ctx, AVCodecContext **dec_ctx);
    // 获取视频解码上下文
    int getVideoDecodeContext(AVFormatContext *fmt_ctx, AVCodecContext **dec_ctx);
    // 解码一个数据包
    AVFrame *decodePacket(AVCodecContext *codec_ctx, AVPacket *packet);
    // 编码一帧数据
    AVPacket *encodeFrame(AVCodecContext *codec_ctx, AVFrame *frame);

    // 初始化输出上下文
    int initOutput(const char *url, AVFormatContext **fmt_ctx);
    // 初始化输出上下文，带格式
    int initOutput(const char *url, AVFormatContext **fmt_ctx, const char *format);
    // 添加输出视频流
    int addVideoStream(AVFormatContext *fmt_ctx, AVCodecContext **codec_ctx, AVCodecParameters codecpar, int out_frame_rate);
    // 添加输出音频流
    int addAudioStream(AVFormatContext *fmt_ctx, AVCodecContext **codec_ctx, AVCodecParameters codecpar);
    // 写入文件头部信息
    int writeHeader(AVFormatContext *fmt_ctx, const char *url);
    // 写入文件尾部信息
    int writeTrailer(AVFormatContext *fmt_ctx);

protected:
    Mutex mMutex;
    Condition mCondition;
    Thread *mThread;
    EditListener *mEditListener; // 编辑监听器
    bool abortRequest;  // 停止处理
    bool mExit;         // 已经退出
};

#endif //EDITOR_H
