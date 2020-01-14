//
// Created by CainHuang on 2020-01-14.
//

#include "AudioPlayer.h"

AudioPlayer::AudioPlayer(const std::shared_ptr<AudioProvider> &audioProvider) {
    mAudioProvider = audioProvider;

}

AudioPlayer::~AudioPlayer() {

}