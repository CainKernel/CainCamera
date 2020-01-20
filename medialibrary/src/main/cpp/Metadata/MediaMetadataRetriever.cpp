//
// Created by cain on 2018/11/29.
//

#include <unistd.h>
#include "MediaMetadataRetriever.h"
#include "AndroidLog.h"

MediaMetadataRetriever::MediaMetadataRetriever() {
    av_register_all();
    avformat_network_init();
    state = NULL;
    mMetadata = new Metadata();
}

MediaMetadataRetriever::~MediaMetadataRetriever() {
    release();
    avformat_network_deinit();
    delete mMetadata;
    mMetadata = NULL;
}

void MediaMetadataRetriever::release() {
    Mutex::Autolock lock(mLock);
    release(&state);
}

int MediaMetadataRetriever::setDataSource(const char *url) {
    Mutex::Autolock lock(mLock);
    return setDataSource(&state, url, NULL);
}

status_t
MediaMetadataRetriever::setDataSource(const char *url, int64_t offset, const char *headers) {
    Mutex::Autolock lock(mLock);
    return setDataSource(&state, url, headers);
}

const char* MediaMetadataRetriever::getMetadata(const char *key) {
    Mutex::Autolock lock(mLock);
    return extractMetadata(&state, key);
}

const char* MediaMetadataRetriever::getMetadata(const char *key, int chapter) {
    Mutex::Autolock lock(mLock);
    return extractMetadata(&state, key, chapter);
}

int MediaMetadataRetriever::getMetadata(AVDictionary **metadata) {
    Mutex::Autolock lock(mLock);
    return getMetadata(&state, metadata);
}

int MediaMetadataRetriever::getEmbeddedPicture(AVPacket *pkt) {
    Mutex::Autolock lock(mLock);
    return getCoverPicture(&state, pkt);
}

int MediaMetadataRetriever::getFrame(int64_t timeUs, AVPacket *pkt) {
    Mutex::Autolock lock(mLock);
    return getFrame(&state, timeUs, pkt);
}

int MediaMetadataRetriever::getFrame(int64_t timeus, AVPacket *pkt, int width, int height) {
    Mutex::Autolock lock(mLock);
    return getFrame(&state, timeus, pkt, width, height);
}

/**
 * 设置数据源
 * @param ps
 * @param path
 * @param headers
 * @return
 */
int MediaMetadataRetriever::setDataSource(MetadataState **ps, const char *path, const char *headers) {
    MetadataState *state = *ps;

    init(&state);

    state->headers = headers;

    *ps = state;

    return setDataSource(ps, path);
}

/**
 * 解析metadata
 * @param ps
 * @param key
 * @return
 */
const char* MediaMetadataRetriever::extractMetadata(MetadataState **ps, const char *key) {
    char* value = NULL;

    MetadataState *state = *ps;

    if (!state || !state->pFormatCtx) {
        return value;
    }

    return mMetadata->extractMetadata(state->pFormatCtx, state->audioStream, state->videoStream, key);
}

/**
 * 解析metadata
 * @param ps
 * @param key
 * @param chapter
 * @return
 */
const char* MediaMetadataRetriever::extractMetadata(MetadataState **ps, const char *key,
                                                    int chapter) {
    char* value = NULL;

    MetadataState *state = *ps;

    if (!state || !state->pFormatCtx || state->pFormatCtx->nb_chapters <= 0) {
        return value;
    }

    if (chapter < 0 || chapter >= state->pFormatCtx->nb_chapters) {
        return value;
    }

    return mMetadata->extractMetadata(state->pFormatCtx, state->audioStream, state->videoStream,
                                     key, chapter);
}

/**
 * 获取metadata
 * @param ps
 * @param metadata
 * @return
 */
int MediaMetadataRetriever::getMetadata(MetadataState **ps, AVDictionary **metadata) {
    MetadataState *state = *ps;

    if (!state || !state->pFormatCtx) {
        return -1;
    }

    mMetadata->getMetadata(state->pFormatCtx, metadata);

    return 0;
}

/**
 * 获取专辑/封面图片
 * @param ps
 * @param pkt
 * @return
 */
