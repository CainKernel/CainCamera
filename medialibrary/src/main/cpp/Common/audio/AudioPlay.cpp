//
// Created by CainHuang on 2020-01-14.
//

#include "AudioPlay.h"

AudioPlay::AudioPlay(const std::shared_ptr<AudioProvider> &audioProvider) {
    mAudioProvider = audioProvider;
    mSampleFmt = AV_SAMPLE_FMT_S16;
}
