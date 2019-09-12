//
// Created by CainHuang on 2019/5/18.
//

#include "MediaEditor.h"


MediaEditor::MediaEditor() : abortRequest(true), mThread(nullptr) {

}

/**
 * 设置监听器
 * @param listener
 */
void MediaEditor::setListener(EditListener *listener) {
    mEditListener =  listener;
}

/**
 * 开始
 */
void MediaEditor::start() {
    mMutex.lock();
    abortRequest = false;
    mCondition.signal();
    mMutex.unlock();
    if (mThread == nullptr) {
        mThread = new Thread(this);
        mThread->start();
        mThread->detach();
    }
}

/**
 * 停止处理
 */
void MediaEditor::stop() {
    mMutex.lock();
    abortRequest = true;
    mCondition.signal();
    mMutex.unlock();
    mMutex.lock();
    while (!mExit) {
        mCondition.wait(mMutex);
    }
    mMutex.unlock();
    if (mThread != nullptr) {
        mThread->join();
        delete mThread;
        mThread = nullptr;
    }
}

/**
 * 运行处理
 */
void MediaEditor::run() {
    abortRequest = false;
    process();
    mExit = true;
    mCondition.signal();
}

/**
 * 释放资源
 */
void MediaEditor::release() {
    if (mEditListener != nullptr) {
        delete mEditListener;
        mEditListener = nullptr;
    }
}

/**
 * 打开输入文件
 * @param filename
 * @param fmt_ctx
 * @return
 */
int MediaEditor::openInputFile(const char *filename, AVFormatContext **fmt_ctx) {
    int ret;
    if ((ret = avformat_open_input(fmt_ctx, filename, nullptr, nullptr)) < 0) {
       LOGE("Failed to call avformat_open_input - '%s'", filename);
        return ret;
    }
    if ((ret = avformat_find_stream_info(*fmt_ctx, nullptr)) < 0) {
        LOGE("Failed to call avformat_find_stream_info");
        return ret;
    }
    return ret;
}

/**
 * 获取解码上下文
 * @param fmt_ctx
 * @param dec_ctx
 * @param mediaType
 * @return
 */
int MediaEditor::getDecodeContext(AVFormatContext *fmt_ctx, AVCodecContext **dec_ctx,
                             AVMediaType mediaType) {
    // 目前仅支持创建音视、视频频解码上下文
    if (mediaType != AVMEDIA_TYPE_AUDIO && mediaType != AVMEDIA_TYPE_VIDEO) {
        LOGE("media type %d is unsupported", (int)mediaType);
        return -1;
    }

    if (mediaType == AVMEDIA_TYPE_AUDIO) {
        return getAudioDecodeContext(fmt_ctx, dec_ctx);
    } else {
        return getVideoDecodeContext(fmt_ctx, dec_ctx);
    }
}

/**
 * 获取音频解码上下文
 * @param fmt_ctx
 * @param dec_ctx
 * @return 返回音频流索引，失败返回小于0
 */
int MediaEditor::getAudioDecodeContext(AVFormatContext *fmt_ctx, AVCodecContext **dec_ctx) {
    int ret, audio_index;
    AVCodec *codec = nullptr;
    // 查找音频流索引和解码器
    ret = av_find_best_stream(fmt_ctx, AVMEDIA_TYPE_AUDIO, -1, -1, &codec, 0);
    if (ret < 0) {
        LOGE("Failed to call av_find_best_stream");
        return -1;
    }
    audio_index = ret;
    // 创建解码上下文
    *dec_ctx = avcodec_alloc_context3(codec);
    if (!(*dec_ctx)) {
        LOGE("Failed to call avcodec_alloc_context3");
        return -1;
    }
    // 复制解码参数到解码上下文
    ret = avcodec_parameters_to_context(*dec_ctx,
            fmt_ctx->streams[audio_index]->codecpar);
    if (ret < 0) {
        LOGE("Failed to call avcodec_parameters_to_context");
        return ret;
    }
    // 打开解码器
    if ((ret = avcodec_open2(*dec_ctx, codec, nullptr)) < 0) {
        LOGE("Failed to call avcodec_open2");
        return ret;
    }
    return audio_index;
}

