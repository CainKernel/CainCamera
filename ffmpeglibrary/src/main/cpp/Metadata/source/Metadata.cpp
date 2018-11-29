//
// Created by cain on 2018/11/29.
//

#include "Metadata.h"

/**
 * metadata的各个参数可参考官网的介绍：
 * http://www.ffmpeg.org/ffmpeg-protocols.html
 */

const char *DURATION = "duration";
const char *AUDIO_CODEC = "audio_codec";
const char *VIDEO_CODEC = "video_codec";
const char *ICY_METADATA = "icy_metadata";
const char *ROTATE = "rotate";
const char *FRAME_RATE = "frame_rate";
const char *CHAPTER_START = "chapter_start";
const char *CHAPTER_END = "chapter_end";
const char *CHAPTER_COUNT = "chapter_count";
const char *FILE_SIZE = "file_size";
const char *VIDEO_WIDTH = "video_width";
const char *VIDEO_HEIGHT = "video_height";

Metadata::Metadata() {

}

Metadata::~Metadata() {

}

void Metadata::setShoutcastMetadata(AVFormatContext *pFormatCtx) {
    char *value = NULL;

    if (av_opt_get(pFormatCtx, "icy_metadata_packet", 1, (uint8_t **) &value) < 0) {
        value = NULL;
    }

    if (value && value[0]) {
        av_dict_set(&pFormatCtx->metadata, ICY_METADATA, value, 0);
    }
}

void Metadata::setDuration(AVFormatContext *pFormatCtx) {
    char value[30] = "0";
    int duration = 0;

    if (pFormatCtx) {
        if (pFormatCtx->duration != AV_NOPTS_VALUE) {
            duration = ((pFormatCtx->duration / AV_TIME_BASE) * 1000);
        }
    }

    sprintf(value, "%d", duration); // %i
    av_dict_set(&pFormatCtx->metadata, DURATION, value, 0);
}

void Metadata::setCodec(AVFormatContext *pFormatCtx, int streamIndex) {
    const char *codec_type = av_get_media_type_string(pFormatCtx->streams[streamIndex]->codec->codec_type);

    if (!codec_type) {
        return;
    }

    const char *codec_name = avcodec_get_name(pFormatCtx->streams[streamIndex]->codec->codec_id);

    if (strcmp(codec_type, "audio") == 0) {
        av_dict_set(&pFormatCtx->metadata, AUDIO_CODEC, codec_name, 0);
    } else if (codec_type && strcmp(codec_type, "video") == 0) {
        av_dict_set(&pFormatCtx->metadata, VIDEO_CODEC, codec_name, 0);
    }
}

void Metadata::setRotation(AVFormatContext *pFormatCtx, AVStream *audioStream,
                           AVStream *videoStream) {
    if (!extractMetadata(pFormatCtx, audioStream, videoStream, ROTATE)
        && videoStream && videoStream->metadata) {
        AVDictionaryEntry *entry = av_dict_get(videoStream->metadata, ROTATE, NULL, AV_DICT_MATCH_CASE);

        if (entry && entry->value) {
            av_dict_set(&pFormatCtx->metadata, ROTATE, entry->value, 0);
        } else {
            av_dict_set(&pFormatCtx->metadata, ROTATE, "0", 0);
        }
    }
}

void Metadata::setFrameRate(AVFormatContext *pFormatCtx, AVStream *audioStream,
                            AVStream *videoStream) {
    char value[30] = "0";

    if (videoStream && videoStream->avg_frame_rate.den && videoStream->avg_frame_rate.num) {
        double d = av_q2d(videoStream->avg_frame_rate);
        uint64_t v = lrintf(d * 100);
        if (v % 100) {
            sprintf(value, "%3.2f", d);
        } else if (v % (100 * 1000)) {
            sprintf(value,  "%1.0f", d);
        } else {
            sprintf(value, "%1.0fk", d / 1000);
        }

        av_dict_set(&pFormatCtx->metadata, FRAME_RATE, value, 0);
    }
}

