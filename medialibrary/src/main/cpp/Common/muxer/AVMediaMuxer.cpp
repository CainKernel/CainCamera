//
// Created by CainHuang on 2020-01-09.
//

#include "AVMediaMuxer.h"

AVMediaMuxer::AVMediaMuxer() {
    mPath = nullptr;
    pFormatCtx = nullptr;
}

AVMediaMuxer::~AVMediaMuxer() {
    LOGD("AVMediaMuxer - destructor");
    if (mPath) {
        av_freep(&mPath);
        mPath = nullptr;
    }
}

/**
 * 设置输出路径
 * @param path
 */
void AVMediaMuxer::setOutputPath(const char *path) {
    mPath = av_strdup(path);
}

/**
 * 初始化复用器
 * @return
 */
int AVMediaMuxer::init() {
    if (!mPath) {
        LOGE("AVMediaMuxer - failed to find output path");
        return -1;
    }
    int ret = avformat_alloc_output_context2(&pFormatCtx, nullptr, nullptr, mPath);
    if (!pFormatCtx || ret < 0) {
        LOGI("AVMediaMuxer - failed to call avformat_alloc_output_context2: %s", av_err2str(ret));
        return AVERROR_UNKNOWN;
    }
    return 0;
}

/**
 * 打开复用器
 * @return
 */
int AVMediaMuxer::openMuxer() {
    if (!pFormatCtx) {
        LOGE("AVMediaMuxer - Failed to find muxer context");
        return -1;
    }
    int ret;
    if (!(pFormatCtx->oformat->flags & AVFMT_NOFILE)) {
        if ((ret = avio_open(&pFormatCtx->pb, mPath, AVIO_FLAG_WRITE)) < 0) {
            LOGE("AVMediaMuxer - Failed to open output file '%s'", mPath);
            return ret;
        }
    }
    return 0;
}

/**
 * 创建新的媒体流
 * @param encoder 编码器
 * @return
 */
AVStream* AVMediaMuxer::createStream(AVCodec *encoder) {
    if (!pFormatCtx) {
        LOGE("AVMediaMuxer - Failed to find muxer context");
        return nullptr;
    }
    if (!encoder) {
        LOGE("AVMediaMuxer - Failed to find encoder");
        return nullptr;
    }
    return avformat_new_stream(pFormatCtx, encoder);
}

AVStream *AVMediaMuxer::createStream(AVCodecID id) {
    if (!pFormatCtx) {
        LOGE("AVMediaMuxer - Failed to find muxer context");
        return nullptr;
    }
    if (id == AV_CODEC_ID_NONE) {
        LOGE("AVMediaMuxer - Failed to find encoder: %s", avcodec_get_name(id));
        return nullptr;
    }
    auto encoder = avcodec_find_encoder(id);
    if (encoder == nullptr) {
        LOGE("AVMediaMuxer - Failed to find encoder: %s", avcodec_get_name(id));
        return nullptr;
    }
    return avformat_new_stream(pFormatCtx, encoder);
}

/**
 * 写入文件头部信息
 * @param options
 * @return
 */
int AVMediaMuxer::writeHeader(AVDictionary **options) {
    if (!pFormatCtx) {
        LOGE("AVMediaMuxer - Failed to find muxer context");
        return -1;
    }
    // 判断是否需要写入全局头部信息
    if (!hasGlobalHeader()) {
        return 0;
    }
    int ret = avformat_write_header(pFormatCtx, options);
    if (ret < 0) {
        LOGE("AVMediaMuxer - Failed to call avformat_write_header: %s", av_err2str(ret));
        return ret;
    }
    return 0;
}

/**
 * 将数据包写入复用器
 * @param packet
 * @return
 */
int AVMediaMuxer::writeFrame(AVPacket *packet) {
    if (!pFormatCtx) {
        LOGE("AVMediaMuxer - Failed to find muxer context");
        return -1;
    }
    int ret = av_interleaved_write_frame(pFormatCtx, packet);
    if (ret < 0) {
        LOGE("AVMediaMuxer - Failed to call av_interleaved_write_frame: %s, stream: %d", av_err2str(ret), packet->stream_index);
        return ret;
    }
    return 0;
}

/**
 * 写入文件尾部信息
 * @return
 */
int AVMediaMuxer::writeTrailer() {
    if (!pFormatCtx) {
        LOGE("AVMediaMuxer - Failed to find muxer context");
        return -1;
    }
    int ret = av_write_trailer(pFormatCtx);
    if (ret < 0) {
        LOGE("AVMediaMuxer -Failed to call av_write_trailer: %s", av_err2str(ret));
        return ret;
    } else {
        LOGD("AVMediaMuxer - muxer writer success");
    }
    return 0;
}

/**
 * 关闭复用器
 */
void AVMediaMuxer::closeMuxer() {
    if (pFormatCtx && !(pFormatCtx->oformat->flags & AVFMT_NOFILE)) {
        avio_closep(&pFormatCtx->pb);
        avformat_close_input(&pFormatCtx);
        pFormatCtx = nullptr;
        LOGD("AVMediaMuxer - close file");
    }
}

/**
 * 判断是否存在全局头部信息
 * @return
 */
bool AVMediaMuxer::hasGlobalHeader() {
    if (pFormatCtx) {
        return (bool)(pFormatCtx->oformat->flags & AVFMT_GLOBALHEADER);
    }
    return false;
}
/**
 * 打印复用器信息
 */
void AVMediaMuxer::printInfo() {
    if (pFormatCtx && mPath) {
        av_dump_format(pFormatCtx, 0, mPath, 1);
    }
}
