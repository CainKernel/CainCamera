//
// Created by CainHuang on 2020/5/31.
//

#ifndef AVASSETTRACK_H
#define AVASSETTRACK_H

#include <list>
#include "AVAssetTrackSegment.h"

class AVAsset;

class AVAssetTrack {

public:
    AVAssetTrack();

    virtual ~AVAssetTrack();

    /**
     * 初始化轨道信息
     */
    void initAVAssetTrack(const std::weak_ptr<AVAsset> &asset, const char *uri, int trackId,
            AVMediaType mediaType, const AVTimeRange &timeRange, const CGSize &naturalSize);

    /**
     * 根据轨道时间查找轨道片段
     */
    virtual AVAssetTrackSegment *segmentForTrackTime(const AVTime &time);

private:
    void release();

protected:
    std::weak_ptr<AVAsset> asset;
    const char *uri;
    int trackID;
    AVMediaType mediaType;
    AVTimeRange timeRange;
    CGSize naturalSize;
    int naturalTimeScale;

private:
    const float preferredVolume;
    const bool frameReordering;
    std::list<AVAssetTrackSegment *> trackSegments;

public:
    const std::weak_ptr<AVAsset> &getAsset() const;

    const char *getUri() const;

    int getTrackID() const;

    AVMediaType getMediaType() const;

    const AVTimeRange &getTimeRange() const;

    const CGSize &getNaturalSize() const;

    int getNaturalTimeScale() const;

    float getPreferredVolume() const;

    bool getFrameReordering() const;

    std::list<AVAssetTrackSegment *> &getTrackSegments();
};


#endif //AVASSETTRACK_H
