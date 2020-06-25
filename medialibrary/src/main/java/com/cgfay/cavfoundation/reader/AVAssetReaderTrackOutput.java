package com.cgfay.cavfoundation.reader;

import androidx.annotation.NonNull;

import com.cgfay.cavfoundation.AVAssetTrack;

public class AVAssetReaderTrackOutput extends AVAssetReaderOutput {

    private AVAssetTrack mTrack;

    public AVAssetReaderTrackOutput(@NonNull AVAssetTrack track) {

    }

    public void initWithTrack(AVAssetTrack track) {

    }

    public AVAssetTrack getTrack() {
        return mTrack;
    }
}
