//
// Created by CainHuang on 2019/8/11.
//

#include "AVFrameFilter.h"
#include "AVFormatter.h"

#define RATIONAL_MAX 1000000

#define SAMPLE_SIZE 1024

AVFrameFilter::AVFrameFilter() {
    av_register_all();
    avfilter_register_all();

    mWidth = 0;
    mHeight = 0;
    mFrameRate = 0;
    mInPixelFormat = AV_PIX_FMT_NONE;
    mOutPixelFormat = AV_PIX_FMT_NONE;
    mVideoFilter = "null";
    mVideoEnable = false;
    mVideoBuffersrcCtx = nullptr;
    mVideoBuffersinkCtx = nullptr;
    mVideoFilterGraph = nullptr;

    mInSampleRate = 0;
    mInChannels = 0;
    mOutSampleRate = 0;
    mOutChannels = 0;
    mInSampleFormat = AV_SAMPLE_FMT_NONE;
    mOutSampleFormat = AV_SAMPLE_FMT_NONE;
    mAudioFilter = "anull";
    mAudioEnable = false;
    mAudioBuffersrcCtx = nullptr;
    mAudioBuffersinkCtx = nullptr;
    mAudioFilterGraph = nullptr;
}

AVFrameFilter::~AVFrameFilter() {
    release();
}

/**
 * 设置视频输入参数
 * @param width
 * @param height
 * @param pixelFormat
 * @param frameRate
 * @param filter
 */
void AVFrameFilter::setVideoInput(int width, int height, AVPixelFormat pixelFormat, int frameRate,
                                  const char *filter) {
    mWidth = width;
    mHeight = height;
    mInPixelFormat = pixelFormat;
    mFrameRate = frameRate;
    if (mOutPixelFormat == AV_PIX_FMT_NONE) {
        mOutPixelFormat = pixelFormat;
    }
    mVideoFilter = (filter == nullptr) ? "null" : filter;
    mVideoEnable = true;
}

/**
 * 设置视频输出参数
 * @param format
 */
void AVFrameFilter::setVideoOutput(AVPixelFormat format) {
    mOutPixelFormat = format;
}

/**
 * 设置音频输入参数
 * @param sampleRate
 * @param channels
 * @param sampleFormat
 * @param filter
 */
void AVFrameFilter::setAudioInput(int sampleRate, int channels, AVSampleFormat sampleFormat,
                                  const char *filter) {
    mInSampleRate = sampleRate;
    mInChannels = channels;
    mInSampleFormat = sampleFormat;
    if (mOutSampleRate == 0) {
        mOutSampleRate = sampleRate;
    }
    if (mOutChannels == 0) {
        mOutChannels = channels;
    }
    if (mOutSampleFormat == AV_SAMPLE_FMT_NONE) {
        mOutSampleFormat = sampleFormat;
    }
    mAudioFilter = (filter == nullptr) ? "anull" : filter;
    mAudioEnable = true;
}

/**
 * 设置音频输出参数
 * @param sampleRate
 * @param channels
 * @param sampleFormat
 */
void AVFrameFilter::setAudioOutput(int sampleRate, int channels, AVSampleFormat sampleFormat) {
    mOutSampleRate = sampleRate;
    mOutChannels = channels;
    mOutSampleFormat = sampleFormat;
}

/**
 * 初始化AVFilter
 * @return
 */
int AVFrameFilter::initFilter() {
    if (mVideoEnable) {
        initVideoFilter();
    }
    if (mAudioEnable) {
        initAudioFilter();
    }
    return 0;
}

/**
 * 初始化AVFilter
 */
int AVFrameFilter::initFilter(AVMediaType type) {
    if (type == AVMEDIA_TYPE_VIDEO && mVideoEnable) {
        initVideoFilter();
    } else if (type == AVMEDIA_TYPE_AUDIO && mAudioEnable) {
        initAudioFilter();
    } else {
        LOGE("unknown type");
    }
    return 0;
}

/**
 * 初始化视频AVFilter
 * @return
 */
