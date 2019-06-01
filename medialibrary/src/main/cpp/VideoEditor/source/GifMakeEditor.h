//
// Created by CainHuang on 2019/5/18.
//

#ifndef GIFMAKEEDITOR_H
#define GIFMAKEEDITOR_H


#include "Editor.h"

/**
 * 将裁剪一段视频并转化成GIF
 */
class GifMakeEditor : public Editor {
public:
    GifMakeEditor(const char *srcUrl, const char *dstUrl);

    ~GifMakeEditor();

    void setDuration(long start, long duration);

    int process() override;

private:

    int initSwsContext(int width, int height, int pixFmt);

    int openInput(const char *url);

    int openOutput(const char *url);

private:
    const char *srcUrl, *dstUrl;
    AVFormatContext *ifmt_ctx;
    AVCodecContext *dec_ctx;
    int out_width;
    int out_height;
    int frame_count;
    bool abort_request;
    int64_t frame_duration;
    AVFormatContext *ofmt_ctx;
    AVCodecContext *enc_ctx;
    SwsContext *sws_ctx;
    AVPixelFormat out_fmt;
    long start;
    long duration;
    AVRational time_base;
};


#endif //GIFMAKEEDITOR_H
