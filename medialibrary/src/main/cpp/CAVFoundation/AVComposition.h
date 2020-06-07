//
// Created by CainHuang on 2020/5/31.
//

#ifndef AVCOMPOSITION_H
#define AVCOMPOSITION_H

#include "AVAsset.h"
#include "AVCompositionTrack.h"

class AVComposition : public AVAsset {

public:
    AVComposition();

    virtual ~AVComposition();

    AVCompositionTrack *getTrackWithTrackID(int trackID);

    std::list<AVCompositionTrack *> *getTrackWithMediaType(AVMediaType type);

    const char *getUri();

    AVTime &getDuration();

    float getPreferredRate();

    float getPreferredVolume();

    CGSize &getNaturalSize();

    std::list<AVCompositionTrack *> &getTracks();

private:
    std::list<AVCompositionTrack *> tracks;
};


#endif //AVCOMPOSITION_H
