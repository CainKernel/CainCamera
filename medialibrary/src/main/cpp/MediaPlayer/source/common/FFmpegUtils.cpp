//
// Created by cain on 2018/12/29.
//
#include "FFmpegUtils.h"

#if defined(__ANDROID__)
#include <AndroidLog.h>
#endif

#ifdef __cplusplus
extern "C" {
#endif

#include <libavutil/display.h>

/**
 * 过滤解码器属性
 * @param opts
 * @param codec_id
 * @param s
 * @param st
 * @param codec
 * @return
 */
AVDictionary *filterCodecOptions(AVDictionary *opts, enum AVCodecID codec_id,
                                 AVFormatContext *s, AVStream *st, AVCodec *codec) {
    AVDictionary *ret = NULL;
    AVDictionaryEntry *t = NULL;
    int flags = s->oformat ? AV_OPT_FLAG_ENCODING_PARAM
                           : AV_OPT_FLAG_DECODING_PARAM;
    char prefix = 0;
    const AVClass *cc = avcodec_get_class();

    if (!codec) {
        codec = s->oformat ? avcodec_find_encoder(codec_id)
                           : avcodec_find_decoder(codec_id);
    }

    switch (st->codecpar->codec_type) {
        case AVMEDIA_TYPE_VIDEO: {
            prefix = 'v';
            flags |= AV_OPT_FLAG_VIDEO_PARAM;
            break;
        }
        case AVMEDIA_TYPE_AUDIO: {
            prefix = 'a';
            flags |= AV_OPT_FLAG_AUDIO_PARAM;
            break;
        }
        case AVMEDIA_TYPE_SUBTITLE: {
            prefix = 's';
            flags |= AV_OPT_FLAG_SUBTITLE_PARAM;
            break;
        }
    }

    while ((t = av_dict_get(opts, "", t, AV_DICT_IGNORE_SUFFIX))) {
        char *p = strchr(t->key, ':');

        /* check stream specification in opt name */
        if (p) {
            switch (checkStreamSpecifier(s, st, p + 1)) {
                case 1: {
                    *p = 0;
                    break;
                }
                case 0: {
                    continue;
                }
                default: {
                    break;
                }
            }
        }

        if (av_opt_find(&cc, t->key, NULL, flags, AV_OPT_SEARCH_FAKE_OBJ) || !codec
            || (codec->priv_class
                && av_opt_find(&codec->priv_class, t->key, NULL, flags, AV_OPT_SEARCH_FAKE_OBJ))) {
            av_dict_set(&ret, t->key, t->value, 0);
        } else if (t->key[0] == prefix
                   && av_opt_find(&cc, t->key + 1, NULL, flags, AV_OPT_SEARCH_FAKE_OBJ)) {
            av_dict_set(&ret, t->key + 1, t->value, 0);
        }

        if (p) {
            *p = ':';
        }
    }
    return ret;
}

/**
 * 检查媒体流
 * @param s
 * @param st
 * @param spec
 * @return
 */
int checkStreamSpecifier(AVFormatContext *s, AVStream *st, const char *spec) {
    int ret = avformat_match_stream_specifier(s, st, spec);
    if (ret < 0) {
#if defined(__ANDROID__)
        ALOGE("Invalid stream specifier: %s.\n", spec);
#else
        av_log(s, AV_LOG_ERROR, "Invalid stream specifier: %s.\n", spec);
#endif
    }
    return ret;
}

/**
 * 设置媒体流信息
 * @param s
 * @param codec_opts
 * @return
 */
AVDictionary **setupStreamInfoOptions(AVFormatContext *s, AVDictionary *codec_opts) {
    int i;
    AVDictionary **opts;

    if (!s->nb_streams) {
        return NULL;
    }
    opts = (AVDictionary **) av_mallocz_array(s->nb_streams, sizeof(*opts));
    if (!opts) {
#if defined(__ANDROID__)
        ALOGE("Could not alloc memory for stream options.\n");
#else
        av_log(NULL, AV_LOG_ERROR,
               "Could not alloc memory for stream options.\n");
#endif
        return NULL;
    }
    for (i = 0; i < s->nb_streams; i++) {
        opts[i] = filterCodecOptions(codec_opts, s->streams[i]->codecpar->codec_id,
                                     s, s->streams[i], NULL);
    }
    return opts;
}

/**
 * 打印错误
 * @param filename
 * @param err
 */
void printError(const char *filename, int err) {
    char errbuf[128];
    const char *errbuf_ptr = errbuf;

    if (av_strerror(err, errbuf, sizeof(errbuf)) < 0) {
        errbuf_ptr = strerror(AVUNERROR(err));
    }
#if defined(__ANDROID__)
    ALOGE("%s: %s\n", filename, errbuf_ptr);
#else
    av_log(NULL, AV_LOG_ERROR, "%s: %s\n", filename, errbuf_ptr);
#endif
}

/**
 * 获取旋转角度
 * @param st
 * @return
 */
double getRotation(AVStream *st) {
    AVDictionaryEntry *rotate_tag = av_dict_get(st->metadata, "rotate", NULL, 0);
    uint8_t* displaymatrix = av_stream_get_side_data(st,
                                                     AV_PKT_DATA_DISPLAYMATRIX, NULL);
    double theta = 0;

    if (rotate_tag && *rotate_tag->value && strcmp(rotate_tag->value, "0")) {
        char *tail;
        theta = av_strtod(rotate_tag->value, &tail);
        if (*tail) {
            theta = 0;
        }
    }
    if (displaymatrix && !theta){
        theta = -av_display_rotation_get((int32_t*) displaymatrix);
    }

    theta -= 360*floor(theta/360 + 0.9/360);

    if (fabs(theta - 90 * round(theta /90)) > 2) {

#if defined(__ANDROID__)
        ALOGW("Odd rotation angle.\n"
                      "If you want to help, upload a sample "
                      "of this file to ftp://upload.ffmpeg.org/incoming/ "
                      "and contact the ffmpeg-devel mailing list. (ffmpeg-devel@ffmpeg.org)");
#else
        av_log(NULL, AV_LOG_WARNING, "Odd rotation angle.\n"
                "If you want to help, upload a sample "
                "of this file to ftp://upload.ffmpeg.org/incoming/ "
                "and contact the ffmpeg-devel mailing list. (ffmpeg-devel@ffmpeg.org)");
#endif
    }

    return theta;
}

/**
 * 判断是否实时流
 * @param s
 * @return
 */
int isRealTime(AVFormatContext *s) {
    if (!strcmp(s->iformat->name, "rtp") || !strcmp(s->iformat->name, "rtsp")
        || !strcmp(s->iformat->name, "sdp")) {
        return 1;
    }

    if (s->pb && (!strncmp(s->filename, "rtp:", 4) || !strncmp(s->filename, "udp:", 4))) {
        return 1;
    }
    return 0;
}


#ifdef __cplusplus
};
#endif