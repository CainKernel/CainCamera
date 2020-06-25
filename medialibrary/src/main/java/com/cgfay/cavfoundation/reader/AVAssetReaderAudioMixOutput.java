package com.cgfay.cavfoundation.reader;

import androidx.annotation.NonNull;

import com.cgfay.cavfaudio.AVAudioMix;
import com.cgfay.cavfoundation.AVAssetTrack;

import java.util.ArrayList;
import java.util.List;

/**
 * 读取混音输出
 */
public class AVAssetReaderAudioMixOutput extends AVAssetReaderOutput {

    private List<AVAssetTrack> mAudioTracks = new ArrayList<>();

    private AVAudioMix mAudioMix;

    public AVAssetReaderAudioMixOutput(@NonNull List<AVAssetTrack> audioTracks) {
        initWithAudioTracks(audioTracks);
    }

    public void initWithAudioTracks(@NonNull List<AVAssetTrack> audioTracks) {
        mAudioTracks.clear();
        mAudioTracks.addAll(audioTracks);
    }

    public List<AVAssetTrack> getAudioTracks() {
        return mAudioTracks;
    }

    public AVAudioMix getAudioMix() {
        return mAudioMix;
    }

    public void setAudioMix(AVAudioMix audioMix) {
        mAudioMix = audioMix;
    }
}
