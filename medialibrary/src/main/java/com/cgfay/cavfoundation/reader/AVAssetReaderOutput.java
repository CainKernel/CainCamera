package com.cgfay.cavfoundation.reader;

import com.cgfay.cavfoundation.AVMediaType;

public class AVAssetReaderOutput {

    private AVMediaType mMediaType;

    private boolean alwaysCopiesSampleData;

    private boolean supportsRandomAccess;

    public AVMediaType getMediaType() {
        return mMediaType;
    }

    public boolean isAlwaysCopiesSampleData() {
        return alwaysCopiesSampleData;
    }

    public void setAlwaysCopiesSampleData(boolean alwaysCopiesSampleData) {
        this.alwaysCopiesSampleData = alwaysCopiesSampleData;
    }

    public boolean isSupportsRandomAccess() {
        return supportsRandomAccess;
    }

    public void setSupportsRandomAccess(boolean supportsRandomAccess) {
        this.supportsRandomAccess = supportsRandomAccess;
    }
}