/**
 * 获取视频解码上下文
 * @param fmt_ctx
 * @param dec_ctx
 * @return 返回视频流索引，失败返回小于0
 */
int MediaEditor::getVideoDecodeContext(AVFormatContext *fmt_ctx, AVCodecContext **dec_ctx) {
    int ret, video_index;
    AVCodec *codec = nullptr;
    // 查找视频流索引和解码器
    ret = av_find_best_stream(fmt_ctx, AVMEDIA_TYPE_VIDEO, -1, -1, &codec, 0);
    if (ret < 0) {
        LOGE("Failed to call av_find_best_stream");
        return ret;
    }
    video_index = ret;

    // 创建解码上下文
    *dec_ctx = avcodec_alloc_context3(codec);
    if (!(*dec_ctx)) {
        LOGE("Failed to call avcodec_alloc_context3");
        return -1;
    }

    // 复制解码参数到解码上下文中
    ret = avcodec_parameters_to_context(*dec_ctx,
            fmt_ctx->streams[video_index]->codecpar);
    if (ret < 0) {
        LOGE("Failed to call avcodec_parameters_to_context");
        return ret;
    }

    // 打开解码器
    if ((ret = avcodec_open2(*dec_ctx, codec, nullptr)) < 0) {
        LOGE("Failed to call avcodec_open2");
        return ret;
    }

    return video_index;
}

/**
 * 解码一个数据包
 * @param codec_ctx
 * @param packet
 * @return
 */
AVFrame* MediaEditor::decodePacket(AVCodecContext *codec_ctx, AVPacket *packet) {
    int ret;
    if (!codec_ctx || !packet) {
        return nullptr;
    }
    ret = avcodec_send_packet(codec_ctx, packet);
    if (ret < 0) {
        LOGE("Failed to call avcodec_send_packet - '%s'", av_err2str(ret));
        return nullptr;
    }

    AVFrame *frame = av_frame_alloc();
    while (ret >= 0) {
        ret = avcodec_receive_frame(codec_ctx, frame);
        if (ret < 0) {
            LOGE("Failed to call avcodec_receive_frame - '%s'", av_err2str(ret));
            av_frame_unref(frame);
            av_frame_free(&frame);
            return nullptr;
        }
        return frame;
    }

    av_frame_unref(frame);
    av_frame_free(&frame);
    return nullptr;
}

/**
 * 编码一帧数据
 * @param codec_ctx
 * @param frame
 * @return
 */
AVPacket* MediaEditor::encodeFrame(AVCodecContext *codec_ctx, AVFrame *frame) {
    int ret;
    if (!codec_ctx || !frame) {
        return nullptr;
    }
    ret = avcodec_send_frame(codec_ctx, frame);
    if (ret < 0) {
        LOGE("Failed to call avcodec_send_frame - '%s'", av_err2str(ret));
        return nullptr;
    }
    AVPacket *packet = av_packet_alloc();
    while (ret >= 0) {
        ret = avcodec_receive_packet(codec_ctx, packet);
        if (ret < 0) {
            LOGE("Failed to call avcodec_receive_packet - '%s'", av_err2str(ret));
            av_packet_unref(packet);
            av_packet_free(&packet);
            return nullptr;
        }
        return packet;
    }

    av_packet_unref(packet);
    av_packet_free(&packet);
    return nullptr;
}

/**
 * 初始化输出上下文
 * @param url
 * @param fmt_ctx
 * @return
 */
int MediaEditor::initOutput(const char *url, AVFormatContext **fmt_ctx) {
    int ret;
    if (!url) {
        return -1;
    }
    ret = avformat_alloc_output_context2(fmt_ctx, nullptr, nullptr, url);
    if (ret < 0) {
        LOGE("Failed to call avformat_alloc_output_context2 - '%s', url:'%s'", av_err2str(ret), url);
        return -1;
    }
    return 0;
}