int MediaMetadataRetriever::getCoverPicture(MetadataState **ps, AVPacket *pkt) {
    int i = 0;
    int got_packet = 0;
    AVFrame *frame = NULL;

    MetadataState *state = *ps;

    if (!state || !state->pFormatCtx) {
        return -1;
    }

    for (i = 0; i < state->pFormatCtx->nb_streams; i++) {
        if (state->pFormatCtx->streams[i]->disposition & AV_DISPOSITION_ATTACHED_PIC) {
            if (pkt) {
                av_packet_unref(pkt);
                av_init_packet(pkt);
            }
            av_copy_packet(pkt, &state->pFormatCtx->streams[i]->attached_pic);
            got_packet = 1;

            if (pkt->stream_index == state->videoStreamIndex) {
                int codec_id = state->videoStream->codec->codec_id;
                int pix_fmt = state->videoStream->codec->pix_fmt;

                if (!formatSupport(codec_id, pix_fmt)) {
                    int got_frame = 0;

                    frame = av_frame_alloc();

                    if (!frame) {
                        break;
                    }

                    // 解码视频帧
                    if (avcodec_decode_video2(state->videoStream->codec, frame, &got_frame, pkt) <= 0) {
                        break;
                    }

                    if (got_frame) {
                        AVPacket avPacket;
                        av_init_packet(&avPacket);
                        avPacket.size = 0;
                        avPacket.data = NULL;

                        encodeImage(state, state->videoStream->codec, frame, &avPacket,
                                    &got_packet, -1, -1);

                        av_packet_unref(pkt);
                        av_init_packet(pkt);
                        av_copy_packet(pkt, &avPacket);

                        av_packet_unref(&avPacket);
                        break;
                    }
                } else {
                    av_packet_unref(pkt);
                    av_init_packet(pkt);
                    av_copy_packet(pkt, &state->pFormatCtx->streams[i]->attached_pic);

                    got_packet = 1;
                    break;
                }
            }
        }
    }

    av_frame_free(&frame);

    return got_packet ? 0 : -1;
}

/**
 * 提取视频帧
 * @param ps
 * @param timeUs
 * @param pkt
 * @return
 */
int MediaMetadataRetriever::getFrame(MetadataState **ps, int64_t timeUs, AVPacket *pkt) {
    return getFrame(ps, timeUs, pkt, -1, -1);
}

/**
 * 提取视频帧
 * @param ps
 * @param timeUs
 * @param pkt
 * @param width
 * @param height
 * @return
 */
int MediaMetadataRetriever::getFrame(MetadataState **ps, int64_t timeUs, AVPacket *pkt, int width,
                                     int height) {
    int got_packet = 0;
    int64_t desired_frame_number = -1;

    MetadataState *state = *ps;

    if (!state || !state->pFormatCtx || state->videoStreamIndex < 0) {
        return -1;
    }

    if (timeUs > -1) {

        // 计算定位的时间，需要转换为time_base对应的时钟格式
        int stream_index = state->videoStreamIndex;
        ALOGD("timeUs = %lld", timeUs);
        int64_t seek_time = av_rescale_q(timeUs, AV_TIME_BASE_Q, state->pFormatCtx->streams[stream_index]->time_base);
        int64_t seek_stream_duration = state->pFormatCtx->streams[stream_index]->duration;
        if (seek_stream_duration > 0 && seek_time > seek_stream_duration) {
            seek_time = seek_stream_duration;
        }

        if (seek_time < 0) {
            return -1;
        }

        // 定位
        int ret = av_seek_frame(state->pFormatCtx, stream_index, seek_time, AVSEEK_FLAG_BACKWARD);

        // 刷新缓冲
        if (ret < 0) {
            return -1;
        } else {
            if (state->audioStreamIndex >= 0) {
                avcodec_flush_buffers(state->audioStream->codec);
            }
            if (state->videoStreamIndex >= 0) {
                avcodec_flush_buffers(state->videoStream->codec);
            }
        }
    }

    // 解码
    decodeFrame(state, pkt, &got_packet, desired_frame_number, width, height);

    return got_packet ? 0 : -1;
}

/**
 * 释放资源
 * @param ps
 */
