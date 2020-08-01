package com.cgfay.cavfoundation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cgfay.coremedia.AVTime;
import com.cgfay.coremedia.AVTimeRange;

/**
 * 混音输入参数对象
 */
public class AVAudioMixInputParameters {

    /**
     * 轨道ID
     */
    private int mTrackID;


    public AVAudioMixInputParameters() {
        mTrackID = AVAssetTrack.kTrackIDInvalid;
    }

    public AVAudioMixInputParameters(@Nullable AVAssetTrack track) {
        if (track != null) {
            mTrackID = track.getTrackID();
        } else {
            mTrackID = AVAssetTrack.kTrackIDInvalid;
        }
    }

    /**
     * 设置音量渐变处理
     * @param startVolume   起始音量
     * @param endVolume     结束音量
     * @param timeRange     时钟区间
     */
    public void setVolumeRampFromTimeToTime(float startVolume, float endVolume,
                                            @NonNull AVTimeRange timeRange) {

    }

    /**
     * 设置某时刻的音量
     * @param volume
     * @param time
     */
    public void setVolume(float volume, @NonNull AVTime time) {

    }

    /**
     * 绑定轨道ID
     */
    public void setTrackID(int trackID) {
        mTrackID = trackID;
    }

    /**
     * 获取轨道ID
     */
    public int getTrackID() {
        return mTrackID;
    }
}
