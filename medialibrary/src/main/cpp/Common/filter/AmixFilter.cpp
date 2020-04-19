//
// Created by CainHuang on 2020-04-19.
//

#include "AmixFilter.h"

AmixFilter::AmixFilter() : mInSampleRate1(44100), mInSampleRate0(44100), mOutSampleRate(44100),
mInChannelLayout1(AV_CH_LAYOUT_STEREO), mInChannelLayout0(AV_CH_LAYOUT_STEREO), mOutChannelLayout(AV_CH_LAYOUT_STEREO),
mInFormat1(AV_SAMPLE_FMT_S16), mInFormat0(AV_SAMPLE_FMT_S16), mOutFormat(AV_SAMPLE_FMT_S16),
pFilterGraph(nullptr), pBufferSrcContext1(nullptr), pBufferSrcContext0(nullptr),
pBufferSinkContext(nullptr), pFormatContext(nullptr), pAmixContext(nullptr), mInited(false) {

}

AmixFilter::~AmixFilter() {
    avfilter_free(pBufferSrcContext1);
    avfilter_free(pBufferSrcContext0);
    avfilter_free(pBufferSinkContext);
    avfilter_free(pFormatContext);
    avfilter_free(pAmixContext);
    avfilter_graph_free(&pFilterGraph);
}

int AmixFilter::setOption(std::string key, std::string value) {
    if (!strcmp(key.c_str(), "rate_in0")) {
        mInSampleRate0 = atoi(value.c_str());
    } else if (!strcmp(key.c_str(), "rate_in1")) {
        mInSampleRate1 = atoi(value.c_str());
    } else if (!strcmp(key.c_str(), "rate_out")) {
        mOutSampleRate = atoi(value.c_str());
    } else if (!strcmp(key.c_str(), "format_in0")) {
        mInFormat0 = !strcmp(value.c_str(), "u8")
                ? AV_SAMPLE_FMT_U8
                : ((!strcmp(value.c_str(), "s32")) ? AV_SAMPLE_FMT_S32 : AV_SAMPLE_FMT_S16);
    } else if (!strcmp(key.c_str(), "format_in1")) {
        mInFormat1 = !strcmp(value.c_str(), "u8")
                ? AV_SAMPLE_FMT_U8
                : ((!strcmp(value.c_str(), "s32")) ? AV_SAMPLE_FMT_S32 : AV_SAMPLE_FMT_S16);
    } else if (!strcmp(key.c_str(), "format_out")) {
        mOutFormat = !strcmp(value.c_str(), "u8")
                ? AV_SAMPLE_FMT_U8
                : ((!strcmp(value.c_str(), "s32")) ? AV_SAMPLE_FMT_S32 : AV_SAMPLE_FMT_S16);
    } else if (!strcmp(key.c_str(), "channel_in0")) {
        mInChannelLayout0 = (!strcmp(value.c_str(), "2")) ? AV_CH_LAYOUT_STEREO : AV_CH_LAYOUT_MONO;
    } else if (!strcmp(key.c_str(), "channel_in1")) {
        mInChannelLayout1 = (!strcmp(value.c_str(), "2")) ? AV_CH_LAYOUT_STEREO : AV_CH_LAYOUT_MONO;
    } else if (!strcmp(key.c_str(), "channel_out")) {
        mOutChannelLayout = (!strcmp(value.c_str(), "2")) ? AV_CH_LAYOUT_STEREO : AV_CH_LAYOUT_MONO;
    }
    return 0;
}

