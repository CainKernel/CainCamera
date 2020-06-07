//
// Created by CainHuang on 2020/5/31.
//

#ifndef AVCOMPOSITIONTRACKSEGMENT_H
#define AVCOMPOSITIONTRACKSEGMENT_H


#include "AVAssetTrackSegment.h"

class AVCompositionTrackSegment : public AVAssetTrackSegment {
public:
    AVCompositionTrackSegment(const AVTimeRange &timeRange);

    AVCompositionTrackSegment(const char *uri, int sourceTrackID, const AVTimeRange &source,
            const AVTimeRange &target);

    virtual ~AVCompositionTrackSegment();

    AVCompositionTrackSegment *clone() const;

    void setTimeMapping(const AVTimeMapping &timeMapping);

    const char *getSourceUri() const;

    int getSourceTrackID() const;

private:
    AVCompositionTrackSegment() = delete;

    AVCompositionTrackSegment(const AVCompositionTrackSegment &segment);

    const char *sourceUri;

    int sourceTrackID;
};


#endif //AVCOMPOSITIONTRACKSEGMENT_H