void MediaMetadataRetriever::release(MetadataState **ps) {
    MetadataState *state = *ps;
    if (!state) {
        return;
    }
    if (state->audioStream && state->audioStream->codec) {
        avcodec_close(state->audioStream->codec);
    }
    if (state->videoStream && state->videoStream->codec) {
        avcodec_close(state->videoStream->codec);
    }
    if (state->pFormatCtx) {
        avformat_close_input(&state->pFormatCtx);
    }
    if (state->fd != -1) {
        close(state->fd);
    }
    if (state->pSwsContext) {
        sws_freeContext(state->pSwsContext);
        state->pSwsContext = NULL;
    }
    if (state->pCodecContext) {
        avcodec_close(state->pCodecContext);
        av_free(state->pCodecContext);
    }
    if (state->pSwsContext) {
        sws_freeContext(state->pSwsContext);
    }
    if (state->pScaleCodecContext) {
        avcodec_close(state->pScaleCodecContext);
        av_free(state->pScaleCodecContext);
    }
    if (state->pScaleSwsContext) {
        sws_freeContext(state->pScaleSwsContext);
    }
    av_freep(&state);
    ps = NULL;
}

/**
 * 判断格式是否支持
 * @param codec_id
 * @param pix_fmt
 * @return
 */
int MediaMetadataRetriever::formatSupport(int codec_id, int pix_fmt) {
    if ((codec_id == AV_CODEC_ID_PNG ||
         codec_id == AV_CODEC_ID_MJPEG ||
         codec_id == AV_CODEC_ID_BMP) &&
        pix_fmt == AV_PIX_FMT_RGBA) {
        return 1;
    }

    return 0;
}

/**
 * 初始化
 * @param ps
 */
void MediaMetadataRetriever::init(MetadataState **ps) {
    MetadataState *state = *ps;

    if (state && state->pFormatCtx) {
        avformat_close_input(&state->pFormatCtx);
    }

    if (state && state->fd != -1) {
        close(state->fd);
    }

    if (!state) {
        state = static_cast<MetadataState *>(av_mallocz(sizeof(MetadataState)));
    }

    state->pFormatCtx = NULL;
    state->audioStreamIndex = -1;
    state->videoStreamIndex = -1;
    state->audioStream = NULL;
    state->videoStream = NULL;
    state->fd = -1;
    state->offset = 0;
    state->headers = NULL;

    *ps = state;
}

/**
 * 设置数据源
 * @param ps 
 * @param path 
 * @return 
 */
int MediaMetadataRetriever::setDataSource(MetadataState **ps, const char *path) {
    int audioIndex = -1;
    int videoIndex = -1;

    MetadataState *state = *ps;

    AVDictionary *options = NULL;
    av_dict_set(&options, "icy", "1", 0);
    av_dict_set(&options, "user_agent", "FFmpegMediaMetadataRetriever", 0);

    if (state->headers) {
        av_dict_set(&options, "headers", state->headers, 0);
    }

    if (state->offset > 0) {
        state->pFormatCtx = avformat_alloc_context();
        state->pFormatCtx->skip_initial_bytes = state->offset;
    }

    if (avformat_open_input(&state->pFormatCtx, path, NULL, &options) != 0) {
        ALOGE("Metadata could not be retrieved\n");
        *ps = NULL;
        return -1;
    }

    if (avformat_find_stream_info(state->pFormatCtx, NULL) < 0) {
        ALOGE("Metadata could not be retrieved\n");
        avformat_close_input(&state->pFormatCtx);
        *ps = NULL;
        return -1;
    }


    // 查找媒体流
    for (int i = 0; i < state->pFormatCtx->nb_streams; i++) {
        if (state->pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO && videoIndex < 0) {
            videoIndex = i;
        }
        if (state->pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO && audioIndex < 0) {
            audioIndex = i;
        }
        // 设置编解码器信息
        mMetadata->setCodec(state->pFormatCtx, i);
    }
    // 打开音频流
    if (audioIndex >= 0) {
        openStream(state, audioIndex);
    }

    // 打开视频流
    if (videoIndex >= 0) {
        openStream(state, videoIndex);
    }

    // 设置metadata数据
    mMetadata->setDuration(state->pFormatCtx);
    mMetadata->setShoutcastMetadata(state->pFormatCtx);
    mMetadata->setRotation(state->pFormatCtx, state->audioStream, state->videoStream);
    mMetadata->setFrameRate(state->pFormatCtx, state->audioStream, state->videoStream);
    mMetadata->setFileSize(state->pFormatCtx);
    mMetadata->setChapterCount(state->pFormatCtx);
    mMetadata->setVideoSize(state->pFormatCtx, state->videoStream);

    *ps = state;
    return 0;
}

