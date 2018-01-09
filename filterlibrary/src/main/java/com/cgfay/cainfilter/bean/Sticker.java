package com.cgfay.cainfilter.bean;

import android.graphics.Bitmap;

import com.cgfay.cainfilter.type.StickerType;

/**
 * 贴纸数据
 * Created by cain.huang on 2017/11/24.
 */

public class Sticker {
    StickerType mType;
    Bitmap mBitmap;

    public StickerType getmType() {
        return mType;
    }

    public void setmType(StickerType mType) {
        this.mType = mType;
    }

    public Bitmap getmBitmap() {
        return mBitmap;
    }

    public void setmBitmap(Bitmap mBitmap) {
        this.mBitmap = mBitmap;
    }

    public void clear() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
    }
}
