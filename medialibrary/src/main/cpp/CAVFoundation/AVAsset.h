//
// Created by CainHuang on 2020/5/31.
//

#ifndef AVASSET_H
#define AVASSET_H

#include "CAVFoundation.h"
#include "coregraphics/Geometry.h"
#include "AVAssetTrack.h"
#include <list>


class AVAsset {
public:
    AVAsset();

    ~AVAsset();

    /**
     * 根据轨道ID查找媒体轨道对象
     */
    AVAssetTrack *getTrackWithTrackID(int trackID);

    /**
     * 根据媒体类型查找媒体轨道列表
     */
    std::list<AVAssetTrack *> *getTrackWithMediaType(AVMediaType type);

    const char *getUri() const;

    const AVTime &getDuration() const;

    float getPreferredRate() const;

    float getPreferredVolume() const;

    const CGSize &getNaturalSize() const;

    const std::list<AVAssetTrack *> &getTracks() const;

protected:
    const char *uri;
    int64_t offset;
    const char *headers;
    AVTime duration;
    const float preferredRate;
    const float preferredVolume;
    CGSize naturalSize;

private:
    std::list<AVAssetTrack *> tracks;

    void release();
};


#endif //AVASSET_H
