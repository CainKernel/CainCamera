//
// Created by CainHuang on 2020-04-19.
//

#include "VolumeFilter.h"

VolumeFilter::VolumeFilter() : pFilterGraph(nullptr), pBufferSrcContext(nullptr),
        pVolumeContext(nullptr), pFormatContext(nullptr), pBufferSinkContext(nullptr),
        mSampleRate(44100), mChannelLayout(AV_CH_LAYOUT_STEREO), mSampleFormat(AV_SAMPLE_FMT_S16),
        mVolumeValue("1.0"), mInited(false) {

}

VolumeFilter::~VolumeFilter() {
    avfilter_free(pBufferSrcContext);
    avfilter_free(pBufferSinkContext);
    avfilter_free(pVolumeContext);
    avfilter_free(pFormatContext);
    avfilter_graph_free(&pFilterGraph);
}

int VolumeFilter::setOption(std::string key, std::string value) {
    if (!strcmp(key.c_str(), "format")) {
        mSampleFormat = (!strcmp(value.c_str(), "u8"))
                ? AV_SAMPLE_FMT_U8
                : ((!strcmp(value.c_str(), "s32")) ? AV_SAMPLE_FMT_S32 : AV_SAMPLE_FMT_S16);
    } else if (!strcmp(key.c_str(), "channel")) {
        mChannelLayout = (!strcmp(value.c_str(), "2")) ? AV_CH_LAYOUT_STEREO : AV_CH_LAYOUT_MONO;
    } else if (!strcmp(key.c_str(), "rate")) {
        mSampleRate = atoi(value.c_str());
    } else if (!strcmp(key.c_str(), "volume")) {
        mVolumeValue = value;
    }
    return 0;
}

