//
// Created by CainHuang on 2020/5/31.
//

#include "AVCompositionTrackSegment.h"

AVCompositionTrackSegment::AVCompositionTrackSegment(const AVTimeRange &timeRange)
        : AVAssetTrackSegment(timeRange) {
}

AVCompositionTrackSegment::AVCompositionTrackSegment(const char *uri, int sourceTrackID,
                                                     const AVTimeRange &source, const AVTimeRange &target)
                                                     : AVAssetTrackSegment(source, target) {
    this->sourceUri = av_strdup(uri);
    this->sourceTrackID = sourceTrackID;
}

AVCompositionTrackSegment::~AVCompositionTrackSegment() {

}

AVCompositionTrackSegment::AVCompositionTrackSegment(const AVCompositionTrackSegment &segment)
        : AVAssetTrackSegment(segment.timeMapping.target) {
    this->timeMapping = AVTimeMappingMake(segment.timeMapping.source, segment.timeMapping.target);
    this->sourceUri = segment.sourceUri;
    this->sourceTrackID = segment.sourceTrackID;
    this->empty = segment.empty;
}

AVCompositionTrackSegment *AVCompositionTrackSegment::clone() const {
    return new AVCompositionTrackSegment(*this);
}

void AVCompositionTrackSegment::setTimeMapping(const AVTimeMapping &timeMapping) {
    this->timeMapping = timeMapping;
}

const char *AVCompositionTrackSegment::getSourceUri() const {
    return sourceUri;
}

int AVCompositionTrackSegment::getSourceTrackID() const {
    return sourceTrackID;
}


