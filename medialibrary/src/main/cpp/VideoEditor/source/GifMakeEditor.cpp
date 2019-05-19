//
// Created by CainHuang on 2019/5/18.
//

#include "GifMakeEditor.h"

GifMakeEditor::GifMakeEditor(const char *srcUrl, const char *dstUrl)
               : Editor(), srcUrl(srcUrl), dstUrl(dstUrl), start(0), duration(0) {
    abort_request = false;
    frame_count = 0;
    out_fmt = AV_PIX_FMT_RGB8;

}

GifMakeEditor::~GifMakeEditor() {
    abort_request = true;
    if (sws_ctx != NULL) {
        sws_freeContext(sws_ctx);
        sws_ctx = NULL;
    }
    if (dec_ctx != NULL) {
        avcodec_free_context(&dec_ctx);
        dec_ctx = NULL;
    }
    if (ifmt_ctx != NULL) {
        avformat_free_context(ifmt_ctx);
        ifmt_ctx = NULL;
    }
    if (enc_ctx != NULL) {
        avcodec_free_context(&enc_ctx);
        enc_ctx = NULL;
    }
    if (ofmt_ctx != NULL) {
        avformat_free_context(ofmt_ctx);
        ofmt_ctx = NULL;
    }
}

void GifMakeEditor::setDuration(long start, long duration) {
    this->start = start;
    this->duration = duration;
}

int GifMakeEditor::initSwsContext(int width, int height, int pixFmt) {
    sws_ctx = sws_getContext(width, height, (AVPixelFormat)pixFmt,
            out_width, out_height, out_fmt,
            SWS_BILINEAR, NULL, NULL, NULL);
    if (!sws_ctx) {
        return -1;
    }
    return 0;
}

int GifMakeEditor::openInput(const char *url) {
    int ret;
    ifmt_ctx = NULL;
    ret = openInputFile(url, &ifmt_ctx);
    if (ret < 0) {
        LOGE("Failed to open input file");
        return -1;
    }

    ret = getVideoDecodeContext(ifmt_ctx, &dec_ctx);
    if (ret < 0) {
        LOGE("Failed to get video codec context");
        return -1;
    }
    video_index = ret;
    return ret;
}

int GifMakeEditor::openOutput(const char *url) {
    int ret;
    ofmt_ctx = NULL;
    ret = initOutput(url, &ofmt_ctx);
    if (ret < 0) {
        LOGE("Failed to init output file");
        return -1;
    }
    AVCodecParameters *codecpar = avcodec_parameters_alloc();
    codecpar->width = out_width;
    codecpar->height = out_height;
    codecpar->format = out_fmt;
    ret = addVideoStream(ofmt_ctx, &enc_ctx, *codecpar);
    if (ret < 0) {
        LOGE("Failed to add video stream");
        return -1;
    }
    avcodec_parameters_free(&codecpar);
    out_video_index = ret;
    ret = writeHeader(ofmt_ctx, url);
    if (ret < 0) {
        LOGE("Failed to write header");
        return -1;
    }
    frame_duration = AV_TIME_BASE / out_frame_rate;
    return ret;
}

int GifMakeEditor::process() {
    int ret, seekFlag;
    AVPacket *pkt;
    AVFrame *oframe;
    av_register_all();
    ret = openInput(srcUrl);
    if (ret < 0) {
        LOGE("Failed to open input file");
        return ret;
    }
    LOGD("output path: %s", dstUrl);
    out_width = dec_ctx->width;
    out_height = dec_ctx->height;
    ret = openOutput(dstUrl);
    if (ret < 0) {
        LOGE("Failed to open output file");
        return ret;
    }
    ret = initSwsContext(dec_ctx->width, dec_ctx->height, dec_ctx->pix_fmt);
    if (ret < 0) {
        LOGE("Failed to init SwsContext");
        return ret;
    }
    pkt = av_packet_alloc();
    oframe = av_frame_alloc();
    oframe->width = out_width;
    oframe->height = out_height;
    oframe->format = out_fmt;
    ret = av_frame_get_buffer(oframe, 0);
    if (ret < 0) {
        LOGE("Failed to call av_frame_get_buffer -'%s'", av_err2str(ret));
        av_packet_free(&pkt);
        av_frame_free(&oframe);
        return -1;
    }

    // 定位到起始位置，av_seek_frame 有可能会造成比较大的误差，这里要用avformat_seek_file来处理
    seekFlag &= ~AVSEEK_FLAG_BYTE;
    ret = avformat_seek_file(ifmt_ctx, -1, INT64_MIN, (int64_t) (start / 1000 * AV_TIME_BASE), INT64_MAX, seekFlag);
    if (ret < 0) {
        LOGE("Failed to call avformat_seek_file -'%s'", av_err2str(ret));
        av_packet_free(&pkt);
        av_frame_free(&oframe);
        return -1;
    }
    int64_t pts = 0;
    while (!abort_request) {
        ret = av_read_frame(ifmt_ctx, pkt);
        if (ret < 0) {
            LOGE("Failed to call av_read_frame");
            break;
        }
        AVFrame *frame = NULL;
        if (pkt->stream_index == video_index) {
            // 解码
            frame = decodePacket(dec_ctx, pkt);
            av_packet_unref(pkt);
            if (frame != NULL) {
                pts = (int64_t) (frame->pts * av_q2d(ifmt_ctx->streams[video_index]->time_base) * 1000);
                if ((pts >= start) && (pts <= (start + duration))) {
                    ret = av_frame_make_writable(oframe);
                    if (ret < 0) {
                        LOGE("Failed to call av_frame_make_writable");
                        av_frame_unref(oframe);
                        av_frame_free(&frame);
                        break;
                    }
                    // 转码
                    sws_scale(sws_ctx, (const uint8_t *const *)frame->data, frame->linesize,
                            0, frame->height, oframe->data, oframe->linesize);
                    oframe->pts = frame_count * frame_duration;
                    frame_count++;

                    // 编码一帧数据
                    AVPacket *packet = encodeFrame(enc_ctx, oframe);
                    av_frame_unref(oframe);
                    if (packet != NULL) {
                        av_packet_rescale_ts(packet, time_base, ofmt_ctx->streams[out_video_index]->time_base);
                        packet->stream_index = 0;
                        ret = av_write_frame(ofmt_ctx, packet);
                        av_packet_free(&packet);
                        if (ret < 0) {
                            LOGE("Failed to call av_write_frame - '%s'", av_err2str(ret));
                            av_frame_unref(frame);
                            av_frame_free(&frame);
                            break;
                        }
                    }
                }
                av_frame_free(&frame);
            }
        } else {
            av_packet_unref(pkt);
        }
    }

    av_packet_free(&pkt);
    av_frame_free(&oframe);
    writeTailer(ofmt_ctx);
    return 0;
}