//
// Created by CainHuang on 2020/9/5.
//

#include "CAVMediaMuxer.h"

CAVMediaMuxer::CAVMediaMuxer() : path(nullptr), pFormatCtx(nullptr),
        audioInfo(nullptr), videoInfo(nullptr), prepared(false), started(false) {
    av_register_all();
}

CAVMediaMuxer::~CAVMediaMuxer() {
    release();
    if (path) {
        av_freep(&path);
        path = nullptr;
    }
    if (audioInfo) {
        av_freep(&audioInfo);
        audioInfo = nullptr;
    }
    if (videoInfo) {
        av_freep(&videoInfo);
        videoInfo = nullptr;
    }
}

void CAVMediaMuxer::setOutputPath(const char *url) {
    path = av_strdup(url);
}

int CAVMediaMuxer::prepare() {
    if (!audioInfo && !videoInfo) {
        LOGE("CAVMediaMuxer - couldn't find audio or video info, failed to prepare!");
        return -1;
    }

    int ret = init();
    if (ret < 0) {
        return ret;
    }

    // create audio stream and copy audio info.
    if (audioInfo) {
        ret = prepareTrack(audioInfo);
        audioInfo->track = ret;
        LOGD("CAVMediaMuxer - audio track: %d", ret);
    }

    // create video stream and copy video info.
    if (videoInfo) {
        ret = prepareTrack(videoInfo);
        videoInfo->track = ret;
        LOGD("CAVMediaMuxer - video track: %d", ret);
    }

    // dump ffmpeg muxer info.
    printInfo();

    ret = openMuxer();
    if (ret < 0) {
        LOGE("CAVMediaMuxer - failed to open ffmpeg muxer");
        return ret;
    }

    // write file global header
    ret = writeHeader();
    if (ret < 0) {
        LOGE("CAVMediaMuxer - failed to write header");
        return ret;
    }
    prepared = true;
    return ret;
}

/**
 * 开始封装
 */
void CAVMediaMuxer::start() {
    if (!prepared) {
        return;
    }
    started = true;
}

/**
 * 停止封装
 */
int CAVMediaMuxer::stop() {
    if (!started) {
        LOGE("CAVMediaMuxer - failed to stop Muxer, illegal state");
        return -1;
    }
    started = false;
    int ret = writeTrailer();
    if (ret < 0) {
        LOGE("CAVMediaMuxer - failed to write trailer");
    }
    return ret;
}

/**
 * 释放资源
 */
void CAVMediaMuxer::release() {
    closeMuxer();
}

/**
 * 设置音频编码参数
 * @param audioInfo 音频参数
 */
void CAVMediaMuxer::setAudioInfo(CAVAudioInfo &audioInfo) {
    if (this->audioInfo == nullptr) {
        this->audioInfo = static_cast<CAVAudioInfo *>(av_mallocz(sizeof(CAVAudioInfo)));
    }
    memcpy(this->audioInfo, &audioInfo, sizeof(audioInfo));
    LOGD("audio info - sample rate: %d, channels: %d, bit rate: %d",
            audioInfo.sample_rate, audioInfo.channels, audioInfo.bit_rate);
}

/**
 * 视频编码参数
 * @param videoInfo 视频参数
 */
void CAVMediaMuxer::setVideoInfo(CAVVideoInfo &videoInfo) {
    if (this->videoInfo == nullptr) {
        this->videoInfo = static_cast<CAVVideoInfo *>(av_mallocz(sizeof(CAVVideoInfo)));
    }
    memcpy(this->videoInfo, &videoInfo, sizeof(videoInfo));
    LOGD("video info - width: %d, height: %d, frame rate: %d, bit rate: %d",
            videoInfo.width, videoInfo.height, videoInfo.frame_rate, videoInfo.bit_rate);
}

/**
 * 是否存在全局头部信息
 */
bool CAVMediaMuxer::hasGlobalHeader() {
    if (pFormatCtx) {
        return (bool)(pFormatCtx->oformat->flags & AVFMT_GLOBALHEADER);
    }
    return false;
}

/**
 * 打印复用器的信息
 */
void CAVMediaMuxer::printInfo() {
    if (pFormatCtx && path) {
        av_dump_format(pFormatCtx, 0, path, 1);
    }
}

