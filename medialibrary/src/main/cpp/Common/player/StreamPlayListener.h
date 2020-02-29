//
// Created by erenhuang on 2020-02-27.
//

#ifndef STREAMPLAYLISTENER_H
#define STREAMPLAYLISTENER_H

#ifdef __cplusplus
extern "C" {
#endif

#include <libavutil/avutil.h>

#ifdef __cplusplus
};
#endif

class StreamPlayListener {
public:
    virtual ~StreamPlayListener() = default;

    virtual void onPlaying(AVMediaType type, float pts) = 0;

    virtual void onSeekComplete(AVMediaType type) = 0;

    virtual void onCompletion(AVMediaType type) = 0;

    virtual void onError(AVMediaType type, int errorCode, const char *msg) = 0;
};

#endif //STREAMPLAYLISTENER_H
