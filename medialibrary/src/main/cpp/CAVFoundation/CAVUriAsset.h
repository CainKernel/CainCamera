//
// Created by CainHuang on 2020/5/30.
//

#ifndef CAVURIASSET_H
#define CAVURIASSET_H

#include "CAVFoundation.h"
#include "AVAsset.h"
#include "AVAssetTrack.h"

struct AVDictionary {
    int count;
    AVDictionaryEntry *elements;
};

class CAVUriAsset : public AVAsset {
public:
    CAVUriAsset();

    virtual ~CAVUriAsset();

    status_t setDataSource(const char *path, int64_t offset, const char *headers);

    void release();

    AVMediaType getTrackType(int index);

    /**
     * 根据轨道索引获取轨道ID
     * @param index 轨道索引
     */
    int getTrackID(int index);

    /**
     * 获取轨道数量
     */
    int getTrackCount();

    int getWidth() const;

    int getHeight() const;

    int getRotation() const;

    int getSampleRate() const;

    int getChannelCount() const;

private:
    AVFormatContext *pFormatCtx;
    const char *uri;
    int64_t offset;
    const char *headers;
    int videoIndex;
    int audioIndex;
    int rotation;
    int sampleRate;
    int channelCount;
};

#endif //CAVURIASSET_H