void Metadata::setFileSize(AVFormatContext *pFormatCtx) {
    char value[30] = "0";

    int64_t size = pFormatCtx->pb ? avio_size(pFormatCtx->pb) : -1;
    sprintf(value, "%lld", size);
    av_dict_set(&pFormatCtx->metadata, FILE_SIZE, value, 0);
}

void Metadata::setChapterCount(AVFormatContext *pFormatCtx) {
    char value[30] = "0";
    int count = 0;

    if (pFormatCtx) {
        if (pFormatCtx->nb_chapters) {
            count = pFormatCtx->nb_chapters;
        }
    }

    sprintf(value, "%d", count); // %i
    av_dict_set(&pFormatCtx->metadata, CHAPTER_COUNT, value, 0);
}

void Metadata::setVideoSize(AVFormatContext *pFormatCtx, AVStream *videoStream) {
    char value[30] = "0";

    if (videoStream) {
        sprintf(value, "%d", videoStream->codec->width);
        av_dict_set(&pFormatCtx->metadata, VIDEO_WIDTH, value, 0);

        sprintf(value, "%d", videoStream->codec->height);
        av_dict_set(&pFormatCtx->metadata, VIDEO_HEIGHT, value, 0);
    }
}

const char* Metadata::extractMetadata(AVFormatContext *pFormatCtx, AVStream *audioStream,
                                      AVStream *videoStream, const char *key) {
    char* value = NULL;

    if (!pFormatCtx) {
        return value;
    }

    if (key) {
        if (av_dict_get(pFormatCtx->metadata, key, NULL, AV_DICT_MATCH_CASE)) {
            value = av_dict_get(pFormatCtx->metadata, key, NULL, AV_DICT_MATCH_CASE)->value;
        } else if (audioStream && av_dict_get(audioStream->metadata, key, NULL, AV_DICT_MATCH_CASE)) {
            value = av_dict_get(audioStream->metadata, key, NULL, AV_DICT_MATCH_CASE)->value;
        } else if (videoStream && av_dict_get(videoStream->metadata, key, NULL, AV_DICT_MATCH_CASE)) {
            value = av_dict_get(videoStream->metadata, key, NULL, AV_DICT_MATCH_CASE)->value;
        }
    }

    return value;
}

const char* Metadata::extractMetadata(AVFormatContext *pFormatCtx, AVStream *audioStream,
                                      AVStream *videoStream, const char *key, int chapter) {
    char* value = NULL;

    if (!pFormatCtx || pFormatCtx->nb_chapters <= 0) {
        return value;
    }

    if (chapter < 0 || chapter >= pFormatCtx->nb_chapters) {
        return value;
    }

    AVChapter *ch = pFormatCtx->chapters[chapter];

    if (strcmp(key, CHAPTER_START) == 0) {
        char time[30];
        int start_time = (int)(ch->start * av_q2d(ch->time_base) * 1000);
        sprintf(time, "%d", start_time);
        value = static_cast<char *>(malloc(strlen(time)));
        sprintf(value, "%s", time);
    } else if (strcmp(key, CHAPTER_END) == 0) {
        char time[30];
        int end_time = (int)(ch->end * av_q2d(ch->time_base) * 1000);
        sprintf(time, "%d", end_time);
        value = static_cast<char *>(malloc(strlen(time)));
        sprintf(value, "%s", time);
    } else if (av_dict_get(ch->metadata, key, NULL, AV_DICT_MATCH_CASE)) {
        value = av_dict_get(ch->metadata, key, NULL, AV_DICT_MATCH_CASE)->value;
    }

    return value;
}

int Metadata::getMetadata(AVFormatContext *pFormatCtx, AVDictionary **metadata) {
    if (!pFormatCtx) {
        return -1;
    }

    setShoutcastMetadata(pFormatCtx);
    av_dict_copy(metadata, pFormatCtx->metadata, 0);

    return 0;
}

