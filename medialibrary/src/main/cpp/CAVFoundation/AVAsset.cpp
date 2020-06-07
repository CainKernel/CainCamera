//
// Created by CainHuang on 2020/5/31.
//

#include "AVAsset.h"
#include "AVAssetTrack.h"

AVAsset::AVAsset() : uri(nullptr), offset(0), headers(nullptr), duration(kAVTimeZero),
                     preferredRate(1.0), preferredVolume(1.0), naturalSize(CGSizeZero) {
    tracks = std::list<AVAssetTrack *>();
}

AVAsset::~AVAsset() {
    release();
}

/**
 * 释放资源
 */
void AVAsset::release() {
    while (!tracks.empty()) {
        delete tracks.front();
        tracks.pop_front();
    }
}

/**
 * 根据轨道ID获取轨道对象
 */
AVAssetTrack *AVAsset::getTrackWithTrackID(int trackID) {
    AVAssetTrack *result = nullptr;
    std::list<AVAssetTrack *>::iterator iterator;
    for (iterator = tracks.begin(); iterator != tracks.end(); iterator++) {
        auto track = (*iterator);
        if (track->getTrackID() == trackID) {
            result = track;
            break;
        }
    }
    return result;
}

std::list<AVAssetTrack *> *AVAsset::getTrackWithMediaType(AVMediaType type) {
    auto trackList = new std::list<AVAssetTrack *>();
    std::list<AVAssetTrack *>::iterator iterator;
    for (iterator = tracks.begin(); iterator != tracks.end(); iterator++) {
        auto track = (*iterator);
        if (track->getMediaType() == type) {
            trackList->push_back(track);
        }
    }
    return trackList;
}

const char *AVAsset::getUri() const {
    return uri;
}

const AVTime &AVAsset::getDuration() const {
    return duration;
}

float AVAsset::getPreferredRate() const {
    return preferredRate;
}

float AVAsset::getPreferredVolume() const {
    return preferredVolume;
}

const CGSize &AVAsset::getNaturalSize() const {
    return naturalSize;
}

const std::list<AVAssetTrack *> &AVAsset::getTracks() const {
    return tracks;
}