int VolumeFilter::init() {
    int ret;
    AVFilter *abuffersrc, *abuffersink, *volume, *aformat;
    char options_str[1024];

    // 注册全部 ffmpeg filter
    avfilter_register_all();

    pFilterGraph = avfilter_graph_alloc();
    if (!pFilterGraph) {
        LOGE("Failed to call avfilter_graph_alloc");
        return -1;
    }

    //根据 abuffer 获取一个音频输入端filter
    abuffersrc = avfilter_get_by_name("abuffer");
    if (!abuffersrc) {
        LOGE("Failed to call avfilter_get_by_name - abuffer");
        return -1;
    }

    // 创建输入的上下文环境
    pBufferSrcContext = avfilter_graph_alloc_filter(pFilterGraph, abuffersrc, "src0");
    if (!pBufferSrcContext) {
        LOGE("Failed to call avfilter_graph_alloc_filter - src0");
        return -1;
    }

    // 构造参数配置（输入音频格式、输入采样率、输入声道配置）
    // 部分平台上（例如MacOS） PRIx64无法识别，需要添加编译宏定义 __STDC_FORMAT_MACROS
    snprintf(options_str, sizeof(options_str),
             "sample_fmt=%s:sample_rate=%d:channel_layout=0x%" PRIx64 ,
             av_get_sample_fmt_name(mSampleFormat), mSampleRate, mChannelLayout);

    // 初始化输入端上下文
    ret = avfilter_init_str(pBufferSrcContext, options_str);
    if (ret < 0) {
        LOGE("Failed to call avfilter_init_str: %s", av_err2str(ret));
        return ret;
    }

    // 创建音量处理的filter - volume
    volume = avfilter_get_by_name("volume");
    if (!volume) {
        LOGE("Failed to call avfilter_get_by_name - volume");
        return -1;
    }

    // 创建 volume 上下文
    pVolumeContext = avfilter_graph_alloc_filter(pFilterGraph, volume, "volume");
    if (!pVolumeContext) {
        LOGE("Failed to call avfilter_graph_alloc_filter - volume");
        return -1;
    }

    // 构造参数配置（目标调节音量的大小，例如0.5为原来的一半）
    snprintf(options_str, sizeof(options_str),
             "volume=%s", mVolumeValue.c_str());

    // 初始化 volume 上下文
    ret = avfilter_init_str(pVolumeContext, options_str);
    if (ret < 0) {
        LOGE("Failed to call avfilter_init_str: %s", av_err2str(ret));
        return ret;
    }

    // 创建格式转换的filter
    aformat = avfilter_get_by_name("aformat");
    if (!aformat) {
        LOGE("Failed to call avfilter_get_by_name - aformat");
        return -1;
    }

    // 创建格式转换的filter上下文
    pFormatContext = avfilter_graph_alloc_filter(pFilterGraph, aformat, "aformat");
    if (!pFormatContext) {
        LOGE("Failed to call avfilter_graph_alloc_filter - aformat");
        return -1;
    }

    // 构造参数配置（输出音频格式、输出采样率、输出声道配置）
    snprintf(options_str, sizeof(options_str),
             "sample_fmts=%s:sample_rates=%d:channel_layouts=0x%" PRIx64 ,
             av_get_sample_fmt_name(mSampleFormat), mSampleRate, mChannelLayout);

    // 初始化输出格式上下文
    ret = avfilter_init_str(pFormatContext, options_str);
    if (ret < 0) {
        LOGE("Failed to call avfilter_init_str: %s", av_err2str(ret));
        return ret;
    }

    // 根据 abuffersink 获取一个音频数据输出的filter
    abuffersink = avfilter_get_by_name("abuffersink");
    if (!abuffersink) {
        LOGE("Failed to call avfilter_get_by_name - abuffersink");
        return -1;
    }

    // 创建 abuffersink 上下文
    pBufferSinkContext = avfilter_graph_alloc_filter(pFilterGraph, abuffersink, "sink");
    if (!pBufferSinkContext) {
        LOGE("Failed to call avfilter_graph_alloc_filter - sink");
        return -1;
    }

    // 初始化 abuffersink 上下文
    ret = avfilter_init_str(pBufferSinkContext, nullptr);
    if (ret < 0) {
        LOGE("Failed to call avfilter_init_str: %s", av_err2str(ret));
        return ret;
    }

    // 链接filter，链路流程：
    // abuffer -> (aformat0) -> volume -> aformat -> abuffersink
    // 在 abuffer -> volume 链路中，会根据输入情况插入格式转换 filter(aformat0) 导致输出格式变化
    // 因此，在 abuffersink 输出前，需要添加 aformat 将格式转换为实际需要的输出格式
    ret = avfilter_link(pBufferSrcContext, 0, pVolumeContext, 0);
    if (ret < 0) {
        LOGE("Failed to call avfilter_link - pBufferSrcContext: %s", av_err2str(ret));
        return ret;
    }

    ret = avfilter_link(pVolumeContext, 0, pFormatContext, 0);
    if (ret < 0) {
        LOGE("Failed to call avfilter_link - pVolumeContext: %s", av_err2str(ret));
        return ret;
    }

    ret = avfilter_link(pFormatContext, 0, pBufferSinkContext, 0);
    if (ret < 0) {
        LOGE("Failed to call avfilter_link - pFormatContext: %s", av_err2str(ret));
        return ret;
    }

    // 初始化filter链路
    ret = avfilter_graph_config(pFilterGraph, nullptr);
    if (ret < 0) {
        LOGE("Failed to call avfilter_graph_config: %s", av_err2str(ret));
        return ret;
    }

    mInited = true;

    return 0;
}

/**
 * 将一帧数据添加到volume音量调节链路中
 * @param frame
 * @param index
 * @return
 */
int VolumeFilter::addFrame(AVFrame *frame, int index) {
    int ret;
    if (!mInited) {
        return -1;
    }
    ret = av_buffersrc_add_frame(pBufferSinkContext, frame);
    if (ret < 0) {
        LOGE("Failed to call av_buffersrc_add_frame: %s", av_err2str(ret));
        return ret;
    }
    return 0;
}

AVFrame *VolumeFilter::getFrame() {
    AVFrame *frame;
    int ret;
    if (!mInited) {
        return nullptr;
    }

    frame = av_frame_alloc();
    if (!frame) {
        return nullptr;
    }
    ret = av_buffersink_get_frame(pBufferSinkContext, frame);
    if (ret < 0) {
        LOGE("Failed to call av_buffersink_get_frame: %s", av_err2str(ret));
        av_frame_free(&frame);
        return nullptr;
    }
    return frame;
}
