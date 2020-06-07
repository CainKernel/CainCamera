//
// Created by CainHuang on 2020/5/31.
//

#include "AVAssetTrackSegment.h"

AVAssetTrackSegment::AVAssetTrackSegment(const AVTimeRange &timeRange) {
    this->timeMapping.source = AVTimeRangeMake(timeRange.start, timeRange.duration);
    this->timeMapping.target = AVTimeRangeMake(timeRange.start, timeRange.duration);
    empty = true;
}

AVAssetTrackSegment::AVAssetTrackSegment(const AVTimeRange &source, const AVTimeRange &target) {
    this->timeMapping = AVTimeMappingMake(source, target);
    empty = false;
}

AVAssetTrackSegment::~AVAssetTrackSegment() {

}

bool AVAssetTrackSegment::isEmpty() const {
    return empty;
}

const AVTimeMapping &AVAssetTrackSegment::getTimeMapping() const {
    return timeMapping;
}
