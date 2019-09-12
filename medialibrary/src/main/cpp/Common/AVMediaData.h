//
// Created by CainHuang on 2019/8/18.
//

#ifndef AVMEDIADATA_H
#define AVMEDIADATA_H

#include <cstdint>

/**
 * 媒体类型枚举
 */
enum MediaType {
    MediaNone = -1,
    MediaAudio = 0,
    MediaVideo = 1,
};

/**
 * 获取媒体类型字符串
 * @param type
 * @return
 */
inline const char *get_media_type_string(MediaType type) {
    if (type == MediaNone) {
        return "MediaNone";
    } else if (type == MediaAudio) {
        return "MediaAudio";
    } else if (type == MediaVideo) {
        return "MediaVideo";
    } else {
        return "Unknown";
    }
}

class AVMediaData {
public:
    AVMediaData();

    virtual ~AVMediaData();

    void setVideo(uint8_t *data, int length, int width, int height, int pixelFormat);

    void setAudio(uint8_t *data, int size);

    void setPts(int64_t pts);

    int64_t getPts();

    MediaType getType();

    const char *getName();

    void free();
public:
    uint8_t *image;
    int length;

    uint8_t *sample;
    int sample_size;

    int width;
    int height;
    int pixelFormat;
    int64_t pts; // pts(ms)
    MediaType type;
};


#endif //AVMEDIADATA_H
