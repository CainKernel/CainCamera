//
// Created by CainHuang on 2020/5/31.
//

#include "AVCompositionTrack.h"

AVCompositionTrack::AVCompositionTrack() : enabled(true) {
    trackSegments = std::list<AVCompositionTrackSegment *>();
}

AVCompositionTrack::~AVCompositionTrack() {

}

void AVCompositionTrack::release() {
    while (!trackSegments.empty()) {
        delete trackSegments.front();
        trackSegments.pop_front();
    }
}

/**
 * 根据轨道时间查找轨道片段
 */
AVCompositionTrackSegment *AVCompositionTrack::segmentForTrackTime(const AVTime &time) {
    std::list<AVCompositionTrackSegment*>::iterator iterator = trackSegments.begin();
    AVCompositionTrackSegment *result = nullptr;
    while (iterator != trackSegments.end()) {
        auto segment = (*iterator);
        if (AVTimeRangeContainsTime(segment->getTimeMapping().target, time)) {
            result = segment;
        }
    }
    return result;
}

void AVCompositionTrack::setNaturalTimeScale(int timeScale) {
    naturalTimeScale = timeScale;
}

int AVCompositionTrack::getNaturalTimeScale() {
    return naturalTimeScale;
}

void AVCompositionTrack::setPreferredVolume(float volume) {
    preferredVolume = volume;
}

float AVCompositionTrack::getPreferredVolume() {
    return preferredVolume;
}

void AVCompositionTrack::setTrackSegments(const std::list<AVCompositionTrackSegment *> &trackSegments) {
    // 清理旧轨道片段，设置新轨道片段数据
    while (!this->trackSegments.empty()) {
        delete trackSegments.front();
        this->trackSegments.pop_front();
    }
    this->trackSegments = trackSegments;
}

std::list<AVCompositionTrackSegment *> &AVCompositionTrack::getTrackSegments() {
    return trackSegments;
}

bool AVCompositionTrack::isEnabled() const {
    return enabled;
}

void AVCompositionTrack::setEnabled(bool enabled) {
    this->enabled = enabled;
}

/**
 * 在轨道startTime的位置上插入时间区间为timeRange的源轨道数据，
 * 如果此时的轨道没有轨道片段，则startTime会被强制设置为kAVTimeZero.
 * @param timeRange
 * @param track
 * @param startTime
 * @return
 */
