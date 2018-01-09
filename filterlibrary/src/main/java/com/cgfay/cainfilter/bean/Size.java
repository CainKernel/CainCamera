package com.cgfay.cainfilter.bean;

/**
 * Created by cain.huang on 2017/7/27.
 */

public class Size {
    int mWidth;
    int mHeight;

    public Size() {
    }

    public Size(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        this.mHeight = height;
    }

}