/**
 * 获取音频轨道
 */
int CAVMediaMuxer::getAudioTrack() {
    if (audioInfo) {
        return audioInfo->track;
    }
    return -1;
}

/**
 * 获取视频轨道
 */
int CAVMediaMuxer::getVideoTrack() {
    if (videoInfo) {
        return videoInfo->track;
    }
    return -1;
}

/**
 * 是否已经开始
 */
bool CAVMediaMuxer::isStarted() {
    return true;
}

/**
 * 重新计算AVPacket的ts数值
 * @param packet    AVPacket
 * @param track     audio stream track / video stream track
 * @return          0 if success, negative when fail.
 */
int CAVMediaMuxer::rescalePacketTs(AVPacket *packet, int track, long pts, AVRational rational) {
    if (!pFormatCtx || !packet) {
        return -1;
    }
    // track在streams列表里
    if (pFormatCtx && track >= 0 && track < pFormatCtx->nb_streams) {
        int num = 1;
        int den = 0;
        if (audioInfo != nullptr && track == audioInfo->track) {
            den = audioInfo->sample_rate;
        } else if (videoInfo != nullptr && track == videoInfo->track) {
            den = videoInfo->frame_rate;
        }
        if (den == 0) {
            return -1;
        }
        // 将pts转换成FFmpeg的时钟值
        packet->pts = pts;
        // 重新计算数据包的pts
        av_packet_rescale_ts(packet, rational,
                             pFormatCtx->streams[track]->time_base);
        return 0;
    }
    return -1;
}

/**
 * 准备音频轨道
 * @param info  音频参数
 * @return      音频流索引，-1表示失败，大于等于0表示成功
 */
int CAVMediaMuxer::prepareTrack(CAVAudioInfo *info) {
    auto stream = createStream(info->codec_id);
    if (stream != nullptr) {
        stream->time_base = (AVRational){1, info->sample_rate};
        stream->codecpar->format = info->audio_format;
        stream->codecpar->sample_rate = info->sample_rate;
        stream->codecpar->channels = info->channels;
        stream->codecpar->channel_layout = (uint64_t)av_get_default_channel_layout(info->channels);
        stream->codecpar->bit_rate = info->bit_rate;
        stream->codecpar->codec_type = AVMEDIA_TYPE_AUDIO;
        stream->codecpar->codec_id = info->codec_id;
        stream->codecpar->codec_tag = 0;
        return stream->index;
    }
    return -1;
}

/**
 * 准备视频轨道
 * @param info  视频参数
 * @return      视频流索引，-1表示失败，大于等于0表示成功
 */
int CAVMediaMuxer::prepareTrack(CAVVideoInfo *info) {
    auto stream = createStream(info->codec_id);
    if (stream != nullptr) {
        stream->time_base = (AVRational){1, info->frame_rate};
        stream->codecpar->width = info->width;
        stream->codecpar->height = info->height;
        stream->codecpar->bit_rate = info->bit_rate;
        stream->codecpar->level = info->level;
        stream->codecpar->profile = info->profile;
        stream->codecpar->codec_type = AVMEDIA_TYPE_VIDEO;
        stream->codecpar->codec_id = info->codec_id;
        stream->codecpar->codec_tag = 0;
        return stream->index;
    }
    return -1;
}

/**
 * 初始化复用器
 */
int CAVMediaMuxer::init() {
    if (!path) {
        LOGE("CAVMediaMuxer - failed to find output path");
        return -1;
    }
    int ret = avformat_alloc_output_context2(&pFormatCtx, nullptr, nullptr, path);
    if (!pFormatCtx || ret < 0) {
        LOGI("CAVMediaMuxer - failed to call avformat_alloc_output_context2: %s", av_err2str(ret));
        return AVERROR_UNKNOWN;
    }
    return 0;
}

/**
 * 打开封装器
 * @return 0 if success, otherwise failed.
 */
int CAVMediaMuxer::openMuxer() {
    if (!pFormatCtx) {
        LOGE("CAVMediaMuxer - Failed to find muxer context");
        return -1;
    }
    int ret;
    if (!(pFormatCtx->oformat->flags & AVFMT_NOFILE)) {
        if ((ret = avio_open(&pFormatCtx->pb, path, AVIO_FLAG_WRITE)) < 0) {
            LOGE("CAVMediaMuxer - Failed to open output file '%s'", path);
            return ret;
        }
    }
    return 0;
}