/**
 * 初始化缩放转码上下文
 * @param s
 * @param pCodecCtx
 * @param width
 * @param height
 * @return
 */
int MediaMetadataRetriever::initScaleContext(MetadataState *s, AVCodecContext *pCodecCtx, int width,
                                             int height) {
    AVCodec *targetCodec = avcodec_find_encoder(AV_CODEC_ID_PNG);
    if (!targetCodec) {
        ALOGE("avcodec_find_decoder() failed to find encoder\n");
        return -1;
    }

    s->pScaleCodecContext = avcodec_alloc_context3(targetCodec);
    if (!s->pScaleCodecContext) {
        ALOGE("avcodec_alloc_context3 failed\n");
        return -1;
    }

    s->pScaleCodecContext->bit_rate = s->videoStream->codec->bit_rate;
    s->pScaleCodecContext->width = width;
    s->pScaleCodecContext->height = height;
    s->pScaleCodecContext->pix_fmt = AV_PIX_FMT_RGBA;
    s->pScaleCodecContext->codec_type = AVMEDIA_TYPE_VIDEO;
    s->pScaleCodecContext->time_base.num = s->videoStream->codec->time_base.num;
    s->pScaleCodecContext->time_base.den = s->videoStream->codec->time_base.den;

    if (avcodec_open2(s->pScaleCodecContext, targetCodec, NULL) < 0) {
        ALOGE("avcodec_open2() failed\n");
        return -1;
    }

    s->pScaleSwsContext = sws_getContext(s->videoStream->codec->width,
                                         s->videoStream->codec->height,
                                         s->videoStream->codec->pix_fmt,
                                         width,
                                         height,
                                         AV_PIX_FMT_RGBA,
                                         SWS_BILINEAR,
                                         NULL,
                                         NULL,
                                         NULL);

    return 0;
}

/**
 * 打开媒体流
 * @param s 
 * @param streamIndex 
 * @return 
 */
int MediaMetadataRetriever::openStream(MetadataState *s, int streamIndex) {
    AVFormatContext *pFormatCtx = s->pFormatCtx;
    AVCodecContext *codecCtx;


    if (streamIndex < 0 || streamIndex >= pFormatCtx->nb_streams) {
        return -1;
    }

    // 解码上下文
    codecCtx = pFormatCtx->streams[streamIndex]->codec;

    // 查找解码器
    AVCodec *codec = avcodec_find_decoder(codecCtx->codec_id);
    if (codec == NULL) {
        ALOGE("avcodec_find_decoder() failed to find audio decoder\n");
        return -1;
    }

    // 打开解码器
    if (avcodec_open2(codecCtx, codec, NULL) < 0) {
        ALOGE("avcodec_open2() failed\n");
        return -1;
    }

    // 根据解码类型查找媒体
    switch(codecCtx->codec_type) {
        case AVMEDIA_TYPE_AUDIO: {
            s->audioStreamIndex = streamIndex;
            s->audioStream = pFormatCtx->streams[streamIndex];
            break;
        }

        case AVMEDIA_TYPE_VIDEO: {
            s->videoStreamIndex = streamIndex;
            s->videoStream = pFormatCtx->streams[streamIndex];

            AVCodec *pCodec = avcodec_find_encoder(AV_CODEC_ID_PNG);
            if (!pCodec) {
                ALOGE("avcodec_find_decoder() failed to find encoder\n");
                return -1;
            }

            s->pCodecContext = avcodec_alloc_context3(pCodec);
            if (!s->pCodecContext) {
                ALOGE("avcodec_alloc_context3 failed\n");
                return -1;
            }

            s->pCodecContext->bit_rate = s->videoStream->codec->bit_rate;
            s->pCodecContext->width = s->videoStream->codec->width;
            s->pCodecContext->height = s->videoStream->codec->height;
            s->pCodecContext->pix_fmt = AV_PIX_FMT_RGBA;
            s->pCodecContext->codec_type = AVMEDIA_TYPE_VIDEO;
            s->pCodecContext->time_base.num = s->videoStream->codec->time_base.num;
            s->pCodecContext->time_base.den = s->videoStream->codec->time_base.den;

            if (avcodec_open2(s->pCodecContext, pCodec, NULL) < 0) {
                ALOGE("avcodec_open2() failed\n");
                return -1;
            }

            s->pSwsContext = sws_getContext(s->videoStream->codec->width,
                                            s->videoStream->codec->height,
                                            s->videoStream->codec->pix_fmt,
                                            s->videoStream->codec->width,
                                            s->videoStream->codec->height,
                                            AV_PIX_FMT_RGBA,
                                            SWS_BILINEAR,
                                            NULL,
                                            NULL,
                                            NULL);
            break;
        }

        default: {
            break;
        }
    }
    return 0;
}

