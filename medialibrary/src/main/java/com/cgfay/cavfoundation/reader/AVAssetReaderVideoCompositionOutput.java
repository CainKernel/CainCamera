package com.cgfay.cavfoundation.reader;

import androidx.annotation.NonNull;

import com.cgfay.cavfoundation.AVAssetTrack;
import com.cgfay.cavfoundation.AVVideoComposition;

import java.util.ArrayList;
import java.util.List;

public class AVAssetReaderVideoCompositionOutput extends AVAssetReaderOutput {

    private List<AVAssetTrack> mVideoTracks = new ArrayList<>();

    private AVVideoComposition mVideoComposition;


    public AVAssetReaderVideoCompositionOutput(@NonNull List<AVAssetTrack> videoTracks) {
        initWithVideoTracks(videoTracks);
    }

    public void initWithVideoTracks(@NonNull List<AVAssetTrack> videoTracks) {
        mVideoTracks.clear();
        mVideoTracks.addAll(videoTracks);
    }


    public AVVideoComposition getVideoComposition() {
        return mVideoComposition;
    }

    public void setVideoComposition(AVVideoComposition videoComposition) {
        mVideoComposition = videoComposition;
    }

    public List<AVAssetTrack> getVideoTracks() {
        return mVideoTracks;
    }
}
