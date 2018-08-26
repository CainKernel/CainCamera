//
// Created by admin on 2018/4/10.
//

#ifndef CAINMUSICPLAYER_SOUNDTOUCHWRAPPER_H
#define CAINMUSICPLAYER_SOUNDTOUCHWRAPPER_H

#include <stdint.h>
#include "SoundTouch.h"

using namespace std;

using namespace soundtouch;

class SoundTouchWrapper {

public:
    SoundTouchWrapper();

    virtual ~SoundTouchWrapper();
    // 初始化
    void create();
    // 转换
    int translate(short* data, float speed, float pitch, int len, int bytes_per_sample, int n_channel, int n_sampleRate);
    // 销毁
    void destroy();
    // 获取SoundTouch对象
    SoundTouch * getSoundTouch();

private:
    SoundTouch *mSoundTouch;
};


#endif //CAINMUSICPLAYER_SOUNDTOUCHWRAPPER_H
