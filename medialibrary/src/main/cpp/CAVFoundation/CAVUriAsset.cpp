//
// Created by CainHuang on 2020/5/30.
//

#include "CAVUriAsset.h"
#include <unistd.h>

CAVUriAsset::CAVUriAsset() {
    av_register_all();
    avformat_network_init();
    pFormatCtx = nullptr;
    videoIndex = -1;
    audioIndex = -1;
    rotation = 0;
    sampleRate = 0;
    channelCount = 0;
}

CAVUriAsset::~CAVUriAsset() {
    release();
    avformat_network_deinit();
}

status_t CAVUriAsset::setDataSource(const char *path, int64_t offset, const char *headers) {
    int ret;
    this->uri = av_strdup(path);
    this->offset = offset;
    this->headers = av_strdup(headers);

    AVDictionary *options = nullptr;
    av_dict_set(&options, "icy", "1", 0);
    av_dict_set(&options, "user_agent", "FFmpegMediaMetadataRetriever", 0);

    if (headers) {
        av_dict_set(&options, "headers", headers, 0);
    }

    if (offset > 0) {
        pFormatCtx = avformat_alloc_context();
        pFormatCtx->skip_initial_bytes = offset;
    }

    // 打开文件
    ret = avformat_open_input(&pFormatCtx, uri, nullptr, &options);
    if (ret != 0) {
        LOGE("Failed to call avformat_open_input: %s", av_err2str(ret));
        return -1;
    }

    // 查找媒体流信息
    ret = avformat_find_stream_info(pFormatCtx, nullptr);
    if (ret < 0) {
        LOGE("Failed to call avformat_find_stream_info: %s", av_err2str(ret));
        avformat_close_input(&pFormatCtx);
        return -1;
    }

    for (int i = 0; i < pFormatCtx->nb_streams; ++i) {
        if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoIndex = i;

            // 设置视频宽高
            int width = pFormatCtx->streams[i]->codecpar->width;
            int height = pFormatCtx->streams[i]->codecpar->height;
            naturalSize.width = width;
            naturalSize.height = height;

            // 提取视频旋转角度
            char *value = nullptr;
            AVDictionaryEntry *keyEntry = av_dict_get(pFormatCtx->metadata, "rotate", nullptr, AV_DICT_MATCH_CASE);
            if (!keyEntry) {
                keyEntry = av_dict_get(pFormatCtx->streams[i]->metadata, "rotate", nullptr, AV_DICT_MATCH_CASE);
            }
            if (keyEntry) {
                value = keyEntry->value;
            }
            if (value) {
                rotation = atoi(value);
            }
        } else if (pFormatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            audioIndex = i;
            sampleRate = pFormatCtx->streams[i]->codecpar->sample_rate;
            channelCount = pFormatCtx->streams[i]->codecpar->channels;
        }
    }

    // 设置时长
    duration.value = pFormatCtx->duration;
    duration.timescale = AV_TIME_BASE;

    return 0;
}

void CAVUriAsset::release() {
    if (pFormatCtx != nullptr) {
        if (videoIndex != -1) {
            avcodec_close(pFormatCtx->streams[videoIndex]->codec);
        }
        if (audioIndex != -1) {
            avcodec_close(pFormatCtx->streams[audioIndex]->codec);
        }
        avformat_close_input(&pFormatCtx);
    }
    videoIndex = -1;
    audioIndex = -1;
    sampleRate = 0;
    channelCount = 0;
}

AVMediaType CAVUriAsset::getTrackType(int index) {
    if (pFormatCtx) {
        return pFormatCtx->streams[index]->codecpar->codec_type;
    }
    return AVMEDIA_TYPE_UNKNOWN;
}

int CAVUriAsset::getTrackID(int index) {
    if (pFormatCtx) {
        return pFormatCtx->streams[index]->id;
    }
    return -1;
}

int CAVUriAsset::getTrackCount() {
    if (pFormatCtx) {
        return pFormatCtx->nb_streams;
    }
    return 0;
}

int CAVUriAsset::getWidth() const {
    return static_cast<int>(naturalSize.width);
}

int CAVUriAsset::getHeight() const {
    return static_cast<int>(naturalSize.height);
}

int CAVUriAsset::getRotation() const {
    return rotation;
}

int CAVUriAsset::getSampleRate() const {
    return sampleRate;
}

int CAVUriAsset::getChannelCount() const {
    return channelCount;
}