bool AVCompositionTrack::insertTimeRange(const AVTimeRange &timeRange, AVAssetTrack *track,
                                         const AVTime &startTime) {
    if (track == nullptr) {
        return false;
    }
    AVCompositionTrack *compositionTrack = dynamic_cast<AVCompositionTrack*>(track);
    // 如果track是组合媒体轨道，则直接从轨道中的片段取出来进行处理
    if (compositionTrack != nullptr) {
        auto compositionSegments = compositionTrack->trackSegments;
        // 如果组合轨道的片段是空的，则插入一段空的片段
        if (compositionSegments.empty()) {
            insertEmptyTimeRange(AVTimeRangeMake(startTime, timeRange.duration));
        } else {
            auto segments = compositionTrack->getTrackSegments();
            auto iterator = segments.begin();
            while (iterator != segments.end()) {
                auto segment = (*iterator);
                // 如果交集时间区间不为空，则直接将交集部分所对应的轨道片段对象复制出来并重新设置时间区间，最后插入到轨道片段中
                auto intersectTimeRange = AVTimeRangeGetIntersection(segment->getTimeMapping().target, timeRange);
                if (!AVTimeRangeEqual(kAVTimeRangeZero, intersectTimeRange)) {
                    // 如果是空片段，则直接插入timeRange交集区间的空片段
                    // 如果不是空片段，则非空部分需要计算出交集区间源媒体区间的timeRange，创建一个交集区间映射对象的片段
                    if (segment->isEmpty()) {
                        insertEmptyTimeRange(AVTimeRangeMake(startTime, intersectTimeRange.duration));
                    } else {
                        // 计算出源轨道时长
                        auto duration = AVTimeMapDurationFromDurationToRange(intersectTimeRange.duration,
                                                                             segment->getTimeMapping().target,
                                                                             segment->getTimeMapping().source);
                        // 计算出源轨道起始时间
                        auto start = AVTimeMapTimeFromRangeToRange(intersectTimeRange.start,
                                                                   segment->getTimeMapping().target,
                                                                   segment->getTimeMapping().source);
                        // 计算出要插入的轨道时间的映射关系
                        auto mapping = AVTimeMappingMake(AVTimeRangeMake(start, duration),
                                                         AVTimeRangeMake(startTime,
                                                                 intersectTimeRange.duration));
                        auto insertSegment = segment->clone();
                        insertSegment->setTimeMapping(mapping);
                        trackSegments.push_back(insertSegment);
                    }
                }
                iterator++;
            }

            // 当片段插入之后，更新轨道的总时长，使用并集的方式更新
            this->timeRange = AVTimeRangeGetUnion(this->timeRange, timeRange);
        }
        return true;
    }

    // 源媒体轨道AVAssetTrack，判断源媒体数据是否为空，如果为空，则插入一段空的片段，否则插入一段带源媒体对象和源轨道ID的片段
    // 需要处理插入后的轨道时长
    auto segments = track->getTrackSegments();
    // 如果轨道为空的，则直接插入一个起始时间为startTime，时长为timeRange.duration的空片段
    if (segments.empty()) {
        insertEmptyTimeRange(AVTimeRangeMake(startTime, timeRange.duration));
    } else {
        // 遍历查找与timeRange区间存在交集的片段，构建并插入组合媒体轨道片段
        auto iterator = segments.begin();
        while (iterator != segments.end()) {
            auto segment = (*iterator);
            // 如果交集时间区间不为空，则直接将交集部分所对应的轨道片段对象复制出来并重新设置时间区间，最后插入到轨道片段中
            auto intersectTimeRange = AVTimeRangeGetIntersection(segment->getTimeMapping().target, timeRange);
            if (!AVTimeRangeEqual(kAVTimeRangeZero, intersectTimeRange)) {
                // 如果不存在源数据Uri，则说明该轨道是空的，直接插入一个则直接插入timeRange交集区间的空片段
                // 如果源数据存在，则利用源轨道构建一个组合轨道片段
                if (track->getUri() == nullptr) {
                    insertEmptyTimeRange(AVTimeRangeMake(startTime, intersectTimeRange.duration));
                } else {
                    // 根据交集计算出源轨道时长
                    auto duration = AVTimeMapDurationFromDurationToRange(intersectTimeRange.duration,
                                                                         segment->getTimeMapping().target,
                                                                         segment->getTimeMapping().source);
                    // 根据交集计算出源轨道起始时间
                    auto start = AVTimeMapTimeFromRangeToRange(intersectTimeRange.start,
                                                               segment->getTimeMapping().target,
                                                               segment->getTimeMapping().source);
                    // 计算出源时间区间和片段目的时间区间
                    auto sourceTimeRange = AVTimeRangeMake(start, duration);
                    auto targetTimeRange = AVTimeRangeMake(startTime, intersectTimeRange.duration);
                    // 创建一个新轨道片段
                    auto insertSegment = new AVCompositionTrackSegment(track->getUri(),
                            track->getTrackID(), sourceTimeRange, targetTimeRange);
                    trackSegments.push_back(insertSegment);
                }
            }
            iterator++;
        }

        // 当片段插入之后，更新轨道的总时长，使用并集的方式更新
        this->timeRange = AVTimeRangeGetUnion(this->timeRange, timeRange);
    }

    return true;
}

/**
 * 插入一段空的时间区间
 * @param timeRange
 */
void AVCompositionTrack::insertEmptyTimeRange(const AVTimeRange &timeRange) {
    // 如果当前轨道片段列表是空的，说明啥都没有，直接插入一段空的片段，片段从kAVTimeZero开始
    // 如果已经存在轨道片段，则直接加入轨道片段，然后拿当前轨道总时间区间与插入轨道片段时间区间并集作为新的轨道总时间区间
    if (trackSegments.empty()) {
        auto insertTimeRange = AVTimeRangeMake(kAVTimeZero, timeRange.duration);
        auto segment = new AVCompositionTrackSegment(insertTimeRange);
        trackSegments.push_back(segment);
        this->timeRange = insertTimeRange;
    } else {
        auto segment = new AVCompositionTrackSegment(timeRange);
        trackSegments.push_back(segment);
        // 将时间区间交集作为轨道的总时长区间
        this->timeRange = AVTimeRangeGetUnion(this->timeRange, timeRange);
    }
}

/**
 * 比较开始时间
 */