/**
 * 解码视频帧
 * @param state
 * @param pkt
 * @param got_frame
 * @param desired_frame_number
 * @param width
 * @param height
 */
void MediaMetadataRetriever::decodeFrame(MetadataState *state, AVPacket *pkt, int *got_frame,
                                         int64_t desired_frame_number, int width, int height) {
    AVFrame *frame = av_frame_alloc();

    *got_frame = 0;

    if (!frame) {
        return;
    }

    // 读入数据
    while (av_read_frame(state->pFormatCtx, pkt) >= 0) {
        // 找到视频流所在的裸数据
        if (pkt->stream_index == state->videoStreamIndex) {
            int codec_id = state->videoStream->codec->codec_id;
            int pix_fmt = state->videoStream->codec->pix_fmt;

            if (!formatSupport(codec_id, pix_fmt)) {
                *got_frame = 0;

                // 解码得到视频帧
                if (avcodec_decode_video2(state->videoStream->codec, frame, got_frame, pkt) <= 0) {
                    *got_frame = 0;
                    break;
                }

                // 图片转码
                if (*got_frame) {
                    if (desired_frame_number == -1 ||
                            (desired_frame_number != -1 && frame->pts >= desired_frame_number)) {
                        if (pkt->data) {
                            av_packet_unref(pkt);
                        }
                        av_init_packet(pkt);
                        encodeImage(state, state->videoStream->codec, frame, pkt, got_frame, width,
                                    height);
                        break;
                    }
                }
            } else {
                *got_frame = 1;
                break;
            }
        }
    }

    av_frame_free(&frame);
}

/**
 * 编码成图片
 * @param state
 * @param pCodecCtx
 * @param pFrame
 * @param packet
 * @param got_packet
 * @param width
 * @param height
 */
void MediaMetadataRetriever::encodeImage(MetadataState *state, AVCodecContext *pCodecCtx,
                                         AVFrame *pFrame, AVPacket *packet, int *got_packet,
                                         int width, int height) {
    AVCodecContext *codecCtx;
    struct SwsContext *scaleCtx;
    AVFrame *frame = av_frame_alloc();

    *got_packet = 0;

    if (width != -1 && height != -1) {
        if (state->pScaleCodecContext == NULL ||
            state->pScaleSwsContext == NULL) {
            initScaleContext(state, pCodecCtx, width, height);
        }

        codecCtx = state->pScaleCodecContext;
        scaleCtx = state->pScaleSwsContext;
    } else {
        codecCtx = state->pCodecContext;
        scaleCtx = state->pSwsContext;
    }

    if (width == -1) {
        width = pCodecCtx->width;
    }

    if (height == -1) {
        height = pCodecCtx->height;
    }

    // 设置视频帧参数
    frame->format = AV_PIX_FMT_RGBA;
    frame->width = codecCtx->width;
    frame->height = codecCtx->height;

    // 创建缓冲区
    int numBytes=av_image_get_buffer_size(AV_PIX_FMT_RGBA, pCodecCtx->width, pCodecCtx->height, 1);
    uint8_t *buffer = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));
    av_image_fill_arrays(frame->data,
                         frame->linesize,
                         buffer,
                         AV_PIX_FMT_RGBA,
                         codecCtx->width,
                         codecCtx->height, 1);

    // 转码
    sws_scale(scaleCtx,
              (const uint8_t * const *) pFrame->data,
              pFrame->linesize,
              0,
              pFrame->height,
              frame->data,
              frame->linesize);

    // 视频帧编码
    int ret = avcodec_encode_video2(codecCtx, packet, frame, got_packet);

    if (ret < 0) {
        *got_packet = 0;
    }

    // 释放内存
    av_frame_free(&frame);
    if (buffer) {
        free(buffer);
    }

    // 出错时释放裸数据包资源
    if (ret < 0 || !*got_packet) {
        av_packet_unref(packet);
    }
}


