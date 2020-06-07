//
// Created by CainHuang on 2020/5/31.
//

#include "AVAssetTrack.h"

AVAssetTrack::AVAssetTrack() : frameReordering(false), preferredVolume(1.0) {
    trackSegments = std::list<AVAssetTrackSegment *>();
    uri = nullptr;
    trackID = -1;
    mediaType = AVMEDIA_TYPE_UNKNOWN;
    timeRange = kAVTimeRangeInvalid;
    naturalSize = CGSizeZero;
    naturalTimeScale = 0;
}

AVAssetTrack::~AVAssetTrack() {

}

void AVAssetTrack::release() {
    while (!trackSegments.empty()) {
        delete trackSegments.front();
        trackSegments.pop_front();
    }
}

void AVAssetTrack::initAVAssetTrack(const std::weak_ptr<AVAsset> &asset,
                                       const char *uri, int trackId, AVMediaType mediaType,
                                       const AVTimeRange &timeRange, const CGSize &naturalSize) {
    this->asset = asset;
    this->uri = uri;
    this->trackID = trackId;
    this->mediaType = mediaType;
    this->timeRange = timeRange;
    this->naturalSize = naturalSize;
}


AVAssetTrackSegment *AVAssetTrack::segmentForTrackTime(const AVTime &time) {
    std::list<AVAssetTrackSegment*>::iterator iterator = trackSegments.begin();
    AVAssetTrackSegment *result = nullptr;
    while (iterator != trackSegments.end()) {
        auto segment = (*iterator);
        if (AVTimeRangeContainsTime(segment->getTimeMapping().target, time)) {
            result = segment;
        }
    }
    return result;
}

const std::weak_ptr<AVAsset> &AVAssetTrack::getAsset() const {
    return asset;
}


const char *AVAssetTrack::getUri() const {
    return uri;
}


int AVAssetTrack::getTrackID() const {
    return trackID;
}


AVMediaType AVAssetTrack::getMediaType() const {
    return mediaType;
}


const AVTimeRange &AVAssetTrack::getTimeRange() const {
    return timeRange;
}


const CGSize &AVAssetTrack::getNaturalSize() const {
    return naturalSize;
}


int AVAssetTrack::getNaturalTimeScale() const {
    return naturalTimeScale;
}


float AVAssetTrack::getPreferredVolume() const {
    return preferredVolume;
}


bool AVAssetTrack::getFrameReordering() const {
    return frameReordering;
}


std::list<AVAssetTrackSegment *> &AVAssetTrack::getTrackSegments() {
    return trackSegments;
}