int AmixFilter::init() {
    int ret;
    AVFilter *abuffersrc0, *abuffersrc1, *abuffersink, *amix, *aformat;
    char options_str[1024];
    avfilter_register_all();

    pFilterGraph = avfilter_graph_alloc();
    if (!pFilterGraph) {
        LOGE("Failed to call avfilter_graph_alloc");
        return -1;
    }

    // 创建第0路abuffer输入端
    abuffersrc0 = avfilter_get_by_name("abuffer");
    if (!abuffersrc0) {
        LOGE("Failed to call avfilter_get_by_name - abuffer");
        return -1;
    }

    // 创建第0路输入的上下文环境
    pBufferSrcContext0 = avfilter_graph_alloc_filter(pFilterGraph, abuffersrc0, "src0");
    if (!pBufferSrcContext0) {
        LOGE("Failed to call avfilter_graph_alloc_filter - src0");
        return -1;
    }

    // 构造参数配置（输入音频格式、输入采样率、输入声道配置）
    snprintf(options_str, sizeof(options_str),
             "sample_fmt=%s:sample_rate=%d:channel_layout=0x%" PRIx64 ,
             av_get_sample_fmt_name(mInFormat0), mInSampleRate0, mInChannelLayout0);
    // 初始化第0路输入端上下文
    ret = avfilter_init_str(pBufferSrcContext0, options_str);
    if (ret < 0) {
        LOGE("Failed to call avfilter_init_str: %s", av_err2str(ret));
        return ret;
    }

    // 创建第1路abuffer输入端
    abuffersrc1 = avfilter_get_by_name("abuffer");
    if (!abuffersrc1) {
        LOGE("Failed to call avfilter_get_by_name - abuffer");
        return -1;
    }

    // 创建第1路输入的上下文环境
    pBufferSrcContext1 = avfilter_graph_alloc_filter(pFilterGraph, abuffersrc1, "src1");
    if (!pBufferSrcContext1) {
        LOGE("Failed to call avfilter_graph_alloc_filter - src1");
        return -1;
    }

    // 构造参数配置（输入音频格式、输入采样率、输入声道配置）
    snprintf(options_str, sizeof(options_str),
             "sample_fmt=%s:sample_rate=%d:channel_layout=0x%" PRIx64 ,
             av_get_sample_fmt_name(mInFormat1), mInSampleRate1, mInChannelLayout1);

    // 初始化第1路输入端上下文
    ret = avfilter_init_str(pBufferSrcContext1, options_str);
    if (ret < 0) {
        LOGE("Failed to call avfilter_init_str: %s", av_err2str(ret));
        return ret;
    }

    // 创建混音处理的filter - amix
    amix = avfilter_get_by_name("amix");
    if (!amix) {
        LOGE("Failed to call avfilter_get_by_name - amix");
        return -1;
    }

    // 创建amix上下文
    pAmixContext = avfilter_graph_alloc_filter(pFilterGraph, amix, "amix");
    if (!pAmixContext) {
        LOGE("Failed to call avfilter_graph_alloc_filter - amix");
        return -1;
    }

    // 初始化amix上下文
    ret = avfilter_init_str(pAmixContext, nullptr);
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
             av_get_sample_fmt_name(mOutFormat), mOutSampleRate, mOutChannelLayout);

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
    // abuffer0 -> (aformat0)
    //                        -> amix -> aformat -> abuffersink
    // abuffer1 -> (aformat0)
    // 在 abuffer0/abuffer1 -> amix 链路中，会根据输入情况插入格式转换 filter(aformat0) 导致输出格式变化
    // 因此，在 abuffersink 输出前，需要添加 aformat 将格式转换为实际需要的输出格式
    ret = avfilter_link(pBufferSrcContext0, 0, pAmixContext, 0);
    if (ret < 0) {
        LOGE("Failed to call avfilter_link - pBufferSrcContext0: %s", av_err2str(ret));
        return ret;
    }

    ret = avfilter_link(pBufferSrcContext1, 0, pAmixContext, 1);
    if (ret < 0) {
        LOGE("Failed to call avfilter_link - pBufferSrcContext1: %s", av_err2str(ret));
        return ret;
    }

    ret = avfilter_link(pAmixContext, 0, pFormatContext, 0);
    if (ret < 0) {
        LOGE("Failed to call avfilter_link - pAmixContext: %s", av_err2str(ret));
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
 * 将一帧数据添加到amix混音链路中
 * @param frame
 * @param index
 * @return
 */
int AmixFilter::addFrame(AVFrame *frame, int index) {
    AVFrame *avframe;
    int ret;
    if (!mInited) {
        LOGE("AmixFilter is not inited!");
        return -1;
    }

    // 根据索引获取输入端的采样率、采样布局、采样格式参数
    int sample_rate = index == 0 ? mInSampleRate0 : mInSampleRate1;
    uint64_t sample_channel = index == 0 ? mInChannelLayout0 : mInChannelLayout1;
    AVSampleFormat sample_format = index == 0 ? mInFormat0 : mInFormat1;

    int nb_sample = frame->nb_samples;

    //获取一个AVFrame实例
    avframe = av_frame_alloc();
    if (!avframe) {
        return -1;
    }

    //配置AVFrame的音频格式信息
    avframe->sample_rate = sample_rate;
    avframe->format = sample_format;
    avframe->channel_layout = sample_channel;
    avframe->nb_samples = nb_sample;

    // 申请AVFrame的音频数据内存空间
    ret = av_frame_get_buffer(avframe, 1);
    if (ret < 0) {
        av_frame_free(&avframe);
        return ret;
    }

    // 将外部输入数据复制到AVFrame
    av_frame_move_ref(avframe, frame);

    // 将AVFrame传输到对应的音频输入端环境中
    ret = av_buffersrc_add_frame(index == 0 ? pBufferSrcContext0 : pBufferSrcContext1, avframe);
    if (ret < 0) {
        LOGE("Failed to call av_buffersrc_add_frame: %s", av_err2str(ret));
        av_frame_free(&avframe);
        return ret;
    }

    av_frame_free(&avframe);
    return 0;
}

/**
 * 获取过滤后的音频帧
 * @return
 */
AVFrame *AmixFilter::getFrame() {
    AVFrame *frame;
    int ret;
    if (!mInited) {
        return nullptr;
    }

    frame = av_frame_alloc();
    if (!frame) {
        return nullptr;
    }

    // 从 abuffersink 的上下文中获取处理后的音频数据包
    ret = av_buffersink_get_frame(pBufferSinkContext, frame);
    if (ret < 0) {
        av_frame_free(&frame);
        return nullptr;
    }
    return frame;
}