int AVFrameFilter::initVideoFilter() {
    int ret = 0;
    AVRational timebase = av_inv_q(av_d2q(mFrameRate, RATIONAL_MAX));
    AVRational ratio = av_d2q(1, 255);

    char args[512];
    AVFilter *buffersrc = nullptr;
    AVFilter *buffersink = nullptr;
    AVFilterInOut *outputs = avfilter_inout_alloc();
    AVFilterInOut *inputs = avfilter_inout_alloc();
    mVideoFilterGraph = avfilter_graph_alloc();

    if (!outputs || !inputs || !mVideoFilterGraph) {
        LOGE("Failed to allocate video filter graph object");
        goto end;
    }

    buffersrc = avfilter_get_by_name("buffer");
    buffersink = avfilter_get_by_name("buffersink");
    if (!buffersrc || !buffersink) {
        LOGE("Failed to found filtering source or sink element");
        ret = AVERROR_UNKNOWN;
        goto end;
    }

    // 设置视频过滤链参数
    snprintf(args, sizeof(args),
             "video_size=%dx%d:pix_fmt=%d:time_base=%d/%d:pixel_aspect=%d/%d",
             mWidth, mHeight, mInPixelFormat, timebase.num, timebase.den, ratio.num,
             ratio.den);

    // 创建视频过滤器输入缓冲区
    ret = avfilter_graph_create_filter(&mVideoBuffersrcCtx, buffersrc, "in",
                                       args, nullptr, mVideoFilterGraph);
    if (ret < 0) {
        LOGE("Failed to create video buffer source");
        goto end;
    }

    // 创建视频过滤器输出缓冲区
    ret = avfilter_graph_create_filter(&mVideoBuffersinkCtx, buffersink, "out",
                                       nullptr, nullptr, mVideoFilterGraph);
    if (ret < 0) {
        LOGE("Failed to create video buffer sink");
        goto end;
    }

    // 设置像素转换过滤参数
    ret = av_opt_set_bin(mVideoBuffersinkCtx, "pix_fmts",
                         (uint8_t *) &mOutPixelFormat, sizeof(mOutPixelFormat),
                         AV_OPT_SEARCH_CHILDREN);
    if (ret < 0) {
        LOGE("Failed to set output pixel format");
        goto end;
    }

    // 绑定输入端
    outputs->name = av_strdup("in");
    outputs->filter_ctx = mVideoBuffersrcCtx;
    outputs->pad_idx = 0;
    outputs->next = nullptr;

    // 绑定输出端
    inputs->name = av_strdup("out");
    inputs->filter_ctx = mVideoBuffersinkCtx;
    inputs->pad_idx = 0;
    inputs->next = nullptr;

    if (!outputs->name || !inputs->name) {
        ret = AVERROR(ENOMEM);
        goto end;
    }

    // 解析filter描述
    if ((ret = avfilter_graph_parse_ptr(mVideoFilterGraph, mVideoFilter,
                                        &inputs, &outputs, nullptr)) < 0) {
        LOGE("Failed to call avfilter_graph_parse_ptr: %s", av_err2str(ret));
        goto end;
    }

    // 配置FilterGraph
    if ((ret = avfilter_graph_config(mVideoFilterGraph, nullptr)) < 0) {
        LOGE("Failed to call avfilter_graph_config: %s", av_err2str(ret));
        goto end;
    }

    end:
    avfilter_inout_free(&inputs);
    avfilter_inout_free(&outputs);

    return ret;
}

/**
 * 初始化音频AVFilter
 * @return
 */