bool AVCompositionTrack::compareStartTime(AVCompositionTrackSegment *lhs, AVCompositionTrackSegment *rhs) {
    return (AVTimeCompare(lhs->getTimeMapping().target.start, rhs->getTimeMapping().target.start) <= 0);
}

/**
 * 移除时间区间
 * 移除的时间区间与轨道的交集存在四种情况：
 * 在轨道片段开头、在轨道片段结尾、整个片段、中间一小段
 * 其中交集是中间一小段的时候需要插入一段新的轨道片段
 * @param timeRange
 */
void AVCompositionTrack::removeTimeRange(const AVTimeRange &timeRange) {
    // 没有轨道片段，则直接退出
    if (trackSegments.empty()) {
        return;
    }

    // 判断时间区间是否在轨道时长内，不在直接跳过不做处理
    auto trackIntersection = AVTimeRangeGetIntersection(this->timeRange, timeRange);
    if (AVTimeRangeEqual(trackIntersection, kAVTimeRangeZero)) {
        return;
    }

    // 需要额外插入的轨道片段，时间区间在轨道片段中间，将一个片段成两个片段时，该对象不为空
    AVCompositionTrackSegment *otherInsertSegment = nullptr;
    // 被删除的时长，记录每一段的时长
    auto deleteDuration = kAVTimeZero;

    // 遍历处理片段与时间区间的交集
    auto iterator = trackSegments.begin();
    while (iterator != trackSegments.end()) {
        auto segment = (*iterator);
        auto mapping = segment->getTimeMapping();
        // 判断当前片段是否存在交集，如果不存在交集，则直接删除
        auto intersect = AVTimeRangeGetIntersection(mapping.target, timeRange);
        if (!AVTimeRangeEqual(kAVTimeRangeZero, intersect)) {
            // 删除当前交集部分的时长

            // 有四种情况，交集当前片段整个区间，交集在开头，交集在结尾和交集在中间的情况

            // 获取交集结束时间
            auto intersectEnd = AVTimeRangeGetEndWithTimescale(intersect, naturalTimeScale);
            // 获取片段目的时长
            auto targetDuration = AVTimeSubtract(mapping.target.duration, intersect.duration);
            // 交集映射到源数据的时长
            auto sourceIntersect = AVTimeMapDurationFromDurationToRange(intersect.duration,
                                                                        mapping.target,
                                                                        mapping.source);
            // 交集是否在开头
            auto onIntersectStart = AVTimeEqual(mapping.target.start, intersect.start);
            // 交集是否在结尾
            auto onIntersectEnd = AVTimeEqual(AVTimeRangeGetEndWithTimescale(mapping.target, naturalTimeScale), intersectEnd);

            // 1、如果交集是整个片段，删除整个片段，并记录总删除时长
            if (onIntersectStart && onIntersectEnd) {
                trackSegments.erase(iterator);
                // 记录总删除时长
                deleteDuration = AVTimeAdd(deleteDuration, intersect.duration);
                // 跳过后面的计算新的起始时间，该片段不需要了
                iterator++;
                continue;
            }

            // 2、交集在开头，删除交集的区间，需要更新起始时间
            if (onIntersectStart) {
                // 2.1、计算出新的源数据时长和时间区间
                auto newSourceStart = AVTimeRangeGetEndWithTimescale(AVTimeRangeMake(mapping.source.start, sourceIntersect), naturalTimeScale);
                auto newSourceDuration = AVTimeSubtract(mapping.source.duration, sourceIntersect);
                auto sourceRange = AVTimeRangeMake(newSourceStart, newSourceDuration);

                // 2.2、计算出删除后的轨道时长和时间区间
                auto targetRange = AVTimeRangeMake(intersectEnd, targetDuration);

                // 2.3、更新轨道片段的时间区间
                mapping.source = sourceRange;
                mapping.target = targetRange;

                // 2.4、计算总的删除时长，给后面的片段使用
                deleteDuration = AVTimeAdd(deleteDuration, intersect.duration);

                // 2.5、删除完之后，需要调整删除之后的起始位置，前面片段有可能删除过一段时间
                // 新的起始位置 = 原起始位置 - (前面的总删除时长 + 交集时长)
                auto startTime = AVTimeSubtract(mapping.target.start, deleteDuration);
                mapping.target.start = startTime;

                // 2.6、更新轨道片段的时间映射对象
                segment->setTimeMapping(mapping);

                iterator++;
                continue;
            }

            // 3、交集在结尾，删除交集的区间，说明前面没有删除的时长，不需要更新起始时间
            if (onIntersectEnd) {

                // 3.1、计算出新的源数据时长和时间区间
                auto newSourceDuration = AVTimeSubtract(mapping.source.duration, sourceIntersect);
                auto sourceRange = AVTimeRangeMake(mapping.source.start, newSourceDuration);

                // 3.2、计算出删除后的轨道时长和时间区间
                auto targetRange = AVTimeRangeMake(mapping.target.start, targetDuration);

                // 3.3、更新轨道片段的时间区间
                mapping.source = sourceRange;
                mapping.target = targetRange;

                // 3.4、计算总的删除时长，给后面的片段使用
                deleteDuration = AVTimeAdd(deleteDuration, intersect.duration);

                // 3.5、更新轨道片段的时间映射关系
                segment->setTimeMapping(mapping);

                iterator++;
                continue;
            }

            // 4、交集在中间的情况，原有片段删除交集起始位置的后半段，并添加原有片段交集结尾作为起始时间到原片段结尾时间的区间
            // 4.1、先clone一个片段
            otherInsertSegment = segment->clone();
            // 4.2、删除segment中的交集开始的后片段
            // 计算开头剩余的区间
            auto targetEndTime = AVTimeConvertScale(intersect.start, naturalTimeScale);
            targetEndTime.value -= 1;
            auto sourceEndTime = AVTimeMapTimeFromRangeToRange(targetEndTime, mapping.target, mapping.source);
            auto sourceRange = AVTimeRangeFromTimeToTime(mapping.source.start, sourceEndTime);
            auto targetRange = AVTimeRangeFromTimeToTime(mapping.target.start, targetEndTime);
            mapping.source = sourceRange;
            mapping.target = targetRange;
            segment->setTimeMapping(mapping);

            // 4.3、使用交集结尾作为另外一个片段的起始位置
            auto otherMapping = otherInsertSegment->getTimeMapping();
            // 计算新插入片段的起始和结束位置
            auto otherTargetStart = AVTimeRangeGetEndWithTimescale(intersect, naturalTimeScale);
            auto otherTargetEnd = AVTimeAdd(otherMapping.target.start, otherMapping.target.duration);
            auto otherSourceStart = AVTimeMapTimeFromRangeToRange(otherTargetStart, otherMapping.target, otherMapping.source);
            auto otherSourceEnd = AVTimeAdd(otherMapping.source.start, otherMapping.source.duration);
            otherMapping.source = AVTimeRangeFromTimeToTime(otherSourceStart, otherSourceEnd);
            otherMapping.target = AVTimeRangeFromTimeToTime(otherTargetStart, otherTargetEnd);

            // 计算出总的删除时长
            deleteDuration = AVTimeAdd(deleteDuration, intersect.duration);

            // 计算出新片段和起始时间
            otherMapping.target.start = AVTimeSubtract(otherTargetStart, deleteDuration);
            otherInsertSegment->setTimeMapping(otherMapping);

            iterator++;
            continue;
        }

        // 如果有被删除过时间，则说明前面的片段有删除时长的处理，需要调整当前轨道片段的起始时间
        if (!AVTimeEqual(kAVTimeZero, deleteDuration)) {
            // 新的起始时间 = 原起始时间 - 删除的时长
            auto startTime = AVTimeSubtract(mapping.target.start, deleteDuration);
            mapping.target.start = startTime;
            segment->setTimeMapping(mapping);
        }
        iterator++;
    }

    if (otherInsertSegment != nullptr) {
        trackSegments.push_back(otherInsertSegment);
        // 轨道片段以开始时间进行重新排序
        trackSegments.sort(compareStartTime);
    }

    // 新的轨道时长 = 总轨道时长 - 轨道交集时长
    this->timeRange = AVTimeRangeMake(kAVTimeZero, AVTimeSubtract(this->timeRange.duration,
                                                                  trackIntersection.duration));
}

/**
 * 将轨道的某个时间区间timeRange缩放成时长为duration的区间
 * 缩放的时间区间存在交集开头、交集在结尾、整个片段进行缩放基于只缩放片段中的其中一小段四种场景
 * @param timeRange 时间区间
 * @param duration  目的时长
 */
void AVCompositionTrack::scaleTimeRange(const AVTimeRange &timeRange, const AVTime duration) {
    // 需要另外插入的轨道片段
    std::list<AVCompositionTrackSegment *> insertTrackSegments;

}