/**
 * 初始化输出上下文
 * @param url
 * @param fmt_ctx
 * @param format
 * @return
 */
int MediaEditor::initOutput(const char *url, AVFormatContext **fmt_ctx, const char *format) {
    int ret;
    if (!url) {
        return -1;
    }
    ret = avformat_alloc_output_context2(fmt_ctx, nullptr, format, url);
    if (ret < 0) {
        LOGE("Failed to call avformat_alloc_output_context2 - '%s', url:'%s'", av_err2str(ret), url);
        return -1;
    }
    return 0;
}

/**
 * 添加输出视频流
 * @param fmt_ctx
 * @param codec_ctx
 * @param codecpar
 * @param out_frame_rate
 * @return 返回媒体流索引，失败返回小于0
 */
int MediaEditor::addVideoStream(AVFormatContext *fmt_ctx, AVCodecContext **codec_ctx,
                           AVCodecParameters codecpar, int out_frame_rate) {
    int ret, out_video_index;
    if (!fmt_ctx) {
        LOGE("AVFormatContext is nullptr");
        return -1;
    }

    // 创建媒体流
    AVStream *stream = avformat_new_stream(fmt_ctx, nullptr);
    if (!stream) {
        LOGE("Failed to create video stream");
        return -1;
    }

    AVOutputFormat *oformat = fmt_ctx->oformat;
    if (oformat->video_codec == AV_CODEC_ID_NONE) {
        LOGE("Failed to find video codec ID");
        return -1;
    }
    out_video_index = stream->index;

    if (!codec_ctx) {
        avcodec_parameters_copy(stream->codecpar, &codecpar);
        return out_video_index;
    }
    // 查找编码器
    AVCodec *codec = avcodec_find_encoder(oformat->video_codec);
    if (!codec) {
        LOGE("Failed to call avcodec_find_encoder");
        return -1;
    }

    // 创建编码上下文
    *codec_ctx = avcodec_alloc_context3(codec);
    if (!(*codec_ctx)) {
        LOGE("Failed to call avcodec_alloc_context3");
        return -1;
    }

    // 设置编码参数
    (*codec_ctx)->bit_rate = codecpar.width * codecpar.height * 3/2 * out_frame_rate;
    (*codec_ctx)->time_base = (AVRational) {1, out_frame_rate};
    (*codec_ctx)->framerate = (AVRational) {out_frame_rate, 1};
    (*codec_ctx)->gop_size = 30;
    (*codec_ctx)->pix_fmt = (AVPixelFormat)codecpar.format;
    (*codec_ctx)->codec_type = AVMEDIA_TYPE_VIDEO;
    (*codec_ctx)->width = codecpar.width;
    (*codec_ctx)->height = codecpar.height;
    if ((*codec_ctx)->codec_id == AV_CODEC_ID_MPEG2VIDEO) {
        (*codec_ctx)->max_b_frames = 2;
    }
    if ((*codec_ctx)->codec_id == AV_CODEC_ID_MPEG1VIDEO) {
        /* Needed to avoid using macroblocks in which some coeffs overflow.
         * This does not happen with normal video, it just happens here as
         * the motion of the chroma plane does not match the luma plane. */
        (*codec_ctx)->mb_decision = 2;
    }
    if ((*codec_ctx)->codec_id == AV_CODEC_ID_H264) {
        av_opt_set((*codec_ctx)->priv_data, "preset", "slow", 0);
    }

    // 将编码器参数复制到媒体流中
    ret = avcodec_parameters_from_context(stream->codecpar, *codec_ctx);
    if (ret < 0) {
        LOGE("Failed to call avcodec_parameters_from_context");
        return -1;
    }

    // 打开编码器
    ret = avcodec_open2(*codec_ctx, codec, nullptr);
    if (ret < 0) {
        LOGE("Failed to call avcodec_open2 - '%s'", av_err2str(ret));
        return ret;
    }
    return out_video_index;
}

