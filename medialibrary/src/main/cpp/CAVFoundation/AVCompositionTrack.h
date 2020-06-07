//
// Created by CainHuang on 2020/5/31.
//

#ifndef AVCOMPOSITIONTRACK_H
#define AVCOMPOSITIONTRACK_H

#include "AVAssetTrack.h"
#include "AVCompositionTrackSegment.h"

class AVCompositionTrack : public AVAssetTrack {

public:
    AVCompositionTrack();

    virtual ~AVCompositionTrack();

    /**
     * 根据轨道时间查找轨道片段
     */
    AVCompositionTrackSegment *segmentForTrackTime(const AVTime &time);

    /**
     * 在轨道startTime的位置上插入时间区间为timeRange的源轨道数据，如果此时的轨道没有轨道片段，则startTime会被强制设置为kAVTimeZero.
     * @param timeRange
     * @param track
     * @param startTime
     * @return
     */
    bool insertTimeRange(const AVTimeRange &timeRange, AVAssetTrack *track, const AVTime &startTime);

    /**
     * 在合成轨道中插入或扩展一段空的时间区间为timeRange的片段
     * @param timeRange
     */
    void insertEmptyTimeRange(const AVTimeRange &timeRange);

    /**
     * 移除一段timeRange的时间区间
     * 删除timeRange并不会让轨道从合成媒体中删除，只会删除或截断与timeRange相交的轨道线段
     */
    void removeTimeRange(const AVTimeRange &timeRange);

    /**
     * 将timeRange的时间区间扩展成duration的长度
     * @param timeRange
     * @param duration
     */
    void scaleTimeRange(const AVTimeRange &timeRange, const AVTime duration);

    /**
     * 是否允许，默认是允许
     * @return
     */
    bool isEnabled() const;

    /**
     * 设置是否允许轨道
     * @param enabled
     */
    void setEnabled(bool enabled);

    /**
     * 设置时间刻度
     */
    void setNaturalTimeScale(int timeScale);

    /**
     * 获取时间刻度
     */
    int getNaturalTimeScale();

    /**
     * 设置音量
     */
    void setPreferredVolume(float volume);

    /**
     * 获取音量
     */
    float getPreferredVolume();

    /**
     * 设置轨道片段
     */
    void setTrackSegments(const std::list<AVCompositionTrackSegment *> &trackSegments);

    /**
     * 获取轨道列表
     */
    std::list<AVCompositionTrackSegment *> &getTrackSegments();

private:
    void release();

    bool compareStartTime(AVCompositionTrackSegment *lhs, AVCompositionTrackSegment *rhs);
private:
    bool enabled;
    int naturalTimeScale;
    float preferredVolume;
    std::list<AVCompositionTrackSegment *> trackSegments;
};

#endif //AVCOMPOSITIONTRACK_H
