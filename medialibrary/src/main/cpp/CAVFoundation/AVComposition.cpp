//
// Created by CainHuang on 2020/5/31.
//

#include "AVComposition.h"


AVComposition::AVComposition() {

}

AVComposition::~AVComposition() {

}

/**
 * 根据轨道ID来查找媒体组合轨道
 */
AVCompositionTrack *AVComposition::getTrackWithTrackID(int trackID) {
    AVCompositionTrack *result = nullptr;
    std::list<AVCompositionTrack *>::iterator iterator;
    for (iterator = tracks.begin(); iterator != tracks.end(); iterator++) {
        auto track = (*iterator);
        if (track->getTrackID() == trackID) {
            result = track;
            break;
        }
    }
    return result;
}

/**
 * 根据轨道类型来查找轨道列表
 */
std::list<AVCompositionTrack *> *AVComposition::getTrackWithMediaType(AVMediaType type) {
    auto trackList = new std::list<AVCompositionTrack *>();
    std::list<AVCompositionTrack *>::iterator iterator;
    for (iterator = tracks.begin(); iterator != tracks.end(); iterator++) {
        auto track = (*iterator);
        if (track->getMediaType() == type) {
            trackList->push_back(track);
        }
    }
    return trackList;
}

const char *AVComposition::getUri() {
    return uri;
}

AVTime &AVComposition::getDuration() {
    return duration;
}

float AVComposition::getPreferredRate() {
    return preferredRate;
}

float AVComposition::getPreferredVolume() {
    return preferredVolume;
}

CGSize &AVComposition::getNaturalSize() {
    return naturalSize;
}

std::list<AVCompositionTrack *> &AVComposition::getTracks() {
    return tracks;
}