int AVFrameFilter::initAudioFilter() {
    int ret = 0;
    AVRational timebase = av_inv_q(av_d2q(mInSampleRate, RATIONAL_MAX));

    char args[512];
    AVFilter *buffersrc = nullptr;
    AVFilter *buffersink = nullptr;
    AVFilterInOut *outputs = avfilter_inout_alloc();
    AVFilterInOut *inputs = avfilter_inout_alloc();
    mAudioFilterGraph = avfilter_graph_alloc();
    int64_t outChannelLayout = av_get_default_channel_layout(mOutChannels);

    if (!outputs || !inputs || !mAudioFilterGraph) {
        LOGE("Failed to  allocate audio filter object");
        goto end;
    }

    buffersrc = avfilter_get_by_name("abuffer");
    buffersink = avfilter_get_by_name("abuffersink");
    if (!buffersrc || !buffersink) {
        LOGE("Failed to found source or sink element");
        ret = AVERROR_UNKNOWN;
        goto end;
    }

    // 设置音频过滤链参数
    snprintf(args, sizeof(args),
             "time_base=%d/%d:sample_rate=%d:sample_fmt=%s:channel_layout=%lld",
             timebase.num, timebase.den, mInSampleRate, av_get_sample_fmt_name(mInSampleFormat),
             av_get_default_channel_layout(mInChannels));

    // 创建音频过滤器输入缓冲区
    ret = avfilter_graph_create_filter(&mAudioBuffersrcCtx, buffersrc, "in",
                                       args, nullptr, mAudioFilterGraph);
    if (ret < 0) {
        LOGE("Failed to create audio buffer source");
        goto end;
    }

    // 创建音频过滤器输出缓冲区
    ret = avfilter_graph_create_filter(&mAudioBuffersinkCtx, buffersink, "out",
                                       nullptr, nullptr, mAudioFilterGraph);
    if (ret < 0) {
        LOGE("Failed to create audio buffer sink");
        goto end;
    }

    // 设置输出采样率格式
    if (mOutSampleFormat != AV_SAMPLE_FMT_NONE) {
        ret = av_opt_set_bin(mAudioBuffersinkCtx, "sample_fmts", (uint8_t *) &mOutSampleFormat,
                             sizeof(mOutSampleFormat),
                             AV_OPT_SEARCH_CHILDREN);
        if (ret < 0) {
            LOGE("Failed to set output sample format");
            goto end;
        }
    }

    // 设置输出声道格式
    ret = av_opt_set_bin(mAudioBuffersinkCtx, "channel_layouts", (uint8_t *) &outChannelLayout,
                         sizeof(outChannelLayout), AV_OPT_SEARCH_CHILDREN);
    if (ret < 0) {
        LOGE("Failed to set output channel layout");
        goto end;
    }

    // 设置输出采样率
    ret = av_opt_set_bin(mAudioBuffersinkCtx, "sample_rates", (uint8_t *) &mOutSampleRate,
                         sizeof(mOutSampleRate), AV_OPT_SEARCH_CHILDREN);
    if (ret < 0) {
        LOGE("Failed to set output sample rate");
        goto end;
    }

    // 绑定输入端
    outputs->name = av_strdup("in");
    outputs->filter_ctx = mAudioBuffersrcCtx;
    outputs->pad_idx = 0;
    outputs->next = nullptr;

    // 绑定输出端
    inputs->name = av_strdup("out");
    inputs->filter_ctx = mAudioBuffersinkCtx;
    inputs->pad_idx = 0;
    inputs->next = nullptr;

    if (!outputs->name || !inputs->name) {
        ret = AVERROR(ENOMEM);
        goto end;
    }

    // 解析Filter描述
    if ((ret = avfilter_graph_parse_ptr(mAudioFilterGraph, mAudioFilter,
                                        &inputs, &outputs, nullptr)) < 0) {
        LOGE("Failed to call avfilter_graph_parse_ptr: %s", av_err2str(ret));
        goto end;
    }

    if ((ret = avfilter_graph_config(mAudioFilterGraph, nullptr)) < 0) {
        LOGE("Failed to call avfilter_graph_config: %s", av_err2str(ret));
        goto end;
    }

    end:
    avfilter_inout_free(&inputs);
    avfilter_inout_free(&outputs);

    return ret;
}

/**
 * 过滤媒体数据
 * @param mediaData
 * @return
 */
int AVFrameFilter::filterData(AVMediaData *mediaData) {
    if (mediaData->type == MediaVideo) {
        return filterVideo(mediaData);
    } else if (mediaData->type == MediaAudio) {
        return filterAudio(mediaData);
    }

    LOGE("unknown media data: %s", mediaData->getName());
    return -1;
}

/**
 * 过滤视频数据
 * @param mediaData
 * @return
 */
