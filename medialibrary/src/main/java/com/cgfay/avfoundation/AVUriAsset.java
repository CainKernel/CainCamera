package com.cgfay.avfoundation;

import android.net.Uri;

import androidx.annotation.NonNull;

/**
 * 使用Uri
 */
public class AVUriAsset extends AVAsset<AVAssetTrack> {

    public AVUriAsset(@NonNull Uri uri) {
        super();
        mUri = uri;
    }
}
