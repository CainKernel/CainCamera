//
// Created by cain on 2019/1/26.
//

#include <AndroidLog.h>
#include "PlayerState.h"

PlayerState::PlayerState() {
    init();
    reset();
}

PlayerState::~PlayerState() {
    reset();
}

void PlayerState::init() {
    sws_dict = (AVDictionary *) malloc(sizeof(AVDictionary));
    memset(sws_dict, 0, sizeof(AVDictionary));
    swr_opts = (AVDictionary *) malloc(sizeof(AVDictionary));
    memset(swr_opts, 0, sizeof(AVDictionary));
    format_opts = (AVDictionary *) malloc(sizeof(AVDictionary));
    memset(format_opts, 0, sizeof(AVDictionary));
    codec_opts = (AVDictionary *) malloc(sizeof(AVDictionary));
    memset(codec_opts, 0, sizeof(AVDictionary));

    iformat = NULL;
    url = NULL;
    headers = NULL;

    audioCodecName = NULL;
    videoCodecName = NULL;
}

void PlayerState::reset() {

    if (sws_dict) {
        av_dict_free(&sws_dict);
        av_dict_set(&sws_dict, "flags", "bicubic", 0);
    }
    if (swr_opts) {
        av_dict_free(&swr_opts);
    }
    if (format_opts) {
        av_dict_free(&format_opts);
    }
    if (codec_opts) {
        av_dict_free(&codec_opts);
    }
    if (url) {
        av_freep(&url);
        url = NULL;
    }
    offset = 0;
    abortRequest = 1;
    pauseRequest = 1;
    seekByBytes = 0;
    syncType = AV_SYNC_AUDIO;
    startTime = AV_NOPTS_VALUE;
    duration = AV_NOPTS_VALUE;
    realTime = 0;
    infiniteBuffer = -1;
    audioDisable = 0;
    videoDisable = 0;
    displayDisable = 0;
    fast = 0;
    genpts = 0;
    lowres = 0;
    playbackRate = 1.0;
    playbackPitch = 1.0;
    seekRequest = 0;
    seekFlags = 0;
    seekPos = 0;
    seekRel = 0;
    seekRel = 0;
    autoExit = 0;
    loop = 1;
    mute = 0;
    frameDrop = 1;
    reorderVideoPts = -1;
}

void PlayerState::setOption(int category, const char *type, const char *option) {
    switch (category) {
        case OPT_CATEGORY_FORMAT: {
            av_dict_set(&format_opts, type, option, 0);
            break;
        }

        case OPT_CATEGORY_CODEC: {
            av_dict_set(&codec_opts, type, option, 0);
            break;
        }

        case OPT_CATEGORY_SWS: {
            av_dict_set(&sws_dict, type, option, 0);
            break;
        }

        case OPT_CATEGORY_PLAYER: {
            parse_string(type, option);
            break;
        }

        case OPT_CATEGORY_SWR: {
            av_dict_set(&swr_opts, type, option, 0);
            break;
        }
    }
}

void PlayerState::setOptionLong(int category, const char *type, int64_t option) {
    switch (category) {
        case OPT_CATEGORY_FORMAT: {
            av_dict_set_int(&format_opts, type, option, 0);
            break;
        }

        case OPT_CATEGORY_CODEC: {
            av_dict_set_int(&codec_opts, type, option, 0);
            break;
        }

        case OPT_CATEGORY_SWS: {
            av_dict_set_int(&sws_dict, type, option, 0);
            break;
        }

        case OPT_CATEGORY_PLAYER: {
            parse_int(type, option);
            break;
        }

        case OPT_CATEGORY_SWR: {
            av_dict_set_int(&swr_opts, type, option, 0);
            break;
        }
    }
}

void PlayerState::parse_string(const char *type, const char *option) {
    if (!strcmp("acodec", type)) { // 指定音频解码器名称
        audioCodecName = av_strdup(option);
    } else if (!strcmp("vcodec", type)) {   // 指定视频解码器名称
        videoCodecName = av_strdup(option);
    } else if (!strcmp("sync", type)) { // 制定同步类型
        if (!strcmp("audio", option)) {
            syncType = AV_SYNC_AUDIO;
        } else if (!strcmp("video", option)) {
            syncType = AV_SYNC_VIDEO;
        } else if (!strcmp("ext", option)) {
            syncType = AV_SYNC_EXTERNAL;
        } else {    // 其他则使用默认的音频同步
            syncType = AV_SYNC_AUDIO;
        }
    } else if (!strcmp("f", type)) { // f 指定输入文件格式
        iformat = av_find_input_format(option);
        if (!iformat) {
            av_log(NULL, AV_LOG_FATAL, "Unknown input format: %s\n", option);
        }
    }
}

void PlayerState::parse_int(const char *type, int64_t option) {
    if (!strcmp("an", type)) { // 禁用音频
        audioDisable = (option != 0) ? 1 : 0;
    } else if (!strcmp("vn", type)) { // 禁用视频
        videoDisable = (option != 0) ? 1 : 0;
    } else if (!strcmp("bytes", type)) { // 以字节方式定位
        seekByBytes = (option > 0) ? 1 : ((option < 0) ? -1 : 0);
    } else if (!strcmp("nodisp", type)) { // 不显示
        displayDisable = (option != 0) ? 1 : 0;
    } else if (!strcmp("fast", type)) { // fast标志
        fast = (option != 0) ? 1 : 0;
    } else if (!strcmp("genpts", type)) { // genpts标志
        genpts = (option != 0) ? 1 : 0;
    } else if (!strcmp("lowres", type)) { // lowres标准字
        lowres = (option != 0) ? 1 : 0;
    } else if (!strcmp("drp", type)) { // 重排pts
        reorderVideoPts = (option != 0) ? 1 : 0;
    } else if (!strcmp("autoexit", type)) { // 自动退出标志
        autoExit = (option != 0) ? 1 : 0;
    } else if (!strcmp("framedrop", type)) { // 丢帧标志
        frameDrop = (option != 0) ? 1 : 0;
    } else if (!strcmp("infbuf", type)) { // 无限缓冲区标志
        infiniteBuffer = (option > 0) ? 1 : ((option < 0) ? -1 : 0);
    } else {
        ALOGE("unknown option - '%s'", type);
    }
}
