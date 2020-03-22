//
// Created by CainHuang on 2020-03-21.
//

#ifndef ONPLAYLISTENER_H
#define ONPLAYLISTENER_H

/**
 * 播放监听器
 */
class OnPlayListener {
public:
    virtual ~OnPlayListener() = default;

    virtual void onPrepared() = 0;

    virtual void onPlaying(float pts) = 0;

    virtual void onSeekComplete() = 0;

    virtual void onCompletion() = 0;

    virtual void onError(int errorCode, const char *msg) = 0;
};

#endif //ONPLAYLISTENER_H