int AVFrameFilter::filterVideo(AVMediaData *mediaData) {

    if (!(mVideoEnable)) {
        LOGE("Unable video filter");
        return -1;
    }

    int ret;

    // 创建输入帧
    AVFrame *srcFrame = av_frame_alloc();
    if (!srcFrame) {
        LOGE("Failed to allocate source frame");
        return -1;
    }

    // 将媒体数据复制到输入帧中
    ret = av_image_fill_arrays(srcFrame->data, srcFrame->linesize, mediaData->image, mInPixelFormat,
                               mWidth, mHeight, 1);
    if (ret < 0) {
        LOGE("av_image_fill_arrays error: %s, [%d, %d, %s], [%d, %d, %s], [%d, %d, %s]",
             av_err2str(ret), srcFrame->width, srcFrame->height,
             av_get_pix_fmt_name((AVPixelFormat) srcFrame->format), mWidth, mHeight,
             av_get_pix_fmt_name(mInPixelFormat), mediaData->width, mediaData->height,
             av_get_pix_fmt_name(getPixelFormat((PixelFormat)mediaData->pixelFormat)));
        return ret;
    }
    srcFrame->width = mWidth;
    srcFrame->height = mHeight;
    srcFrame->format = mInPixelFormat;

    // 将输入放入AVFilter输入端
    ret = av_buffersrc_add_frame_flags(mVideoBuffersrcCtx, srcFrame, 0);
    if (ret < 0) {
        LOGE("Failed to call av_buffersrc_add_frame_flags: %s", av_err2str(ret));
        freeFrame(srcFrame);
        return ret;
    }

    // 创建输出帧
    AVFrame *dstFrame = av_frame_alloc();
    if (!dstFrame) {
        LOGE("Error allocate dst frame");
        freeFrame(srcFrame);
        return -1;
    }

    // 从AVFilter输出端取出视频帧
    ret = av_buffersink_get_frame(mVideoBuffersinkCtx, dstFrame);
    if (ret < 0) {
        LOGE("Failed to call av_buffersink_get_frame: %s", av_err2str(ret));
        freeFrame(srcFrame);
        freeFrame(dstFrame);
        return ret;
    }

    // 计算过滤后的帧的大小
    int size = av_image_get_buffer_size(AVPixelFormat(dstFrame->format), dstFrame->width,
                                        dstFrame->height, 1);
    if (size < 0) {
        LOGE("Failed to get image buffer size: %s", av_err2str(size));
        freeFrame(srcFrame);
        freeFrame(dstFrame);
        return -1;
    }

    // 创建缓冲区
    uint8_t *imageBuffer = (uint8_t *) av_malloc((size_t) size);
    if (imageBuffer == nullptr) {
        LOGE("Failed to allocate image buffer");
        freeFrame(srcFrame);
        freeFrame(dstFrame);
        return -1;
    }

    // 将数据复制到缓冲区中
    ret = av_image_copy_to_buffer(imageBuffer, size, (const uint8_t **) dstFrame->data,
                                  dstFrame->linesize, AVPixelFormat(dstFrame->format),
                                  dstFrame->width, dstFrame->height, 1);
    if (ret < 0) {
        LOGE("Failed to copy frame data to image buffer: %s", av_err2str(ret));
        av_free(imageBuffer);
        freeFrame(srcFrame);
        freeFrame(dstFrame);
        return ret;
    }

    // 绑定过滤后的结果
    mediaData->free();
    mediaData->image = imageBuffer;
    mediaData->length = size;
    mediaData->width = dstFrame->width;
    mediaData->height = dstFrame->height;
    mediaData->pixelFormat = pixelFormatConvert(AVPixelFormat(dstFrame->format));

    // 释放帧对象
    freeFrame(srcFrame);
    freeFrame(dstFrame);

    return 0;
}

/**
 * 过滤音频数据
 * @param mediaData
 * @return
 */
