//
// Created by cain on 2018/11/25.
//

#ifndef CAINCAMERA_PLAYERCALLBACK_H
#define CAINCAMERA_PLAYERCALLBACK_H


#include <cwchar>

class PlayerCallback {
public:
    PlayerCallback();

    virtual ~PlayerCallback();

    // 准备完成回调
    virtual void onPrepared();

    // 当前播放时长
    virtual void onCurrentInfo(double current, double total);

    // 播放出错
    virtual void onError(int code, char *msg);

    // 播放完成
    virtual void onComplete();

    // 音频PCM数据
    virtual void onGetPCM(uint8_t *pcmData, size_t size);

    // 音量分贝
    virtual void onVolumeDB(int db);
};


#endif //CAINCAMERA_PLAYERCALLBACK_H