/**
 * 添加输出音频流
 * @param fmt_ctx
 * @param codec_ctx
 * @param codecpar
 * @return 返回音频流索引，失败返回小于0
 */
int MediaEditor::addAudioStream(AVFormatContext *fmt_ctx, AVCodecContext **codec_ctx,
                           AVCodecParameters codecpar) {
    int ret, out_audio_index;
    if (!fmt_ctx) {
        LOGE("AVFormatContext is nullptr");
        return -1;
    }

    // 创建音频流
    AVStream *stream = avformat_new_stream(fmt_ctx, nullptr);
    if (!stream) {
        LOGE("Failed to create audio stream");
        return -1;
    }

    AVOutputFormat *oformat = fmt_ctx->oformat;
    if (oformat->audio_codec == AV_CODEC_ID_NONE) {
        LOGE("Failed to find audio codec ID");
        return -1;
    }
    out_audio_index = stream->index;
    if (!codec_ctx) {
        avcodec_parameters_copy(stream->codecpar, &codecpar);
        return out_audio_index;
    }

    // 查找编码器
    AVCodec *codec = avcodec_find_encoder(oformat->audio_codec);
    if (!codec) {
        LOGE("Failed to call avcodec_find_encoder");
        return -1;
    }
    *codec_ctx = avcodec_alloc_context3(codec);
    if (!(*codec_ctx)) {
        LOGE("Failed to call avcodec_alloc_context3");
        return -1;
    }

    // 设置编码参数
    (*codec_ctx)->sample_rate = codecpar.sample_rate;
    (*codec_ctx)->bit_rate = codecpar.sample_rate * 2;
    (*codec_ctx)->sample_fmt = (AVSampleFormat)codecpar.format;
    (*codec_ctx)->channel_layout = codecpar.channel_layout;
    (*codec_ctx)->channels = codecpar.channels;
    (*codec_ctx)->time_base = (AVRational){1, codecpar.sample_rate};
    (*codec_ctx)->codec_type = AVMEDIA_TYPE_AUDIO;

    stream->time_base = (AVRational){1, codecpar.sample_rate};

    // 复制编码参数到媒体流中
    ret = avcodec_parameters_from_context(stream->codecpar, *codec_ctx);
    if (ret < 0) {
        LOGE("Failed to call avcodec_alloc_context3");
        return -1;
    }

    // 打开编码器
    ret = avcodec_open2(*codec_ctx, codec, nullptr);
    if (ret < 0) {
        LOGE("Failed to call avcodec_open2 - '%s'", av_err2str(ret));
        return -1;
    }

    return out_audio_index;
}

/**
 * 写入文件头部信息
 * @param fmt_ctx
 * @param url
 * @return
 */
int MediaEditor::writeHeader(AVFormatContext *fmt_ctx, const char *url) {
    int ret;
    if (!fmt_ctx) {
        LOGE("AVFormatContext is nullptr");
        return -1;
    }
    if (!(fmt_ctx->oformat->flags & AVFMT_NOFILE)) {
        ret = avio_open(&fmt_ctx->pb, url, AVIO_FLAG_WRITE);
        if (ret < 0) {
            LOGE("Failed to call avio_open");
            return ret;
        }
    }
    ret = avformat_write_header(fmt_ctx, nullptr);
    if (ret < 0) {
        LOGE("Failed to call avformat_write_header - '%s'", av_err2str(ret));
        return ret;
    }
    return 0;
}

/**
 * 写入文件尾
 * @param fmt_ctx
 * @return
 */
int MediaEditor::writeTrailer(AVFormatContext *fmt_ctx) {
    if (!fmt_ctx) {
        LOGE("AVFormatContext is nullptr");
        return -1;
    }
    return av_write_trailer(fmt_ctx);
}