int AVFrameFilter::filterAudio(AVMediaData *mediaData) {

    if (!(mAudioEnable)) {
        LOGE("Unable audio filter");
        return -1;
    }

    int ret;
    AVFrame *srcFrame = av_frame_alloc();
    if (!srcFrame) {
        LOGE("Failed to allocate src frame");
        return -1;
    }

    // 将音频数据复制到输入帧中
    ret = av_samples_fill_arrays(srcFrame->data, srcFrame->linesize, mediaData->sample, mInChannels,
                                 SAMPLE_SIZE, mInSampleFormat, 1);
    if (ret < 0) {
        LOGE("Failed to call av_samples_fill_arrays: %s", av_err2str(ret));
        freeFrame(srcFrame);
        return ret;
    }

    // 设置输入参数
    srcFrame->sample_rate = mInSampleRate;
    srcFrame->channel_layout = (uint64_t) av_get_default_channel_layout(mInChannels);
    srcFrame->channels = mInChannels;
    srcFrame->format = mInSampleFormat;
    srcFrame->nb_samples = SAMPLE_SIZE;

    // 将输入帧放入过滤器输入端
    ret = av_buffersrc_add_frame_flags(mAudioBuffersrcCtx, srcFrame, 0);
    if (ret < 0) {
        LOGE("Failed to call av_buffersrc_add_frame_flags: %s", av_err2str(ret));
        freeFrame(srcFrame);
        return ret;
    }

    // 创建输出帧
    AVFrame *dstFrame = av_frame_alloc();
    if (!dstFrame) {
        LOGE("Faied to allocate dst frame");
        freeFrame(srcFrame);
        return -1;
    }

    // 从AVFilter输出端取出音频帧
    ret = av_buffersink_get_frame(mAudioBuffersinkCtx, dstFrame);
    if (ret < 0) {
        LOGE("Failed to call av_buffersink_get_frame: %s", av_err2str(ret));
        freeFrame(srcFrame);
        freeFrame(dstFrame);
        return -1;
    }

    // 计算过滤后的帧的大小
    int size = av_samples_get_buffer_size(dstFrame->linesize, dstFrame->channels,
                                          dstFrame->nb_samples, AVSampleFormat(dstFrame->format),
                                          1);
    if (size < 0) {
        LOGE("Failed to get sample buffer size: %s", av_err2str(size));
        freeFrame(srcFrame);
        freeFrame(dstFrame);
        return -1;
    }

    // 创建缓冲区
    uint8_t *sampleBuffer = (uint8_t *) av_malloc((size_t) size);
    if (sampleBuffer == nullptr) {
        LOGE("Failed to allocate memory");
        freeFrame(srcFrame);
        freeFrame(dstFrame);
        return -1;
    }

    // 将数据复制到缓冲区中
    ret = av_samples_copy(&sampleBuffer, dstFrame->data, 0, 0, dstFrame->nb_samples,
                          dstFrame->channels, AVSampleFormat(dstFrame->format));
    if (ret < 0) {
        LOGE("av_samples_copy error: %s", av_err2str(ret));
        av_free(sampleBuffer);
        freeFrame(srcFrame);
        freeFrame(dstFrame);
        return ret;
    }

    // 绑定过滤后的结果
    mediaData->free();
    mediaData->sample = sampleBuffer;
    mediaData->sample_size = size;

    // 释放帧对象
    freeFrame(srcFrame);
    freeFrame(dstFrame);

    return 0;
}

/**
 * 获取一帧数据
 * @param frame
 * @param type
 * @return
 */
AVFrame *AVFrameFilter::filterFrame(AVFrame *frame, AVMediaType type) {
    if (type == AVMEDIA_TYPE_AUDIO) {
        return filterAudio(frame);
    } else if (type == AVMEDIA_TYPE_VIDEO) {
        return filterVideo(frame);
    }
    return frame;
}

/**
 * 过滤一个视频帧
 * @param frame
 * @return
 */
