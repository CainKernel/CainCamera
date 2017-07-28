package com.cgfay.caincamera.bean;

/**
 * 相机信息
 * Created by cain.huang on 2017/7/27.
 */
public class CameraInfo {
    int facing;
    int orientation;

    public CameraInfo(int facing, int orientation) {
        this.facing = facing;
        this.orientation = orientation;
    }

    public int getFacing() {
        return facing;
    }

    public void setFacing(int facing) {
        this.facing = facing;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
}