/**
 * 创建媒体流
 * @param id    编码器id
 * @return      媒体流对象
 */
AVStream *CAVMediaMuxer::createStream(AVCodecID id) {
    if (!pFormatCtx) {
        LOGE("CAVMediaMuxer - Failed to find muxer context");
        return nullptr;
    }
    if (id == AV_CODEC_ID_NONE) {
        LOGE("CAVMediaMuxer - Failed to find encoder: %s", avcodec_get_name(id));
        return nullptr;
    }
    auto encoder = avcodec_find_encoder(id);
    if (encoder == nullptr) {
        LOGE("CAVMediaMuxer - Failed to find encoder: %s", avcodec_get_name(id));
        return nullptr;
    }
    return avformat_new_stream(pFormatCtx, encoder);
}

/**
 * 写入文件头部信息
 * @param options   参数
 * @return          处理结果，0表示成功，否则表示失败
 */
int CAVMediaMuxer::writeHeader(AVDictionary **options) {
    if (!pFormatCtx) {
        LOGE("CAVMediaMuxer - Failed to find muxer context");
        return -1;
    }
    // 判断是否需要写入全局头部信息
    if (!hasGlobalHeader()) {
        return 0;
    }
    int ret = avformat_write_header(pFormatCtx, options);
    if (ret < 0) {
        LOGE("CAVMediaMuxer - Failed to call avformat_write_header: %s", av_err2str(ret));
        return ret;
    }
    return 0;
}

/**
 * 写入额外参数
 * @param track         media track
 * @param extraData     extract data
 * @param size          extract data size
 * @return 0 if success, otherwise failed.
 */
int CAVMediaMuxer::writeExtraData(int track, uint8_t *extraData, size_t size) {
    if (track < 0 || track > pFormatCtx->nb_streams || !extraData || size <= 0) {
        return -1;
    }
    AVStream *stream = pFormatCtx->streams[track];
    if (stream != nullptr && stream->codecpar != nullptr) {
        stream->codecpar->extradata = (uint8_t *) av_mallocz(size + AV_INPUT_BUFFER_PADDING_SIZE);
        memcpy(stream->codecpar->extradata, extraData, size);
        stream->codecpar->extradata_size = static_cast<int>(size);
        return 0;
    }
    return -1;
}

/**
 * 将数据包写入封装器中
 * @param packet
 * @return  0 if success, otherwise failed.
 */
int CAVMediaMuxer::writeFrame(AVPacket *packet) {
    if (!pFormatCtx) {
        LOGE("CAVMediaMuxer - Failed to find muxer context");
        return -1;
    }
    if (packet == nullptr || packet->data == nullptr || packet->stream_index < 0) {
        return -1;
    }
    int ret = av_interleaved_write_frame(pFormatCtx, packet);
    if (ret < 0) {
        LOGE("CAVMediaMuxer - Failed to call av_interleaved_write_frame: %s, stream: %d", av_err2str(ret), packet->stream_index);
        return ret;
    }
    return 0;
}

/**
 * 写入文件尾部信息
 * @return          处理结果，0表示成功，否则表示失败
 */
int CAVMediaMuxer::writeTrailer() {
    if (!pFormatCtx) {
        LOGE("CAVMediaMuxer - Failed to find muxer context");
        return -1;
    }
    int ret = av_write_trailer(pFormatCtx);
    if (ret < 0) {
        LOGE("CAVMediaMuxer -Failed to call av_write_trailer: %s", av_err2str(ret));
        return ret;
    } else {
        LOGD("CAVMediaMuxer - muxer writer success");
    }
    return 0;
}

/**
 * 关闭封装器
 */
void CAVMediaMuxer::closeMuxer() {
    if (pFormatCtx && !(pFormatCtx->oformat->flags & AVFMT_NOFILE)) {
        avio_closep(&pFormatCtx->pb);
        avformat_close_input(&pFormatCtx);
        pFormatCtx = nullptr;
        LOGD("CAVMediaMuxer - close file");
    }
}