AVFrame* AVFrameFilter::filterVideo(AVFrame *frame) {
    int ret;
    if (!(mVideoEnable)) {
        LOGE("Unable video filter");
        return frame;
    }

    // 将输入放入AVFilter输入端
    ret = av_buffersrc_add_frame_flags(mVideoBuffersrcCtx, frame, 0);
    if (ret < 0) {
        LOGE("Failed to call av_buffersrc_add_frame_flags: %s", av_err2str(ret));
        return frame;
    }

    // 创建输出帧
    AVFrame *dstFrame = av_frame_alloc();
    if (!dstFrame) {
        LOGE("Error allocate dst frame");
        return frame;
    }

    // 从AVFilter输出端取出视频帧
    ret = av_buffersink_get_frame(mVideoBuffersinkCtx, dstFrame);
    if (ret < 0) {
        LOGE("Failed to call av_buffersink_get_frame: %s", av_err2str(ret));
        freeFrame(dstFrame);
        return frame;
    }

    // 计算过滤后的帧的大小
    int size = av_image_get_buffer_size(AVPixelFormat(dstFrame->format), dstFrame->width,
                                        dstFrame->height, 1);
    if (size < 0) {
        LOGE("Failed to get image buffer size: %s", av_err2str(size));
        freeFrame(dstFrame);
        return frame;
    }

    // 创建缓冲区
    uint8_t *imageBuffer = (uint8_t *) av_malloc((size_t) size);
    if (imageBuffer == nullptr) {
        LOGE("Failed to allocate image buffer");
        freeFrame(dstFrame);
        return frame;
    }

    // 将数据复制到缓冲区中
    ret = av_image_copy_to_buffer(imageBuffer, size, (const uint8_t **) dstFrame->data,
                                  dstFrame->linesize, AVPixelFormat(dstFrame->format),
                                  dstFrame->width, dstFrame->height, 1);
    if (ret < 0) {
        LOGE("Failed to copy frame data to image buffer: %s", av_err2str(ret));
        av_free(imageBuffer);
        freeFrame(dstFrame);
        return frame;
    }

    // 释放源视频帧
    freeFrame(frame);

    // 返回过滤后的视频帧
    return dstFrame;
}

/**
 * 过滤一个音频帧
 * @param frame
 * @return
 */
AVFrame* AVFrameFilter::filterAudio(AVFrame *frame) {
    int ret;
    if (!(mAudioEnable)) {
        LOGE("Unable audio filter");
        return frame;
    }

    // 将输入帧放入过滤器输入端
    ret = av_buffersrc_add_frame_flags(mAudioBuffersrcCtx, frame, 0);
    if (ret < 0) {
        LOGE("Failed to call av_buffersrc_add_frame_flags: %s", av_err2str(ret));
        return frame;
    }

    // 创建输出帧
    AVFrame *dstFrame = av_frame_alloc();
    if (!dstFrame) {
        LOGE("Faied to allocate dst frame");
        return frame;
    }

    // 从AVFilter输出端取出音频帧
    ret = av_buffersink_get_frame(mAudioBuffersinkCtx, dstFrame);
    if (ret < 0) {
        LOGE("Failed to call av_buffersink_get_frame: %s", av_err2str(ret));
        freeFrame(dstFrame);
        return frame;
    }

    // 计算过滤后的帧的大小
    int size = av_samples_get_buffer_size(dstFrame->linesize, dstFrame->channels,
                                          dstFrame->nb_samples, AVSampleFormat(dstFrame->format),
                                          1);
    if (size < 0) {
        LOGE("Failed to get sample buffer size: %s", av_err2str(size));
        freeFrame(dstFrame);
        return frame;
    }

    // 创建缓冲区
    uint8_t *sampleBuffer = (uint8_t *) av_malloc((size_t) size);
    if (sampleBuffer == nullptr) {
        LOGE("Failed to allocate memory");
        freeFrame(dstFrame);
        return frame;
    }

    // 将数据复制到缓冲区中
    ret = av_samples_copy(&sampleBuffer, dstFrame->data, 0, 0, dstFrame->nb_samples,
                          dstFrame->channels, AVSampleFormat(dstFrame->format));
    if (ret < 0) {
        LOGE("av_samples_copy error: %s", av_err2str(ret));
        av_free(sampleBuffer);
        freeFrame(dstFrame);
        return frame;
    }

    // 释放源音频帧
    freeFrame(frame);

    // 返回过滤后的音频帧
    return dstFrame;
}


/**
 * 释放所有资源
 */
void AVFrameFilter::release() {
    mVideoBuffersrcCtx = nullptr;
    mVideoBuffersinkCtx = nullptr;
    if (mVideoFilterGraph != nullptr) {
        avfilter_graph_free(&mVideoFilterGraph);
        mVideoFilterGraph = nullptr;
    }
    mAudioBuffersrcCtx = nullptr;
    mAudioBuffersinkCtx = nullptr;
    if (mAudioFilterGraph != nullptr) {
        avfilter_graph_free(&mAudioFilterGraph);
        mAudioFilterGraph = nullptr;
    }
}

/**
 * 释放帧对象
 * @param frame
 */
void AVFrameFilter::freeFrame(AVFrame *frame) {
    if (frame) {
        av_frame_free(&frame);
    }
}