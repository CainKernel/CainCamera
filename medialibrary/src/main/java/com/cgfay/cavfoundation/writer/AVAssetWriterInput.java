package com.cgfay.cavfoundation.writer;

import com.cgfay.cavfoundation.AVMediaType;
import com.cgfay.coregraphics.AffineTransform;
import com.cgfay.coregraphics.CGSize;

public class AVAssetWriterInput {

    private AVMediaType mMediaType;

    private CGSize mNaturalSize;

    private AffineTransform mTransform;

    private float mPreferredVolume;

    private int mMediaTimeScale;

    public AVAssetWriterInput(AVMediaType mediaType) {
        this.mMediaType = mediaType;
    }



    public AVMediaType getMediaType() {
        return mMediaType;
    }


    public int getMediaTimeScale() {
        return mMediaTimeScale;
    }

    public void setMediaTimeScale(int mediaTimeScale) {
        mMediaTimeScale = mediaTimeScale;
    }
}
