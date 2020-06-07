//
// Created by CainHuang on 2020/5/31.
//

#ifndef AVASSETTRACKSEGMENT_H
#define AVASSETTRACKSEGMENT_H

#include "CAVFoundation.h"

/**
 * 轨道片段
 */
class AVAssetTrackSegment {
public:

    AVAssetTrackSegment(const AVTimeRange &timeRange);

    virtual ~AVAssetTrackSegment();

    bool isEmpty() const;

    const AVTimeMapping &getTimeMapping() const;

protected:
    AVAssetTrackSegment(const AVTimeRange &source, const AVTimeRange &target);

    /**
     * 是否空片段
     */
    bool empty;

    /**
     * 轨道片段映射
     */
    AVTimeMapping timeMapping;
};


#endif //AVASSETTRACKSEGMENT_H
